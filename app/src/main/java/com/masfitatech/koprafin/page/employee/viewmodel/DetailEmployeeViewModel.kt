package com.masfitatech.koprafin.page.employee.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataEmployee
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class DetailEmployeeViewModel(application: Application) : AndroidViewModel(application) {

    val detail = MutableLiveData<DataEmployee?>()
    val isLoading = MutableLiveData<Boolean>()
    val success = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    private val TAG = "DetailEmployeeVM"

    fun getEmployeeDetail(token: String, id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                val res = ApiClient.instance.getEmployeeDetail(token, id)

                if (res.isSuccessful) {
                    detail.postValue(res.body()?.data)
                } else {
                    error.postValue(parseError(res.errorBody()?.string()))
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun createEmployee(
        token: String,
        name: String,
        nik: String,
        email: String,
        password: String,
        phone: String,
        role: String,
        company: String,
        division: String,
        joinDate: String,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val avatarPart = getImageMultipart(photoUri)

                val res = ApiClient.instance.createEmployee(
                    token,
                    name.toRB(),
                    nik.toRB(),
                    email.toRB(),
                    password.toRB(),
                    phone.toRB(),
                    role.toRB(),
                    company.toRB(),
                    division.toRB(),
                    joinDate.toRB(),
                    avatarPart
                )

                if (res.isSuccessful) {
                    success.postValue(true)
                } else {
                    error.postValue(parseError(res.errorBody()?.string()))
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    // ============================================================
    // UPDATE EMPLOYEE TEXT DATA ONLY
    // ============================================================
    fun updateEmployee(
        token: String,
        id: Int,
        name: String,
        nik: String,
        email: String,
        phone: String,
        role: String,
        company: String,
        division: String,
        joinDate: String,
    ) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val res = ApiClient.instance.updateEmployee(
                    token,
                    id,
                    name,
                    nik,
                    email,
                    phone,
                    role,
                    company,
                    division,
                    joinDate
                )

                if (res.isSuccessful) {
                    success.postValue(true)
                } else {
                    error.postValue(parseError(res.errorBody()?.string()))
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun updateAvatarEmployee(token: String, id: Int, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val part = getImageMultipart(uri)
                if (part == null) {
                    error.postValue("Gagal membaca gambar")
                    return@launch
                }

                val res = ApiClient.instance.updateAvatarEmployee(token, id, part)

                if (res.isSuccessful) {
                    success.postValue(true)
                } else {
                    error.postValue("Gagal mengupdate foto profil")
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }


    private fun String.toRB(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    private fun getImageMultipart(uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null

        return try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val tempFile = File.createTempFile("avatar_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output -> inputStream.copyTo(output) }

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())

            MultipartBody.Part.createFormData(
                "avatar",
                tempFile.name,
                requestFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "getImageMultipart ERR: ${e.message}")
            null
        }
    }

    private fun parseError(err: String?): String {
        if (err.isNullOrEmpty()) return "Terjadi kesalahan"

        return try {
            val json = JSONObject(err)

            if (json.has("errors")) {
                val errors = json.getJSONObject("errors")
                val keys = errors.keys()

                if (keys.hasNext()) {
                    val key = keys.next()
                    val msgs = errors.getJSONArray(key)
                    return msgs.getString(0)
                }
            }

            json.optString("message", "Terjadi kesalahan")
        } catch (e: Exception) {
            "Terjadi kesalahan"
        }
    }
}
