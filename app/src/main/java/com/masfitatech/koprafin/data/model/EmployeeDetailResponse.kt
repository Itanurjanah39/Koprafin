package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class EmployeeDetailResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataEmployee?    = DataEmployee()
)
