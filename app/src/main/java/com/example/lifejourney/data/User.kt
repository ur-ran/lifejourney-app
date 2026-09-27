package com.example.lifejournal.data


data class EditableUser(
    var name: String = "",
    var profileImageUri: String = ""
)

data class User(
    val name: String,
    val imageUri: String
)


