package com.example.africanschools.data.model

data class UserProfile(
    val userId: String,
    val email: String,
    val cv: String? = null
)
