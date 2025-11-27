package ru.netology.statsview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import ru.netology.statsview.databinding.ActivityAppBinding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stats.data = listOf(
            0.20F,
            0.20F,
            0.20F,
            0.20F,
            0.20F
        )

    }
}