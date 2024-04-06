package com.adilmulimani.todoApp

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.adilmulimani.todoApp.databinding.ActivityMain2Binding
import com.adilmulimani.todoApp.fragments.AIFragment
import com.adilmulimani.todoApp.fragments.FocusFragment
import com.adilmulimani.todoApp.fragments.IndexFragment
import com.adilmulimani.todoApp.fragments.ProfileFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class MainActivity2 : AppCompatActivity(){
    private lateinit var bottomNavigation :CurvedBottomNavigation
    private lateinit var binding:ActivityMain2Binding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        if(!hasNotificationPolicyAccess(this))
        {
            binding.container.visibility = View.GONE
            bottomNavigation.visibility = View.GONE
           showPermissionRationaleDialog()
        }
        else if(hasNotificationPolicyAccess(this))
        {
            binding.container.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            setUpBottomNavigation()
        }
        else
        {
            showPermissionDeniedDialog()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRestart() {
        super.onRestart()
        bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        if(!hasNotificationPolicyAccess(this)) {

            showPermissionDeniedDialog()
            binding.container.visibility = View.GONE
            bottomNavigation.visibility = View.GONE
        }
        else if(hasNotificationPolicyAccess(this))
        {
            setUpBottomNavigation()
            binding.container.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
        }
    }

    private fun replaceFragment(fragment: Fragment)
    {
        supportFragmentManager.beginTransaction().
        replace(R.id.container,fragment
        ).addToBackStack(null).commit()

    }

    private fun setUpBottomNavigation() {
        bottomNavigation.add(CurvedBottomNavigation.Model(0,"Index",R.drawable.index))
        bottomNavigation.add(CurvedBottomNavigation.Model(1,"Focus",R.drawable.focus))
        bottomNavigation.add(CurvedBottomNavigation.Model(2,"AI",R.drawable.ai_image_icon))
        bottomNavigation.add(CurvedBottomNavigation.Model(3,"Profile",R.drawable.profile))

        supportFragmentManager.beginTransaction().replace(R.id.container,IndexFragment()).commitAllowingStateLoss()
        bottomNavigation.show(0)

        bottomNavigation.setOnShowListener {

            when(it.id)
            {
                0->{
                    replaceFragment(IndexFragment())
                    binding.bottomNavigation.fabColor = resources.getColor(R.color.maroon)
                }
                1->{
                    replaceFragment(FocusFragment())
                    binding.bottomNavigation.fabColor = resources.getColor(R.color.orange)
                }
                2->{
                    replaceFragment(AIFragment())
                    binding.bottomNavigation.fabColor = resources.getColor(R.color.purple)
                }
                3->{
                    replaceFragment(ProfileFragment())
                    binding.bottomNavigation.fabColor = resources.getColor(R.color.maroon)
                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.S)
    private fun showPermissionRationaleDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.alert_dialog)
        //showing the dialog box
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dimensions of dialog
        val lp = WindowManager.LayoutParams()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.blurBehindRadius = 100
        lp.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        dialog.window?.attributes=lp

        dialog.setOnShowListener {
            val message = dialog.findViewById<TextView>(R.id.textForPermission)
            val negativeButton = dialog.findViewById<Button>(R.id.cancelButton)
            val positiveButton = dialog.findViewById<Button>(R.id.doneButton)


            positiveButton.setOnClickListener {
                val intent = Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
            negativeButton.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showPermissionDeniedDialog() {

        val dialog = Dialog(this)
     dialog.setContentView(R.layout.alert_dialog)

        //showing the dialog box
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dimensions of dialog
        val lp = WindowManager.LayoutParams()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.blurBehindRadius = 100
        lp.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        dialog.window?.attributes=lp

        dialog.setOnShowListener {
            val message = dialog.findViewById<TextView>(R.id.textForPermission)
            val negativeButton = dialog.findViewById<Button>(R.id.cancelButton)
            val positiveButton = dialog.findViewById<Button>(R.id.doneButton)

            positiveButton.text = "Settings"
            negativeButton.text="Cancel"

            message.text = "You denied the permission to acess notifications. Please grant the permission in app settings to proceed."
            positiveButton.setOnClickListener {
                // Open app settings so the user can grant the permission
                val intent = Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
            negativeButton.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun hasNotificationPolicyAccess(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

}



