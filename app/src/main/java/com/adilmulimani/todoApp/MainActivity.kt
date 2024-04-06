package com.adilmulimani.todoApp


import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.adilmulimani.todoApp.databinding.ActivityMainBinding
import com.adilmulimani.todoApp.fragments.FinishedTaskFragment
import com.adilmulimani.todoApp.fragments.HomeFragment
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.domainUrl
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    //declaring the object for view binding
    private lateinit var binding : ActivityMainBinding
    //initialising the header fields
   private lateinit var navUserProfile:ImageView
   private lateinit var navUserName:TextView
   private lateinit var navUserEmail:TextView
   //drawer toggle
   private lateinit var drawerToggle:ActionBarDrawerToggle
    //creating the object to store token of current user
    private lateinit var token :String
    private lateinit var mySharedPreference :MySharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setting up view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySharedPreference = MySharedPreference(this@MainActivity)

        //setup Navigation Drawer
        navigationDrawerSetup()
        //initializing drawer such that only home fragment should pop up first
        initDrawer()
        //setting support to tool bar inorder to access menu items
        //setSupportActionBar(binding.include.toolBar)
        //getting user profile details
        getUserProfile()
    }

    private fun initDrawer()
    {
        //when we open main activity first then Home Fragment must be displayed
        supportFragmentManager.beginTransaction().replace(R.id.content,HomeFragment()).commit()

        drawerToggle = object : ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.include.toolBar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }
        }

        drawerToggle.drawerArrowDrawable.color = resources.getColor(R.color.white)
        binding.drawerLayout.addDrawerListener(drawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun navigationDrawerSetup()
    {
        //open navigation drawer when clicked on menu button
        binding.include.toolBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        //To access navigation header
        val headerBinding = binding.navigationVIew.getHeaderView(0)

        navUserProfile = headerBinding.findViewById(R.id.headerImage)
        navUserName = headerBinding.findViewById(R.id.headerUserName)
        navUserEmail = headerBinding.findViewById(R.id.headerEmail)


        //To access navigation menu
        binding.navigationVIew.setNavigationItemSelectedListener {

            //we close the navigation drawer when we click on an item
            binding.drawerLayout.close()

            when(it.itemId)
            {
                R.id.logout->{
                    //we delete all his stored data like token
                    mySharedPreference.clearUserData()
                    //we then direct user back to login Activity and end this activity
                    startActivity(Intent(this@MainActivity,LoginActivity::class.java))
                    finish()
                }

                R.id.nav_home->{
                    val t = supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.content,HomeFragment())

                    t.commit()
                }
                R.id.finishedTask->{
                    val t = supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.content,FinishedTaskFragment())

                    t.commit()
                }
            }

            //to highlight an item when clicked
            it.isChecked = true
            return@setNavigationItemSelectedListener true
        }
    }

    private fun getUserProfile() {

        val getUserProfileUrl = domainUrl+"api/todo/auth/"

        token = mySharedPreference.getStringValue("token").toString()

        val jsonObjectRequest = object :JsonObjectRequest(Method.GET,getUserProfileUrl,null,
            Response.Listener {
                    response ->
                try {
                    val status = response.getBoolean("success")
                    if(status)
                    {
                        val userObj :JSONObject = response.getJSONObject("user")
                        navUserName.text = userObj.getString("username")
                        navUserEmail.text = userObj.getString("email")
                        val imageUrl = userObj.getString("avatar")
                        Picasso.get().load(imageUrl).into(navUserProfile)
                    }
                }
                catch (e:JSONException)
                {
                    //code
                }
            },
            Response.ErrorListener {
                    error ->
                error.printStackTrace()
                Toast.makeText(this,error.printStackTrace().toString(),Toast.LENGTH_LONG).show()
            })
        {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = token
                return headers
            }
        }
        //  These lines set a retry policy for the request.
        //  It retries the request with a socket timeout of 3000 milliseconds
        //  and the default maximum number of retries.
        val socketTime = 3000
        val policy = DefaultRetryPolicy(socketTime,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        //sending the retry policy
        jsonObjectRequest.retryPolicy = policy

        //creating a volley queue and adding our addTaskRequest
        Volley.newRequestQueue(this@MainActivity).add(jsonObjectRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when (item.itemId) {
            R.id.shareMenu-> {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.setType("text/plain")
                val shareBody = "Hey try this to do app, it uses permanent saving of your task."
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, "Share Via"))
                return true
            }
            R.id.refreshMenu -> {
                supportFragmentManager.beginTransaction().replace(R.id.content, HomeFragment())
                    .commit()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}