package com.example.modulesubmission.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.modulesubmission.FirebaseRestManager
import com.example.modulesubmission.R
import com.example.modulesubmission.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var firebaseRestManager: FirebaseRestManager<UserTb>
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseRestManager = FirebaseRestManager()
        firebaseAuth = FirebaseAuth.getInstance()
        fetchOwnerData()


        //adding data
        binding.addOwnerData.setOnClickListener {
            val tempData = UserTb("1","arin","owner")
            val databaseReference = Firebase.database.getReference("arindb/usertb")

            //first add data to firebase authentication
            firebaseAuth.createUserWithEmailAndPassword("alvfcoc@gmail.com","Try@99").addOnCompleteListener { authtask->
                if(authtask.isSuccessful){
                    firebaseAuth.currentUser!!.sendEmailVerification()
                    val tempUser = UserTb(firebaseAuth.currentUser!!.uid,"arin","owner")
                    firebaseRestManager.addItemWithCustomId(tempUser,firebaseAuth.currentUser!!.uid,databaseReference){success,error->
                        if(success){
                            Toast.makeText(requireContext(), "User Data added!!", Toast.LENGTH_SHORT).show()

                            firebaseAuth.signOut()

                            fetchOwnerData()
                        }
                        else{
                            Log.e("TAG", "onCreateView: ${error!!.message}", )
                        }
                    }
                }
                else{
                    Log.e("TAG", "onCreateView: ${authtask.exception}", )
                }
            }

        }


        return root
    }

    @SuppressLint("SetTextI18n")
    private fun fetchOwnerData(){
        binding.OwnerCardsHere.removeAllViews()
        firebaseRestManager.getAllItems(UserTb::class.java,"arindb/usertb"){items->
            if(items.isNotEmpty()){
                for(item in items){
                    val card = layoutInflater.inflate(R.layout.display_card, binding.OwnerCardsHere, false)

                    val txt = card.findViewById<TextView>(R.id.text)
                    txt.text = "Name : ${item.userName}\nUserType : ${item.userType}"

                    binding.OwnerCardsHere.addView(card)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}