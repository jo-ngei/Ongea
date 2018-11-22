package com.ongea.models

class Room {
    var to: String? = null
    var from: String? = null
    var time: String? = null
    var conversationId: String? = null
    var messageId: String? = null
    var isSeen: Boolean? = false
    var isBlocked: Boolean? = false

    companion object {
        fun createRoom(): Room = Room();
    }

}