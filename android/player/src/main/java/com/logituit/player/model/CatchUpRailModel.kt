package com.logituit.player.model

data class CatchUpRailModel(
    val audio_url: List<Any>,
    val categories: List<String>,
    val category: String,
    val category_ids: List<String>,
    val custom_data: CustomData,
    val duration: String,
    val duration_secs: Int,
    val export_details: List<ExportDetail>,
    val introEnd: Int,
    val introStart: Int,
    val isLIve: Boolean,
    val last_udpated_time: String,
    val media_type: String,
    val portal_ids: List<String>,
    val portals: List<String>,
    val publish_time: String,
    val status: String,
    val tags: List<String>,
    val thumbnail_url: String,
    val thumbnail_urls: List<ThumbnailUrl>,
    val upload_time: String,
    val video_id: String,
    val video_url: String,
    val views: Int
)