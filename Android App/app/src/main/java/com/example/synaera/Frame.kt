package com.example.synaera

import java.nio.ByteBuffer

data class Frame(
    val byteBuffer: ByteBuffer,
    val width: Int,
    val height: Int,
    val position: Int,
    val timestamp: Long,
    val rotation:Int,
    val isFlipX: Boolean,
    val isFlipY: Boolean
)
