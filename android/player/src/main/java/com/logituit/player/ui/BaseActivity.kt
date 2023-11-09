package com.logituit.player.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.logituit.player.R

open class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_base)

    }
}