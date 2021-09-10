package com.example.atnachta.data

import java.time.LocalDate
import java.time.LocalTime

data class Reference(
//        details:
        var receiverName: String = "",
        var dateOfRef: String = "",
        var timeOfRef: String = "",
        var reason: String = "",
        var referrerName: String = "",
        var referrerJob: String = "",
        var referrerPhone: String = "",
        var refStatus : String = "",
//        Welfare services:
        var knownToWelfare : Boolean = false,
        var welfareName: String = "",
        var welfarePhone: String = "",
        var welfarePosition: String = "",
//      Leaving deatils:
        var leavingDate: LocalDate? = null,
        var leavingTime: LocalTime? = null,
        var leavingReason : String = "",
        var leavingDestination : String = "",
        var messageSentTo : String =""

) {
    //TODO : Change date and time to proper types. String is only temporary!
}