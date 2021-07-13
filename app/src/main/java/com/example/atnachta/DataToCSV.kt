package com.example.atnachta




class DataToCSV {

    companion object {
        fun mapFields(fieldsName: MutableList<String>) {
            for (i in 1..fieldsName.size-1) {
                fieldsName[i] = fieldsMap[fieldsName[i]].toString()
            }
        }
        val fieldsMap : HashMap<String, String> = hashMapOf(

            "firstName" to "שם פרטי",
            "lastName" to "שם משפחה",
            "phone" to "טלפון פרופיל",
            "ID" to "תעודת זהות פרופיל",
            "homeAddress" to "כתובת מגורים פרופיל",
            "dateOfBirth" to "תאריך לידה פרופיל",
            "originCountry" to "ארץ לידה",
            "yearOfAliyah" to "שנת עליה",
            "religiosity" to "מידת דתיות",
            "citizenshipStatus" to "סטטוס אזרחות",
            "age" to "גיל",
            "lastStudyingAt" to "מסגרת לימודים אחרונה",
            "grade" to "כיתה",
            "isActiveStudent" to "האם תלמידה פעילה",
            "medication" to "טיפול תרופתי קבוע",
            "medicalCare" to "קופת חולים",
            "medicalProblems" to "בעיות רפואיות",
            "fatherName" to "שם אב",
            "fatherAddress" to "כתובת מגורים אב",
            "fatherPhone" to "טלפון אב",
            "fatherStatus" to "מצב משפחתי אב",
            "motherName" to "שם אם",
            "motherAddress" to "כתובת מגורים אם",
            "motherPhone" to "טלפון אם",
            "motherStatus" to "מצב משפחתי אם",
            "isOtherGuardian" to "קיים אפוטרופוס",
            "guardianName" to "שם אפוטרופוס",
            "guardianAddress" to "כתובת אפוטרופוס",
            "guardianPhone" to "טלפון אפוטרופוס",
            "guardianStatus" to "מצב משפחתי אפוטרופוס"
        )
    }


}