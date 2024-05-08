package com.example.combined_loginregister

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.combined_loginregister.databinding.FragmentCinemaOwnerManageCInemaAdminBinding
import com.example.combined_loginregister.databinding.FragmentCinemaOwnerManageCinemaBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CinemaOwnerManageCInemaAdmin.newInstance] factory method to
 * create an instance of this fragment.
 */
class CinemaOwnerManageCInemaAdmin : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentCinemaOwnerManageCInemaAdminBinding


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
        binding = FragmentCinemaOwnerManageCInemaAdminBinding.inflate(
            layoutInflater,
            container,
            false
        )

        getOwnedCinemaList(binding.cinemaAdminHereForCO)

        binding.btnOpenAddCinemaAdminDialog.setOnClickListener {
        }



        return binding.root
    }

    private fun getOwnedCinemaList(cinemaAdminHereForCO: RecyclerView) {
        val firebaseRestManager = FirebaseRestManager<CinemaOwnerTb>()
        firebaseRestManager.getAllItems(CinemaOwnerTb::class.java,"moviedb/CinemaOwnerTb"){items->
            if(items.isNotEmpty()){
                val onwedCinemaList = ArrayList<CinemaTb>()
                for(item in items){
                    if(item.uid==FirebaseAuth.getInstance().currentUser!!.uid){
                        val firebaseRestManager2 = FirebaseRestManager<CinemaTb>()

                        firebaseRestManager2.getSingleItem(CinemaTb::class.java,"moviedb/cinematb",item.cinemaId!!){tempCinemaItem->
                            onwedCinemaList.add(tempCinemaItem!!)
                        }
                    }

                }

                val adapter = CinemaOwnerCinemaListAdapter(onwedCinemaList)

                cinemaAdminHereForCO.adapter = adapter
                cinemaAdminHereForCO.layoutManager = LinearLayoutManager(requireContext())

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
         * @return A new instance of fragment CinemaOwnerManageCInemaAdmin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CinemaOwnerManageCInemaAdmin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}