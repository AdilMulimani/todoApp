package com.adilmulimani.todoApp.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.adilmulimani.todoApp.adapters.mAdapter
import com.adilmulimani.todoApp.databinding.FragmentAIBinding

import com.adilmulimani.todoApp.datamodels.MessageModel
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.UtilService
import com.adilmulimani.todoApp.utility.domainUrl
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject


class AIFragment : Fragment() {

    private lateinit var binding: FragmentAIBinding
    //creating and initializing api url object
    private val  apiUrl = "https://api.pawan.krd/v1/completions"
    //creating and initializing access tokem
    private val accessToken = "pk-hQShElmuVUMglJcOqXdbwcFGpVNuNjREltCEeaIuJfHnblfb"
    //creating list having message model objects
    private  var messageList :ArrayList<MessageModel> = arrayListOf()
    //creating adapter object
    private lateinit var messageAdapter: mAdapter
    //creating the shared preference file
    private lateinit var mySharedPreference : MySharedPreference
    //creating the object to store token of current user
    private lateinit var token :String
    private lateinit var userName: String
    private lateinit var userEmail:String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAIBinding.inflate(layoutInflater)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        //get user information
        mySharedPreference = MySharedPreference(requireContext())
        getUserProfile()

        messageAdapter = mAdapter(messageList,requireContext())
       binding.recyclerView.layoutManager = LinearLayoutManager(this@AIFragment.context,LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.adapter = messageAdapter

        binding.button.setOnClickListener {
            processAI()
            UtilService().hideKeyboardForFragments(this@AIFragment)
            binding.aiImage.visibility = View.GONE
            binding.aiDescription.visibility = View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteChatButton.setOnClickListener {
            messageAdapter.notifyDataSetChanged()
            messageList.clear()
            binding.aiImage.visibility = View.VISIBLE
            binding.aiDescription.visibility = View.VISIBLE
        }

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
                        userName= userObj.getString("username")
                        mySharedPreference.setStringValue("username",userName)
                        binding.helloUserTextView.text = "Hello $userName"
                        userEmail= userObj.getString("email")
                        mySharedPreference.setStringValue("email",userEmail)
                        val imageUrl = userObj.getString("avatar")
                        mySharedPreference.setStringValue("imageUrl",imageUrl)
                        Picasso.get().load(imageUrl).into(binding.userProfileimage)
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
        Volley.newRequestQueue(this@AIFragment.context).add(jsonObjectRequest)
    }

    private fun processAI() {
        binding.aiProgressBar.visibility = View.VISIBLE
        val text = binding.editText.text.toString()
        messageList.add(MessageModel(text, true))
        messageAdapter.notifyItemInserted(messageList.size - 1)
       binding.recyclerView.scrollToPosition(messageList.size - 1)
        binding.editText.text.clear()

        val requestBody = JSONObject()
        try {
            requestBody.put("model", "pai-001")
            requestBody.put("prompt", text)
            requestBody.put("max_tokens", 256)
            requestBody.put("temperature", 0.7)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val request = object : JsonObjectRequest(Method.POST, apiUrl, requestBody,
            Response.Listener { response ->
                try {
                    val js = response.getJSONArray("choices")
                    val jsonObject = js.getJSONObject(0)
                    val myText:String = jsonObject.getString("text")
                    messageList.add(MessageModel((myText.replaceFirst("\n", "").replaceAfterLast("\n", "")), false))
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                   binding.recyclerView.scrollToPosition(messageList.size - 1)
                    binding.aiProgressBar.visibility = View.GONE
                } catch (ex: Exception) {
                    binding.aiProgressBar.visibility = View.GONE
                    ex.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                binding.aiProgressBar.visibility = View.GONE
                error.printStackTrace()
                messageList.add(MessageModel(error.toString().replaceFirst("\n", "").replaceFirst("\n", ""), false))
                messageAdapter.notifyItemInserted(messageList.size - 1)
                binding.recyclerView.scrollToPosition(messageList.size - 1)
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Authorization"] = "Bearer $accessToken"
                headers["Content-Type"] = "application/json"
                return headers
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                return super.parseNetworkResponse(response)
            }
        }

        val timeoutMs = 25000 // 25 seconds timeout
        val policy: RetryPolicy = DefaultRetryPolicy(timeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        request.retryPolicy = policy
        // Add the request to the RequestQueue
        Volley.newRequestQueue(this@AIFragment.context).add(request)
    }
}

