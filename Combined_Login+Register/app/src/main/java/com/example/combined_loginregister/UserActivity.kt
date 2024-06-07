package com.example.combined_loginregister


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.combined_loginregister.databinding.ActivityUserBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import nl.joery.animatedbottombar.AnimatedBottomBar

class UserActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)



        setUpBottomBar()

        // Initialize with the HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(UserMainFragment())
        }
    }

    private fun setUpBottomBar() {

        binding.LogoutButton.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("390229249723-kgf51fevhonod7sf18vnd5ga6tnna0ed.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            mGoogleSignInClient.signOut()

            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            val intent = Intent(this, LoginAndRegister::class.java)
            startActivity(intent)
        }

        binding.bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                when (newTab.title) {
                    "Home" -> replaceFragment(UserMainFragment())
                    "Bookings"-> replaceFragment(TicketFragment())
                    "Feedback" -> replaceFragment(UserFeedbackFragment())
                    "Profile" -> replaceFragment(CommonProfileFragment())
                }
            }
        })

        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val firebaseRestManager = FirebaseRestManager<UserTb>()
        firebaseRestManager.getSingleItem(UserTb::class.java,"moviedb/usertb",currentUser){
            binding.UserName.text = "Welcome Back "+it!!.uname
        }



        binding.bottomBar.selectTabAt(0)
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("TAG", "replaceFragment:$fragment ")
        if (fragment is CommonProfileFragment) {

                Log.d("TAG", "replaceFragment:yessssssssssssssssssssssss ")

                binding.fragmentContainer.setBackgroundColor(getColor(R.color.white))

        }else{
            binding.fragmentContainer.setBackgroundColor(getColor(R.color.black))
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
