package com.logituit.player.common

fun textValue(yourLongText:String):String{

    val maxCharacters = 75 // Maximum characters to display
    val truncatedText = if (yourLongText.length > maxCharacters) {
        val truncated = yourLongText.substring(0, maxCharacters)
        val lastSpaceIndex = truncated.lastIndexOf(' ')
        if (lastSpaceIndex > -1) {
            truncated.substring(0, lastSpaceIndex)+" ...."
        } else {
             "$truncated ...."
        }
    } else {
        yourLongText
    }
    return truncatedText

}

 fun getFormattedDuration(duration: Int): String {
    var hours = (duration / 60) / 60
    var minutes = (duration / 60) % 60
    val seconds = duration % 60
    if (seconds in 30..59 &&  minutes < 60 || duration < 30) {
        minutes++
    }
    if ((duration / 60) % 60 == 59 && seconds >= 45) {
        hours++
    }
    val durationString = if( hours == 0)
        String.format("%2d ${getMinString(minutes)}", minutes)
    else{
        if (minutes == 0 || minutes == 60)
            String.format("%2d ${getHrString(hours)} ", hours)
        else
            String.format("%2d hrs ", hours)+ String.format("%2d ${getMinString(minutes)}", minutes)
    }
    return durationString
}

fun getMinString(minutes:Int):String{
    return if (minutes == 1) "min" else "mins"
}
fun getHrString(hour:Int):String{
    return if (hour == 1) "hr" else "hrs"
}