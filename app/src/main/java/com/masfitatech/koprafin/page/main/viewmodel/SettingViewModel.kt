package com.masfitatech.koprafin.page.main.viewmodel

import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masfitatech.koprafin.data.model.DataCategory
import com.masfitatech.koprafin.data.model.DetailCategory
import com.masfitatech.koprafin.data.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class SettingViewModel : ViewModel() {


    // list
    val categories = MutableLiveData<List<DataCategory>>()

    //detail
    val categoryDetail = MutableLiveData<DetailCategory>()

    //state
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()
    val success = MutableLiveData<Boolean>()
    val deleteSuccess = MutableLiveData<Boolean>()

    private val _actionSuccess = MutableLiveData<Boolean>()
    val actionSuccess: LiveData<Boolean> = _actionSuccess


    fun getCategories(token: String) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val response = ApiClient.instance.getCategories(token)

                if (response.isSuccessful) {
                    val list = response.body()?.data ?: arrayListOf()
                    categories.postValue(list)
                } else {
                    errorMessage.postValue(parseError(response.errorBody()?.string()))
                }

            } catch (e: Exception) {
                Log.e("CategoryVM", "getCategories: ${e.message}")
                errorMessage.postValue(e.message ?: "Terjadi kesalahan")
            } finally {
                isLoading.postValue(false)
            }
        }
    }


    fun getCategoryDetail(token: String, id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val response = ApiClient.instance.detailCategory(token, id)

                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        categoryDetail.postValue(it)
                    }
                } else {
                    errorMessage.postValue(parseError(response.errorBody()?.string()))
                }

            } catch (e: Exception) {
                Log.e("CategoryVM", "getCategoryDetail: ${e.message}")
                errorMessage.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun createCategory(
        token: String,
        name: String,
        minTenor: Int,
        maxTenor: Int,
        margin: Int
    ) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val response = ApiClient.instance.createCategory(
                    token = token,
                    name = name,
                    minTenor = minTenor,
                    maxTenor = maxTenor,
                    margin = margin
                )

                if (response.isSuccessful) {
                    success.postValue(true)
                    _actionSuccess.postValue(true)

                } else {
                    errorMessage.postValue(parseError(response.errorBody()?.string()))
                    success.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("CategoryVM", "createCategory: ${e.message}")
                errorMessage.postValue(e.message)
                success.postValue(false)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun updateCategory(
        token: String,
        id: Int,
        name: String,
        minTenor: Int,
        maxTenor: Int,
        margin: Int
    ) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val response = ApiClient.instance.updateCategory(
                    token = token,
                    id = id,
                    name = name,
                    minTenor = minTenor,
                    maxTenor = maxTenor,
                    margin = margin
                )

                if (response.isSuccessful) {
                    success.postValue(true)
                    _actionSuccess.postValue(true)

                } else {
                    errorMessage.postValue(parseError(response.errorBody()?.string()))
                    success.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("CategoryVM", "updateCategory: ${e.message}")
                errorMessage.postValue(e.message)
                success.postValue(false)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun deleteCategory(token: String, id: Int) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true)

                val response = ApiClient.instance.deleteCategory(token, id)

                if (response.isSuccessful) {
                    deleteSuccess.postValue(true)
                    _actionSuccess.postValue(true)

                } else {
                    errorMessage.postValue(parseError(response.errorBody()?.string()))
                    deleteSuccess.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("CategoryVM", "deleteCategory: ${e.message}")
                errorMessage.postValue(e.message)
                deleteSuccess.postValue(false)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    private fun parseError(errorBody: String?): String {
        return try {
            val json = JSONObject(errorBody ?: "")
            when {
                json.has("errors") -> {
                    val errors = json.getJSONObject("errors")
                    val keys = errors.keys()
                    if (keys.hasNext()) {
                        val key = keys.next()
                        errors.getJSONArray(key).getString(0)
                    } else {
                        json.optString("message", "Terjadi kesalahan")
                    }
                }
                else -> json.optString("message", "Terjadi kesalahan")
            }
        } catch (e: Exception) {
            Log.e("CategoryVM", "parseError: ${e.message}")
            "Terjadi kesalahan"
        }
    }

    fun resetEvent() {
        _actionSuccess.value = false
    }

}
