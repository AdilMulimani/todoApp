package com.adilmulimani.todoApp.fragments

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.databinding.FragmentFocusBinding
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.adilmulimani.todoApp.utility.domainUrl
import com.androchef.happytimer.countdowntimer.HappyTimer
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.shawnlin.numberpicker.NumberPicker
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import org.json.JSONException
import org.json.JSONObject
import kotlin.properties.Delegates


class FocusFragment : Fragment() {

    private lateinit var binding:FragmentFocusBinding
    //user details
    private lateinit var userName: String
    private lateinit var userEmail:String
    //  private lateinit var userPassword:String
    //creating the object to store token of current user
    private lateinit var token :String
    //creating the shared preference file
    private lateinit var mySharedPreference : MySharedPreference
    //creating dialog
    private lateinit var dialog:Dialog
    //time
    private var hours by Delegates.notNull<Int>()
    private var minutes by Delegates.notNull<Int>()
    private var seconds by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
      binding = FragmentFocusBinding.inflate(layoutInflater)
        //get user information
        mySharedPreference = MySharedPreference(requireContext())
        getUserProfile()
        hours = 0
        minutes = 0
        seconds = 0
        binding.startButton.setOnClickListener { timePickerDialog()
        visibilityViews()
        }
        binding.quitButton.setOnClickListener {
            //Stop Timer
            binding.ProfileDescriptionText.setText(R.string.dndoff)
            binding.circularCountDownView.stopTimer()
            //startAllNotifications(requireContext())
            getVisibleBack()
            hours = 0
            minutes = 0
            seconds = 0
        }

