package com.adilmulimani.todoApp.interfaces

//we want some functions to be used for multiple classes so we create an interface
//this interface is implemented by multiple classes which increases re-usability

//we will use this interface in adapter
//we get position from adapter for these abstract functions
//we can then access these positions  in home fragment
interface RecyclerViewClickListenerInterface
{
    //when an item of recycler view is clicked a single time
    //we need position parameter to determine the exact location of the view
     fun onItemClick(position:Int)

     //when yuo click on the view for a long time
     fun onLongItemClick(position:Int)

     //when clicked on edit button
     fun onEditButtonClick(position: Int)

    //when clicked on delete button
     fun onDeleteButtonClick(position: Int)

    //when clicked on done button
     fun onDoneButtonClick(position: Int)
}