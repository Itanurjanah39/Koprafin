package com.masfitatech.koprafin.page.product.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataDetail
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AddProductViewModel(application: Application) : AndroidViewModel(application) {

    val productDetail = MutableLiveData<DataDetail?>()
    val isLoading = MutableLiveData<Boolean>()
    val success = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    var isDeleteProcess: Boolean = false

    private val TAG = "AddProductVM"

    fun getProductDetail(token: String, id: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getDetailProduct(token, id)
                if (response.isSuccessful) {
                    productDetail.postValue(response.body()?.data)
                } else {
                    error.postValue("Gagal memuat detail produk")
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
            }
        }
    }

    fun updateProduct(
        token: String,
        id: Int,
        categoryId: String,
        name: String,
        price: String,
        description: String,
        images: List<Uri>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            isLoading.postValue(true)
            isDeleteProcess = false

            try {
                val imageParts = mutableListOf<MultipartBody.Part>()

                for (uri in images) {

                    val file = if (uri.toString().startsWith(ApiClient.BASE_URL_IMAGE)) {
                        Log.e("UPDATE_IMAGE", "⬇️ Download gambar lama: $uri")
                        downloadImageToFile(uri.toString())
                            ?: throw Exception("Gagal download gambar lama: $uri")
                    } else {
                        compressImage(uri)
                    }

                    val req = file.asRequestBody("image/*".toMediaType())

                    imageParts.add(
                        MultipartBody.Part.createFormData("pictures[]", file.name, req)
                    )
                }

                val response = ApiClient.instance.updateProduct(
                    token,
                    id,
                    categoryId.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    price.toRequestBody("text/plain".toMediaType()),
                    description.toRequestBody("text/plain".toMediaType()),
                    imageParts
                )

                if (response.isSuccessful) {
                    success.postValue(true)
                } else {
                    error.postValue("Gagal memperbarui produk")
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
    fun uploadProduct(
        token: String,
        categoryId: String,
        name: String,
        price: String,
        description: String,
        images: List<Uri>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            isLoading.postValue(true)
            isDeleteProcess = false

            try {
                val imageParts = mutableListOf<MultipartBody.Part>()

                images.take(6).forEachIndexed { index, uri ->
                    val file = compressImage(uri)
                    Log.d(TAG, "Compressed file[$index]: ${file.length() / 1024} KB")

                    val req = file.asRequestBody("image/*".toMediaType())
                    imageParts.add(
                        MultipartBody.Part.createFormData("pictures[]", file.name, req)
                    )
                }

                val res = ApiClient.instance.addProduct(
                    token,
                    categoryId.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    price.toRequestBody("text/plain".toMediaType()),
                    description.toRequestBody("text/plain".toMediaType()),
                    imageParts
                )

                if (res.isSuccessful) {
                    success.postValue(true)
                } else {
                    val parsed = parseError(res.errorBody()?.string())
                    error.postValue(parsed)
                }

            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun deleteProduct(token: String, id: Int) {
        viewModelScope.launch {
            isLoading.postValue(true)
            isDeleteProcess = true
            try {
                val res = ApiClient.instance.deleteProduct(token, id)
                if (res.isSuccessful) {
                    success.postValue(true)
                } else {
                    error.postValue("Gagal menghapus produk")
                    isDeleteProcess = false
                }
            } catch (e: Exception) {
                error.postValue(e.localizedMessage)
                isDeleteProcess = false
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    private fun parseError(err: String?): String {
        if (err.isNullOrEmpty()) return "Terjadi kesalahan"

        return try {
            val json = JSONObject(err)

            if (json.has("errors")) {
                val errors = json.getJSONObject("errors")
                val sb = StringBuilder()
                errors.keys().forEach { key ->
                    val messages = errors.getJSONArray(key)
                    sb.append("$key: ${messages.join(", ")}\n")
                }
                return sb.toString().trim()
            }

            json.optString("message", "Gagal menambah produk")

        } catch (e: Exception) {
            "Gagal menambah produk"
        }
    }
    private suspend fun compressImage(uri: Uri): File = withContext(Dispatchers.IO) {

        val context = getApplication<Application>().applicationContext

        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        var compressQuality = 90
        lateinit var file: File

        do {
            file = File(context.cacheDir, "img_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, output)
            }
            compressQuality -= 10
        } while (file.length() > 1024 * 1024 && compressQuality > 10)

        return@withContext file
    }

    private suspend fun downloadImageToFile(url: String): File? = withContext(Dispatchers.IO) {

        try {
            val conn = (URL(url).openConnection() as HttpURLConnection)
            conn.instanceFollowRedirects = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.connect()

            Log.e("DOWNLOAD_FILE", "Response Code: ${conn.responseCode}")

            if (conn.responseCode !in 200..299) {
                Log.e("DOWNLOAD_FILE", "Gagal download: ${conn.responseMessage}")
                return@withContext null
            }

            val file = File(getApplication<Application>().cacheDir, "old_${System.currentTimeMillis()}.jpg")

            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.e("DOWNLOAD_FILE", "Download sukses: ${file.absolutePath}")
            return@withContext file

        } catch (e: Exception) {
            Log.e("DOWNLOAD_FILE", "ERROR:", e)
            return@withContext null
        }
    }
}
