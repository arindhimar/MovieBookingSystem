package com.example.combined_loginregister

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.combined_loginregister.databinding.FragmentCinemaOwnerLeaseMovieBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CinemaOwnerLeaseMovie.newInstance] factory method to
 * create an instance of this fragment.
 */
class CinemaOwnerLeaseMovie : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding:FragmentCinemaOwnerLeaseMovieBinding
    lateinit var dialogView: View
    lateinit var alertDialog: AlertDialog

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

        binding = FragmentCinemaOwnerLeaseMovieBinding.inflate(layoutInflater, container, false)

        getLeasedMovies(binding.LeasedMovieCardsHereForCO)

        binding.btnOpenAddLeaseMovieDialog.setOnClickListener {
            // Inflate the custom layout
            dialogView = layoutInflater.inflate(R.layout.addleasemoviesdialog, null)

            // Create the dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            // Show the dialog
            alertDialog = dialogBuilder.create()
            alertDialog.show()

            // Initialize the recyclerView object
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.UnleasedMoviesHere)

            // Set custom window animations
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            // Display the movies
            getUnleasedMovies(recyclerView)
        }
        return binding.root
    }

    private fun getUnleasedMovies(recyclerView: RecyclerView) {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        // Get data of all the movies in movietb
        val movieClass = MovieTB::class.java
        val node = "moviedb/movietb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(movieClass, node) { items ->
            requireActivity().runOnUiThread {
                loadingScreen.dismissLoadingDialog()

                if (items.isNotEmpty()) {
                    val movieList = ArrayList<MovieTB>()

                    // Iterate through each movie item
                    for (tempMovieItem in items) {
                        val firebaseRestManager2 = FirebaseRestManager<LeaseMovieTb>()
                        firebaseRestManager2.getAllItems(
                            LeaseMovieTb::class.java,
                            "moviedb/leasemovietb"
                        ) { leaseMovies ->
                            // Check if the movie is not leased by the current user
                            val isLeased = leaseMovies.any {
                                it.userId == FirebaseAuth.getInstance().currentUser?.uid && it.movieId == tempMovieItem.mid
                            }
                            if (!isLeased) {
                                movieList.add(tempMovieItem)
                                Log.d("ye leeee movie ka data", "onItemClick: ashdjashdkjashjdksahd")
                            }


                                Log.d("TAG", "getUnleasedMovies: mkc ashdkhaskjdhkjashdjkhasjkdh")

                                // Set up RecyclerView adapter and layout manager
                                val adapter = OwnerMovieListAdapter(movieList)
                                adapter.setOnItemClickListener(object :
                                    OwnerMovieListAdapter.OnItemClickListener {
                                    override fun onItemClick(movie: MovieTB) {
                                        Log.d("ye leeee movie ka data", "onItemClick: ${movie.mid}")

                                        val yesOrNoLoadingHelper = YesOrNoLoadingHelper()
                                        yesOrNoLoadingHelper.showLoadingDialog(requireContext())
                                        yesOrNoLoadingHelper.updateCheckBoxText("Do you agree with terms and condition for leasing the movie??")
                                        yesOrNoLoadingHelper.hideText()


                                        val view = yesOrNoLoadingHelper.getView()

                                        val yesBtn = view.findViewById<Button>(R.id.btn_yes)
                                        val noBtn = view.findViewById<Button>(R.id.btn_no)

                                        noBtn.setOnClickListener {
                                            yesOrNoLoadingHelper.dismissLoadingDialog()
                                        }

                                        yesBtn.setOnClickListener {
                                            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
                                            if (checkBox.isChecked) {
                                                yesOrNoLoadingHelper.dismissLoadingDialog()




                                                val firebaseRestManager =
                                                    FirebaseRestManager<LeaseMovieTb>()
                                                val dbRef = FirebaseDatabase.getInstance()
                                                    .getReference("moviedb/leasemovietb")
                                                val tempId = dbRef.push().key
                                                val tempData = LeaseMovieTb(
                                                    tempId,
                                                    movie.mid,
                                                    FirebaseAuth.getInstance().currentUser!!.uid
                                                )
                                                firebaseRestManager.addItem(
                                                    tempData,
                                                    dbRef
                                                ) { success, error ->
                                                    if (success) {
                                                        val successLoadingHelper = SuccessLoadingHelper()
                                                        successLoadingHelper.showLoadingDialog(
                                                            requireContext()
                                                        )
                                                        getLeasedMovies(binding.LeasedMovieCardsHereForCO)

                                                        alertDialog.dismiss()

                                                        successLoadingHelper.hideButtons()
                                                        successLoadingHelper.updateText("Congratulations you have successfully leased the movie!!")

                                                        val handler = Handler()
                                                        handler.postDelayed({
                                                            successLoadingHelper.dismissLoadingDialog()
                                                        }, 2000)

                                                    } else {
                                                        Log.e(
                                                            "Error while adding to the db",
                                                            "${error!!.message}: ",
                                                        )
                                                        yesOrNoLoadingHelper.dismissLoadingDialog()

                                                    }
                                                }
                                            } else {
                                                val warningDialogBinding = WarningLoadingHelper()
                                                warningDialogBinding.showLoadingDialog(requireContext())
                                                warningDialogBinding.hideButtons()
                                                warningDialogBinding.updateText("Please accept the terms and conditions to lease the movie!!")

                                                val handler = Handler()
                                                handler.postDelayed({
                                                    warningDialogBinding.dismissLoadingDialog()
                                                }, 2000)
                                            }

                                        }

                                    }

                                })
                                recyclerView.adapter = adapter
                                recyclerView.layoutManager = LinearLayoutManager(requireContext())

                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No movies found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





    private fun getLeasedMovies(recyclerView: RecyclerView) {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        // Get data of all the movies in movietb
        val movieClass = MovieTB::class.java
        val node = "moviedb/movietb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(movieClass, node) { items ->
            requireActivity().runOnUiThread {
                loadingScreen.dismissLoadingDialog()

                if (items.isNotEmpty()) {
                    val movieList = ArrayList<MovieTB>()

                    // Iterate through each movie item
                    for (tempMovieItem in items) {
                        val firebaseRestManager2 = FirebaseRestManager<LeaseMovieTb>()
                        firebaseRestManager2.getAllItems(
                            LeaseMovieTb::class.java,
                            "moviedb/leasemovietb"
                        ) { leaseMovies ->
                            // Check if the movie is not leased by the current user
                            val isLeased = leaseMovies.any {
                                it.userId == FirebaseAuth.getInstance().currentUser?.uid && it.movieId == tempMovieItem.mid

                            }
                            if (isLeased) {
                                Log.d("ye leeee movie ka data", "onItemClick: ashdjashdkjashjdksahd")
                                movieList.add(tempMovieItem)

                            }


                            Log.d("TAG", "getUnleasedMovies: mkc ashdkhaskjdhkjashdjkhasjkdh")

                            // Set up RecyclerView adapter and layout manager
                            val adapter = OwnerMovieListAdapter(movieList)
                            adapter.setOnItemClickListener(object :
                                OwnerMovieListAdapter.OnItemClickListener {
                                override fun onItemClick(movie: MovieTB) {
                                    TODO("Not yet implemented")
                                }

                            })
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(requireContext())

                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No movies found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CinemaOwnerLeaseMovie.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaOwnerLeaseMovie().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}