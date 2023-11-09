package com.logituit.player.common

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.logituit.player.R
import com.logituit.player.databinding.DialogCustomBinding

class CustomDialog(
    var title: String = "",
    var message: String = "",
    var positiveButtonText: String = "",
    var negativeButtonText: String = "",
    var cancellable: Boolean = false,
    mListener: ClickListener?
) : DialogFragment() {
    interface ClickListener {
        fun onSuccess()
        fun onCancel()
    }

    /*val logger by lazy {
        Logger(this::class.java.simpleName)
    }*/
    private var listener: ClickListener? = mListener
    private var binding: DialogCustomBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = cancellable
        return inflater.inflate(R.layout.dialog_custom, container, false)
    }


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        if (title.isEmpty()) {
            title = getString(R.string.app_name)
        }
        binding?.apply {

            binding?.okButton?.text=positiveButtonText
            binding?.cancelButton?.text=negativeButtonText
            binding?.exitDescription?.text=message
            binding?.exitText?.text=title
            okButton.setOnClickListener {

                listener?.onSuccess()
                Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
                }, 500)
            }

            cancelButton.setOnClickListener {
                listener?.onCancel()
                Handler(Looper.getMainLooper()).postDelayed({
                    dismiss()
                }, 500)
            }


            okButton.requestFocus()
            okButton.requestFocusFromTouch()
            okButton.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    okButton.setTextColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.blue_black
                        )
                    )
                    okButton.setBackgroundResource(R.drawable.bg_btn_ok_screen)
                } else {
                    okButton.setTextColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.logituit_white_70
                        )
                    )
                    okButton.setBackgroundResource(R.drawable.bg_btn_exit_screen)
                }
                cancelButton.setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        cancelButton.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.blue_black
                            )
                        )
                      //  cancelButton.setBackgroundResource(R.drawable.bg_btn_ok_screen)
                    } else {
                        cancelButton.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                     //   cancelButton.setBackgroundResource(R.drawable.bg_btn_exit_screen)
                    }
                }
            }
        }
    }
}