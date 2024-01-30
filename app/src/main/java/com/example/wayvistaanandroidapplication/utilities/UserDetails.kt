package com.example.wayvistaanandroidapplication.utilities

import io.realm.annotations.PrimaryKey
import io.realm.kotlin.types.RealmObject

open class UserDetails() : RealmObject {
    @PrimaryKey
    var id: Int = 1
    var image: ByteArray? = null
    var name: String = ""
    var email: String = ""
    var password: String = ""
    var isLoggedIn: Boolean = false
}
