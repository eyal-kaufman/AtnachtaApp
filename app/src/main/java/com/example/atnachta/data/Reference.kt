package com.example.atnachta.data

data class Reference(
        var receiverName: String, var date: String, var time: String, var reason: String, var refererName: String,
        var refererJob: String, var refererPhone: Int) {
    //TODO : Change date and time to proper types. String is only temporary!
}