package com.masfitatech.koprafin.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import com.masfitatech.koprafin.R

class LoadingDialog (private val activity: Activity) {
    private var dialog: AlertDialog? = null

    @SuppressLint("SetTextI18n")
    fun show(message: String ) {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_loading, null)
        val text = view.findViewById<TextView>(R.id.tvText)

        text.text = "$message ..."
        builder.setView(view)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
