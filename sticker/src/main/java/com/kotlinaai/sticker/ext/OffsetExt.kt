package com.kotlinaai.sticker.ext

import androidx.compose.ui.geometry.Offset
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * @Author:         chenp
 * @CreateDate:     2024/9/20 11:38
 * @UpdateUser:     chenp
 * @UpdateDate:     2024/9/20 11:38
 * @Version:        1.0
 * @Description:
 */
fun Offset.angleTo(other: Offset): Float {
    val dotProduct = x * other.x + y * other.y
    val magnitudeA = getDistance()
    val magnitudeB = other.getDistance()

    // Calculate the cosine of the angle
    val cosTheta = (dotProduct / (magnitudeA * magnitudeB)).coerceIn(-1f, 1f)

    // Calculate the angle in radians and then convert to degrees
    val angleRadians = acos(cosTheta)
    val angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()

    // Determine the direction using the cross product
    val crossProduct = x * other.y - y * other.x

    return if (crossProduct >= 0) angleDegrees else -angleDegrees
}

fun Offset.rotate(degree: Float): Offset {
    val radians = Math.toRadians(degree.toDouble())
    val cosTheta = cos(radians)
    val sinTheta = sin(radians)

    val newX = x * cosTheta - y * sinTheta
    val newY = x * sinTheta + y * cosTheta

    return Offset(newX.toFloat(), newY.toFloat())
}