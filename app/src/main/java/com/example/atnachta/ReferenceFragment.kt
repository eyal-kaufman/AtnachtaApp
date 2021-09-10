package com.example.atnachta


import android.app.DatePickerDialog
import android.app.DownloadManager
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_reference.*
import com.example.atnachta.databinding.FragmentReferenceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val REFERENCES_COLLECTION = "References"
private const val PROFILES_COLLECTION = "profiles"

// typealias for the file system - pair of a TextView, which is the view that displays the file link,
// and String that is the file type (i.e "parantApproval","medicalIntake"....) which is used to upload
// the file to the appropriate field on the database
typealias FileTypePair = Pair<TextView,String>

/**
 * A simple [Fragment] subclass.
 * Use the [ReferenceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReferenceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val TAG: String = "reference"

    /* firebase singletons */
    private val firestore = Firebase.firestore
    private val storageRef = Firebase.storage.reference

    lateinit var binding: FragmentReferenceBinding
    lateinit var docID: String
    lateinit var referenceID: String
    lateinit var girlDocRef: DocumentReference
    lateinit var referenceDocRef: DocumentReference

    // array with all of the edit texts (this to disable when in display mode)
    var editTextTitlesArray: Array<TextView?> = arrayOfNulls(20) // todo ask shira why not late init

    lateinit var viewsMap: Map<String, TextView> // maps the ref field names to the view that displays them
    lateinit var titlesMap: Map<TextView, ViewGroup> // maps the subsection titles to their cards


    lateinit var fileTextViewsArray: Array<TextView> // array of all the text views that display file links

    lateinit var leavingDataViewsArray: Array<TextView> // array of all the text views that display leaving data

    private val formatDate = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY) // Italy is a random eu state
    private val formatTime = SimpleDateFormat("kk:mm", Locale.ITALY)

    private lateinit var refStatusOptions : Array<String> // all the possible ref-status values

    // builders for all the dialogs
    private lateinit var statusDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var leavingDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var cancelLeaveDialogBuilder: MaterialAlertDialogBuilder

    /* variables for the file system */

    /* fileButtonMap:
    * Keys - the upload button of of each file type
    * Values - see FileTypePair alias definition */
    private lateinit var fileButtonsMap: MutableMap<ImageButton,FileTypePair>

    private lateinit var fileGetter: ActivityResultLauncher<String> // launcher for the file activity
    private lateinit var currentFileType: String
    private lateinit var currentFileView: TextView

    /* fileDialogMap:
    * Keys - see FileTypePair alias definition
    * Values - a pair of dialog builders. The first is for download dialog, second is for delete dialog */
    private lateinit var fileDialogBuilderMap: Map
    <FileTypePair,Pair<MaterialAlertDialogBuilder,MaterialAlertDialogBuilder>>

    // builder fot the file overwrite dialog, which is identical for every file
    private lateinit var fileOverwriteDialogBuilder: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reference, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ActionBar title text
        activity?.titleTextView?.text = getString(R.string.referenceFragmentTitle)

        docID = ReferenceFragmentArgs.fromBundle(requireArguments()).docID
        referenceID = ReferenceFragmentArgs.fromBundle(requireArguments()).referenceID
        girlDocRef = firestore.collection(PROFILES_COLLECTION).document(docID)
        referenceDocRef = girlDocRef.collection(REFERENCES_COLLECTION).document(referenceID)

        initialData(view)
        setUpTitles()
        displayMode(view) // the initial mode is display mode (and not edit mode)

        // edit and submit button listeners
        binding.refEditButton.setOnClickListener {
            editMode(it)
        }
        binding.refSubmitChanges.setOnClickListener {
            for ((k, v) in viewsMap) {
                referenceDocRef.update(k, v.text.toString()).addOnSuccessListener {
                    Log.d(TAG, "document updated successfully")
                }
                    .addOnFailureListener { exception -> Log.d(TAG, "update failed with ", exception) }
            }
            referenceDocRef.update("knownToWelfare",binding.ifWelfare.isChecked)
            displayMode(view)
        }

        // calls for the functions that set up the ref status dialog
        setupStatusDialog()
        setupLeavingDialog()
        setupCancelLeavingDialog()

        setupFileDialogs()

        setUpDateTimePickers()

        // calls for the functions that set up the ref file system
        setUpFileGetter()
        setUpFileListeners()
    }

    /**
     * set up the subsections titles - the listeners that open the subsections card, and move the
     * arrow icons etc.
     */
    private fun setUpTitles() {
        for ((k, v) in titlesMap) {
            k.setOnClickListener {
                if (v.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(v, AutoTransition())
                    k.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_keyboard_arrow_up_24,
                        0,
                        0,
                        0
                    )
                    v.visibility = View.VISIBLE;
                } else {
                    k.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_keyboard_arrow_down_24,
                        0,
                        0,
                        0
                    )
                    v.visibility = View.GONE;
                }
            }
        }
        // sets up the checkbox for the welfare data
        binding.ifWelfare.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                welfare_data.visibility = View.VISIBLE
            } else {
                welfare_data.visibility = View.GONE
            }
        }
    }


    /**
     * initialized all of the dialogs of the file system
     */
    private fun setupFileDialogs() {
        referenceDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    for ((fileTypes,builders) in fileDialogBuilderMap ){
                        builders.first // download dialog
                            .setTitle(R.string.download_file_dialog_title)
                            .setMessage(getString(R.string.download_file_dialog_body,
                                document.getString("${fileTypes.second}Filename")))
                            .setNegativeButton(R.string.cancel){ dialog, which ->
                                // do nothing
                            }
                            .setPositiveButton(R.string.accept){dialog, which ->
                                // download
                                downloadFile(fileTypes.second)
                            }
                        builders.second // delete dialog
                            .setTitle(R.string.delete_file_dialog_title)
                            .setMessage(getString(R.string.delete_file_dialog_body,
                                document.getString("${fileTypes.second}Filename")))
                            .setNegativeButton(R.string.cancel){ dialog, which ->
                                // do nothing
                            }
                            .setPositiveButton(R.string.accept){dialog, which ->
                                // delete
                                deleteFile(fileTypes)
                            }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        // file overwrite dialog
        fileOverwriteDialogBuilder = dialogInitializer()
            .setTitle(R.string.file_overwrite_dialog_title)
            .setMessage(R.string.file_overwrite_dialog_body)
            .setNegativeButton(R.string.cancel){ dialog, which ->
                // does nothing
            }
            .setPositiveButton(R.string.accept){dialog, which ->
                // upload file anyway
                fileGetter.launch("application/pdf")
            }

    }


    private fun setUpDateTimePickers() {
        edit_ref_date.setOnClickListener {v:View -> setDatePicker(v) }
        edit_ref_leaving_date.setOnClickListener { v:View -> setDatePicker(v) }
        edit_ref_time.setOnClickListener { v:View -> setTimePicker(v) }
        edit_ref_leaving_time.setOnClickListener { v:View -> setTimePicker(v) }
    }

    /**
     * a function that builds a date picker over a view
     * v - the view that the displays the date picker
     */
    private fun setDatePicker(v : View) {
        val dateView :TextView = v as TextView
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            v.context, DatePickerDialog.OnDateSetListener{ view, chosenYear, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox)
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR,chosenYear)
                selectedDate.set(Calendar.MONTH,monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                val date = formatDate.format(selectedDate.time)
                dateView.text = date
            }, year, month, day)
        datePicker.show()
    }

    /**
     * a function that builds a time picker over a view
     * v - the view that the displays the time picker
     */
    private fun setTimePicker(v:View){
        val timeView :TextView = v as TextView
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val timePicker = TimePickerDialog(
            v.context, TimePickerDialog.OnTimeSetListener{ view, chosenHour, chosenMinute ->
                // Display Selected time in textbox)
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY,chosenHour)
                selectedTime.set(Calendar.MINUTE,chosenMinute)
                val time = formatTime.format(selectedTime.time)
                timeView.text = time
            }, hour, minute,true)
        timePicker.show()
    }

    /**
     * this function checks what ref status has been chosen, and colors the text in the appropriate
     * color
     */
    private fun setUpStatusColor() {
        val statusColor : Int = when(refStatusOptions.indexOf(edit_ref_status.text.toString())){
            0 -> R.color.accepted_not_arrived
            1 -> R.color.accepted_arrived
            2 -> R.color.not_accepted
            else -> {
                R.color.arrived_and_left
            }
        }
        edit_ref_status.setTextColor(ContextCompat.getColorStateList(requireActivity(),statusColor))
    }

    /**
     * initialized the dialog of the ref status
     */
    private fun setupStatusDialog() {
        refStatusOptions = resources.getStringArray(R.array.reference_status_array)
        statusDialogBuilder = dialogInitializer()
            .setTitle(R.string.status_dialog_title)
            .setItems(R.array.reference_status_array) { dialog, which ->
                val currStatus = edit_ref_status.text.toString()
                edit_ref_status.text = resources.getStringArray(R.array.reference_status_array)[which]
                setUpStatusColor()
                if (which == 3){
                    dialog.dismiss()
                    leavingDialogBuilder.show()
                } else if (currStatus == resources.getStringArray(R.array.reference_status_array)[3]){
                    // if the user cancels the status "left"
                    cancelLeaveDialogBuilder.show()
                }
            }
    }

    /**
     * initialized the leaving dialog, which appears when the user has chosen the "left" status for
     * the reference
     */
    private fun setupLeavingDialog() {
        leavingDialogBuilder = dialogInitializer()
            .setTitle(R.string.leaving_dialog_title)
            .setNeutralButton(R.string.close) { dialog, which ->
                    leavingCard.visibility = View.VISIBLE
                    leavingData.visibility = View.VISIBLE
                    edit_ref_leaving_date.text = formatDate.format(Calendar.getInstance().time)
                    edit_ref_leaving_time.text = formatTime.format(Calendar.getInstance().time)
                scroll_view.post {
                    scroll_view.fullScroll(View.FOCUS_DOWN)
                    leavingData.requestFocus()
                }
            }
    }

    /**
     * initialized the "cancel leave" dialog, which appears when the user has changed the status from
     * "left" to a different status. this means the user canceled the "left" status, and all of the
     * leaving details will be deleted
     */
    private fun setupCancelLeavingDialog() {
        cancelLeaveDialogBuilder = MaterialAlertDialogBuilder(
            requireActivity(), R.style.Title_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(R.string.cancel_leave_dialog_title)
            .setMessage(R.string.cancel_leave_dialog_body)
            .setNegativeButton(R.string.cancel){ dialog, which ->
                // return the status to "left"
                edit_ref_status.text = resources.getStringArray(R.array.reference_status_array)[3]
                setUpStatusColor()
            }
            .setPositiveButton(R.string.accept){dialog, which ->
                // delete leaving data and remove leaving card
                for(v in leavingDataViewsArray){
                    v.text = ""
                }
                leavingCard.visibility = View.GONE
                leavingData.visibility = View.GONE
            }
    }

    /**
     * initializes all of the data structures that the fragment needs
     */
    private fun initialData(view: View) {
        editTextTitlesArray = arrayOf(
            binding.editRefDate, binding.editRefLeavingDate, binding.editRefLeavingDest,
            binding.editRefLeavingMsg, binding.editRefLeavingReason, binding.editRefLeavingTime,
            binding.editRefDate, binding.editRefName, binding.editRefPhone, binding.editRefReason,
            binding.editRefRule, binding.editRefStatus, binding.editRefTime, binding.editRefWelfareName,
            binding.editRefWelfarePhone, binding.editRefWelfareRule, binding.editReferrerName, binding.ifWelfare,
        )

        viewsMap = mapOf(
            "refStatus" to edit_ref_status,"receiverName" to edit_ref_name, "dateOfRef" to edit_ref_date,
            "timeOfRef" to edit_ref_time, "welfareName" to edit_ref_welfare_name,
            "welfarePhone" to edit_ref_welfare_phone, "welfarePosition" to edit_ref_welfare_rule,
            "referrerName" to edit_referrer_name, "referrerJob" to edit_ref_rule,
            "referrerPhone" to edit_ref_phone, "reason" to edit_ref_reason,
            "leavingDate" to edit_ref_leaving_date, "leavingTime" to edit_ref_leaving_time,
            "leavingReason" to edit_ref_leaving_reason,"leavingDestination" to edit_ref_leaving_dest,
            "messageSentTo" to edit_ref_leaving_msg,"parentApprovalFilename" to parentApprovalLink,
            "courtOrderFilename" to courtOrderLink, "socialWorkerIntakeFilename" to socialWorkerIntakeLink,
            "medicalIntakeFilename" to medicalIntakeLink
        )

        fileTextViewsArray = arrayOf(
            parentApprovalLink, courtOrderLink, socialWorkerIntakeLink, medicalIntakeLink
        )
        leavingDataViewsArray = arrayOf(
            edit_ref_leaving_time, edit_ref_leaving_date, edit_ref_leaving_reason, edit_ref_leaving_dest, edit_ref_leaving_msg
        )
        titlesMap = mapOf(filesTitle to filesData, leavingTitle to leavingData)
        fileButtonsMap = mutableMapOf(
            binding.parentApprovalButton to Pair(binding.parentApprovalLink,"parentApproval"),
            binding.courtOrderButton to Pair(binding.courtOrderLink,"courtOrder"),
            binding.socialWorkerIntakeButton to Pair(binding.socialWorkerIntakeLink,"socialWorkerIntake"),
            binding.medicalIntakeButton to Pair(binding.medicalIntakeLink,"medicalIntake")
        )

        fileDialogBuilderMap = mapOf(
            Pair(binding.parentApprovalLink,"parentApproval") to Pair(dialogInitializer(),dialogInitializer()),
            Pair(binding.courtOrderLink,"courtOrder") to Pair(dialogInitializer(),dialogInitializer()),
            Pair(binding.socialWorkerIntakeLink,"socialWorkerIntake") to  Pair(dialogInitializer(),dialogInitializer()),
            Pair(binding.medicalIntakeLink,"medicalIntake") to Pair(dialogInitializer(),dialogInitializer()))
    }

    /**
     * this function creates a empty dialog, that later can be set up with title, message, etc...
     */
    private fun dialogInitializer(): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(
            requireActivity(),R.style.Title_ThemeOverlay_MaterialComponents_MaterialAlertDialog)
    }

    /**
     * reads the data from the reference firestore document and puts it in the views
     */
    private fun retrieveReferenceData(document: DocumentSnapshot) {
        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
        for ((k, v) in viewsMap) {
            v.text = document.getString(k)
        }
        if (document.getBoolean("knownToWelfare")!!){
            binding.ifWelfare.isChecked = true
        }
        if (document.getString("refStatus") == resources.getStringArray(R.array.reference_status_array)[3]){
            // if status is "left"
            leavingCard.visibility = View.VISIBLE
        }
    }

    /**
     * sets the mode of the fragment to "display" - the user can't edit the profile data
     */
    private fun displayMode(view: View) {
//        test.isEnabled = false
//        test.setTextColor(-16777216)
        for (edit_text in editTextTitlesArray) {
            if (edit_text != null) {
                edit_text.isEnabled = false
            }
        }
        referenceDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    retrieveReferenceData(document)
                    setUpStatusColor()
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        view.visibility = View.VISIBLE
        ref_edit_button.visibility = View.VISIBLE
        ref_submit_changes.visibility = View.GONE

        /* reference status edit view*/
        edit_ref_status.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        edit_ref_status.setOnClickListener {} // Clicking should do nothing in display mode
    }

    /**
     * sets the mode of the fragment to "edit" - the user can edit the profile data
     */
    private fun editMode(view: View) {
        for (edit_text in editTextTitlesArray) {
            if (edit_text != null) {
                edit_text.isEnabled = true
            }
        }
        view.visibility = View.GONE
        ref_submit_changes.visibility = View.VISIBLE

        /* reference status edit view*/
        edit_ref_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_edit_24,0,0,0)
        edit_ref_status.compoundDrawablePadding = 25
        edit_ref_status.setOnClickListener {
            statusDialogBuilder.show()
        }
    }

    /**
     * builds the activity that prompts the user to pick a file
     */
    private fun setUpFileGetter() {
        fileGetter = registerForActivityResult(ActivityResultContracts.GetContent()){
            if (it != null){
                uploadFile(it)
            } else{ // if the operation failed, or back was pressed (no file picked)
                Log.w(TAG, "pickingFileFromDevice:failure")
                Toast.makeText(context, getString(R.string.filePickerError),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * sets up the listeners for the file system:
     * 1. the upload button listener
     * 2. listener for the FileTypePair view (the view that holds the link for each file)
     */
    private fun setUpFileListeners() {
        for((button,fileTypePair) in fileButtonsMap){
            button.setOnClickListener{
                currentFileView = fileTypePair.first
                currentFileType = fileTypePair.second
                if (currentFileView.text.toString().isNotBlank()){
                    fileOverwriteDialogBuilder.show()
                } else{
                    fileGetter.launch("application/pdf")
                }
            }
            fileTypePair.first.setOnClickListener {
                showFileMenu(fileTypePair)
                // show the file menu, with the download and delete options
            }
        }
    }

    /**
     * show the file menu for the chosen file type. it has to options - Download and Delete
     */
    private fun showFileMenu(FileTypePair: FileTypePair) {
        PopupMenu(requireActivity(), FileTypePair.first).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.download -> {
                        fileDialogBuilderMap[FileTypePair]?.first?.show()
                        true
                    }
                    R.id.delete -> {
                        fileDialogBuilderMap[FileTypePair]?.second?.show()
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.reference_file_menu)
            show()
        }
    }

    /**
     * takes the URI of the file the user has chosen, and uploads it to firebase in 2 ways
     * 1. uploads the file to the firebase storage, in a path respective for the current reference
     * 2. uploads the file name and the download link to the firebase document of the current reference
     */
    private fun uploadFile(fileUri: Uri) {
        val filenameFromURI = getFilename(fileUri)
        if (filenameFromURI == null){ // making sure the filename was retrieved correctly
            Log.w(TAG, "uploadFileToStorage:failure")
            Toast.makeText(context, getString(R.string.fileUploadError),
                Toast.LENGTH_SHORT).show()
            return
        }
        val fileRef = storageRef.child(docID).child(referenceID).child(filenameFromURI)
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
                val url =downloadUri.toString()
                val filenameField = "${currentFileType}Filename" // the firestore field for the filename
                val fileURLField = "${currentFileType}URL" // the firestore field for the URL
                referenceDocRef
                    .update(mapOf(
                        filenameField to  filenameFromURI,
                        fileURLField to url))
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                        val currentFileName = currentFileView.text.toString()
                        if (currentFileName.isNotBlank()){
                            // delete existing file from storage
                            storageRef.child(docID).child(referenceID).child(currentFileName).delete()
                        }
                        currentFileView.text = filenameFromURI}
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            } else {
                Log.w(TAG, "gettingFileURL:failure")
                Toast.makeText(context, getString(R.string.getURLFailure),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * download the chosen file type (parentApproval, medicalIntake...) to the users phone
     */
    private fun downloadFile(fileType: String) {
        var url : String?
        var filename : String?
        referenceDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    url = document.getString("${fileType}URL")
                    filename = document.getString("${fileType}Filename")
                    if (url == null || filename==null) {
                        Log.w(TAG, "downloadingFileFromURL:failure")
                        Toast.makeText(
                            context, getString(R.string.downloadFromStorageError),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else{ // everything is valid - download the file
                        val request = DownloadManager.Request(
                            Uri.parse(url))
                            .setTitle(filename)
                            .setDescription(getString(R.string.fileDownloadDescription))
                            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "$filename.pdf")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        val downloadManager : DownloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val downloadId = downloadManager.enqueue(request) // not sure why this is useful
                    }
                } else { // for some reason, the document is null
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception -> // if getting the doc snapshot failed
                Log.d(TAG, "get failed with ", exception)
            }
    }

    /**
     * deletes the chosen file type from firebase in 2 ways
     * 1. deletes the file from firebase storage
     * 2. deletes the file name and the download link from the firebase document of the current reference
     */
    private fun deleteFile(fileTypePair: FileTypePair) {
        val filenameField = "${fileTypePair.second}Filename" // the firestore field for the filename
        val fileURLField = "${fileTypePair.second}URL" // the firestore field for the URL
        val filename = fileTypePair.first.text.toString()
        val fileRef = storageRef.child(docID).child(referenceID).child(filename)
        fileRef.delete().addOnSuccessListener {
            // File deleted successfully
            // remove filename and file url from firestore and from the textView
            referenceDocRef
                .update(mapOf(
                    filenameField to  "",
                    fileURLField to ""))
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    Toast.makeText(context, getString(R.string.fileDeleteSuccess),
                        Toast.LENGTH_SHORT).show()
                    currentFileView.text = ""}
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    Toast.makeText(
                        context, getString(R.string.deleteFileError),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }.addOnFailureListener {e ->
            Log.w(TAG, "Error deleting file from storage", e)
            Toast.makeText(
                context, getString(R.string.deleteFileError),
                Toast.LENGTH_SHORT
            ).show()
        }
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
         * @return A new instance of fragment ReferenceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReferenceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}