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

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CinemaOwnerLeaseMovie : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCinemaOwnerLeaseMovieBinding
    private lateinit var dialogView: View
    private lateinit var alertDialog: AlertDialog

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
    ): View {
        binding = FragmentCinemaOwnerLeaseMovieBinding.inflate(inflater, container, false)
        getLeasedMovies(binding.LeasedMovieCardsHereForCO)

        binding.btnOpenAddLeaseMovieDialog.setOnClickListener {
            showAddLeaseMovieDialog(inflater)
        }

        return binding.root
    }

    private fun showAddLeaseMovieDialog(inflater: LayoutInflater) {
        dialogView = inflater.inflate(R.layout.addleasemoviesdialog, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.show()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.UnleasedMoviesHereForCO)
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        if (recyclerView != null) {
            getUnleasedMovies(recyclerView)
        } else {
            Toast.makeText(requireContext(), "Error: RecyclerView not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUnleasedMovies(recyclerView: RecyclerView) {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        val movieClass = MovieTB::class.java
        val node = "moviedb/movietb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(movieClass, node) { items ->
            requireActivity().runOnUiThread {
                loadingScreen.dismissLoadingDialog()

                if (items.isNotEmpty()) {
                    val movieList = ArrayList<MovieTB>()
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser == null) {
                        Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    for (tempMovieItem in items) {
                        val firebaseRestManager2 = FirebaseRestManager<LeaseMovieTb>()
                        firebaseRestManager2.getAllItems(LeaseMovieTb::class.java, "moviedb/leasemovietb") { leaseMovies ->
                            val isLeased = leaseMovies.any {
                                it.userId == currentUser.uid && it.movieId == tempMovieItem.mid
                            }
                            if (!isLeased) {
                                movieList.add(tempMovieItem)
                            }

                            val adapter = OwnerMovieListAdapter(movieList)
                            adapter.setOnItemClickListener(object : OwnerMovieListAdapter.OnItemClickListener {
                                override fun onItemClick(movie: MovieTB) {
                                    showLeaseConfirmationDialog(movie)
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

    private fun showLeaseConfirmationDialog(movie: MovieTB) {
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
                leaseMovie(movie)
            } else {
                val warningDialogBinding = WarningLoadingHelper()
                warningDialogBinding.showLoadingDialog(requireContext())
                warningDialogBinding.hideButtons()
                warningDialogBinding.updateText("Please accept the terms and conditions to lease the movie!!")

                Handler().postDelayed({
                    warningDialogBinding.dismissLoadingDialog()
                }, 2000)
            }
        }
    }

    private fun leaseMovie(movie: MovieTB) {
        val firebaseRestManager = FirebaseRestManager<LeaseMovieTb>()
        val dbRef = FirebaseDatabase.getInstance().getReference("moviedb/leasemovietb")
        val tempId = dbRef.push().key
        val tempData = LeaseMovieTb(tempId, movie.mid, FirebaseAuth.getInstance().currentUser!!.uid)

        firebaseRestManager.addItemWithCustomId(tempData,tempId!!, dbRef) { success, error ->
            if (success) {
                val successLoadingHelper = SuccessLoadingHelper()
                successLoadingHelper.showLoadingDialog(requireContext())
                getLeasedMovies(binding.LeasedMovieCardsHereForCO)
                alertDialog.dismiss()
                successLoadingHelper.hideButtons()
                successLoadingHelper.updateText("Congratulations you have successfully leased the movie!!")

                Handler().postDelayed({
                    successLoadingHelper.dismissLoadingDialog()
                }, 2000)
            } else {
                Log.e("Error while adding to the db", "${error!!.message}: ")
            }
        }
    }

    private fun getLeasedMovies(recyclerView: RecyclerView) {
        val loadingScreen = LoadingDialogHelper()
        loadingScreen.showLoadingDialog(requireContext())

        val movieClass = MovieTB::class.java
        val node = "moviedb/movietb"

        val firebaseRestManager = FirebaseRestManager<MoviePosterTb>()
        firebaseRestManager.getAllItems(movieClass, node) { items ->
            requireActivity().runOnUiThread {
                loadingScreen.dismissLoadingDialog()

                if (items.isNotEmpty()) {
                    val movieList = ArrayList<MovieTB>()
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser == null) {
                        Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    for (tempMovieItem in items) {
                        val firebaseRestManager2 = FirebaseRestManager<LeaseMovieTb>()
                        firebaseRestManager2.getAllItems(LeaseMovieTb::class.java, "moviedb/leasemovietb") { leaseMovies ->
                            val isLeased = leaseMovies.any {
                                it.userId == currentUser.uid && it.movieId == tempMovieItem.mid
                            }
                            if (isLeased) {
                                movieList.add(tempMovieItem)
                            }

                            val adapter = OwnerMovieListAdapter(movieList)
                            adapter.setOnItemClickListener(object : OwnerMovieListAdapter.OnItemClickListener {
                                override fun onItemClick(movie: MovieTB) {
                                    val movieShowsHelperClass = MovieRatingDialogHelper(requireContext(),movie)
                                    movieShowsHelperClass.showMovieRatingDialog()
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
