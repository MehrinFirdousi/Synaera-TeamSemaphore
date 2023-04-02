package com.example.synaera

import java.security.MessageDigest

class Hasher {
    companion object {
        fun hash(string: String): String {
            val bytes = string.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }
                .toString()
        }
    }
}