package com.masfitatech.koprafin.page.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataCategory
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {


    val categories = MutableLiveData<List<DataCategory>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun fetchCategories(token: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val response = ApiClient.instance.getCategories(token)

                if (response.isSuccessful) {
                    categories.postValue(response.body()?.data ?: emptyList())
                } else {
                    errorMessage.postValue("Error ${response.code()}: ${response.message()}")
                    categories.postValue(emptyList())
                }

            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }
}
