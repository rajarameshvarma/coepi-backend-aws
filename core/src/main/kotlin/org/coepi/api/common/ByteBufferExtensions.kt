package org.coepi.api.common

import java.nio.ByteBuffer
import java.util.*

private val base64Decoder = Base64.getDecoder()

fun ByteBuffer.base64Decode(): ByteBuffer = base64Decoder.decode(this)

fun ByteBuffer.decodeToString(): String = String(array())

fun String.toByteBuffer(): ByteBuffer = ByteBuffer.wrap(toByteArray())

fun ByteArray.toByteBuffer(): ByteBuffer = ByteBuffer.wrap(this)

fun ByteArray?.orEmpty(): ByteArray = this ?: byteArrayOf()