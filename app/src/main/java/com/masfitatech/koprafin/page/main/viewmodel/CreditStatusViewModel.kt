package com.masfitatech.koprafin.page.main.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.CounterCreditResponse
import com.masfitatech.koprafin.data.model.DataCounterCredit
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreditStatusViewModel : ViewModel() {

    val statusData = MutableLiveData<DataCounterCredit?>()
    val errorMessage = MutableLiveData<String?>()

    fun fetchStatus(token: String) {
        viewModelScope.launch {
            try {
                val res = ApiClient.instance.getCreditStatus(token)

                if (res.isSuccessful) {
                    statusData.postValue(res.body()?.data)
                    Log.d("Data Counter", "${res.body()?.data}")
                } else {
                    val msg = try {
                        val json = JSONObject(res.errorBody()?.string() ?: "{}")
                        json.optString("message", "Terjadi kesalahan")
                    } catch (e: Exception) {
                        "Terjadi kesalahan"
                    }
                    errorMessage.postValue(msg)
                }

            } catch (e: Exception) {
                errorMessage.postValue(e.message)
            }
        }
    }
}
