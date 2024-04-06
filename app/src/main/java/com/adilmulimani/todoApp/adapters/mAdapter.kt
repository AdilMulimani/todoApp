package com.adilmulimani.todoApp.adapters

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.datamodels.MessageModel
import com.adilmulimani.todoApp.utility.MySharedPreference
import com.squareup.picasso.Picasso

class mAdapter(private val mmsg: List<MessageModel>,val context: Context) :
    RecyclerView.Adapter<mAdapter.MessageViewHolder>() {

        val sharedPreference = MySharedPreference(context)

    companion object {
        private const val userviewtype = 0
        private const val botviewtype = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = if (viewType == userviewtype)
            inflater.inflate(R.layout.item_message_user_layout, parent, false)
        else
            inflater.inflate(R.layout.item_message_bot_layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: mAdapter.MessageViewHolder, position: Int) {
        val message: MessageModel = mmsg[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return mmsg.size
    }

    override fun getItemViewType(position: Int): Int {
        val msg = mmsg[position]
        return if (msg.isSentByUser) userviewtype else botviewtype
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var mtext: TextView? = itemView.findViewById(R.id.text_message_user)

        fun bind(message: MessageModel) {
            if(message.isSentByUser)
            {   val imageUrl = sharedPreference.getStringValue("imageUrl")
                val userProfileImage = itemView.findViewById<ImageView>(R.id.user_message_image)
                Picasso.get().load(imageUrl).into(userProfileImage)
                mtext=itemView.findViewById(R.id.text_message_user)
                mtext?.text = message.messageText
            }
            else
            {
                mtext=itemView.findViewById(R.id.text_message_bot)
                mtext?.text = message.messageText
            }
        }
    }
}