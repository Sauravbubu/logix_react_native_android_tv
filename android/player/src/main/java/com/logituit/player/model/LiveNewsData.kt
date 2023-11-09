package com.logituit.player.model

import com.google.gson.annotations.SerializedName

data class LiveNewsData(
    @SerializedName("category") var category: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("thumbnametitle") var thumbnametitle: String? = null,
    @SerializedName("thumbnail_urls") var thumbnailUrls: ArrayList<ThumbnailUrls> = arrayListOf(),
    @SerializedName("poster1920x1080_url") var poster1920x1080Url: String? = null,
    @SerializedName("poster800x450_url") var poster800x450Url: String? = null,
    @SerializedName("video_url") var videoUrl: String? = null
    )