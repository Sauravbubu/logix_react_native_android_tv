package com.logituit.player.model

data class ExportDetail(
    val connection_name: String,
    val connection_type: String,
    val status: String,
    val uid: String,
    val url: String
)