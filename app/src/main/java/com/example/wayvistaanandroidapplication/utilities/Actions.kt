package com.example.wayvistaanandroidapplication.utilities

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlin.math.log

class Actions : ViewModel() {
    val loginData by lazy { MutableLiveData<List<UserDetails>>() }
    val locationData by lazy { MutableLiveData<List<MapLocation>>() }

    val userData by lazy { MutableLiveData<List<UserDetails>>() }
    val userId by lazy { MutableLiveData<Int>() }

    val config = RealmConfiguration.create(schema = setOf(UserDetails::class, MapLocation::class))
    val realm = Realm.open(config)


    fun signIn(userEmail: String, userPassword: String) {
        val results: RealmResults<UserDetails> =
            realm.query<UserDetails>("email == $0 AND password == $1", userEmail, userPassword)
                .find()
        loginData.value = realm.copyFromRealm(results)
    }

    fun signUp(imageData: String?, userName: String, userEmail: String, userPassword: String) {
        val resultItems: RealmResults<UserDetails> =
            realm.query<UserDetails>().sort("id", Sort.DESCENDING).limit(1).find()
        if (resultItems.size == 0) {
            userId.value = 1
        } else {
            userId.value = resultItems[0].id + 1
        }

        realm.writeBlocking {
            copyToRealm(UserDetails().apply {
                id = userId.value!!
                image = imageData?.let { Base64.decode(it, Base64.DEFAULT) }
                name = userName
                email = userEmail
                password = userPassword
                isLoggedIn = true
            })
        }
    }

    fun loginUserDet() {
        val resultItems: RealmResults<UserDetails> =
            realm.query<UserDetails>("isLoggedIn == $0", false).find()
        userData.value = realm.copyFromRealm(resultItems)
    }

    fun updateStatus(id: Int, loggedInStatus: Boolean) {
        realm.writeBlocking {
            val resultItems: RealmResults<UserDetails> =
                this.query<UserDetails>("id == $0", id).find()
            if (resultItems.isNotEmpty()) {
                resultItems[0].isLoggedIn = loggedInStatus
            }
        }
    }

    fun createLocation(
        user: Int,
        lat: Double,
        long: Double,
        timeStamp: String,
        place: String,
        district: String,
        state: String
    ) {
        realm.writeBlocking {
            copyToRealm(MapLocation().apply {
                userId = user
                latitude = lat
                longitude = long
                time = timeStamp
                places = place
                districts = district
                states = state

            })
        }
    }
    fun getLocation(userId: Int) {
        val resultItems: RealmResults<MapLocation> =
            realm.query<MapLocation>("userId == $0", userId).sort("time", sortOrder = Sort.DESCENDING)
                .find()
        locationData.value = realm.copyFromRealm(resultItems)
    }
}