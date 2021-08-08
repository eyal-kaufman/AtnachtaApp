package com.example.atnachta

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.OpenableColumns
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.atnachta.databinding.FragmentProfileBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_profile.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "profileFragment"
private const val FILE_SELECT_CODE = 0 // added for file uploads

/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentProfileBinding

    lateinit var firestore: FirebaseFirestore
    lateinit var profileRef: DocumentReference
    lateinit var storage: FirebaseStorage
    lateinit var storageRef: StorageReference
    private var downloadId : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
//        binding.button4.setOnClickListener { view : View -> view.findNavController().navigate(R.id.action_profileFragment_to_newReferenceFragment)}
        activity?.setTitle(R.string.basicDetails)
        binding.editButton.setOnClickListener {
            editMode(it)
        }

        binding.button5.setOnClickListener {
            displayMode(it)
        }
        binding.parentsTitle.setOnClickListener {
            if (parentsData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(parentsData, AutoTransition())
                parentsData.visibility = View.VISIBLE;
            } else {
                parentsData.visibility = View.GONE;
            }
        }
        binding.generalTitle.setOnClickListener {
            if (generalData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(generalData, AutoTransition())
                generalData.visibility = View.VISIBLE;
            } else {
                generalData.visibility = View.GONE;
            }
        }
        binding.referenceTitle.setOnClickListener {
            if (referenceData.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(referenceData, AutoTransition())
                referenceData.visibility = View.VISIBLE;
            } else {
                referenceData.visibility = View.GONE;
            }
        }
        return binding.root
    }


    private fun displayMode(view: View) {
        edited_name.text = edit_name.text
        edited_name.visibility = View.VISIBLE
        edit_name.visibility = View.GONE

        edited_id.text = edit_id.text
        edited_id.visibility = View.VISIBLE
        edit_id.visibility = View.GONE

        edited_phone.text = edit_phone.text
        edited_phone.visibility = View.VISIBLE
        edit_phone.visibility = View.GONE

        edited_father_name.text = edit_father_name.text
        edited_father_name.visibility = View.VISIBLE
        edited_mother_name.visibility = View.VISIBLE

        edited_mother_name.text = edit_mother_name.text
        edit_father_name.visibility = View.GONE
        edit_mother_name.visibility = View.GONE

        edited_father_phone.text = edit_father_phone.text
        edited_father_phone.visibility = View.VISIBLE
        edited_mother_phone.visibility = View.VISIBLE

        edited_mother_phone.text = edit_mother_phone.text
        edit_father_phone.visibility = View.GONE
        edit_mother_phone.visibility = View.GONE

        edited_address.text = edit_address.text
        edited_address.visibility = View.VISIBLE
        edit_address.visibility = View.GONE

        edited_birth_date.text = edit_birth_date.text
        edited_birth_date.visibility = View.VISIBLE
        edit_birth_date.visibility = View.GONE

        edited_kupach.text = edit_kupach.text
        edited_kupach.visibility = View.VISIBLE
        edit_kupach.visibility = View.GONE

        edited_school.text = edit_school.text
        edited_school.visibility = View.VISIBLE
        edit_school.visibility = View.GONE

        edited_civil.text = edit_civil.text
        edited_civil.visibility = View.VISIBLE
        edit_civil.visibility = View.GONE

        view.visibility = View.VISIBLE
        edit_button.visibility = View.VISIBLE
        button5.visibility = View.GONE
    }


    private fun editMode(view: View) {

        edited_name.visibility = View.GONE
        edit_name.visibility = View.VISIBLE

        edited_id.visibility = View.GONE
        edit_id.visibility = View.VISIBLE

        edited_phone.visibility = View.GONE
        edit_phone.visibility = View.VISIBLE

        edited_father_name.visibility = View.GONE
        edited_mother_name.visibility = View.GONE

        edit_father_name.visibility = View.VISIBLE
        edit_mother_name.visibility = View.VISIBLE

        edited_father_phone.visibility = View.GONE
        edited_mother_phone.visibility = View.GONE

        edit_father_phone.visibility = View.VISIBLE
        edit_mother_phone.visibility = View.VISIBLE

        edited_address.visibility = View.GONE
        edit_address.visibility = View.VISIBLE

        edited_birth_date.visibility = View.GONE
        edit_birth_date.visibility = View.VISIBLE

        edited_kupach.visibility = View.GONE
        edit_kupach.visibility = View.VISIBLE

        edited_school.visibility = View.GONE
        edit_school.visibility = View.VISIBLE

        edited_civil.visibility = View.GONE
        edit_civil.visibility = View.VISIBLE

        view.visibility = View.GONE
        button5.visibility = View.VISIBLE
    }

    // todo: these last functions Itay added to test file uploads
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore
        storage = Firebase.storage
        storageRef = storage.reference
        val profileDocID = profileFragmentArgs.fromBundle(requireArguments()).docID
        profileRef = firestore.collection("profiles").document(profileDocID)
        pickFile()
    }

    private fun pickFile() {
        val fileintent = Intent(Intent.ACTION_GET_CONTENT)
        fileintent.type = "application/pdf"
        fileintent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(fileintent,"Select PDF"), FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != FILE_SELECT_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        // upload to cloud storage
        val fileUri = data!!.data
        if (fileUri != null){
            uploadFile(fileUri)
        } else{
            Log.w(TAG, "pickingFileFromDevice:failure")
            Toast.makeText(context, getString(R.string.filePickerError),
                Toast.LENGTH_SHORT).show()
        }
    }



    private fun uploadFile(fileUri: Uri) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val filenameFromURI = getFilename(fileUri)
        if (filenameFromURI == null){
            Log.w(TAG, "uploadFileToStorage:failure")
            Toast.makeText(context, getString(R.string.fileUploadError),
                Toast.LENGTH_SHORT).show()
            return
        }
        val fileRef = storageRef.child(filenameFromURI)
        val uploadTask = fileRef.putFile(fileUri)
        val urlTask = uploadTask.continueWithTask { task ->
            // getting the download URL of the file
            if (!task.isSuccessful) {
                task.exception?.let {
                    Log.w(TAG, "uploadFileToStorage:failure",it)
                    Toast.makeText(context, getString(R.string.fileUploadError),
                        Toast.LENGTH_SHORT).show()
                    throw it
                }
            }
            Log.w(TAG, "uploadFileToStorage:success")
            Toast.makeText(context, getString(R.string.fileUploadSuccess),
                Toast.LENGTH_SHORT).show()
            fileRef.downloadUrl // the result of the task is now the URL, see firebase docs
            // adding the file name as a field in the profile
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                fileRef.metadata.addOnSuccessListener { metadata ->
                    val filename = metadata.name
                    val url =downloadUri.toString()
                    profileRef
                        .update(mapOf(
                            "filename" to  metadata.name,
                            "fileURL" to downloadUri.toString()))
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                            updateUI()}
                        .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                }.addOnFailureListener {e -> Log.w(TAG, "Error getting file metadata", e) }
            } else {
                Log.w(TAG, "gettingFileURL:failure")
                Toast.makeText(context, getString(R.string.getURLFailure),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * update the textView and set onclick listener
     */
    private fun updateUI() {
        profileRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val filename = document.getString("filename") ?: ""
                    binding.editedName.text = filename
                    binding.editedName.setOnClickListener{downloadFile(document.getString("fileURL"),filename)}
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }


    private fun downloadFile(url: String?, filename: String) {
        if (url == null) {
            Log.w(TAG, "downloadingFileFromURL:failure")
            Toast.makeText(
                context, getString(R.string.downloadFromStorageError),
                Toast.LENGTH_SHORT
            ).show()
        }
        val request = DownloadManager.Request(
            Uri.parse(url))
            .setTitle(filename)
            .setDescription(getString(R.string.fileDownloadDescription))
            .setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, "$filename.pdf")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val downloadManager : DownloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
    }

    /**
     * this method receives a file URI and returns its actual name. Any other method i tried, like
     * directly checking the URIs path, resulted in a cryptic name (for example document:1111)
     * this method was copied from stackoverflow:
     * https://stackoverflow.com/questions/45054005/android-incorrect-file-name-for-selected-filefrom-file-chooser
     * currently has no idea what is going on in there or why it works
     */
    private fun getFilename(uri: Uri): String? {
        val cursor = activity?.contentResolver?.query(uri, null, null, null, null)
        var filename: String? = null

        cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)?.let { nameIndex ->
            cursor.moveToFirst()

            filename = cursor.getString(nameIndex)
            cursor.close()
        }
        return filename
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment profileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            profileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


//    fun sendProfilesByEmail(){
//        filename ="myFile.csv"
//        filepath = "MyFileDir"
//        val myextrnal = File(context?.getExternalFilesDir(filepath), filename)
//
////        var headers : SortedSet<String> = sortedSetOf()
//        var headers : MutableList<String> = mutableListOf()
//        try{
//            collectionReference.orderBy("firstName", Query.Direction.DESCENDING)
//                .get().addOnSuccessListener { documents->
//                    val writer = myextrnal.bufferedWriter()
//                    writer.write("\ufeff")
//                    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
//
//                    for (doc in documents){
//                        doc.data.remove("ID")
//                        doc.data.remove("")
//                        if (headers.size==0) {
////                            headers = doc.data.keys.toSortedSet()
//                            headers = doc.data.keys.toMutableList()
//                            headers.sort()
//                            Log.d(TAG, "${doc.id} BEFORE => ${headers}")
//                            DataToCSV.mapFields(headers)
//                            Log.d(TAG, "${doc.id} AFTER => ${headers}")
//                            csvPrinter.printRecord(headers)
//                        }
//                        csvPrinter.printRecord(doc.data.toSortedMap().values)
//
//                        Log.d(TAG, "${doc.id} => ${doc.data.toSortedMap()}")
//
//                    }
//                    val path: Uri =
//                        FileProvider.getUriForFile(requireContext(), "com.example.atnachta.fileprovider", myextrnal)
//
//                    val fileIntent = Intent(Intent.ACTION_SEND)
//                    fileIntent.type = "text/csv"
//                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
//                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    fileIntent.putExtra(Intent.EXTRA_STREAM, path)
//                    startActivity(fileIntent)
//
//                    csvPrinter.flush()
//                    csvPrinter.close()
//                }
//
//        } catch (e : FileNotFoundException){
//            e.printStackTrace()
//        } catch (e : IOException){
//            e.printStackTrace()
//        }
//    }