package com.example.smartgasstation.adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToActionCallback(
    private val onSwipeUp: (Int) -> Unit,
    private val onSwipeDown: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP or ItemTouchHelper.DOWN) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        when (direction) {
            ItemTouchHelper.UP -> onSwipeUp(position)
            ItemTouchHelper.DOWN -> onSwipeDown(position)
        }
    }
}