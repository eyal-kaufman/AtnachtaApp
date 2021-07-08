package com.example.atnachta.data

import java.time.LocalDate


class Profile {
//    Personal Details:
    var firstName : String? = null
    var lastName : String? = null
    var phone : String? = null
    var ID : String = ""
    var homeAddress : String = ""
    var dateOfBirth : LocalDate? = null
    var originCountry : String = ""
    var yearOfAliyah : String = ""
    var religiosity : String = ""
    var citizenshipStatus : String = ""
    var age : Int? = null
//    Education:
    var lastStudyingAt : String = ""
    var grade : String = ""
    var isActiveStudent : String = ""
//    Health:
    var medicalCare : String = ""
    var medication : String = ""
    var medicalProblems : String = ""
//    Father:
    var fatherName : String = ""
    var fatherAddress : String = ""
    var fatherPhone : String = ""
    var fatherStatus : String = ""
//    Mother:
    var motherName : String = ""
    var motherAddress : String = ""
    var motherPhone : String = ""
    var motherStatus : String = ""
//    Other guardian:
    var isOtherGuardian : Boolean = false
    var guardianName : String = ""
    var guardianAddress : String = ""
    var guardianPhone : String = ""
    var guardianStatus : String = ""



    lateinit var searchList : List<String?> // TODO eyal - keep this field


    constructor() {}
    constructor(firstName: String?, age: Int?) {
        this.firstName = firstName ?: "" // if input is null, put empty string
        this.age = age

        // TODO eyal- also keep this line
        /* This list is needed for querying. When user searches for name/id, it searches this list
        * IMPORTANT - this must be updated every time we update first/last name*/
        this.searchList = listOf(firstName,lastName,"$firstName $lastName", ID)

    }

}
