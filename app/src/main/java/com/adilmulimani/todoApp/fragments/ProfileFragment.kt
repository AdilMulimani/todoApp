package com.adilmulimani.todoApp.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.adilmulimani.todoApp.LoginActivity
import com.adilmulimani.todoApp.MainActivity2
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.databinding.FragmentProfileBinding
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.domainUrl
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    //user details
    private lateinit var userName: String
    private lateinit var userEmail:String
    private lateinit var userId :String
    private lateinit var nameEditText:EditText
    private lateinit var emailEditText:EditText
    private lateinit var dialog:Dialog
  //  private lateinit var userPassword:String
    //creating the object to store token of current user
    private lateinit var token :String
    //creating the shared preference file
    private lateinit var mySharedPreference : MySharedPreference
    var countTwo = -1
    var countOne = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)
        //get user information
        mySharedPreference = MySharedPreference(requireContext())
        getFinishedCount()
         getTodoCount()
        getUserProfile()

        //when clicked on edit profile
        binding.editProfileButton.setOnClickListener {
            editProfileDialog()
        }
        binding.logoutButton.setOnClickListener {
            logoutDialog()
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun logoutDialog() {

        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.alerting_dialog)

        val cancelButton =  dialog.findViewById<Button>(R.id.cancelVerificationButton)
        val sureButton =  dialog.findViewById<Button>(R.id.verificationButton)


        sureButton.setOnClickListener {
            mySharedPreference.clearUserData()
            //we then direct user back to login Activity and end this activity
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        //showing the dialog box
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dimensions of dialog
        val lp = WindowManager.LayoutParams()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.blurBehindRadius = 100
        lp.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        dialog.window?.attributes=lp
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun editProfileDialog() {
         dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.edit_profile_dialog)
       nameEditText =dialog.findViewById<EditText>(R.id.titleEditView)
       emailEditText =  dialog.findViewById<EditText>(R.id.userEmailEditView)
       val cancelButton =  dialog.findViewById<Button>(R.id.cancelButton)
       val updateButton =  dialog.findViewById<Button>(R.id.doneButton)

        nameEditText.setText(userName)
        emailEditText.setText(userEmail)

       updateButton.setOnClickListener {
           updateUserProfile()
       }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        //showing the dialog box
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dimensions of dialog
        val lp = WindowManager.LayoutParams()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.blurBehindRadius = 100
        lp.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        dialog.window?.attributes=lp
        dialog.show()
    }

    private fun updateUserProfile() {
        val userUpdatedProfileUrl = domainUrl +"api/todo/auth/update/"+userId
        token = mySharedPreference.getStringValue("token").toString()

        val params:HashMap<String,String> = HashMap()

        token = mySharedPreference.getStringValue("token").toString()

        params["username"] = nameEditText.text.toString()
        params["email"] = emailEditText.text.toString()
        val jsonObjectRequest = object :JsonObjectRequest(Method.PUT,userUpdatedProfileUrl,
            (params as Map<*, *>?)?.let { JSONObject(it) },
            {
                    response ->
                //getting status
                val status = response.getBoolean("success")
                //if status is successful
                if(status)
                {
                    //display update message
                    Toasty.info(requireContext(), "Updated User Details.", Toasty.LENGTH_SHORT).show()
                    binding.userName.text = response.getJSONObject("user").getString("username")
                    binding.userEmail.text = response.getJSONObject("user").getString("email")
                    //dismiss the dialog
                    dialog.dismiss()
                }

            },
            {
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
                        Toasty.info(requireContext(), obj.getString("message"), Toasty.LENGTH_SHORT).show()
                        //making the progress bar invisible
                        //catching error if some json exception occurs
                    } catch (e: JSONException) {
                        e.printStackTrace()

                    }
                    //catching an error if we can't encode the message
                    catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()

                    }
                }
            })
        {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = token
                return headers
            }
        }

        //creating a volley queue and adding our addTaskRequest
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }

    private fun getUserProfile() {

        val getUserProfileUrl = domainUrl +"api/todo/auth/"
        token = mySharedPreference.getStringValue("token").toString()

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,getUserProfileUrl,null,
            Response.Listener {
                    response ->
                try {
                    val status = response.getBoolean("success")
                    if(status)
                    {
                        val userObj : JSONObject = response.getJSONObject("user")
                        userId = userObj.getString("_id")
                        userName= userObj.getString("username")
                        binding.userName.text = userName
                        userEmail = userObj.getString("email")
                        binding.userEmail.text=userEmail
                        val imageUrl = userObj.getString("avatar")
                        Picasso.get().load(imageUrl).into(binding.profileImage)
                    }
                }
                catch (e: JSONException)
                {
                    //code
                }
            },
            Response.ErrorListener {
                    error ->
                error.printStackTrace()
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
        Volley.newRequestQueue(this@ProfileFragment.context).add(jsonObjectRequest)
    }

    private fun getFinishedCount() {

        //url we hit to get all tasks
        val getTasKApiKey = domainUrl+"api/todo/finishedCount"

        //the token for authorization
        token =mySharedPreference.getStringValue("token").toString()


        //volley request to get all the to do tasks
        val jsonObjectRequest = object :JsonObjectRequest(Method.GET,getTasKApiKey,null,
            {
                    response ->

                //getting the message from response and then displaying it
                countTwo = response.getInt("count")
                binding.finishedNumber.text = countTwo.toString()
            },Response.ErrorListener {
                    error ->
                val response = error.networkResponse
                //if error and network response is not null we simply return
                if (response == null || error == null) {
                    return@ErrorListener
                }

                //if error
                //body is a string we get from error
                val body: String

                try {
                    //getting error
                    body = String(error.networkResponse.data, charset("UTF-8"))

                    //conversion to JSON object
                    val errorObject = JSONObject(body)

                    //if the message is token not valid
                    if (errorObject.getString("msg") == "Token not valid")
                    {
                        //we clear the user data
                        mySharedPreference.clearUserData()

                        //send him back to the login activity
                        startActivity(Intent(activity, LoginActivity::class.java))


                        Toasty.error(requireContext(),"Session Expired",Toasty.LENGTH_SHORT).show()
                    }
                    Toasty.error(requireContext(), errorObject.getString("msg"), Toasty.LENGTH_SHORT).show()
                }
                catch (e: UnsupportedEncodingException) {
                    // exception
                }
                catch (e: JSONException) {
                    //exception
                }


            }
        )
        {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = token
                return headers
            }
        }

        // set retry policy
        val socketTime = 3000
        val policy = DefaultRetryPolicy(socketTime,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsonObjectRequest.retryPolicy = policy

        //creating a volley queue and adding our addTaskRequest
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)

    }

    private fun getTodoCount(){

        //url we hit to get all tasks
        val getTasKApiKey = domainUrl+"api/todo/todoCount"

        //the token for authorization
        token =mySharedPreference.getStringValue("token").toString()

        //volley request to get all the to do tasks
        val jsonObjectRequest = object :JsonObjectRequest(Method.GET,getTasKApiKey,null,
            {
                    response ->

                //getting the message from response and then displaying it
                countOne = response.getInt("count")
                binding.unfinishedNumber.text = countOne.toString()

            },Response.ErrorListener {
                    error ->
                val response = error.networkResponse
                //if error and network response is not null we simply return
                if (response == null || error == null) {
                    return@ErrorListener
                }

                //if error
                //body is a string we get from error
                val body: String

                try {
                    //getting error
                    body = String(error.networkResponse.data, charset("UTF-8"))

                    //conversion to JSON object
                    val errorObject = JSONObject(body)

                    //if the message is token not valid
                    if (errorObject.getString("msg") == "Token not valid")
                    {
                        //we clear the user data
                        mySharedPreference.clearUserData()

                        //send him back to the login activity
                        startActivity(Intent(activity, LoginActivity::class.java))

                        Toasty.error(requireContext(), "Session expired", Toasty.LENGTH_SHORT).show()
                    }
                    Toasty.error(requireContext(), errorObject.getString("msg"), Toasty.LENGTH_SHORT).show()
                }
                catch (e: UnsupportedEncodingException) {
                    // exception
                }
                catch (e: JSONException) {
                    //exception
                }

            }
        )
        {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = token
                return headers
            }
        }

        // set retry policy
        val socketTime = 3000
        val policy = DefaultRetryPolicy(socketTime,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        jsonObjectRequest.retryPolicy = policy

        //creating a volley queue and adding our addTaskRequest
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)

    }
}