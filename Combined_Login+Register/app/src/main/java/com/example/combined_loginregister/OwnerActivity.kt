package com.example.combined_loginregister
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.applicaitionowner.ManageCinemaOwner
import com.example.applicaitionowner.MyAccountFragment
import com.example.combined_loginregister.databinding.ActivityOwnerBinding
import com.example.combined_loginregister.databinding.FragmentManageCinemaownerBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

class OwnerActivity : AppCompatActivity() {
    lateinit var binding: ActivityOwnerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        validateUser()

        init()
//        validateUser()
    }

    private fun validateUser(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser==null){
            val warningLoadingHelper = WarningLoadingHelper()
            warningLoadingHelper.hideButtons()
            warningLoadingHelper.updateText("Invalid User Detection!!")

            val handler = Handler()
            handler.postDelayed({
                warningLoadingHelper.dismissLoadingDialog()
            }, 2000)
        }
    }

    private fun init(){
        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            this,
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional

                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()
        setSupportActionBar(binding.ToolBaar)



        val headerView = binding.navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.NavHeaderText)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val email = user.email
            usernameTextView.text = email
            val firebaseRestManager = FirebaseRestManager<UserTb>()
            firebaseRestManager.getSingleItem(UserTb::class.java,"moviedb/usertb",user.uid){
                usernameTextView.text ="Welcome ${it!!.uname}"
            }
        }




//        Log.d("TAG", "onCreate: ahsdjkhasjkhdkhasjkhdkhaskdh")

        binding.ToolBaar.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navView)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Set checked state for the selected item
            menuItem.isChecked = true
            // Close the drawer when item is tapped
            binding.drawerLayout.closeDrawers()
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.nav_ownerdashboard -> {
                    binding.OwnerDashBoard.isVisible = true
                }
                R.id.nav_manage_cinemaowner -> {
                    replaceFragment(ManageCinemaOwner())
                }
                R.id.nav_movies -> {
                    replaceFragment(ManageMovies())
                }
                R.id.nav_menu_account -> {
                    replaceFragment(CommonProfileFragment())
                }

                R.id.logoutuser -> {
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
                    binding.logOut.performClick()
                }

            }
            true
        }



        binding.dashboardManageCinemaOwner.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_manage_cinemaowner)
            binding.navView.menu.performIdentifierAction(R.id.nav_manage_cinemaowner, 0)
        }

        binding.dashboardManageMovies.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_movies)
            binding.navView.menu.performIdentifierAction(R.id.nav_movies, 0)
        }

        binding.dashboardManageProfile.setOnClickListener {
            binding.navView.setCheckedItem(R.id.nav_menu_account)
            binding.navView.menu.performIdentifierAction(R.id.nav_menu_account, 0)
        }
        binding.logOut.setOnClickListener {

            binding.navView.setCheckedItem(R.id.logoutuser)
            binding.navView.menu.performIdentifierAction(R.id.logoutuser, 0)
        }

        binding.navView.setCheckedItem(R.id.nav_ownerdashboard)




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                binding.drawerLayout.closeDrawer(binding.navView)
            } else {
                binding.drawerLayout.openDrawer(binding.navView)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment: Fragment) {
        binding.OwnerDashBoard.isVisible = false

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}





