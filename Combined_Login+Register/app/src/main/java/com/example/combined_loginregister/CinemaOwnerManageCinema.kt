package com.example.combined_loginregister

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.combined_loginregister.databinding.FragmentCinemaOwnerManageCinemaBinding
import com.example.combined_loginregister.databinding.FragmentCommonProfileBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


private const val PICK_IMAGES_REQUEST = 1


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CinemaOwnerManageCinema.newInstance] factory method to
 * create an instance of this fragment.
 */
class CinemaOwnerManageCinema : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCinemaOwnerManageCinemaBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
    private var selectedUri: Uri? = null

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
        binding = FragmentCinemaOwnerManageCinemaBinding.inflate(layoutInflater, container, false)


        displayCinemas()


        binding.btnOpenAddCinemaDialog.setOnClickListener {
            dialogView = layoutInflater.inflate(R.layout.add_update_cinema, null)

            // Create the dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            // Set custom window animations
            alertDialog = dialogBuilder.create()
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val uploadImagesBtn = dialogView.findViewById<Button>(R.id.uploadImagesBtn)
            val AddCinemaData = dialogView.findViewById<Button>(R.id.AddCinemaData)
            val textInputLayout1: TextInputLayout = dialogView.findViewById(R.id.textInputLayout1)
            val textInputLayout2: TextInputLayout = dialogView.findViewById(R.id.textInputLayout2)
            val textInputLayout3: TextInputLayout = dialogView.findViewById(R.id.textInputLayout3)



            uploadImagesBtn.setOnClickListener {
                val cinemaName = textInputLayout1.editText?.text.toString()
                val cinemaCity = textInputLayout2.editText?.text.toString()
                val cinemaCapacity = textInputLayout3.editText?.text.toString()

                if (cinemaName.isEmpty()) {
                    // Display error for empty movie name
                    textInputLayout1.error = "Please enter a cinema name"
                } else {
                    // Clear any previous error
                    textInputLayout1.error = null
                }

                if (cinemaCity.isEmpty()) {
                    // Display error for empty movie duration
                    textInputLayout2.error = "Please select the city"
                } else {
                    // Clear any previous error
                    textInputLayout2.error = null
                }

                if (cinemaCapacity.isEmpty()) {
                    // Display error for empty movie duration
                    textInputLayout3.error = "Please enter the capacity"
                } else {
                    // Clear any previous error
                    textInputLayout3.error = null
                }

                // Open gallery only if both fields are not empty
                if (textInputLayout1.error == null && textInputLayout2.error == null&& textInputLayout3.error == null) {
                    openGallery()
                }
            }


            AddCinemaData.setOnClickListener {
                // Perform action when Add Cinema button is clicked
                val cinemaName = textInputLayout1.editText?.text.toString()
                val cinemaCity = textInputLayout2.editText?.text.toString()
                val cinemaCapacity = textInputLayout3.editText?.text.toString()

                if (cinemaName.isEmpty()) {
                    // Display error for empty cinema name
                    textInputLayout1.error = "Please enter a cinema name"
                } else {
                    // Clear any previous error
                    textInputLayout1.error = null
                }

                if (cinemaCity.isEmpty()) {
                    // Display error for empty city
                    textInputLayout2.error = "Please select the city"
                } else {
                    // Clear any previous error
                    textInputLayout2.error = null
                }

                if (cinemaCapacity.isEmpty()) {
                    // Display error for empty capacity
                    textInputLayout3.error = "Please enter the capacity"
                } else {
                    // Clear any previous error
                    textInputLayout3.error = null
                }

                // Check if a single image is selected
                if (cinemaName.isNotEmpty() && cinemaCity.isNotEmpty() && selectedUri != null) {
                    // A single image is selected, proceed with cinema addition
                    // Reset the error states
                    textInputLayout1.error = null
                    textInputLayout2.error = null

                    uploadImageToFirebaseStorage(selectedUri)
                } else {
                    // No image is selected or multiple images are selected, show error message
                    Toast.makeText(
                        requireContext(),
                        "Please select exactly one image for the cinema!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
            alertDialog.show()

        }

        return binding.root
    }

    fun init(){


    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) // Restrict selection to a single image
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Check if a single image is selected
            if (data.data != null) {
                selectedUri = data.data
            } else {
                // Handle multiple images selected (which should not happen in this case)
                Toast.makeText(requireContext(), "Please select only one image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayCinemas() {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())

        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        val firebaseRestManager2 = FirebaseRestManager<CinemaTb>()

        firebaseRestManager.getAllItems(
            CinemaOwnerTb::class.java,
            "moviedb/CinemaOwnerTb"
        ) { items ->
            val cinemaTbList = ArrayList<CinemaTb>()

            if (items.isNotEmpty()) {
                for (item in items) {
                    if (item.uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                        firebaseRestManager2.getAllItems(
                            CinemaTb::class.java,
                            "moviedb/cinematb"
                        ) { cinemaitems ->
                            if (cinemaitems.isNotEmpty()) {
                                for (cinemaitem in cinemaitems) {
                                    if (cinemaitem.cinemaID == item.cinemaId) {
                                        cinemaTbList.add(cinemaitem)
                                    }
                                }

                                val cinemaOwnerCinemaListAdapter = CinemaOwnerCinemaListAdapter(cinemaTbList)
                                binding.CinemaCardsHereForCO.adapter = cinemaOwnerCinemaListAdapter
                                binding.CinemaCardsHereForCO.layoutManager = LinearLayoutManager(requireContext())

                                // Close loading dialog once data is loaded
                                loadingDialogHelper.dismissLoadingDialog()
                            }
                        }

                        // Break the loop after processing data for the current user
                        break
                    }
                }
            } else {
                // Close loading dialog if no items are found
                loadingDialogHelper.dismissLoadingDialog()
            }
        }
    }




    private fun uploadImageToFirebaseStorage(imageUri: Uri?) {
        // Show loading dialog
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        val textInputLayout1: TextInputLayout = dialogView.findViewById(R.id.textInputLayout1)
        val textInputLayout2: TextInputLayout = dialogView.findViewById(R.id.textInputLayout2)
        val textInputLayout3: TextInputLayout = dialogView.findViewById(R.id.textInputLayout3)

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val firebaseRestManager = FirebaseRestManager<CinemaTb>()
        val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/cinematb")
        val cinemaId = dbRef.push().key


        // Upload image to Firebase Storage
        FirebaseStorageHelper.uploadImage(
            imageUri!!,
            "CinemaPoster/"+cinemaId!!, // Pass the cinema ID instead of the movie title
            onSuccess = { downloadUrl ->
                // Image uploaded successfully
//                Toast.makeText(
//                    context,
//                    "Image uploaded successfully",
//                    Toast.LENGTH_SHORT
//                ).show()
                Log.d("TAG", "uploadImageToFirebaseStorage: $downloadUrl")
                val tempCinema = CinemaTb(cinemaId, textInputLayout1.editText!!.text.toString(), textInputLayout2.editText!!.text.toString(),downloadUrl,textInputLayout3.editText!!.text.toString())

                // Add cinema data to the database
                firebaseRestManager.addItemWithCustomId(tempCinema,cinemaId, dbRef) { success, error ->
                    if (success) {
                        // Cinema data added successfully now adding cinemaownertb

                        val tempKey = dbRef.push().key
                        val tempCinemaOwnerTb = CinemaOwnerTb(tempKey,FirebaseAuth.getInstance().currentUser!!.uid,cinemaId)

                        val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()

                        firebaseRestManager2.addItemWithCustomId(tempCinemaOwnerTb,tempKey!!, dbRef = FirebaseDatabase.getInstance().getReference("moviedb/CinemaOwnerTb")){success, error ->
                            if(success){
                                loadingScreen.dismissLoadingDialog()
                                alertDialog.dismiss() // Close the alert dialog

                                val successLoadingHelper = SuccessLoadingHelper()
                                successLoadingHelper.showLoadingDialog(requireContext())
                                successLoadingHelper.hideButtons()
                                successLoadingHelper.updateText("Cinema Data Added!")
                                displayCinemas()
                                val handler = Handler()
                                handler.postDelayed({
                                    successLoadingHelper.dismissLoadingDialog()
                                }, 2000)
                            }
                        }


                    } else {
                        // Handle error adding cinema data to database
                        Toast.makeText(
                            context,
                            "Error adding cinema data to database: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingScreen.dismissLoadingDialog()
                        alertDialog.dismiss() // Close the alert dialog
                    }
                }
            },
            onFailure = { errorMessage ->
                // Handle unsuccessful uploads
                Toast.makeText(
                    context,
                    "Image upload failed: $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
                loadingScreen.dismissLoadingDialog()
                alertDialog.dismiss() // Close the alert dialog
            }
        )
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CinemaOwnerManageCinema.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaOwnerManageCinema().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}