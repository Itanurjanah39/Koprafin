package com.masfitatech.koprafin.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.databinding.ItemImageBannerAddBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object DialogUtils {

    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Ya",
        cancelText: String = "Batal",
        onConfirm: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_confirm, null)

        val dialog = AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitleDialog)
        val tvContent = dialogView.findViewById<TextView>(R.id.tvContentDialog)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)
        val btnAgree = dialogView.findViewById<AppCompatButton>(R.id.btnAgree)

        tvTitle.text = title
        tvContent.text = message
        tvCancel.text = cancelText
        btnAgree.text = positiveText

        tvCancel.setOnClickListener { dialog.dismiss() }

        btnAgree.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showErrorDialog(
        context: Context,
        title: String = "Gagal",
        message: String
    ) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_error, null)

        val dialog = AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val btnClose = view.findViewById<AppCompatButton>(R.id.btnClose)

        tvTitle.text = title
        tvMessage.text = message

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.setOnShowListener {
            val anim = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_in)
            view.startAnimation(anim)
        }

        dialog.show()
    }

    fun showInputDialog(
        context: Context,
        title: String,
        titleInput : String = "Email",
        hint: String = "",
        value: String = "",
        positiveText: String = "Reset",
        cancelText: String = "Batal",
        onSubmit: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_input, null)

        val dialog = AlertDialog.Builder(context, R.style.CustomDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitleDialog)
        val tvJudulInput = dialogView.findViewById<TextView>(R.id.tvJudulInput)
        val etInput = dialogView.findViewById<EditText>(R.id.etInput)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)
        val btnReset = dialogView.findViewById<AppCompatButton>(R.id.btnReset)

        tvTitle.text = title
        tvJudulInput.text = titleInput
        etInput.hint = hint
        etInput.setText(value)

        tvCancel.text = cancelText
        tvCancel.setOnClickListener { dialog.dismiss() }

        btnReset.text = positiveText
        btnReset.setOnClickListener {
            val inputText = etInput.text.toString().trim()
            onSubmit(inputText)
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            val anim = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_in)
            dialogView.startAnimation(anim)
        }

        dialog.show()
    }



}
