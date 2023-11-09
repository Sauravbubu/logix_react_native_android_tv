package com.logituit.player.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CatchUpData(

    @Json(name = "video_id")
    val id: String,

    @Json(name = "title")
    val name: String,

    @Json(name = "duration_secs")
    val duration: Int,

    @Json(name = "thumbnail_url")
    val thumbnail_url: String,

    @Json(name = "video_url")
    val video_url: String,
) : Parcelable