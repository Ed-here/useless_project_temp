package com.example.phonedialer.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.example.phonedialer.R
import com.example.phonedialer.data.ActiveCallInfo
import com.example.phonedialer.data.CallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

object CallManager {
    private var currentTelecomCall: Call? = null
    private var inCallService: InCallService? = null
    private var timerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var ringtonePlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _callState = MutableStateFlow(ActiveCallInfo())
    val callState: StateFlow<ActiveCallInfo> = _callState.asStateFlow()

    private var context: Context? = null
    private var isFunnyAudioCall: Boolean = false

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun setInCallService(service: InCallService?) {
        this.inCallService = service
    }

    private val telecomCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            updateFromTelecomCall(call)
        }
    }

    fun setTelecomCall(call: Call?) {
        currentTelecomCall?.unregisterCallback(telecomCallback)
        currentTelecomCall = call
        call?.registerCallback(telecomCallback)
        call?.let { updateFromTelecomCall(it) }
    }

    private fun updateFromTelecomCall(call: Call) {
        val mappedState = when (call.state) {
            Call.STATE_RINGING -> CallState.RINGING
            Call.STATE_DIALING, Call.STATE_CONNECTING -> CallState.DIALING
            Call.STATE_ACTIVE -> CallState.ACTIVE
            Call.STATE_HOLDING -> CallState.ON_HOLD
            Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> CallState.DISCONNECTED
            else -> CallState.IDLE
        }

        val handleUri = call.details?.handle
        val number = handleUri?.schemeSpecificPart ?: "Unknown"

        _callState.update { current ->
            current.copy(
                number = number,
                state = mappedState
            )
        }

        if (mappedState == CallState.ACTIVE) {
            startTimer()
            // Reset mute/speaker for new real call
            _callState.update { it.copy(isMuted = false, isSpeakerOn = false) }
            inCallService?.setMuted(false)
            if (isFunnyAudioCall) playFunnyAudio()
        } else if (mappedState == CallState.DISCONNECTED || mappedState == CallState.IDLE) {
            stopTimer()
            stopAudio()
        }
    }

    // Call Action Controls
    fun startSimulatedCall(number: String, name: String = "Contact", playAudio: Boolean = false) {
        stopTimer()
        isFunnyAudioCall = playAudio
        _callState.update {
            ActiveCallInfo(
                number = number,
                name = name,
                state = CallState.DIALING,
                durationSeconds = 0
            )
        }

        // Simulate transition from dialing to active call after 2.5 seconds
        scope.launch {
            delay(2500)
            if (_callState.value.state == CallState.DIALING) {
                _callState.update { it.copy(state = CallState.ACTIVE) }
                startTimer()
                if (isFunnyAudioCall) playFunnyAudio()
            }
        }
    }

    fun startSimulatedIncomingCall(number: String, name: String = "Incoming Call", playAudioOnAnswer: Boolean = false) {
        stopTimer()
        stopRingtone()
        isFunnyAudioCall = playAudioOnAnswer
        
        _callState.update {
            ActiveCallInfo(
                number = number,
                name = name,
                state = CallState.RINGING,
                durationSeconds = 0
            )
        }

        // Start playing ringtone
        val ctx = context ?: return
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            if (ringtoneUri != null) {
                ringtonePlayer = MediaPlayer().apply {
                    setDataSource(ctx, ringtoneUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun answerCall() {
        stopRingtone()
        currentTelecomCall?.answer(0)
        _callState.update { it.copy(state = CallState.ACTIVE) }
        startTimer()
        if (isFunnyAudioCall) playFunnyAudio()
    }

    fun rejectCall() {
        stopRingtone()
        currentTelecomCall?.reject(false, null)
        endCall()
    }

    fun toggleMute() {
        val newMute = !_callState.value.isMuted
        _callState.update { it.copy(isMuted = newMute) }
        
        // Actually mute/unmute the real call if it exists
        inCallService?.setMuted(newMute)
    }

    fun toggleSpeaker() {
        val newSpeaker = !_callState.value.isSpeakerOn
        _callState.update { it.copy(isSpeakerOn = newSpeaker) }
        
        // Actually toggle speaker for the real call
        val route = if (newSpeaker) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_WIRED_OR_EARPIECE
        inCallService?.setAudioRoute(route)
    }

    fun toggleHold() {
        val currentHold = _callState.value.isOnHold
        if (currentHold) {
            currentTelecomCall?.unhold()
        } else {
            currentTelecomCall?.hold()
        }
        _callState.update { it.copy(isOnHold = !currentHold, state = if (!currentHold) CallState.ON_HOLD else CallState.ACTIVE) }
    }

    fun endCall() {
        stopRingtone()
        currentTelecomCall?.disconnect()
        stopTimer()
        stopAudio()
        isFunnyAudioCall = false
        _callState.update { it.copy(state = CallState.DISCONNECTED) }
        
        scope.launch {
            delay(1200)
            _callState.update { ActiveCallInfo(state = CallState.IDLE) }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                _callState.update { current ->
                    if (current.state == CallState.ACTIVE) {
                        current.copy(durationSeconds = current.durationSeconds + 1)
                    } else current
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun playFunnyAudio() {
        val ctx = context ?: return
        stopAudio()

        val audioFiles = listOf(
            R.raw.funny_audio_1,
            R.raw.funny_audio_2,
            R.raw.funny_audio_3,
            R.raw.funny_audio_4
        )

        try {
            val randomAudio = audioFiles.random()
            mediaPlayer = MediaPlayer.create(ctx, randomAudio)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                stopAudio()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun stopRingtone() {
        try {
            ringtonePlayer?.stop()
            ringtonePlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ringtonePlayer = null
    }
}
