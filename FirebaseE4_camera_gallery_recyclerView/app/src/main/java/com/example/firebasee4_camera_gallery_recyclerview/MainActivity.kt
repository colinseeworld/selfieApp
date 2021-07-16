package com.example.firebasee4_camera_gallery_recyclerview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasee4_camera_gallery_recyclerview.adapter.ImageAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("images")

        val imageList: ArrayList<Item> = ArrayList()
        val listAllTask: Task<ListResult> = storageReference.listAll()

        listAllTask.addOnCompleteListener { result ->
            val items: List<StorageReference> = result.result!!.items
            items.forEachIndexed { index, item ->
                item.downloadUrl.addOnSuccessListener {
                    Log.d("item","$it")
                    imageList.add(Item(it.toString()))
                }.addOnCompleteListener {
                    recycler_view.adapter = ImageAdapter(this,imageList)
                    recycler_view.layoutManager = GridLayoutManager(this,2, RecyclerView.VERTICAL,
                        false )
//                    recycler_view.layoutManager = LinearLayoutManager(this)
                }
            }
        }

        init()
    }

    private fun init(){
        button_insert.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            button_insert -> startActivity(Intent(this,UploadActivity::class.java))
        }
    }
}