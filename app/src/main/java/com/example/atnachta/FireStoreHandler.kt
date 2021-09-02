package com.example.atnachta

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import java.util.*

class FireStoreHandler {

    companion object {
        val TAG_DELETE = "Delete"
        fun createReferenceReport(
            profileCollection: CollectionReference
        ){

            profileCollection.get().addOnSuccessListener { profileDocs ->
                val referencesList = mutableListOf<MutableMap<String,Any>>()
                for (profile in profileDocs){
                    profileCollection.document(profile.id).collection("References").get().addOnSuccessListener { referenceDocs ->
                        var referenceMap: MutableMap<String, Any> = mutableMapOf()
                        for (reference in referenceDocs){
                            referenceMap = reference.data
                            referenceMap["profileName"] = profile.data["firstName"].toString() + " "+ profile.data["lastName"].toString()
                            referencesList.add(referenceMap)
                        }

                    }

                }


            }
        }
        fun createReport(
            attendanceCollection: CollectionReference,
            month: Int,
            year: Int
        ) {
            val startDateQuery = Calendar.getInstance()
            val endDateQuery = Calendar.getInstance()
            startDateQuery.set(Calendar.YEAR, year)
            startDateQuery.set(Calendar.MONTH, month - 1)
            startDateQuery.set(Calendar.DAY_OF_MONTH, 1)
            startDateQuery.set(Calendar.HOUR_OF_DAY, 0)
            startDateQuery.set(Calendar.MINUTE, 0)
            startDateQuery.set(Calendar.SECOND, 0)

            endDateQuery.set(Calendar.YEAR, year)
            endDateQuery.set(Calendar.MONTH, month)
            endDateQuery.set(Calendar.DAY_OF_MONTH, 1)
            endDateQuery.set(Calendar.HOUR_OF_DAY, 0)
            endDateQuery.set(Calendar.MINUTE, 0)
            endDateQuery.set(Calendar.SECOND, 0)
            val query: Query = attendanceCollection.whereGreaterThanOrEqualTo(
                "startTime",
                Timestamp(startDateQuery.time)
            )
//            attendanceCollection.whereLessThan("endTime",Timestamp(endDateQuery.time))
            val profileToAttendees: MutableMap<String, Date> = mutableMapOf()

            query.get()
                .addOnSuccessListener {
                    for (attendance in it.documents) {
                        if (profileToAttendees[attendance.data?.get("profile")] != null){

                        }
                        profileToAttendees[attendance.data?.get("profile") as String] =
                            (attendance.data?.get("startTime") as Timestamp).toDate()

                    }
                    Log.d("OUTER_SCOPE", "creating report ${profileToAttendees}")

                    print(profileToAttendees)
                }
        }
        fun deleteReference(
            referenceCollection: CollectionReference,
            referenceDocID: String,
            attendanceCollection: CollectionReference
        ) {
            referenceCollection.document(referenceDocID).delete()
                .addOnSuccessListener { Log.d(TAG_DELETE, "reference ID: $referenceDocID deleted") }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG_DELETE,
                        "Failed to delete reference ID: $referenceDocID deleted",
                        e
                    )
                }
            val query : Query = attendanceCollection.whereEqualTo("reference", referenceDocID)
            query.get()
                .addOnSuccessListener {
                    for (attendance in it.documents){
                        attendance.reference.delete()
                        Log.d(TAG_DELETE, "attendance that related to $referenceDocID with ID: ${attendance.id} deleted")
                    }
//                    Log.d(TAG, "attendance related to reference ID: $referenceDocID deleted")
                    }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG_DELETE,
                        "Failed to delete reference ID: $referenceDocID deleted",
                        e
                    )
                }
        }

        fun deleteProfile(
            profileDocument: DocumentReference,
            referenceCollection: CollectionReference,
            referenceList: MutableCollection<String>?,
            attendanceCollection : CollectionReference
        ) {
            if (referenceList==null){
                referenceCollection.get()
                    .addOnSuccessListener { references_documents ->
                        for (ref in references_documents) {
                            deleteReference(referenceCollection, ref.id,attendanceCollection)
//                            referencesList.add(doc.id)
                        }
                    }
            }
            else{
                for (ref in referenceList) {
                    deleteReference(referenceCollection, ref,attendanceCollection)
                }
            }

            profileDocument.delete()
                .addOnSuccessListener { Log.d(TAG_DELETE, "profile ID: ${profileDocument.id} deleted") }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG_DELETE,
                        "Failed to delete profile ID: ${profileDocument.id} deleted",
                        e
                    )
                }

        }
    }

}