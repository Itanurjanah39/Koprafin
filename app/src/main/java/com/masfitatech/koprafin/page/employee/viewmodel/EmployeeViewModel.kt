package com.masfitatech.koprafin.page.employee.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataEmployee
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class EmployeeViewModel : ViewModel() {

    val employees = MutableLiveData<List<DataEmployee>>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    val deleteSuccess = MutableLiveData<Boolean>()

    fun getEmployees(token: String) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val response = ApiClient.instance.getEmployees(token)

                if (response.isSuccessful) {
                    val list = response.body()?.data?.data ?: emptyList()

                    val filteredList = list.filter { it.role != "admin" }

                    employees.postValue(filteredList)

                } else {
                    val err = response.errorBody()?.string() ?: ""
                    val msg = try {
                        JSONObject(err).optString("message", "Terjadi kesalahan")
                    } catch (e: Exception) {
                        "Terjadi kesalahan"
                    }
                    errorMessage.postValue(msg)
                }

            } catch (e: Exception) {
                errorMessage.postValue(e.message ?: "Terjadi kesalahan")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun deleteEmployee(token: String, employeeId: Int?) {
        if (employeeId == null) {
            errorMessage.postValue("ID tidak ditemukan")
            return
        }

        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                Log.d("DeleteEmployee", "Request DELETE employeeId = $employeeId")

                val response = ApiClient.instance.deleteEmployee(token, employeeId)

                Log.d("DeleteEmployee", "Response code = ${response.code()}")

                if (response.isSuccessful) {

                    Log.d("DeleteEmployee", "Success delete: ${response.body()}")

                    deleteSuccess.postValue(true)

                } else {

                    val rawError = response.errorBody()?.string() ?: ""
                    Log.e("DeleteEmployee", "Raw error = $rawError")

                    val msg = try {
                        val json = JSONObject(rawError)

                        // tampilkan field errors jika ada
                        if (json.has("errors")) {
                            val errors = json.getJSONObject("errors")
                            val keys = errors.keys()

                            if (keys.hasNext()) {
                                val key = keys.next()
                                val arr = errors.getJSONArray(key)
                                val firstErr = arr.getString(0)
                                Log.e("DeleteEmployee", "Parsed error = $firstErr")
                                firstErr
                            } else {
                                json.optString("message", "Gagal menghapus")
                            }
                        } else {
                            json.optString("message", "Gagal menghapus")
                        }

                    } catch (e: Exception) {
                        Log.e("DeleteEmployee", "JSON parse error: ${e.message}")
                        "Gagal menghapus"
                    }

                    errorMessage.postValue(msg)
                    deleteSuccess.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("DeleteEmployee", "Exception deleteEmployee: ${e.message}")
                errorMessage.postValue(e.message)
                deleteSuccess.postValue(false)
            } finally {
                isLoading.postValue(false)
            }
        }

    }
}
