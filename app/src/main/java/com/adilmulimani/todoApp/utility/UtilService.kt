package com.adilmulimani.todoApp.utility

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

//A class designed for helping
 class UtilService
{
    //a function to hide keyboard
    fun hideKeyboard(activity: Activity) {
        try {
            val inputMethodManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Find the currently focused view, if any.
            val currentFocusedView = activity.currentFocus
            if (currentFocusedView != null) {
                // Hide the keyboard from the currently focused view.
                inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideKeyboardForFragments(fragment: Fragment) {
        try {
            val inputMethodManager = fragment.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Find the currently focused view, if any.
            val currentFocusedView = fragment.activity?.currentFocus
            if (currentFocusedView != null) {
                // Hide the keyboard from the currently focused view.
                inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}