package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataAccount?    = DataAccount()
)


data class DataAccount (

    @SerializedName("id"         ) var id        : Int?      = null,
    @SerializedName("email"      ) var email     : String?   = null,
    @SerializedName("employee"   ) var employee  : Employee? = Employee(),
    @SerializedName("last_login" ) var lastLogin : String?   = null

)


data class Employee (

    @SerializedName("id"              ) var id            : Int?    = null,
    @SerializedName("name"            ) var name          : String? = null,
    @SerializedName("nik"             ) var nik           : String? = null,
    @SerializedName("phone"           ) var phone         : String? = null,
    @SerializedName("role"            ) var role          : String? = null,
    @SerializedName("company"         ) var company       : String? = null,
    @SerializedName("division"        ) var division      : String? = null,
    @SerializedName("work_entry_date" ) var workEntryDate : String? = null,
    @SerializedName("avatar"          ) var avatar        : String? = null

)
