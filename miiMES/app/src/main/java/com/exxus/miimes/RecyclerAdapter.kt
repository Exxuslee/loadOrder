package com.exxus.miimes


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_view_item.view.*


class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    val lstPoint = itemView.lstPoint

    fun bind(
        user: User,
        clickListener: OnItemClickListener,
        longClickListener: OnLongClickListener,
        position: Int)
    {
        lstPoint.text = user.username.toString()
        lstPoint.setBackgroundColor(user.color)

        itemView.lstPoint.setOnClickListener {
            clickListener.onItemClicked(user, position)
        }

        itemView.lstPoint.setOnLongClickListener {
            longClickListener.onItemLongClicked(user, position)
            return@setOnLongClickListener true
        }

    }

}


class RecyclerAdapter(
    var users: MutableList<User>,
    val itemClickListener: OnItemClickListener,
    val itemLongClickListener: OnLongClickListener

):RecyclerView.Adapter<MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item,
            parent,
            false
        )
        return MyHolder(view)


    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(myHolder: MyHolder, position: Int) {
        val user = users.get(position)
        myHolder.bind(user, itemClickListener, itemLongClickListener, position)


    }
}


interface OnItemClickListener{
    fun onItemClicked(user: User, position: Int)
}

interface OnLongClickListener{
    fun onItemLongClicked(user: User, position: Int)
}