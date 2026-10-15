package com.masfitatech.koprafin.page.main.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.BannerStatusRequest
import com.masfitatech.koprafin.data.model.DataBanner
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BannerViewModel : ViewModel() {

    val bannersActive = MutableLiveData<List<DataBanner>>()
    val banners = MutableLiveData<List<DataBanner>>()
    val isLoading = MutableLiveData<Boolean>()
    val success = MutableLiveData<Boolean>()
    val deleteSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun getActiveBanners(token: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getBanner(token)
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    bannersActive.postValue(list.filter { it.status == 1 })
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
    fun getBanners(token: String) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                val response = ApiClient.instance.getBanner(token)

                if (response.isSuccessful) {
                    banners.postValue(response.body()?.data ?: emptyList())
                } else {
                    errorMessage.postValue("Gagal mengambil banner")
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun createBanner(
        token: String,
        status: Int,
        imagePart: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val statusBody = status.toString()
                    .toRequestBody("text/plain".toMediaType())

                val response = ApiClient.instance.createBanner(
                    token,
                    statusBody,
                    imagePart
                )

                if (response.isSuccessful) {
                    Log.d("BannerVM", "createBanner success")
                    success.postValue(true)
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Log.e("BannerVM", "createBanner failed: ${response.code()} | $error")
                    errorMessage.postValue(error)
                }

            } catch (e: Exception) {
                Log.e("BannerVM", "createBanner exception", e)
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }


    fun updateBanner(token: String, id: Int, status: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val body = BannerStatusRequest(status)
                val response = ApiClient.instance.updateBanner(token, id, body)

                if (response.isSuccessful) {
                    Log.d(
                        "BannerVM",
                        "updateBanner success | id=$id | status=$status"
                    )
                    success.postValue(true)
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(
                        "BannerVM",
                        "updateBanner failed | id=$id | code=${response.code()} | $error"
                    )
                    errorMessage.postValue(error)
                }

            } catch (e: Exception) {
                Log.e(
                    "BannerVM",
                    "updateBanner exception | id=$id | status=$status",
                    e
                )
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }


    fun deleteBanner(token: String, id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                val response = ApiClient.instance.deleteBanner(token, id)

                if (response.isSuccessful) {
                    deleteSuccess.postValue(true)
                } else {
                    errorMessage.postValue("Gagal hapus banner")
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    private fun parseError(errorBody: String?): String {
        return try {
            if (errorBody.isNullOrEmpty()) {
                "Unknown error (empty body)"
            } else {
                val json = JSONObject(errorBody)
                when {
                    json.has("message") -> json.getString("message")
                    json.has("error") -> json.getString("error")
                    else -> errorBody
                }
            }
        } catch (e: Exception) {
            "Failed to parse error: ${e.message}"
        }
    }


    fun resetEvent() {
        success.postValue(false)
        deleteSuccess.postValue(false)
    }
}
