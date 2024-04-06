package com.adilmulimani.todoApp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adilmulimani.todoApp.R
import com.adilmulimani.todoApp.datamodels.MessageModel

class MessageAdapter(private val messageList:ArrayList<MessageModel>)
    :RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    //creating two identifiers for user view type and bot view type
    private val userViewType = 0
    private val botViewType = 1
    private lateinit var messageView:View

    //Creating View Holder
    inner class MessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        //this is the textview that has to be used in AI fragment
        private var messageTextView:TextView

        //initializing it in order to avoid errors
        init {
            messageTextView = itemView.findViewById(R.id.text_message_user)
        }

        //this function is used to bind the message that is received either from user or ai
        fun bind(messageModel: MessageModel)
        {
            //if message is sent by user
            if(messageModel.isSentByUser)
            {
                messageTextView = itemView.findViewById(R.id.text_message_user)
                messageTextView.text = messageModel.messageText
            }
            //if message is sent by bot
            else
            {
                messageTextView = itemView.findViewById(R.id.text_message_bot)
                messageTextView.text = messageModel.messageText
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
       val inflater:LayoutInflater = LayoutInflater.from(parent.context)

        if(viewType == userViewType)
        {
            messageView = inflater.inflate(R.layout.item_message_user_layout,parent,false)
        }
        else if(viewType == botViewType)
        {
            messageView = inflater.inflate(R.layout.item_message_bot_layout,parent,false)
        }

        return MessageViewHolder(messageView)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
       val  messageListPosition = messageList[position]
        holder.bind(messageListPosition)
    }

    override fun getItemViewType(position: Int): Int {
        val message:MessageModel = messageList[position]
        return if(message.isSentByUser) {
            userViewType
        } else {
            botViewType
        }
    }

}