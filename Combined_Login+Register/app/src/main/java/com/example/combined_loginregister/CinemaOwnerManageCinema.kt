package com.example.combined_loginregister

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.combined_loginregister.databinding.FragmentCinemaOwnerManageCinemaBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private const val PICK_IMAGES_REQUEST = 1

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CinemaOwnerManageCinema : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCinemaOwnerManageCinemaBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog
    private var selectedUri: Uri? = null
    private var cinemaList = ArrayList<CinemaTb>()
    private lateinit var adapter: CinemaOwnerCinemaListAdapter

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
        binding = FragmentCinemaOwnerManageCinemaBinding.inflate(layoutInflater, container, false)
        displayCinemas()

        binding.btnOpenAddCinemaDialog.setOnClickListener {
            dialogView = layoutInflater.inflate(R.layout.add_update_cinema, null)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)
            alertDialog = dialogBuilder.create()
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val uploadImagesBtn = dialogView.findViewById<Button>(R.id.uploadImagesBtn)
            val addCinemaDataBtn = dialogView.findViewById<Button>(R.id.AddCinemaData)
            val textInputLayout1: TextInputLayout = dialogView.findViewById(R.id.textInputLayout1)
            val textInputLayout2: TextInputLayout = dialogView.findViewById(R.id.textInputLayout2)
            val textInputLayout3: TextInputLayout = dialogView.findViewById(R.id.textInputLayout3)

            uploadImagesBtn.setOnClickListener {
                val cinemaName = textInputLayout1.editText?.text.toString()
                val cinemaCity = textInputLayout2.editText?.text.toString()
                val cinemaCapacity = textInputLayout3.editText?.text.toString()

                if (cinemaName.isEmpty()) {
                    textInputLayout1.error = "Please enter a cinema name"
                } else {
                    textInputLayout1.error = null
                }

                if (cinemaCity.isEmpty()) {
                    textInputLayout2.error = "Please select the city"
                } else {
                    textInputLayout2.error = null
                }

                if (cinemaCapacity.isEmpty()) {
                    textInputLayout3.error = "Please enter the capacity"
                } else {
                    textInputLayout3.error = null
                }

                if (textInputLayout1.error == null && textInputLayout2.error == null && textInputLayout3.error == null) {
                    openGallery()
                }
            }

            addCinemaDataBtn.setOnClickListener {
                val cinemaName = textInputLayout1.editText?.text.toString()
                val cinemaCity = textInputLayout2.editText?.text.toString()
                val cinemaCapacity = textInputLayout3.editText?.text.toString()

                if (cinemaName.isEmpty()) {
                    textInputLayout1.error = "Please enter a cinema name"
                } else {
                    textInputLayout1.error = null
                }

                if (cinemaCity.isEmpty()) {
                    textInputLayout2.error = "Please select the city"
                } else {
                    textInputLayout2.error = null
                }

                if (cinemaCapacity.isEmpty()) {
                    textInputLayout3.error = "Please enter the capacity"
                } else {
                    textInputLayout3.error = null
                }

                if (cinemaName.isNotEmpty() && cinemaCity.isNotEmpty() && selectedUri != null) {
                    textInputLayout1.error = null
                    textInputLayout2.error = null
                    uploadImageToFirebaseStorage(selectedUri)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select exactly one image for the cinema!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            alertDialog.show()
        }

        // Add TextWatcher to search bar in main fragment
        val searchBar: EditText = binding.searchBar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCinemas(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.data != null) {
                selectedUri = data.data
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select only one image!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayCinemas() {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(requireContext())

        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        val firebaseRestManager2 = FirebaseRestManager<CinemaTb>()

        firebaseRestManager.getAllItems(CinemaOwnerTb::class.java, "moviedb/CinemaOwnerTb") { items ->
            if (items.isNotEmpty()) {
                cinemaList.clear()  // Clear the existing cinema list


                for (item in items) {
                    if (item.uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                        firebaseRestManager2.getSingleItem(CinemaTb::class.java, "moviedb/cinematb", item.cinemaId!!) { cinemaitem ->
                            if (cinemaitem != null) {
                                cinemaList.add(cinemaitem)
                                adapter = CinemaOwnerCinemaListAdapter(cinemaList)
                                adapter.setOnItemClickListener(object : CinemaOwnerCinemaListAdapter.OnItemClickListener {
                                    override fun onItemClick(cinema: CinemaTb) {
                                        val cinemaRatingDialogHelper = CinemaRatingDialogHelper(requireContext(), cinema)
                                        cinemaRatingDialogHelper.showCinemaRatingDialog()
                                    }
                                })
                                binding.CinemaCardsHereForCO.adapter = adapter
                                binding.CinemaCardsHereForCO.layoutManager = LinearLayoutManager(requireContext())
                                loadingDialogHelper.dismissLoadingDialog()
                            } else {
                                loadingDialogHelper.dismissLoadingDialog()
                            }
                        }
                    } else {
                        loadingDialogHelper.dismissLoadingDialog()
                    }
                }
            } else {
                loadingDialogHelper.dismissLoadingDialog()
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri?) {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        val textInputLayout1: TextInputLayout = dialogView.findViewById(R.id.textInputLayout1)
        val textInputLayout2: TextInputLayout = dialogView.findViewById(R.id.textInputLayout2)
        val textInputLayout3: TextInputLayout = dialogView.findViewById(R.id.textInputLayout3)

        val firebaseRestManager = FirebaseRestManager<CinemaTb>()
        val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/cinematb")
        val cinemaId = dbRef.push().key

        FirebaseStorageHelper.uploadImage(
            imageUri!!,
            "CinemaPoster/" + cinemaId!!,
            onSuccess = { downloadUrl ->
                val tempCinema = CinemaTb(
                    cinemaId,
                    textInputLayout1.editText!!.text.toString(),
                    textInputLayout2.editText!!.text.toString(),
                    downloadUrl,
                    textInputLayout3.editText!!.text.toString()
                )

                firebaseRestManager.addItemWithCustomId(tempCinema, cinemaId, dbRef) { success, error ->
                    if (success) {
                        val tempKey = dbRef.push().key
                        val tempCinemaOwnerTb = CinemaOwnerTb(
                            tempKey,
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            cinemaId
                        )

                        val firebaseRestManager2 = FirebaseRestManager<CinemaOwnerTb>()
                        firebaseRestManager2.addItemWithCustomId(tempCinemaOwnerTb, tempKey!!, dbRef = FirebaseDatabase.getInstance().getReference("moviedb/CinemaOwnerTb")) { success, error ->
                            if (success) {
                                loadingScreen.dismissLoadingDialog()
                                alertDialog.dismiss()
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
                        Toast.makeText(
                            context,
                            "Error adding cinema data to database: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingScreen.dismissLoadingDialog()
                        alertDialog.dismiss()
                    }
                }
            },
            onFailure = { errorMessage ->
                Toast.makeText(
                    context,
                    "Image upload failed: $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
                loadingScreen.dismissLoadingDialog()
                alertDialog.dismiss()
            }
        )
    }

    private fun filterCinemas(query: String) {
        val filteredList = cinemaList.filter { cinema ->
            cinema.cinemaName!!.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }

    companion object {
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
