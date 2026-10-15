package com.masfitatech.koprafin.page.main.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataProduct
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch

class KatalogViewModel : ViewModel(){

    val products = MutableLiveData<List<DataProduct>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun fetchProducts(
        token : String,
        categoryId : Int? = null ,
        search : String? = null,
        perPage : Int? = null
    ){

        viewModelScope.launch {
            try {
                isLoading.value = true
                val response = ApiClient.instance.getProducts(
                    token = token,
                    page = 1,
                    perpage = perPage,
                    categoryId = categoryId,
                    search = search
                )

                Log.d("TAB_API", "====== REQUEST ======")
                Log.d("TAB_API", "Full URL: ${response.raw().request.url}")
                Log.d("TAB_API", "Token: $token")
                Log.d("TAB_API", "Status Code: ${response.code()}")
                Log.d("TAB_API", "Response Body: ${response.body()}")
                Log.d("TAB_API", "=====================")
                if (response.isSuccessful) {
                    val data = response.body()?.data?.data ?: emptyList()

                    products.postValue(data)
                } else {
                    products.postValue(emptyList())
                    errorMessage.postValue(response.message())
                    Log.d("tab_api", response.message())
                }

            }catch (e: Exception) {
                errorMessage.value = e.message
                errorMessage.postValue(errorMessage.value)
                Log.e("tab_api", "Error message: ${errorMessage.value}")
            } finally {
                isLoading.value = false
            }
        }
    }
}