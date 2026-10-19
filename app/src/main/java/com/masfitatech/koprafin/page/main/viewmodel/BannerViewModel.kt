package com.masfitatech.koprafin.page.main.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.ApiResponse
import com.masfitatech.koprafin.data.model.BannerStatusRequest
import com.masfitatech.koprafin.data.model.DataBanner
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.utils.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Banner (dipakai Beranda & Pengaturan, di-share lewat activityViewModels).
 *
 * Endpoint:
 *  GET  banners                 -> daftar
 *  POST banners (multipart)     -> image + status
 *  PUT  banners/{id}/status     -> {"status": 0|1}
 *  PUT  banners/{id}/delete     -> hapus
 */
class BannerViewModel : ViewModel() {

    private companion object { const val TAG = "BannerVM" }

    /** Semua banner (Pengaturan). */
    val banners = MutableLiveData<List<DataBanner>>()

    /** Banner aktif saja (carousel Beranda). */
    val bannersActive = MutableLiveData<List<DataBanner>>()

    val isLoading = MutableLiveData(false)
    val isUploading = MutableLiveData(false)

    /** Id banner yang sedang diproses (toggle/hapus) — UI menonaktifkan kontrolnya. */
    val pendingIds = MutableLiveData<Set<Int>>(emptySet())

    val message = MutableLiveData<Event<String>>()
    val uploadSuccess = MutableLiveData<Event<Unit>>()

    private var loadJob: Job? = null

    // =====================================================
    // LIST
    // =====================================================
    fun getActiveBanners(token: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.getBanner(token)
                if (response.isSuccessful) publish(response.body()?.data.orEmpty())
                else Log.e(TAG, "getActiveBanners ${response.code()}")
            } catch (e: Exception) {
                Log.e(TAG, "getActiveBanners", e)
            }
        }
    }

    fun getBanners(token: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            isLoading.value = true
            try {
                val response = ApiClient.instance.getBanner(token)
                if (response.isSuccessful) {
                    publish(response.body()?.data.orEmpty())
                } else {
                    notify(errorFrom(response, "Gagal memuat banner"))
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) notify(errorFrom(e))
            } finally {
                isLoading.value = false
            }
        }
    }

    // =====================================================
    // CREATE
    // =====================================================
    fun createBanner(token: String, status: Int, imagePart: MultipartBody.Part) {
        if (isUploading.value == true) return
        viewModelScope.launch {
            isUploading.value = true
            try {
                val statusBody = status.toString().toRequestBody("text/plain".toMediaType())
                val response = ApiClient.instance.createBanner(token, statusBody, imagePart)

                if (response.isSuccessful && response.body()?.success != false) {
                    uploadSuccess.value = Event(Unit)
                    notify("Banner berhasil diunggah")
                    getBanners(token)
                } else {
                    notify(errorFrom(response, "Gagal mengunggah banner"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBanner", e)
                notify(errorFrom(e))
            } finally {
                isUploading.value = false
            }
        }
    }

    // =====================================================
    // UPDATE STATUS (optimistic + rollback)
    // =====================================================
    fun updateStatus(token: String, id: Int, status: Int) {
        if (isPending(id)) return
        val oldStatus = banners.value?.firstOrNull { it.id == id }?.status ?: return
        if (oldStatus == status) return

        setStatusLocally(id, status)
        setPending(id, true)

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.updateBanner(token, id, BannerStatusRequest(status))
                if (response.isSuccessful && response.body()?.success != false) {
                    notify(if (status == 1) "Banner ditampilkan di Beranda" else "Banner disembunyikan")
                } else {
                    setStatusLocally(id, oldStatus)
                    notify(errorFrom(response, "Gagal mengubah status banner"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateStatus id=$id", e)
                setStatusLocally(id, oldStatus)
                notify(errorFrom(e))
            } finally {
                setPending(id, false)
            }
        }
    }

    // =====================================================
    // DELETE
    // =====================================================
    fun deleteBanner(token: String, id: Int) {
        if (isPending(id)) return
        setPending(id, true)

        viewModelScope.launch {
            try {
                val response = ApiClient.instance.deleteBanner(token, id)
                if (response.isSuccessful && response.body()?.success != false) {
                    publish(banners.value.orEmpty().filter { it.id != id })
                    notify("Banner dihapus")
                } else {
                    notify(errorFrom(response, "Gagal menghapus banner"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteBanner id=$id", e)
                notify(errorFrom(e))
            } finally {
                setPending(id, false)
            }
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private fun publish(list: List<DataBanner>) {
        banners.value = list
        bannersActive.value = list.filter { it.status == 1 }
    }

    private fun setStatusLocally(id: Int, status: Int) {
        publish(banners.value.orEmpty().map { if (it.id == id) it.copy(status = status) else it })
    }

    private fun isPending(id: Int) = pendingIds.value.orEmpty().contains(id)

    private fun setPending(id: Int, pending: Boolean) {
        val current = pendingIds.value.orEmpty()
        pendingIds.value = if (pending) current + id else current - id
    }

    private fun notify(text: String) {
        message.value = Event(text)
    }

    private fun errorFrom(response: Response<*>, fallback: String): String {
        val body = response.body()
        if (response.isSuccessful && body is ApiResponse && !body.message.isNullOrBlank()) {
            return body.message!!
        }
        return when (response.code()) {
            401 -> "Sesi berakhir, silakan login ulang"
            403 -> "Anda tidak punya akses untuk aksi ini"
            404 -> "Banner tidak ditemukan (mungkin sudah dihapus)"
            413 -> "Ukuran gambar terlalu besar untuk server"
            else -> parseErrorBody(response.errorBody()?.string()) ?: "$fallback (kode ${response.code()})"
        }
    }

    private fun errorFrom(e: Exception): String = when (e) {
        is SocketTimeoutException -> "Koneksi ke server timeout, coba lagi"
        is IOException -> "Tidak dapat terhubung ke server. Periksa koneksi Anda"
        else -> e.message ?: "Terjadi kesalahan"
    }

    private fun parseErrorBody(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = JSONObject(raw)
            val errors = json.optJSONObject("errors")
            if (errors != null && errors.keys().hasNext()) {
                val key = errors.keys().next()
                errors.optJSONArray(key)?.optString(0) ?: errors.optString(key)
            } else {
                json.optString("message").ifBlank { json.optString("error") }.ifBlank { null }
            }
        } catch (e: Exception) {
            null
        }
    }
}
