package com.logituit.player.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.logituit.player.R

class BasePlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_player)
    }
}