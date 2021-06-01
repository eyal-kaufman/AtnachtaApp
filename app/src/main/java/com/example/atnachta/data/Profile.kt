package com.example.atnachta.data

class Profile {
    var firstName : String? = null
    var lastName : String? = null
    var phone : String? = null

    constructor() {}
    constructor(firstName: String?, lastName: String?, phone: String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.phone = phone
    }
}
