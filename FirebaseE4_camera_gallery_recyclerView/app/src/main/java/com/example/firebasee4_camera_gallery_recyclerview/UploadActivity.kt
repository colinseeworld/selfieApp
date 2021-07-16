package com.example.firebasee4_camera_gallery_recyclerview

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_upload.*
import java.text.SimpleDateFormat
import java.util.*

class UploadActivity : AppCompatActivity(),View.OnClickListener {

    private val CAMERA_REQUEST_CODE = 1000
    private val GALLERY_REQUEST_CODE = 1001

    internal var filePath: Uri? = null

    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        storage = FirebaseStorage.getInstance();
        storageReference = storage.reference

        init()
    }

    private fun init() {
        button_camera.setOnClickListener(this)
        button_gallery.setOnClickListener(this)
        button_upload.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            button_camera -> {
                checkCameraPermission()
            }
            button_gallery -> {
                checkGalleryPermission()
            }
            button_upload -> {
                uploadImage()
            }
        }
    }

    private fun uploadImage(){
        if(filePath != null){

            var progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading")
            progressDialog.show()

//            var ImageReference = storageReference.child("images/" + UUID.randomUUID().toString())
            val formatter = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)
            val imageReference = storageReference.child("images/$fileName")

            imageReference.putFile(filePath!!)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@UploadActivity, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    progressDialog.dismiss()
                    Toast.makeText(this@UploadActivity, "Failed to upload", Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { p0 ->
                    var progress = (100.0 * p0.bytesTransferred) / p0.totalByteCount
                    progressDialog.setMessage("Uploaded ${progress.toInt()}%")
                }
        }
    }

    private fun checkCameraPermission() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                    filePath =
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                    camera()

                } else {
                    Toast.makeText(this@UploadActivity, "Permission Denied !", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showDialog()
            }
        }).check()
    }

    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun checkGalleryPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {

                        gallery()

                    } else {
                        Toast.makeText(this@UploadActivity, "Permission Denied !", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).check()
    }

    private fun gallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK ) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    if (data.data != null) {
                        filePath = data.data!!
                        setImage()
                    }
                }
            }
            if (requestCode == CAMERA_REQUEST_CODE) {
                setImage()
            }
        }
    }

    private fun setImage() {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
            image_view_pic.setImageBitmap(bitmap)
    }

    private fun showDialog() {
        AlertDialog.Builder(this)
            .setMessage("Please allow access to your pictures.")
            .setPositiveButton("GO TO SETTINGS"){
                    _,_ ->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)

                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL"){
                    dialog,_ ->
                dialog.dismiss()
            }.show()
    }
}
