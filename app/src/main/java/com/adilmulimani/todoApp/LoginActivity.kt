package com.adilmulimani.todoApp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adilmulimani.todoApp.databinding.ActivityLoginBinding
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.UtilService
import com.adilmulimani.todoApp.utility.domainUrl
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import es.dmoral.toasty.Toasty
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class LoginActivity : AppCompatActivity() {

    //declaring view binding object
    private lateinit var binding : ActivityLoginBinding

    //declaring variables for required fields
    private lateinit var email:String
    private lateinit var password:String

    //declaring an object required for My Shared Preferences
    private lateinit var mySharedPreferences: MySharedPreference

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initialising view binding object and setting it up with root
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //initialising my shared preference class
        mySharedPreferences = MySharedPreference(this@LoginActivity)

        //if user doesn't have an account then he will be directed to Register Activity
        binding.createAccountButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
            finish()
        }

        //count for password
        var count = 0
        //accessing the password visibility
        binding.passwordVisibility.setOnClickListener {
            if (count % 2 == 0) {
                // If count is even, make password visible
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordVisibility.setImageResource(R.drawable.visibility_off)
            } else {
                // If count is odd, hide password
                binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordVisibility.setImageResource(R.drawable.visibility_on)
            }
            // Increment the count for the next click
            count++
        }

        //when user clicks on login button then
        binding.loginButton.setOnClickListener {

            //hiding keyboard after register button is clicked for visibility
            UtilService().hideKeyboard(this@LoginActivity)

             email = binding.emailEditText.text.toString()
            password = binding.passwordEditText.text.toString()

            //checking if all the required fields are present
            if(email.isEmpty() || password.isEmpty()) {
                Toasty.warning(this@LoginActivity, "Fill All Required Fields", Toast.LENGTH_SHORT).show()
            } else {
                //if all the required fields are present then we can proceed to login user
              loginUser()
            }
        }
    }

    // onStart() is called just before the activity becomes visible to the user,
    // so it provides an early opportunity to check for the token.
    override fun onStart() {
        super.onStart()

        val todoPreference : SharedPreferences? = getSharedPreferences("user_todo", MODE_PRIVATE)

        //if to do preference contains token then we directly direct to main activity
        if (todoPreference != null && todoPreference.contains("token")) {
                startActivity(Intent(this@LoginActivity,MainActivity2::class.java))
                finish()
        }
    }

    private fun loginUser(){

        //making the progress bar visible
        binding.progressBar.visibility = View.VISIBLE

        //declaring and initializing the login API key
        val loginApiKey = domainUrl+"api/todo/auth/login"
        //creating the parameters required to be sent as json object
        val params :HashMap<String,String> = HashMap()
        params["email"] = email
        params["password"] = password

        //creating our request
        val jsonObjectRequest = object :JsonObjectRequest(Method.POST,loginApiKey,(params as Map<*, *>?)?.let { JSONObject(it) },
            Response.Listener {
                    response ->
                             val status =  response.getBoolean("success")
                        if(status)

                        {   //making progress bar visible
                            binding.progressBar.visibility = View.VISIBLE
                            //storing the token in a variable
                            val token = response.getString("token")
                            Log.i("mytoken", "loginUser: $token")
                            //storing this token in shared preference
                            mySharedPreferences.setStringValue("token",token)
                            //directing the logged in  user to main page
                            binding.progressBar.setProgress(1000)
                            Toasty.info(this@LoginActivity,response.getString("msg"), Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity2::class.java))
                            //once user login in successfully he will not come back to login page
                            finish()
                        }

            },Response.ErrorListener {
                    error ->
                // This line checks if the error is a ServerError and if networkResponse is not null.
                // ServerError is a Volley error class that indicates an error response from the server.
                if (error is ServerError && error.networkResponse != null) {
                    try {
                        // This block of code tries to extract the error response body from the error.networkResponse.
                        // It reads the response body as a string using the appropriate charset.
                        val res = String(
                            error.networkResponse.data,
                            Charset.forName(HttpHeaderParser.parseCharset(error.networkResponse.headers, "utf-8")) ?: Charset.defaultCharset()
                        )
                        // If the error response body is successfully parsed as JSON,
                        // it extracts the error message and displays it in a toast.
                        // Then, it hides the progress bar.
                        val obj = JSONObject(res)
                        Toasty.error(this@LoginActivity, obj.getString("msg"), Toast.LENGTH_SHORT).show()
                        //making the progress bar invisible
                        binding.progressBar.visibility = View.GONE
                        //catching error if some json exception occurs
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        binding.progressBar.visibility = View.GONE
                    }
                    //catching an error if we can't encode the message
                    catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        binding.progressBar.visibility = View.GONE
                    }
                }
            })
        {
            //  This block of code overrides the getHeaders() method to provide headers for the request.
            //  In this case, it sets the "Content-Type" header to "application/json".
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
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
        //creating our request queue
       val queue =  Volley.newRequestQueue(this@LoginActivity)
        //adding our request to the queue
        queue.add(jsonObjectRequest)
    }
}