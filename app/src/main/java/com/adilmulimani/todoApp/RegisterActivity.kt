package com.adilmulimani.todoApp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import com.adilmulimani.todoApp.databinding.ActivityRegisterBinding
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.UtilService
import com.android.volley.DefaultRetryPolicy
import com.android.volley.ServerError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import es.dmoral.toasty.Toasty
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class RegisterActivity : AppCompatActivity() {
    //declaring view binding object
    private lateinit var binding:ActivityRegisterBinding
    //declaring required fields
    private lateinit var username:String
    private lateinit var email:String
    private lateinit var password:String
    //declaring and initialising the base Url
    private val domainUrl = com.adilmulimani.todoApp.utility.domainUrl
    //declaring MySharedPreference object
    private lateinit var mySharedPreference: MySharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialising view binding object and setting it up with root
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //if user already has an account then we are going direct him to login page
        binding.alreadyHaveAnAccountButton.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            finish()
        }

        //count for password
        var count = 0
        //accessing the password visibility
        binding.passwordVisibilityRegister.setOnClickListener {
            if (count % 2 == 0) {
                // If count is even, make password visible
                binding.passwordRegisterEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordVisibilityRegister.setImageResource(R.drawable.visibility_off)
            } else {
                // If count is odd, hide password
                binding.passwordRegisterEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordVisibilityRegister.setImageResource(R.drawable.visibility_on)
            }
            // Increment the count for the next click
            count++
        }

        //when you click on register Button to register
        binding.registerButton.setOnClickListener{

            //getting all the required fields
            username = binding.usernameEditText.text.toString()
            email = binding.emailRegisterEditText.text.toString()
            password = binding.passwordRegisterEditText.text.toString()

            //checking if all the required fields are present
            if(username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toasty.warning(this@RegisterActivity, "Fill All Required Fields", Toast.LENGTH_SHORT).show()
            }
            else {
                //if all the required fields are present then we can proceed to register user
                registerUser()
            }
            
            //hiding keyboard after register button is clicked for visibility
            UtilService().hideKeyboard(this@RegisterActivity)
        }
    }


    // onStart() is called just before the activity becomes visible to the user,
    // so it provides an early opportunity to check for the token.
    override fun onStart() {
        super.onStart()

        val todoPreference : SharedPreferences? = getSharedPreferences("user_todo", MODE_PRIVATE)

        //if to do preference contains token then we directly direct to main activity
        if (todoPreference != null && todoPreference.contains("token")) {
            startActivity(Intent(this@RegisterActivity,MainActivity2::class.java))
            finish()
        }
    }
    //the main function that registers user
    private fun registerUser() {
        //making the progress bar visible
        binding.progressBar.visibility = View.VISIBLE

        //initialising MySharedPreference object
        mySharedPreference = MySharedPreference(this@RegisterActivity)

        //creating the parameter that we need to pass as request
        // it is in form of Hashmap which will be converted to  JSON object
        val params = hashMapOf(
            "username" to username,
            "email" to email,
            "password" to password
        )

        //creating the url where we need to post
        val registerApiKey = domainUrl+"api/todo/auth/register"

        //This line creates a new instance of JsonObjectRequest, which is a Volley request that expects JSON response from the server.
        // It takes several parameters:
       // Method.POST: Indicates that it's a POST request.
       // registerApiKey: The URL to which the request is sent.
       // (params as Map<*, *>?)?.let { JSONObject(it) }:
        // Converts the params HashMap to a JSONObject. It first casts params to Map<*, *>? to handle nullability,
        // then converts it to a JSONObject using the JSONObject constructor.

        val jsonObjectRequest = object : JsonObjectRequest(
           Method.POST, registerApiKey, (params as Map<*, *>?)?.let { JSONObject(it) },
            { response ->
                try {
                    //if we get response  value of success is true then user is registered
                    if (response.getBoolean("success")) {
                        //storing the token in a variable
                        val token = response.getString("token")
                        //storing this token in shared preference
                        mySharedPreference.setStringValue("token",token)
                        Log.i("mytoken", "registerUser: $token")
                        //directing the registered user to main page
                        binding.progressBar.setProgress(1000)
                        Toasty.info(this@RegisterActivity,"Registration Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity2::class.java))
                        //once user registers in successfully he will not come back to login page
                        finish()
                    }
                    //if we get response value of success as false then user is already registered
                    else if(!response.getBoolean("success"))
                    {
                        //we display user not registered message
                        Toasty.warning(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                    //making the progress bar invisible again after getting response
                    binding.progressBar.visibility = View.GONE
                        //if we don't get JSON object we handle it with catch
                } catch (e: JSONException) {
                    e.printStackTrace()
                    binding.progressBar.visibility = View.GONE
                }
            },
            //if some error occurs then
            { error ->
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
                        Toasty.error(this@RegisterActivity, obj.getString("message"), Toast.LENGTH_SHORT).show()
                        //making the progress bar invisible
                        binding.progressBar.visibility = View.GONE
                        //catching error if some json exception occurs
                    } catch (e: JSONException ) {
                        e.printStackTrace()
                        binding.progressBar.visibility = View.GONE
                    }
                    //catching an error if we can't encode the message
                    catch (e:UnsupportedEncodingException) {
                        e.printStackTrace()
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        ) {
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

        //Finally, this line adds the JsonObjectRequest to the Volley request queue,
        // so it can be executed asynchronously.
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}