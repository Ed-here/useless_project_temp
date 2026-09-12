package com.example.phonedialer.service

import android.telecom.Call
import android.telecom.InCallService

class CustomInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.setInCallService(this)
        CallManager.setTelecomCall(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.setTelecomCall(null)
        CallManager.setInCallService(null)
    }
}
