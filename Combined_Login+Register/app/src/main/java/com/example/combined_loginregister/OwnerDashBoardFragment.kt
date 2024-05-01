package com.example.combined_loginregister

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.cardview.widget.CardView
import com.example.applicaitionowner.ManageCinemaOwner
import com.example.combined_loginregister.databinding.FragmentOwnerDashBoardBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private val ARG_PARAM1 = "param1"
private val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OwnerDashBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OwnerDashBoardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentOwnerDashBoardBinding

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
        // Inflate the layout for this fragment
        binding = FragmentOwnerDashBoardBinding.inflate(layoutInflater, container, false)

        binding.dashboardManageCinemaOwner.setOnClickListener {
            val newFragment = ManageCinemaOwner.newInstance("parameter1", "parameter2")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack(null) // Add to back stack to enable back navigation
                .commit()
        }
        binding.dashboardManageFeedback.setOnClickListener {  }
        binding.dashboardManageProfile.setOnClickListener {  }
        binding.logOut.setOnClickListener {

            val encryption = Encryption(requireContext())

            if(encryption.decrypt("userId")!=""){
                encryption.removeData("userId")
                val intent = Intent(requireContext(), LoginAndRegister::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        binding.dashboardManageMovies.setOnClickListener {
//            val newFragment = ManageMovies.newInstance("parameter1", "parameter2")
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, newFragment)
//                .addToBackStack(null) // Add to back stack to enable back navigation
//                .commit()
        }

        return binding.root
    }





}