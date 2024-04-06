package com.adilmulimani.todoApp.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.adilmulimani.todoApp.LoginActivity
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.adapters.TodoTaskAdapter
import com.adilmulimani.todoApp.databinding.FragmentHomeBinding
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


class HomeFragment : Fragment(),RecyclerViewClickListenerInterface
{
    //view binding declaration
    private lateinit var binding : FragmentHomeBinding
    //creating the dialog object for add
    private lateinit var dialog:Dialog
    //creating the dialog object for update
    private lateinit var updateDialog:Dialog
    //creating the dialog object for update
    private lateinit var verificationDialog:Dialog
    //creating the shared preference file
    private lateinit var mySharedPreference: MySharedPreference
    //creating the object to store token of current user
    private lateinit var token :String
    //creating and initializing  an arraylist for to  do task model
    private var myArrayList :ArrayList<TodoTaskModel> = arrayListOf()
    //creating adapter object
    private lateinit var todoListAdapter: TodoTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        //set up view binding
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing shared preferences
        mySharedPreference = MySharedPreference(requireContext())
        //when clicked on floating action button,dialog should pop up
        binding.add.setOnClickListener{
            showDialog()
        }
        //setting up recycler view
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
         getTasks()

    }

    //function to show dialog
    private fun showDialog() {
        //creating dialog box
        dialog = Dialog(requireContext())
        //setting up its layout
        dialog.setContentView(R.layout.custom_dialog)
        //showing the dialog box
        dialog.show()
        //creating the components of dialog box and initializing it
        val title = dialog.findViewById<TextView>(R.id.titleTextView)
        val description = dialog.findViewById<TextView>(R.id.descriptionTextView)
        val doneButton = dialog.findViewById<Button>(R.id.doneButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

        //when you click on done button
        doneButton.setOnClickListener {
            //if title is not empty then we proceed to add it
           if(checkEmptyFields(title))
           {
               //adding the task
                addTask(title,description)
           }
            else
           {    //else we display toast
               checkEmptyFields(title)
           }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun checkEmptyFields(title:TextView):Boolean
    {
        if(title.text.toString()=="")
        {
            Toasty.warning(requireContext(), "Title required", Toast.LENGTH_SHORT).show()
            return false
        }
        //if title is not empty then we proceed to add it
        return true
    }

    // function to add to do task
    private fun addTask(title: TextView?, description: TextView?)
    {
        val addTasKApiKey = domainUrl+"api/todo"

        val params:HashMap<String,String> = HashMap()

         token = mySharedPreference.getStringValue("token").toString()

        if (title != null) {
            params["title"] = title.text.toString()
        }
        if (description != null) {
            params["description"] = description.text.toString()
        }

        val jsonObjectRequest = object :JsonObjectRequest(Method.POST,addTasKApiKey,
            (params as Map<*, *>?)?.let { JSONObject(it) },
            {
            response ->
                val status = response.getBoolean("success")

                if(status)
                {
                    val message = response.getString("message")
                    //clearing all the previous tasks before adding new one
                    myArrayList.clear()
                    //get all the task including the previous ones and new ones
                    getTasks()
                  //  Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    //function which gets all the tasks yet to do
    private fun getTasks() {

        //url we hit to get all tasks
        val getTasKApiKey = domainUrl+"api/todo"

        //the token for authorization
        token = mySharedPreference.getStringValue("token").toString()

        //making the progress bar visible
        binding.progressBar.visibility = View.VISIBLE

        //volley request to get all the to do tasks
        val jsonObjectRequest = object :JsonObjectRequest(Method.GET,getTasKApiKey,null,
            {

                response ->
                val status = response.getBoolean("success")
                if(status)
                {
                    //getting the message from response and then displaying it
                    val message = response.getString("message")
                   // Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    //paring the JSON array into a kotlin array list
                 parseResponse(response.getJSONArray("todo"),response.getInt("count"))
                    //hiding the progress bar after data has been fetched
                    binding.progressBar.visibility = View.GONE

                }
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

                        Toasty.error(requireContext(), "Session expired", Toast.LENGTH_SHORT).show()
                    }
                  //  Toast.makeText(activity, errorObject.getString("msg"), Toast.LENGTH_SHORT).show()
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

    private fun parseResponse(response: JSONArray,count:Int){

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
            myArrayList.add(item)
        }

        // Once the ArrayList is populated, you can use it as needed.
        // For example, you can update your UI, pass it to an adapter, etc.
        // In this example, we simply log the items.
        for (item in myArrayList) {
            Log.i("hello", "Item: ${item.id},${item.title}, ${item.description},${item.createdAt}")
        }
        //we set the adapter of to do task after getting all the data
        //context is required to get necessary resources like colors,get intents,create dialogs and toasts
        //we pass home fragment object as it implements recycler view click listener interface

       todoListAdapter =  TodoTaskAdapter(myArrayList,requireContext(),this@HomeFragment)

        binding.todoRecyclerView.adapter = todoListAdapter

    }

    override fun onItemClick(position: Int) {
      //  Toast.makeText(requireContext(), position.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onLongItemClick(position: Int) {
       // Toast.makeText(requireContext(), position.toString(), Toast.LENGTH_SHORT).show()
       // Toast.makeText(requireContext(), "Task Title " + myArrayList[position].title, Toast.LENGTH_SHORT).show()

        //creating a dialog
        updateDialog = Dialog(requireContext())
        //adding custom view to it
        updateDialog.setContentView(R.layout.custom_dialog)
        //showing it
        updateDialog.show()

        //accessing the views of dialog box
        val titleEditText = updateDialog.findViewById<TextView>(R.id.titleTextView)
        val descriptionEditText = updateDialog.findViewById<TextView>(R.id.descriptionTextView)
        val cancelButton = updateDialog.findViewById<Button>(R.id.cancelButton)
        val updateButton = updateDialog.findViewById<Button>(R.id.doneButton)


        //changing the name of Dialog box dummy name of  button to update
        updateButton.text = getString(R.string.update_button_name)

        //when dialog box opens up it should have the previous title and description
        titleEditText.text = myArrayList[position].title
        descriptionEditText.text = myArrayList[position].description

        //when clicked on cancel button the dialog box should dismiss
        cancelButton.setOnClickListener {
            updateDialog.dismiss()
        }

        //when clicked on update button updation of task should occur
        updateButton.setOnClickListener {
            //updation of task
            updateTask(myArrayList[position].id.toString(),
                titleEditText.text.toString(), descriptionEditText.text.toString())
        }


    }

    override fun onEditButtonClick(position: Int) {

     //   Toast.makeText(requireContext(), "Task Title "+myArrayList[position].title, Toast.LENGTH_SHORT).show()

        //creating a dialog
            updateDialog = Dialog(requireContext())
        //adding custom view to it
            updateDialog.setContentView(R.layout.custom_dialog)
        //showing it
            updateDialog.show()

            //accessing the views of dialog box
           val titleEditText =  updateDialog.findViewById<TextView>(R.id.titleTextView)
           val descriptionEditText = updateDialog.findViewById<TextView>(R.id.descriptionTextView)
            val cancelButton = updateDialog.findViewById<Button>(R.id.cancelButton)
            val updateButton = updateDialog.findViewById<Button>(R.id.doneButton)


        //changing the name of Dialog box dummy name of  button to update
        updateButton.text = getString(R.string.update_button_name)

        //when dialog box opens up it should have the previous title and description
             titleEditText.text = myArrayList[position].title
            descriptionEditText.text = myArrayList[position].description

        //when clicked on cancel button the dialog box should dismiss
        cancelButton.setOnClickListener {
            updateDialog.dismiss()
        }

        //when clicked on update button updation of task should occur
        updateButton.setOnClickListener {
            //updation of task
                updateTask(myArrayList[position].id.toString(),
                    titleEditText.text.toString(), descriptionEditText.text.toString())
        }
    }

    private fun updateTask(id: String, title: String, description: String)
    {
        val updateTasKApiKey = domainUrl+"api/todo/"+id

        val params:HashMap<String,String> = HashMap()

        token = mySharedPreference.getStringValue("token").toString()

        params["title"] = title
        params["description"] = description

        val jsonObjectRequest = object :JsonObjectRequest(Method.PUT,updateTasKApiKey,
            (params as Map<*, *>?)?.let { JSONObject(it) },
            {
                    response ->
                //getting status
                val status = response.getBoolean("success")
                //if status is successful
                if(status)
                {
                    //display update message
                    val message = response.getString("message")
                 //   Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    //get all the task again along with updated
                    myArrayList.clear()
                    getTasks()
                    //dismiss the dialog
                    updateDialog.dismiss()
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
                   //     Toast.makeText(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show()
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

    override fun onDeleteButtonClick(position: Int) {
      //  Toast.makeText(requireContext(), "Clicked on delete", Toast.LENGTH_SHORT).show()
    //delete task
        deleteTask(myArrayList[position].id.toString())
    }

    private fun deleteTask(id:String) {
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
                 //   Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    //get all the task again along with updated
                    myArrayList.clear()
                    getTasks()
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
                      //  Toast.makeText(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show()
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
       // Toast.makeText(requireContext(), "Clicked on done", Toast.LENGTH_SHORT).show()
        showFinishedTaskDialog(position)
    }

    private fun showFinishedTaskDialog(position: Int) {
        verificationDialog = Dialog(requireContext())

        verificationDialog.setContentView(R.layout.alerting_dialog)

        verificationDialog.show()

        val verificationText = verificationDialog.findViewById<TextView>(R.id.verificationText)
       val verifyButton =  verificationDialog.findViewById<Button>(R.id.verificationButton)
      val cancelButton =   verificationDialog.findViewById<Button>(R.id.cancelVerificationButton)

        verificationText.text = getString(R.string.are_you_sure_you_have_finished_this_task)

        cancelButton.setOnClickListener {
            verificationDialog.dismiss()
        }

        verifyButton.setOnClickListener {
            updateToFinishedTask(myArrayList[position].id.toString(),position)
        }

    }

    private fun updateToFinishedTask(id: String, position: Int) {
        val updateTasKApiKey = domainUrl+"api/todo/"+id

        val params:HashMap<String,String> = HashMap()

        token = mySharedPreference.getStringValue("token").toString()

        params["finished"] = "true"


        val jsonObjectRequest = object :JsonObjectRequest(Method.PUT,updateTasKApiKey,
            (params as Map<*, *>?)?.let { JSONObject(it) },
            {
                    response ->
                //getting status
                val status = response.getBoolean("success")
                //if status is successful
                if(status)
                {
                    //display update message

                    val message = response.getString("message")
                 //   Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    //get all the task again along with updated
                    myArrayList.clear()
                    getTasks()
                    //dismiss the dialog
                    verificationDialog.dismiss()
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
                      //  Toast.makeText(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show()
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
}