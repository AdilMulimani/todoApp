package com.adilmulimani.todoApp.models

import java.util.Date

data class TodoTaskModel(val id:String?=null,
                         val title:String?=null,
                         val description:String?=null,
                         val createdAt:Date?=null,
                        val category:String?=null)
