package com.logituit.demo.common.extension

import android.view.FocusFinder
import android.view.View
import android.view.ViewGroup

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun View.hide(){
    this.visibility = View.INVISIBLE
}

fun ViewGroup.findNextFocus(focusView: View, direction: Int): View? {
    return FocusFinder.getInstance().findNextFocus(this, focusView, direction)
}