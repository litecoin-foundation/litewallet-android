package com.breadwallet.entities

class CountryLangResource {
    fun loadResources() : List<CountryLang> {
        return listOf<CountryLang>(
            CountryLang(Language.ENGLISH.title),
            CountryLang(Language.FRENCH.title),
            CountryLang(Language.CHINESE_TRADITIONAL.title),
            CountryLang(Language.INDONESIAN.title),
            CountryLang(Language.UKRAINIAN.title),
            CountryLang(Language.CHINESE_TRADITIONAL.title),
            CountryLang(Language.GERMAN.title),
            CountryLang(Language.TURKISH.title),
            CountryLang(Language.SPANISH.title),
            CountryLang(Language.ITALIAN.title),
            CountryLang(Language.JAPANESE.title),
            CountryLang(Language.RUSSIAN.title),
            CountryLang(Language.PORTUGUESE.title)
        )
    }
}