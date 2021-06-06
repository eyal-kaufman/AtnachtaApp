package com.example.atnachta.data

class Profile {
    var firstName : String? = null
    var lastName : String? = null
    lateinit var searchList : List<String?> // TODO eyal - keep this field
    var phone : String? = null

    constructor() {}
    constructor(firstName: String?, lastName: String?, phone: String?) {
        this.firstName = firstName ?: "" // if input is null, put empty string
        this.lastName = lastName ?: ""
        this.phone = phone ?: ""

        // TODO eyal- also keep this line
        /* This list is needed for querying. When user searches for name/id, it searches this list
        * IMPORTANT - this must be updated every time we update first/last name*/
        this.searchList = listOf(firstName,lastName,"$firstName $lastName", phone) //todo change phone to ID

    }
}
