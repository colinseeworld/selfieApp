package com.example.firebasee4_camera_gallery_recyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasee4_camera_gallery_recyclerview.Item
import com.example.firebasee4_camera_gallery_recyclerview.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_items.view.*

class ImageAdapter(var context: Context, var imageList: ArrayList<Item>):RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.row_items,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = imageList[position]
        Picasso.get().load(item.imageUrl).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var imageView = itemView.row_image_view_pic

//        var imageView:ImageView = itemView.findViewById(R.id.row_image_view_pic);
    }
}