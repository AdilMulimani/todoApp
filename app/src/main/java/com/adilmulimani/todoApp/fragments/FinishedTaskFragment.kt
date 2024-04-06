package com.adilmulimani.todoApp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.adilmulimani.todoApp.LoginActivity
import com.adilmulimani.todoApp.adapters.FinishedTaskAdapter
import com.adilmulimani.todoApp.databinding.FragmentFinishedTaskBinding
import com.adilmulimani.todoApp.interfaces.RecyclerViewClickListenerInterface
import com.adilmulimani.todoApp.models.TodoTaskModel
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.domainUrl
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import es.dmoral.toasty.Toasty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Locale

class FinishedTaskFragment : Fragment(),RecyclerViewClickListenerInterface {

    //creating binding object
    private lateinit var binding :FragmentFinishedTaskBinding
    //creating the shared preference file
    private lateinit var mySharedPreference: MySharedPreference
    //creating the object to store token of current user
    private lateinit var token :String
    //creating and initializing  an arraylist for to  do task model
    private val finishedArrayList :ArrayList<TodoTaskModel> = arrayListOf()
    //creating adapter object
    private lateinit var finishedListAdapter: FinishedTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFinishedTaskBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initializing shared preferences
        mySharedPreference = MySharedPreference(requireContext())

        //setting up recycler view
        binding.finishedTaskRecyclerView.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)

        getFinishedTasks()
    }


    //function which gets all the tasks yet to do
    private fun getFinishedTasks() {

        //url we hit to get all tasks
        val getTasKApiKey = domainUrl +"api/todo/finished"

        //the token for authorization
        token = mySharedPreference.getStringValue("token").toString()

        //making the progress bar visible
        binding.progressBar.visibility = View.VISIBLE

        //volley request to get all the to do tasks
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,getTasKApiKey,null,
            {
                    response ->
                val status = response.getBoolean("success")
                if(status)
                {
                    //getting the message from response and then displaying it
                    val message = response.getString("message")

                    //paring the JSON array into a kotlin array list
                    parseFinishedResponse(response.getJSONArray("todo"),response.getInt("count"))
                    //hiding the progress bar after data has been fetched
                    binding.progressBar.visibility = View.GONE

                }
            }, Response.ErrorListener {
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

                        Toasty.error(requireContext(), "Session expired", Toast.LENGTH_SHORT).show()
                    }
                    Toasty.error(requireContext(), errorObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
                catch (e: UnsupportedEncodingException) {
                    // exception
                }
                catch (e: JSONException) {
                    //exception
                }

                binding.progressBar.visibility = View.GONE
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

    private fun parseFinishedResponse(response: JSONArray, count:Int){

        //defining the format in which we need date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        //till the json array ends we traverse it
        for (i in 0 until  count) {
            //storing a json object element in this from json array
            val jsonObject = response.getJSONObject(i)
            //getting the id and adding it to into this
            val id = jsonObject.getString("_id")
            //getting the title and adding it into this
            val title = jsonObject.getString("title")
            //getting the description and adding it into this
            val description = jsonObject.getString("description")
            //getting the date in form of string  and adding it into this
            val dateString = jsonObject.getString("createdAt")
            //changing the date string back to date
            val date = dateFormat.parse(dateString)

            // Create an Item object and add it to the ArrayList
            val item = TodoTaskModel(id,title,description,date)
            finishedArrayList.add(item)
        }

        // Once the ArrayList is populated, you can use it as needed.
        // For example, you can update your UI, pass it to an adapter, etc.
        // In this example, we simply log the items.
        for (item in finishedArrayList) {
            Log.i("hello", "Item: ${item.id},${item.title}, ${item.description},${item.createdAt}")
        }
        //we set the adapter of to do task after getting all the data
        //context is required to get necessary resources like colors,get intents,create dialogs and toasts
        //we pass home fragment object as it implements recycler view click listener interface

        finishedListAdapter  =  FinishedTaskAdapter(finishedArrayList,requireContext(),this@FinishedTaskFragment)

        binding.finishedTaskRecyclerView.adapter = finishedListAdapter

    }


    override fun onDeleteButtonClick(position: Int) {
        //delete task
        deleteFinishedTask(finishedArrayList[position].id.toString())
    }

    private fun deleteFinishedTask(id:String) {
        val deleteTasKApiKey = domainUrl+"api/todo/"+id

        token = mySharedPreference.getStringValue("token").toString()


        val jsonObjectRequest = object :JsonObjectRequest(Method.DELETE,deleteTasKApiKey,
            null,
            {
                    response ->
                //getting status
                val status = response.getBoolean("success")
                //if status is successful
                if(status)
                {
                    //display update message
                    val message = response.getString("message")
                    //get all the task again along with updated
                    finishedArrayList.clear()
                    getFinishedTasks()
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
                        Toasty.error(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show()
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

    override fun onDoneButtonClick(position: Int) {
    }

    override fun onItemClick(position: Int) {

    }

    override fun onLongItemClick(position: Int) {

    }

    override fun onEditButtonClick(position: Int) {

    }

}