package com.masfitatech.koprafin.page.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataDetailCredit
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreditDetailViewModel : ViewModel() {

    val detailData = MutableLiveData<DataDetailCredit>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun fetchDetail(token: String, creditId: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val res = ApiClient.instance.getDetailCredit(token, creditId)

                if (res.isSuccessful) {
                    val data = res.body()?.data
                    if (data != null) {
                        detailData.postValue(data)
                    } else {
                        errorMessage.postValue("Data tidak ditemukan")
                    }
                } else {
                    val error = res.errorBody()?.string()
                    val msg = JSONObject(error ?: "{}")
                        .optString("message", "Gagal memuat data")
                    errorMessage.postValue(msg)
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}
