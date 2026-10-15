package com.masfitatech.koprafin.page.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreditViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val successApply = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun applyCredit(token: String, productId: Int, amount: Int, tenor: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val res = ApiClient.instance.postApplyCredit(
                    token = token,
                    id = productId,
                    amount = amount,
                    tenor = tenor
                )

                if (res.isSuccessful) {
                    successApply.postValue(true)
                } else {
                    val errorJson = res.errorBody()?.string()
                    val message = try {
                        JSONObject(errorJson).optString("message", "Terjadi kesalahan")
                    } catch (e: Exception) {
                        "Terjadi kesalahan"
                    }

                    errorMessage.postValue(message)
                }

            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}
