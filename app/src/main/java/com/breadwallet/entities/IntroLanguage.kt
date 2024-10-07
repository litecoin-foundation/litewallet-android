package com.breadwallet.entities

data class IntroLanguage(
    val code: String,
    val name : String,
    val desc : String,
    val audio : Int,
    val message: String,
    val lang: Language
)
