package me.sudodios.codewalker.utils

import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow


object Utils {

    fun File.md5() : String {
        val md = MessageDigest.getInstance("MD5")
        val result = this.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            generateSequence {
                when (val bytesRead = fis.read(buffer)) {
                    -1 -> null
                    else -> bytesRead
                }
            }.forEach { bytesRead -> md.update(buffer, 0, bytesRead) }
            md.digest().joinToString("") { "%02x".format(it) }
        }
        return result
    }

    fun String.toColorInt () : Int {
        var color: Long = this.substring(1).toLong(16)
        if (this.length == 7) { color = color or 0x00000000ff000000L } else require(this.length == 9) { "Unknown color" }
        return color.toInt()
    }

    val Float.degreeToAngle
        get() = (this * Math.PI / 180f).toFloat()

    fun Long.formatToSizeFile(): String {
        val symbols = DecimalFormatSymbols(Locale.ENGLISH)
        if (this <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#", symbols).format(this / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun Long?.formatToPrice(): String {
        val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.ENGLISH))
        return if (this == null) { "" } else { decimalFormat.format(this) }
    }

    fun Double.roundPlace (decimalPlace: Int): Double {
        return BigDecimal(this.toString()).setScale(decimalPlace, RoundingMode.HALF_UP).toDouble()
    }

    fun Float.roundPlace (decimalPlace: Int): Float {
        return if (this.isNaN()) 0f else BigDecimal(this.toString()).setScale(decimalPlace, RoundingMode.HALF_UP).toFloat()
    }

    fun Long?.toValid () : Long {
        return this ?: -1
    }

    fun Long.toTimeAgo(): String {
        val suffix = "ago"
        val dateDiff = Date().time - this
        val second = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
        val minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
        val hour = TimeUnit.MILLISECONDS.toHours(dateDiff)
        val day = TimeUnit.MILLISECONDS.toDays(dateDiff)
        val result = when {
            second < 60 -> "Moments $suffix"
            minute < 60 -> "$minute Minutes $suffix"
            hour < 24 -> "$hour Hours $suffix"
            day >= 7 && day > 360 -> (day / 360).toString() + " Years " + suffix
            day >= 7 && day > 30 -> (day / 30).toString() + " Months " + suffix
            day in 7..29 -> (day / 7).toString() + " Week " + suffix
            day < 7 -> "$day Days $suffix"
            else -> ""
        }
        return result
    }

    fun postDelayed (delay : Long,callback : () -> Unit) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                callback.invoke()
            }
        },delay)
    }

}