package com.example.atnachta.data


import java.util.*

data class Reference(
//        details:
        var receiverName: String = "",
        var dateOfRef: String = "",
        var timeOfRef: String = "",
        var reason: String = "",
        var refererName: String = "",
        var refererJob: String = "",
        var refererPhone: String = "",
        var refStatus: String = "",
//        Welfare services:
        var knownToWelfare: Boolean = false,
        var welfareName: String = "",
        var welfarePhone: String = "",
        var welfarePosition: String = "",
//      Leaving deatils:
        var leavingDate: String? = null,
        var leavingTime: String? = null,
        var leavingReason: String = "",
        var leavingDestination: String = "",
        var messageSentTo: String =""

) {
    //TODO : Change date and time to proper types. String is only temporary!
}