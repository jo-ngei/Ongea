package com.ongea.models

class Message {
    var objectId: String? = null;
    var text: String? = null;
    var photo: String? = null;
    var conversationId: String? = null;
    var time: String? = null;
    var to: String? = null;
    var from: String? = null;
    var seen: Boolean? = false;

    companion object Factory {
        fun createMessage(): Message = Message()
    }
}