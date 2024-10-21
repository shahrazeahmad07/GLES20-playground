package com.example.gles20playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gles20playground.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.glSurfaceView.requestRender()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}