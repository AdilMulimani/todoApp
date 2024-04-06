package com.adilmulimani.todoApp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.databinding.FragmentCongratsBinding

class CongratsFragment : Fragment() {
    private lateinit var binding:FragmentCongratsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCongratsBinding.inflate(layoutInflater)
        binding.continueButton.setOnClickListener {

            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Replace the container with the new fragment
            transaction.replace(R.id.container,FocusFragment())

            // Add the transaction to the back stack
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()
        }
        return binding.root
    }

}