        return binding.root
    }

    private fun visibilityViews() {
        binding.focusImage.visibility = View.GONE
        binding.startButton.visibility = View.GONE
        binding.DescriptionTextForTasks.visibility = View.GONE
        binding.circularCountDownView.visibility = View.VISIBLE
        binding.pauseButton.visibility = View.VISIBLE
        binding.quitButton.visibility = View.VISIBLE
        binding.quitText.visibility = View.VISIBLE
        binding.pauseText.visibility = View.VISIBLE
    }

    private fun getVisibleBack()
    {
        binding.focusImage.visibility = View.VISIBLE
        binding.startButton.visibility = View.VISIBLE
        binding.DescriptionTextForTasks.visibility = View.VISIBLE
        binding.circularCountDownView.visibility = View.GONE
        binding.pauseButton.visibility = View.GONE
        binding.quitButton.visibility = View.GONE
        binding.quitText.visibility = View.GONE
        binding.pauseText.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        binding.helloUserTextView.text = "Hello $userName"
                        userEmail = userObj.getString("email")
                        val imageUrl = userObj.getString("avatar")
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
        Volley.newRequestQueue(this@FocusFragment.context).add(jsonObjectRequest)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun timePickerDialog()
    {
        //creating a dialog
        dialog = Dialog(requireContext())
        //adding custom view to it
        dialog.setContentView(R.layout.time_picker_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dimensions of dialog
        val lp = WindowManager.LayoutParams()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.blurBehindRadius = 100
        lp.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        dialog.window?.attributes=lp

       val numberPickerHours =  dialog.findViewById<NumberPicker>(R.id.number_picker_hours)
       val numberPickerMinutes =  dialog.findViewById<NumberPicker>(R.id.number_picker_minutes)
       val numberPickerSeconds =  dialog.findViewById<NumberPicker>(R.id.number_picker_seconds)
       val selectButton =  dialog.findViewById<Button>(R.id.selButton)
       val cancelButton =  dialog.findViewById<Button>(R.id.canButton)

        selectButton.setOnClickListener {
            //Initialize Your Timer with seconds
                dialog.dismiss()
            val timeInSeconds = hours*60*60 + minutes*60 + seconds

            if(timeInSeconds == 0)
            {
                Toasty.error(requireContext(), "Invalid Format.", Toast.LENGTH_SHORT).show()
                getVisibleBack()
            }
            else {
                binding.ProfileDescriptionText.setText(R.string.dndon)
                //enables do not disturb mode
                enableDoNotDisturb()
                Toasty.info(requireContext(), "Zen Mode On", Toast.LENGTH_SHORT).show()


                visibilityViews()

                binding.circularCountDownView.initTimer(timeInSeconds)

                //Start Timer
                binding.circularCountDownView.startTimer()

                //set OnTickListener for getting updates on time. [Optional]
                binding.circularCountDownView.setOnTickListener(object : HappyTimer.OnTickListener {

                    //OnTick
                    override fun onTick(completedSeconds: Int, remainingSeconds: Int) {
                        if (remainingSeconds <= 10) {
                            binding.circularCountDownView.strokeColorForeground =
                                ContextCompat.getColor(requireContext(), R.color.red)
                            binding.circularCountDownView.timerTextColor =
                                ContextCompat.getColor(requireContext(), R.color.red)
                        } else if(remainingSeconds >= (.75*timeInSeconds))
                        {
                            binding.circularCountDownView.strokeColorForeground =
                                ContextCompat.getColor(requireContext(), R.color.green)
                            binding.circularCountDownView.timerTextColor =
                                ContextCompat.getColor(requireContext(), R.color.white)
                        }
                        else
                        {
                            binding.circularCountDownView.strokeColorForeground =
                                ContextCompat.getColor(requireContext(), R.color.orange)
                            binding.circularCountDownView.timerTextColor =
                                ContextCompat.getColor(requireContext(), R.color.white)
                        }
                    }

                    //OnTimeUp
                    override fun onTimeUp() {
                        //startAllNotifications(requireContext())
                        disableDoNotDisturb()
                        completeTimeUp(CongratsFragment())
                    }
                })
            }

            //set OnStateChangeListener [RUNNING, FINISHED, PAUSED, RESUMED, UNKNOWN, RESET, STOPPED] [Optional]
            binding.circularCountDownView.setStateChangeListener(object : HappyTimer.OnStateChangeListener {
                override fun onStateChange(
                    state: HappyTimer.State,
                    completedSeconds: Int,
                    remainingSeconds: Int
                ) {
                    // write your code here for State Changes

                }
            })

            //Call these functions to perform actions
            var count = 0
            //Pause Timer
            binding.pauseButton.setOnClickListener {
                if(count%2==0)
                {
                    binding.circularCountDownView.pauseTimer()
                    binding.pauseButton.setImageResource(R.drawable.playbutton)
                    binding.pauseText.text = "Resume"
                    count++
                }
                //Resume Timer
                else
                {
                    binding.circularCountDownView.resumeTimer()
                    binding.pauseButton.setImageResource(R.drawable.pause)
                    binding.pauseText.text = "Pause"
                    count++
                }
            }
            //get Total Seconds
            // val totalSeconds =binding.circularCountDownView.getTotalSeconds()

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }


        // OnClickListener
         numberPickerHours.setOnClickListener {
        }
        // OnScrollListener
      numberPickerHours.setOnScrollListener { picker, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
               numberPickerHours.selectedTextColor = resources.getColor(R.color.white)
            }
        }
       numberPickerMinutes.setOnScrollListener { picker, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
               numberPickerMinutes.selectedTextColor = resources.getColor(R.color.white)
            }
        }
        numberPickerSeconds.setOnScrollListener { picker, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                numberPickerSeconds.selectedTextColor = resources.getColor(R.color.white)
            }

        }

        // OnValueChangeListener of hours
      numberPickerHours.setOnValueChangedListener { picker, oldVal, newVal ->
            hours = picker.value
        }

        // OnValueChangeListener of minutes
       numberPickerMinutes.setOnValueChangedListener { picker, oldVal, newVal ->
            minutes = picker.value
        }

        // OnValueChangeListener of seconds
      numberPickerSeconds.setOnValueChangedListener { picker, oldVal, newVal ->
            seconds = picker.value
        }


        //showing it
        dialog.show()
    }

    private fun enableDoNotDisturb() {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
    }

    private fun disableDoNotDisturb() {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }

    fun completeTimeUp(fragment: Fragment)
    {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        // Replace the container with the new fragment
        transaction.replace(R.id.container,fragment)

        // Add the transaction to the back stack
        transaction.addToBackStack(null)

        // Commit the transaction
        transaction.commit()
    }

}
