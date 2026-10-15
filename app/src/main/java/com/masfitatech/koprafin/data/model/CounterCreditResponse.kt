package com.masfitatech.koprafin.data.model

import com.google.gson.annotations.SerializedName


data class CounterCreditResponse (

    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : DataCounterCredit?    = DataCounterCredit()

)
data class DataCounterCredit (

    @SerializedName("pengajuan" ) var pengajuan : Int? = null,
    @SerializedName("berjalan"  ) var berjalan  : Int? = null,
    @SerializedName("selesai"   ) var selesai   : Int? = null,
    @SerializedName("ditolak"   ) var ditolak   : Int? = null

)
