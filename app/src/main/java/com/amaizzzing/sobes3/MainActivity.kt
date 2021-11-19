package com.amaizzzing.sobes3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amaizzzing.sobes3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            startAnimation.setOnClickListener {
                carview.startAnimation()
            }
            stopAnimation.setOnClickListener {
                carview.stopAnimation()
            }
            recreatePath.setOnClickListener {
                carview.recreatePath()
            }
        }
    }
}