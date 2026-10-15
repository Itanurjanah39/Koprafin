package com.masfitatech.koprafin.page.login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch

class OtpViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val success = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    fun resetPassword(
        email: String,
        otp: String,
        newPass: String,
        newPassConfirm: String
    ) {
        viewModelScope.launch {

            try {
                isLoading.postValue(true)

                val res = ApiClient.instance.resetPassword(
                    email = email,
                    otp = otp,
                    newPass = newPass,
                    newPassConfirm = newPassConfirm
                )

                if (res.isSuccessful) {
                    success.postValue(true)
                } else {
                    error.postValue("Gagal memperbarui password")
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}
