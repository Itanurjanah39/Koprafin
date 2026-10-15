package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class EmployeeResponse(

    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : PaginationEmployee?    = PaginationEmployee()
)
data class PaginationEmployee (
    @SerializedName("current_page"   ) var currentPage  : Int?             = null,
    @SerializedName("data"           ) var data         : ArrayList<DataEmployee>  = arrayListOf(),
    @SerializedName("first_page_url" ) var firstPageUrl : String?          = null,
    @SerializedName("from"           ) var from         : Int?             = null,
    @SerializedName("last_page"      ) var lastPage     : Int?             = null,
    @SerializedName("last_page_url"  ) var lastPageUrl  : String?          = null,
    @SerializedName("links"          ) var links        : ArrayList<Links> = arrayListOf(),
    @SerializedName("next_page_url"  ) var nextPageUrl  : String?          = null,
    @SerializedName("path"           ) var path         : String?          = null,
    @SerializedName("per_page"       ) var perPage      : Int?             = null,
    @SerializedName("prev_page_url"  ) var prevPageUrl  : String?          = null,
    @SerializedName("to"             ) var to           : Int?             = null,
    @SerializedName("total"          ) var total        : Int?             = null
        )
data class DataEmployee(
    @SerializedName("id"              ) var id            : Int?    = null,
    @SerializedName("name"            ) var name          : String? = null,
    @SerializedName("nik"             ) var nik           : String? = null,
    @SerializedName("phone"           ) var phone         : String? = null,
    @SerializedName("role"            ) var role          : String? = null,
    @SerializedName("company"         ) var company       : String? = null,
    @SerializedName("division"        ) var division      : String? = null,
    @SerializedName("work_entry_date" ) var workEntryDate : String? = null,
    @SerializedName("avatar"          ) var avatar        : String? = null,
    @SerializedName("user"            ) var user          : User?   = User()
)

data class User (

    @SerializedName("id"          ) var id         : Int?    = null,
    @SerializedName("employee_id" ) var employeeId : Int?    = null,
    @SerializedName("email"       ) var email      : String? = null,
    @SerializedName("created_at"  ) var createdAt  : String? = null

)
