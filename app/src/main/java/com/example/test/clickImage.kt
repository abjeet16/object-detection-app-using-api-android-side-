package com.example.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class clickImage : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_PERMISSION_CODE = 101
    private lateinit var intent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deleteAllFilesFromStorage()

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            dispatchTakePictureIntent()
        }
    }
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start camera
                dispatchTakePictureIntent()
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish() // Close the activity if permission is denied
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageToFirebaseStorage(imageBitmap)
        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val id = System.currentTimeMillis().toString()
        val imagesRef = storageRef.child("${id}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            intent=Intent(this,MainActivity::class.java)
            intent.putExtra("id",id)
            startActivity(intent)
            finish()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    fun deleteAllFilesFromStorage() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        deleteFiles(storageRef)
    }

    fun deleteFiles(ref: StorageReference) {
        ref.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    deleteFile(item)
                }
                for (prefix in listResult.prefixes) {
                    deleteFiles(prefix)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                println("Error: $exception")
            }
    }

    fun deleteFile(ref: StorageReference): Task<Void> {
        return ref.delete()
    }
}