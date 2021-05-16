package org.bandev.labyrinth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bandev.labyrinth.databinding.CommitActivityBinding

class CommitActivity : AppCompatActivity() {
    private lateinit var binding: CommitActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CommitActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}