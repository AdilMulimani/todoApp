package com.adilmulimani.todoApp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.databinding.FinishedTaskItemBinding
import com.adilmulimani.todoApp.interfaces.RecyclerViewClickListenerInterface
import com.adilmulimani.todoApp.models.TodoTaskModel
import kotlin.random.Random

class FinishedTaskAdapter(private val finishedTaskArrayList:ArrayList<TodoTaskModel>,
                          private val context: Context,
                          private val recyclerViewClickListenerInterface: RecyclerViewClickListenerInterface)
    : RecyclerView.Adapter<FinishedTaskAdapter.FinishedTaskViewHolder>(){

       private var count = 0

    inner class FinishedTaskViewHolder
        (val binding:FinishedTaskItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinishedTaskViewHolder {
        val binding
        = FinishedTaskItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FinishedTaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return finishedTaskArrayList.size
    }

    override fun onBindViewHolder(holder: FinishedTaskViewHolder, position: Int) {

        val modelPosition = finishedTaskArrayList[position]

        holder.binding.textView.text = modelPosition.title.toString()
        holder.binding.description.text = modelPosition.description.toString()
        holder.binding.categoryText.text = modelPosition.category
        //coloring categories
        if(modelPosition.category == "Work")
        {
            holder.binding.categoryText.background = context.resources.getDrawable(R.color.red)
        }
        if(modelPosition.category == "Study")
        {
            holder.binding.categoryText.background = context.resources.getDrawable(R.color.blue)
        }
        if(modelPosition.category == "Sports")
        {
            holder.binding.categoryText.background = context.resources.getDrawable(R.color.green)
        }
        if(modelPosition.category == "Others")
        {
            holder.binding.categoryText.background = context.resources.getDrawable(R.color.yellow)
        }


        holder.itemView.findViewById<ImageView>(R.id.deleteButton).setOnClickListener {
            recyclerViewClickListenerInterface.onDeleteButtonClick(position)
        }

        holder.itemView.setOnLongClickListener {
            recyclerViewClickListenerInterface.onLongItemClick(position)
        //    Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

//        //  getting the color array from resource file
//        val colorArray :IntArray = context.resources.getIntArray(R.array.android_colours)
//        // random color to give background and adding it to index
//        val randomIndex = Random.nextInt(colorArray.size)
//        //  adding that index to color
//        val color :Int = colorArray[randomIndex]
//
//        holder.binding.cardViewConstraintLayout.setBackgroundColor(color)
//        holder.binding.accordianBody.setBackgroundColor(color)

        //when clicked on the arrow button
        holder.binding.arrow.setOnClickListener {

            //we will make the extra data visible
            if(count%2==0) {
                count++
                holder.binding.accordianBody.visibility = View.VISIBLE
                holder.binding.arrow.setImageResource(R.drawable.arrowup)
            }
            //we will make the extra data invisible
            else {   count++
                holder.binding.accordianBody.visibility = View.GONE
                holder.binding.arrow.setImageResource(R.drawable.arrow_down)
            }
        }

    }
}