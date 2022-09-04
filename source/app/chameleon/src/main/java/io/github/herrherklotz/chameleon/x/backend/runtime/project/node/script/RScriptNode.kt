package io.github.herrherklotz.chameleon.x.backend.runtime.project.node.script

import com.eclipsesource.v8.*
import com.eclipsesource.v8.utils.V8Executor
import com.eclipsesource.v8.utils.V8ObjectUtils
import com.eclipsesource.v8.utils.V8Map
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem2
import io.github.herrherklotz.chameleon.x.backend.runtime.project.node.RNode
import io.github.herrherklotz.chameleon.x.backend.system.project.node.script.ScriptNode
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext


class RScriptNode(nodeId: String, script: ScriptNode): RNode(nodeId, script) {
    private var mV8Runtime: V8? = null
    private var mCode = ""
    private var mLabel = ""
    private var mProjectLabel = ""
    private var mV8Executor: V8Executor? = null


    init {
        mProjectLabel = ChameleonSystem.projectName!!
        mCode = script.code

        if (script.label != null)
            mLabel = script.label!!
    }


    override fun off() {
        if (mV8Executor!!.isAlive) {
            // Forces the executor to shutdown immediately.
            mV8Executor!!.forceTermination()
            mV8Executor!!.interrupt()
            mV8Executor!!.join()
        }

        super.off()
    }


    override suspend fun worker() {
        val lThis = this

        mV8Executor = object: V8Executor(mCode) {
            override fun setup(runtime: V8) {
                Thread.currentThread().name = "$mProjectLabel/$mLabel"

                runtime.registerJavaMethod(lThis, "receive",
                        "\$receive", arrayOf<Class<*>?>(Int::class
                        .javaPrimitiveType))
                runtime.registerJavaMethod(lThis, "poll", "\$poll",
                        arrayOf<Class<*>?>(Int::class.javaPrimitiveType))
                runtime.registerJavaMethod(lThis, "out", "\$out",
                        arrayOf(Int::class.javaPrimitiveType, Any::class.java))
                runtime.registerJavaMethod(lThis, "request",
                        "\$request",
                        arrayOf(Int::class.javaPrimitiveType, Any::class.java))
                JsObjects(mLabel, runtime)
                lThis.mV8Runtime = runtime
            }
        }

        mV8Executor!!.start()

        try {
            mV8Executor!!.join()
        } catch(ignore: InterruptedException) {
            this.mV8Executor!!.interrupt();
        } finally {
            if (!mV8Executor!!.isShuttingDown) {
                if (mV8Executor!!.hasException()) {
                    LogElement.error("${mProjectLabel}/${mLabel}", mV8Executor!!
                            .exception.localizedMessage!!)
                } else if ((!mV8Executor!!.hasException()) && (!ChameleonSystem2.rProject!!
                                .changingState.get())) {
                    LogElement.info("${mProjectLabel}/${mLabel}", "This " +
                            "script finished execution! Project will be stopped.")
                }

                GlobalScope.launch(Dispatchers.Default) {
                    ChameleonSystem.projectName = null
                }
            }

            while (coroutineContext.isActive)
                delay(100)
        }
    }


    @Throws(InterruptedException::class, Exception::class)
    fun receive(pInputPortId: Int): Any? {
        if (pInputPortId < 0)
            throw Exception("Please use a port number greater than 0!")
        else if (pInputPortId >= this.inputs.size)
            throw Exception("This script has only ${this.inputs.size} input ports!")
        else if (this.inputs[pInputPortId].mLinkType != "stream")
            throw Exception("You can't receive a polling input!")

        this.mInputLock.lock()

        try {
            // TODO: Add timeout
            // TODO: Use the time to make a constant stream out of it? eg. await()... remaining=soll
            //       -ist... sleep(remaining) with an additional boolean... constantSpeed..
            this.mInputCondition.await()
            return this.transformData(this.inputs[pInputPortId].mData)
        } catch (e: InterruptedException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            this.mInputLock.unlock()
        }

        return null
    }


    @Throws(InterruptedException::class, Exception::class)
    fun poll(pInputPortId: Int): Any? {
        if (pInputPortId < 0)
            throw Exception("Please use a port number greater than 0!")
        else if (pInputPortId >= this.inputs.size)
            throw Exception("This script has only ${this.inputs.size} input ports!")
        else if (this.inputs[pInputPortId].mLinkType != "poll")
            throw Exception("You can't poll a streaming input!")

        return when (this.inputs[pInputPortId].mLinkId) {
            "variables" -> ChameleonSystem2.rProject!!.project.variables[this.inputs[pInputPortId]
                    .mLinkPort]!!.value
            else -> this.transformData(this.inputs[pInputPortId].mData)
        }
    }


    private fun transformData(pData: Any?): Any? {
        return when (pData) {
            is Map<*, *> -> V8ObjectUtils.toV8Object(mV8Runtime, pData as Map<String?, *>)
            is List<*> -> V8ObjectUtils.toV8Array(mV8Runtime, pData)
            is Unit -> null
            else -> pData
        }

        /* TODO J2V8 6.1.0 else if (this.inputs!![pInputPortId].mData is ByteArray) {
            val lData = this.inputs!!.get(pInputPortId).mData as ByteArray
            val buffer = V8ArrayBuffer(mV8Runtime, lData.size)
            val array = V8TypedArray(mV8Runtime, buffer, V8Value.UNSIGNED_INT_8_ARRAY, 0, lData.size)
            array.byteBuffer.put(lData)
            buffer.release()
            return array
        } */
        // TODO if (instanceof short[]) -> UNSIGNED_INT_16_ARRAY?
    }


    private fun transformData2(pData: Any): Any {
        return when (pData) {
            is V8ArrayBuffer -> LogElement.error("script", "Use V8TypedArray!")
            is V8TypedArray -> pData.getBytes(0, pData.length())
            is V8Array ->  V8ObjectUtils.toList(pData)
            is V8Map<*>,
            // is V8PropertyMap,
            is V8Object -> V8ObjectUtils.toMap(pData as V8Object) as Map<*,*>
            else -> pData
        }
    }


    override fun out(pIndex: Int, pObject: Any) {
        super.out(pIndex, transformData2(pObject))
    }


    override fun request(pIndex: Int, pObject: Any): Any? {
        return transformData(super.request(pIndex, transformData2(pObject)))
    }


    @Throws(java.lang.NullPointerException::class)
    override fun `in`(pId: Int, pData: Any?) {
        this.mInputLock.lock()

        try {
            this.inputs[pId].mData = pData
            this.mInputCondition.signal()
        } finally {
            this.mInputLock.unlock()
        }
    }
}