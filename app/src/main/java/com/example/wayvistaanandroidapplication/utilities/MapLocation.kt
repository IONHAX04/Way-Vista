package com.example.wayvistaanandroidapplication.utilities

import io.realm.kotlin.types.RealmObject

open class MapLocation() : RealmObject {
    var userId: Int = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var time: String = ""
    var places: String = ""
    var districts: String = ""
    var states: String = ""
}