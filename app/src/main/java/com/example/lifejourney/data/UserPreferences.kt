package com.example.lifejournal.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putString("user_name", user.name)
            putString("user_profile_image_uri", user.imageUri) // fix di sini
            apply()
        }
    }

    fun getUser(): User {
        val name = sharedPreferences.getString("user_name", "") ?: ""
        val profileImageUri = sharedPreferences.getString("user_profile_image_uri", "") ?: ""
        return User(name, profileImageUri) // ini aman, karena User menerima imageUri
    }
}
