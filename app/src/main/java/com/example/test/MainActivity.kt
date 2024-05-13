package com.example.test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.databinding.ActivityMainBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http2.Http2Reader
import org.jetbrains.annotations.NotNull
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private  val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.29.30:5000/?image")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call,response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val gson = Gson()
                    val data = gson.fromJson(responseBody, Data::class.java)
                    val labels = data.detections.labels
                    if (labels.isNotEmpty()) {
                        val label = labels[0].label
                        runOnUiThread {
                            binding.textView.text = label
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "No labels found", Toast.LENGTH_SHORT).show()
                        }
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