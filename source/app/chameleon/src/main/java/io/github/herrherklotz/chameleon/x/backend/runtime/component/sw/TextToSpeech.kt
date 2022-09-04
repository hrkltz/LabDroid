package io.github.herrherklotz.chameleon.x.backend.runtime.component.sw

import android.speech.tts.TextToSpeech
import io.github.herrherklotz.chameleon.MainService.Companion.context
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.system.project.component.sw.TextToSpeechData
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object TextToSpeech : IComponent, TextToSpeechData() {
    private var baseConfig: TextToSpeechData = TextToSpeechData()
    private var mTextToSpeech: TextToSpeech? = null

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(emptyMap())
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(mapOf())
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    override fun off() {
        if (!mRunning)
            return

        if (mTextToSpeech != null) {
            mTextToSpeech!!.stop()
            mTextToSpeech!!.shutdown()
        }

        mTextToSpeech = null

        LogElement.info("TextToSpeech", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        text = baseConfig.text

        when (data) {
            is Map<*,*> -> {
                if (data.containsKey("text"))
                    text = data["text"] as String
            }
            is TextToSpeechData -> {
                text = data.text
        }}

        mTextToSpeech = TextToSpeech(context!!, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val localeUS = Locale("en", "US")

                val result: Int
                result = mTextToSpeech!!.setLanguage(localeUS)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    LogElement.error("TextToSpeech", "This Language is not supported")
                } else {
                    mTextToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }

            } else
                LogElement.error("TextToSpeech", "Initilization Failed!")
        })

        LogElement.info("TextToSpeech", "On")
}}