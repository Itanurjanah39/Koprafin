package com.masfitatech.koprafin.page.main.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataHistory
import com.masfitatech.koprafin.data.model.DataHistoryDetail
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val history = MutableLiveData<List<DataHistory>>()
    val historyDetail = MutableLiveData<DataHistoryDetail?>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun getHistory(token: String) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                val res = ApiClient.instance.getHistory(token)
                if (res.isSuccessful) {
                    history.postValue(res.body()?.data?.data ?: emptyList())
                } else {
                    errorMessage.postValue(parseError(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun getHistoryDetail(token: String, id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)
                val res = ApiClient.instance.getHistoryDetail(token, id)

                if (res.isSuccessful) {
                    historyDetail.postValue(res.body()?.data)
                } else {
                    errorMessage.postValue(parseError(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun creditAction(token: String, id: Int, action: String, remarks: String? = null) {
        viewModelScope.launch {
            try {
                val res = ApiClient.instance.creditActions(token, id, action, remarks)
                if (!res.isSuccessful) {
                    errorMessage.postValue(parseError(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun creditFinishing(token: String, id: Int, remarks: String) {
        viewModelScope.launch {
            try {
                val res = ApiClient.instance.creditFinish(token, id, remarks)
                if (!res.isSuccessful) {
                    errorMessage.postValue(parseError(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }

    private fun parseError(error: String?): String {
        if (error.isNullOrEmpty()) return "Terjadi kesalahan"

        return try {
            val json = JSONObject(error)
            json.optString("message", "Terjadi kesalahan")
        } catch (_: Exception) {
            "Terjadi kesalahan"
        }
    }
}


//class HistoryViewModel : ViewModel() {
//
//    val history = MutableLiveData<List<DataHistory>>()
//    val isLoading = MutableLiveData<Boolean>()
//    val errorMessage = MutableLiveData<String?>()
//
//    fun getHistory(token: String) {
//        viewModelScope.launch {
//            try {
//                isLoading.postValue(true)
//
//                val res = ApiClient.instance.getHistory(token)
//
//                if (res.isSuccessful) {
//
//                    val list = res.body()?.data?.data ?: emptyList()
//
//                    history.postValue(list)
//
//                } else {
//                    // Ambil pesan error JSON
//                    val error = res.errorBody()?.string() ?: ""
//                    val message = try {
//                        JSONObject(error).optString("message", "Terjadi kesalahan")
//                    } catch (e: Exception) {
//                        "Terjadi kesalahan"
//                    }
//
//                    errorMessage.postValue(message)
//                }
//
//            } catch (e: Exception) {
//                errorMessage.postValue(e.message ?: "Terjadi kesalahan")
//            } finally {
//                isLoading.postValue(false)
//            }
//        }
//    }
//}
