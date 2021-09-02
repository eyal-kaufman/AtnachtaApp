package com.example.atnachta

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.QuerySnapshot
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File


class DataToCSV {

    companion object {
        private const val TAG_CSV = "DATA TO CSV"
        private const val TYPE_FILE = "text/csv"
        fun mapFields(fieldsName: MutableList<String>, mapTranslation :HashMap<String, String>){
            for (i in 0..fieldsName.size-1) {
                fieldsName[i] = mapTranslation[fieldsName[i]].toString()
            }
        }
//        fun mapReferenceFields(fieldsName: MutableList<String>){
//            for (i in 0..fieldsName.size-1) {
//                fieldsName[i] = referenceFieldMap[fieldsName[i]].toString()
//            }
//        }
        fun sendEmail(
            context: Context,
            file : File,
            subject_text: String,
            csvPrinter: CSVPrinter
        ){
            val path: Uri =
                FileProvider.getUriForFile(context, "com.example.atnachta.fileprovider", file)

            val fileIntent = Intent(Intent.ACTION_SEND)
            fileIntent.type = TYPE_FILE
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, subject_text)
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fileIntent.putExtra(Intent.EXTRA_STREAM, path)
            context.startActivity(fileIntent)

            csvPrinter.flush()
            csvPrinter.close()
        }
        fun writeCSV(
            documentSnapshot: QuerySnapshot,
            fileName: File,
            mapTranslation :HashMap<String, String>
        ) : CSVPrinter{
            var headers : MutableList<String> = mutableListOf()
            val writer = fileName.bufferedWriter()

            writer.write("\ufeff")//important for input in hebrew
            val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
            var profileMap: MutableMap<String, Any?> = mutableMapOf()
            for (doc in documentSnapshot) {
                profileMap = doc.data
                profileMap.remove("searchList")
                if (headers.size == 0) {

                    headers = profileMap.keys.toMutableList()
                    headers.sort()
                    Log.d(TAG_CSV, "${doc.id} BEFORE => ${headers}")
//                    translateHeadersFunction(headers)
                    DataToCSV.mapFields(headers, mapTranslation)
                    Log.d(TAG_CSV, "${doc.id} AFTER => ${headers}")
                    csvPrinter.printRecord(headers)

                }
                csvPrinter.printRecord(profileMap.toSortedMap().values)
            }

            return csvPrinter
        }


        val referenceFieldMap : HashMap<String, String> = hashMapOf(
            "receiverName" to "שם מקבל ההפניה",
            "dateOfRef" to "תאריך ההפניה",
            "timeOfRef" to "שעת הפניה",
            "reason" to "סיבת הפניה",
            "refererName" to "שם גורם מפנה",
            "refererJob" to "תפקיד גורם מפנה",
            "refererPhone" to "טלפון גורם מפנה",
            "refStatus" to "סטטוס הפניה",
            "knownToWelfare" to "נמצאת בקשר עם הרווחה?",
            "welfareName" to "שם גורם רווחה",
            "welfarePhone" to "טלפון גורם רווחה",
            "welfarePosition" to "תפקיד גורם רווחה",
            "leavingDate" to "תאריך עזיבה",
            "leavingTime" to "זמן עזיבה",
            "leavingReason" to "סיבת עזיבה",
            "leavingDestination" to "יעד עזיבה",
            "messageSentTo" to "הודעה נמסרה ל"
        )
        val fieldsMap : HashMap<String, String> = hashMapOf(

            "firstName" to "שם פרטי",
            "lastName" to "שם משפחה",
            "phone" to "טלפון פרופיל",
            "id" to "תעודת זהות פרופיל", //==ID
            "homeAddress" to "כתובת מגורים פרופיל",
            "dateOfBirth" to "תאריך לידה פרופיל",
            "originCountry" to "ארץ לידה",
            "yearOfAliyah" to "שנת עליה",
            "religiosity" to "מידת דתיות",
            "citizenshipStatus" to "סטטוס אזרחות",
            "age" to "גיל",
            "lastStudyingAt" to "מסגרת לימודים אחרונה",
            "grade" to "כיתה",
            "activeStudent" to "האם תלמידה פעילה", //==isActiveStudent
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
            "otherGuardian" to "קיים אפוטרופוס", //==isOtherGuardian
            "guardianName" to "שם אפוטרופוס",
            "guardianAddress" to "כתובת אפוטרופוס",
            "guardianPhone" to "טלפון אפוטרופוס",
            "guardianStatus" to "מצב משפחתי אפוטרופוס"
        )
    }


}