package com.masfitatech.koprafin.page.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataDetail
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch

class DetailProductViewModel : ViewModel() {

    val detailData = MutableLiveData<DataDetail>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun fetchDetail(token: String, productId: Int) {
        viewModelScope.launch {
            try {
                isLoading.value = true

                val response = ApiClient.instance.getDetailProduct(token, productId)

                if (response.isSuccessful) {
                    detailData.postValue(response.body()?.data!!)
                } else {
                    errorMessage.postValue(response.message())
                }

            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.value = false
            }
        }
    }
}

