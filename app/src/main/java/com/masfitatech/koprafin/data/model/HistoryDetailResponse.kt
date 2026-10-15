package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName

data class HistoryDetailResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataHistoryDetail?    = DataHistoryDetail()
)
data class DataHistoryDetail(
    @SerializedName("id"          ) var id          : Int?                 = null,
    @SerializedName("employee_id" ) var employeeId  : Int?                 = null,
    @SerializedName("product_id"  ) var productId   : Int?                 = null,
    @SerializedName("tenor"       ) var tenor       : Int?                 = null,
    @SerializedName("total"       ) var total       : String?              = null,
    @SerializedName("status"      ) var status      : String?              = null,
    @SerializedName("created_at"  ) var createdAt   : String?              = null,
    @SerializedName("updated_at"  ) var updatedAt   : String?              = null,
    @SerializedName("installment" ) var installment : String?              = null,
    @SerializedName("product"     ) var product     : Product?             = Product(),
    @SerializedName("employee"    ) var employee    : Employee?            = Employee(),
    @SerializedName("histories"   ) var histories   : ArrayList<Histories> = arrayListOf()
)

data class Product (

    @SerializedName("id"          ) var id          : Int?              = null,
    @SerializedName("category_id" ) var categoryId  : Int?              = null,
    @SerializedName("name"        ) var name        : String?           = null,
    @SerializedName("price"       ) var price       : Int?              = null,
    @SerializedName("description" ) var description : String?           = null,
    @SerializedName("picture"     ) var picture     : ArrayList<String> = arrayListOf(),
    @SerializedName("category"    ) var category    : Category?         = Category()

)
