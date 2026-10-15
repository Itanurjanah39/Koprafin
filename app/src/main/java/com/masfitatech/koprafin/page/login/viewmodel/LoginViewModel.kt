package com.masfitatech.koprafin.page.login.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.LoginResponse
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.utils.Resource
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    var resetEmail: String = ""
    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> get() = _loginResult

    private val _resetResult = MutableLiveData<Resource<String>>()
    val resetResult: LiveData<Resource<String>> get() = _resetResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = Resource.Loading
            try {
                val response = ApiClient.instance.login(email, password)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.success == true) {
                            Log.e("LoginViewModel", "success: ${response.body()}")
                            _loginResult.value = Resource.Success(body)
                        } else {
                            _loginResult.value = Resource.Error(body.message.toString())
                        }
                    } else {
                        _loginResult.value = Resource.Error("Terjadi Kesalahan")
                    }
                } else {
                    //  code != 200
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginViewModel", "ErrorBody: $errorBody")
                    _loginResult.value = Resource.Error(parseErrorMessage(errorBody))
                }
            } catch (e: Exception) {
                _loginResult.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetPassword(email: String) {
        resetEmail = email

        viewModelScope.launch {
            _resetResult.value = Resource.Loading
            try {
                val response = ApiClient.instance.forgotPassword(email)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success == true) {
                        _resetResult.value = Resource.Success(body.message) as Resource<String>?
                    } else {
                        _resetResult.value = Resource.Error(body?.message ?: "Gagal mengirim permintaan")
                    }
                } else {
                    val error = response.errorBody()?.string()
                    _resetResult.value = Resource.Error(parseErrorMessage(error))
                }
            } catch (e: Exception) {
                _resetResult.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = JSONObject(errorBody ?: "")
            json.getString("message")
        } catch (e: Exception) {
            "Gagal membaca pesan error"
        }
    }

}