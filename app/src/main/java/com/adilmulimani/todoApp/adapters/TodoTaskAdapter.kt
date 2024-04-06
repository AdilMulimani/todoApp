package com.adilmulimani.todoApp.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.databinding.TodoItemLayoutBinding
import com.adilmulimani.todoApp.interfaces.RecyclerViewClickListenerInterface
import com.adilmulimani.todoApp.models.TodoTaskModel

import kotlin.random.Random

class TodoTaskAdapter(private val todoTaskArrayList: ArrayList<TodoTaskModel>
                      , val context: Context, private val recyclerViewClickListenerInterface: RecyclerViewClickListenerInterface)
    : RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>()
{
    private var count = 0

    inner class TodoTaskViewHolder(val binding: TodoItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        //creating view binding obj
        val binding =
            TodoItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TodoTaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return todoTaskArrayList.size
    }

    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {
       val modelPosition = todoTaskArrayList[position]
        holder.binding.title.text = modelPosition.title
        holder.binding.description.text = modelPosition.description
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
        val id: String = modelPosition.id as String

//      //  getting the color array from resource file
//        val colorArray :IntArray = context.resources.getIntArray(R.array.android_colours)
//       // random color to give background and adding it to index
//        val randomIndex = Random.nextInt(colorArray.size)
//      //  adding that index to color
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

        //when ever an item is clicked we send a position
        holder.itemView.setOnClickListener {
            //we send position through this property
            recyclerViewClickListenerInterface.onItemClick(position)
        }

        holder.itemView.setOnLongClickListener {
            recyclerViewClickListenerInterface.onLongItemClick(position)
            return@setOnLongClickListener true
        }

        holder.itemView.findViewById<ImageView>(R.id.editButton).setOnClickListener {
            recyclerViewClickListenerInterface.onEditButtonClick(position)

        }

        holder.itemView.findViewById<ImageView>(R.id.doneButton).setOnClickListener {

            recyclerViewClickListenerInterface.onDoneButtonClick(position)
        }

        holder.itemView.findViewById<ImageView>(R.id.deleteButton).setOnClickListener {
            recyclerViewClickListenerInterface.onDeleteButtonClick(position)
        }
    }
}