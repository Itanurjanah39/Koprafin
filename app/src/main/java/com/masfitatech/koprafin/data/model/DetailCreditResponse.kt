package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class DetailCreditResponse (
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataDetailCredit?    = DataDetailCredit()

)
data class DataDetailCredit(

    @SerializedName("id"          ) var id         : Int?                 = null,
    @SerializedName("employee_id" ) var employeeId : Int?                 = null,
    @SerializedName("product_id"  ) var productId  : Int?                 = null,
    @SerializedName("tenor"       ) var tenor      : Int?                 = null,
    @SerializedName("total"       ) var total      : String?              = null,
    @SerializedName("status"      ) var status     : String?              = null,
    @SerializedName("created_at"  ) var createdAt  : String?              = null,
    @SerializedName("updated_at"  ) var updatedAt  : String?              = null,
    @SerializedName("product"     ) var product    : DataProduct?             = DataProduct(),
    @SerializedName("employee"    ) var employee   : Employee?            = Employee(),
    @SerializedName("histories"   ) var histories  : ArrayList<Histories> = arrayListOf()
)

data class Histories (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("credit_id"  ) var creditId  : Int?    = null,
    @SerializedName("status"     ) var status    : String? = null,
    @SerializedName("remarks"    ) var remarks   : String? = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null

)