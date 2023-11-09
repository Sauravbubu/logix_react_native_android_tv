package com.logituit.player.model

import com.google.gson.annotations.SerializedName

data class ThumbnailUrls (

    @SerializedName("img1" ) var img1 : String? = null,
    @SerializedName("img2" ) var img2 : String? = null,
    @SerializedName("img3" ) var img3 : String? = null

)