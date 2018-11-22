package com.ongea.models

class User {
    var username: String? = null
    var first_name: String? = null
    var second_name: String? = null
    var user_id: String? = null
    var email: String? = null
    var device_id: String? = null
    var profile_image: String? = null
    var profile_cover: String? = null
    var bio: String? = null
    var age: String? = null
    var phone_number: String? = null
    var gender: String? = null
    var location: String?= null
    var isBlocked: Boolean? = false

    companion object {
        fun createUser(): User = User();
    }
}