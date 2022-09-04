package io.github.herrherklotz.chameleon.x.backend.runtime.project.node

import com.fasterxml.jackson.annotation.*
import io.github.herrherklotz.chameleon.Chameleon
import io.github.herrherklotz.chameleon.x.backend.system.Components
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem2.rProject
import io.github.herrherklotz.chameleon.x.backend.runtime.component.com.client.Ev3
import io.github.herrherklotz.chameleon.x.backend.system.project.Node
import io.github.herrherklotz.chameleon.x.backend.system.project.Port
import io.github.herrherklotz.chameleon.x.extensions.IInput
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.consumeAsFlow
import org.apache.commons.lang3.RandomStringUtils
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock


@ExperimentalCoroutinesApi
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class RNode(val nodeId: String, val node: Node) {
    protected var mInputLock = ReentrantLock()
    protected var mInputCondition = mInputLock.newCondition()
    // TODO @JsonIgnore
    // TODO protected var ReceiveChannels = ArrayList<ReceiveChannel<Any>>()
    private var inputJobs: List<Job> = mutableListOf()
    private var outputChannels: MutableMap<Int, Channel<Any>> = mutableMapOf()
    private var job: Job = Job()
    var outputs: ArrayList<Port> = arrayListOf()
    var inputs: ArrayList<Port> = arrayListOf()

    init {
        node.inputs.forEachIndexed { i, it ->
            val port = Port()

            if (it != null) {
                port.link = it.toList()

                when (port.mLinkGroup) {
                    "component" -> {
                        when (port.mLinkId) {
                            "variables" -> {
                                // Do nothing
                            }
                            else -> {
                                val component = Components[port.mLinkId]

                                if (port.mLinkType == "stream") {
                                    val broadcastChannel = component!!.mBroadcastChannels[port.mLinkPort]
                                    inputJobs.plus(inputWorker(broadcastChannel!!.openSubscription(), i)) // Before  broadcastChannel!!.openSubscription().map { it }
                                }
                            }
                        }
                    }
                }
            }

            inputs.add(port)
        }

        node.outputs.forEachIndexed { i, it ->
            val port = Port()

            if (it != null) {
                port.link = it.toList()

                when (port.mLinkId) {
                    "variables" -> {
                        // Do nothing
                    }
                    else -> {
                        // Component, use link information
                        val component = Components[port.mLinkId]
                        val channel = component!!.mChannels[port.mLinkPort]

                        if (channel != null)
                            outputChannels[i] = channel
                    }
                }
            } else {
                // Node, check connection list
                val result = rProject!!.project.connections
                        .filter { (it[2] == nodeId) && (it[3] == "${i}") }

                if (result.isNotEmpty()) {
                    val target = result.first()
                    val t = target[0].split("/")
                    port.link = listOf(t[0], t[1], target[1], "stream")
                }
            }

            outputs.add(port)
        }

        Chameleon.LOG_D("Thread: " + Thread.currentThread().name + " (RNode.init)")
        job = GlobalScope.launch(newSingleThreadContext(RandomStringUtils.random(8,
                true, false))) {
            worker()
        }
    }


    open fun off() {
        inputJobs.forEach {
            runBlocking {
                it.cancelAndJoin()
            }
        }

        inputJobs =  mutableListOf()
        outputChannels = mutableMapOf()

        runBlocking {
            // TODO interrupt the sleep etc.
            job.cancelAndJoin()
        }
    }


    /*open fun out(pIndex: Int, pObject: Any) {
        val lOutput: Port? = this.outputs!!.getOrNull(pIndex)

        lOutput ?: return

        if (outputChannels.containsKey(pIndex))
            GlobalScope.launch(Dispatchers.Default) {
                outputChannels[pIndex]!!.send(pObject)
            }
        else {
            try {
                lOutput.mTarget.`in`(lOutput.mLinkPort, pObject)
            } catch (ignore: NullPointerException) {
                if (lOutput.mLinkId == null)
                    return;

                val lNode = ChameleonRuntime.oGroups!![lOutput.mLinkGroup]!![lOutput.mLinkId]

                if (lNode != null && lNode.inputs != null) {
                    lOutput.mTarget = IInput.in0 { pId: Int, pData: Any? -> lNode.`in`(pId, pData) }
                    lOutput.mTarget.`in`(lOutput.mLinkPort, pObject)
                }
            }
        }
    }*/

    open fun out(index: Int, value: Any) {
        val output: Port? = this.outputs.getOrNull(index)

        output ?: return

        when (output.mLinkGroup) {
            "component" -> {
                when (output.mLinkId) {
                    "variables" -> { // TODO: Move Variable to Component & Node level
                        val variable = rProject!!.project.variables[output.mLinkPort]!!

                        when (value) {
                            is String -> {
                                if (variable.datatype != "string")
                                    throw Exception("The variable \"${variable.label}\" requires " +
                                            "a ${variable.datatype} value.")
                            }
                            is Number -> {
                                if (variable.datatype != "number")
                                    throw Exception("The variable \"${variable.label}\" requires " +
                                            "a ${variable.datatype} value.")
                            }
                            else -> {
                                throw Exception("The variable \"${variable.label}\" requires " +
                                        "a ${variable.datatype} value.")
                            }
                        }

                        variable.value = value
                    }
                    else -> {
                        when (output.mLinkPort) {
                            "Status" -> {
                                if (value is Map<*, *> || value is Int)
                                    GlobalScope.launch(Dispatchers.Default) {
                                        if (value != 0)
                                            Components[output.mLinkId]!!.on(value)
                                        else
                                            Components[output.mLinkId]!!.off()
                                    }
                            }
                            else -> {
                                GlobalScope.launch(Dispatchers.Default) {
                                    outputChannels[index]!!.send(value)
                                }
                            }
                        }
                    }
                }
            }
            "Script" -> {
                try {
                    output.mTarget!!.`in`(output.mLinkPort!!.toInt(), value)
                } catch (ignore: NullPointerException) {
                    if (output.mLinkId == null)
                        return;

                    val lNode = rProject!!.oGroups!![output.mLinkGroup]!![output.mLinkId]

                    if (lNode?.inputs != null) {
                        output.mTarget = IInput.in0 { pId: Int, pData: Any? -> lNode.`in`(pId, pData) }
                        output.mTarget!!.`in`(output.mLinkPort!!.toInt(), value)
                    }
                }
            }
        }
    }


    open fun request(pIndex: Int, pObject: Any): Any? {
        return Ev3.Button(pObject)
    }


    private fun inputWorker(receiveChannel: ReceiveChannel<Any>, index: Int): Job {
        return GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            while (coroutineContext.isActive) {
                receiveChannel.consumeEach {
                    `in`(index, it)
                }
            }
        }
    }


    private fun outputWorker(receiveChannel: ReceiveChannel<Any>, index: Int): Job {
        return GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            while (coroutineContext.isActive) {
                receiveChannel.consumeEach {
                    `in`(index, it)
                }
            }
        }
    }


    @Throws(java.lang.NullPointerException::class)
    abstract fun `in`(pId: Int, pData: Any?)

    abstract suspend fun worker()
}