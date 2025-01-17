package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.databinding.ActivityMainBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private  val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = intent
        val id = intent.getStringExtra("id").toString()

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("id", id).build()
        val request = Request.Builder()
            .url("http://192.168.29.30:5000/det")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call,response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val gson = Gson()
                    val data = gson.fromJson(responseBody, Data::class.java)
                    //getting all the elements from labels dispensary
                    val labels = data.detections.labels
                    //adding all labels with a , in between
                    val allLabels = labels.joinToString(", ") { it.label } // Concatenate all labels
                    runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                        binding.textView.text = allLabels
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Empty response body", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    data class Data(val detections: Detections)
    data class Detections(val labels: List<Label>)
    data class Label(
        val Height: Int,
        val Width: Int,
        val X: Int,
        val Y: Int,
        val confidences: Double,
        val label: String
    )
}