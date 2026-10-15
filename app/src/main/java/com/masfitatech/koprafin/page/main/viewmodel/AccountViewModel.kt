package com.masfitatech.koprafin.page.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataAccount
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    val sessionExpired = MutableLiveData<Boolean>()
    val accountData = MutableLiveData<DataAccount?>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun getAccount(token: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true

                val response = ApiClient.instance.getAccount(token)

                if (response.isSuccessful) {
                    accountData.postValue(response.body()?.data)
                } else {
                    if (response.code() == 401) {
                        sessionExpired.postValue(true)
                    } else {
                        errorMessage.postValue(response.message())
                    }
                }

            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.value = false
            }
        }
    }

}
