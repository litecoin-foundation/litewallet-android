package com.breadwallet.entities

import com.breadwallet.R

class IntroLanguageResource {

    fun loadResources() : Array<IntroLanguage>{
        return arrayOf<IntroLanguage> (
            IntroLanguage(Language.ENGLISH.title, "The most secure and safest way to use Litecoin.", R.raw.english),
            IntroLanguage(Language.SPANISH.title, "La forma más segura de usar Litecoin.", R.raw.spanish),
            IntroLanguage(Language.INDONESIAN.title, "Cara paling aman dan teraman untuk menggunakan Litecoin.", R.raw.bahasaindonesia),
            IntroLanguage(Language.GERMAN.title, "Die sicherste Option zur Nutzung von Litecoin.", R.raw.deutsch),
            IntroLanguage(Language.UKRAINIAN.title, "Найбезпечніший і найбезпечніший спосіб використання Litecoin.", R.raw.ukrainian),
            IntroLanguage(Language.CHINESE_TRADITIONAL.title, "使用萊特幣最安全、最有保障的方式。", R.raw.traditionalchinese),
            IntroLanguage(Language.ITALIAN.title, "Il modo più sicuro per usare i Litecoin.", R.raw.italiano),
            IntroLanguage(Language.KOREAN.title, "Litecoin을 사용하는 가장 안정되고 안전한 방법.", R.raw.korean),
            IntroLanguage(Language.FRENCH.title, "La façon la plus sécurisée et sûre d'utiliser Litecoin.", R.raw.french),
            IntroLanguage(Language.TURKISH.title, "Litecoin'i kullanmanın en güvenli ve en güvenli yolu.", R.raw.turkish),
            IntroLanguage(Language.JAPANESE.title, "最も安全にリテコインを使う手段。", R.raw.japanese),
            IntroLanguage(Language.PORTUGUESE.title, "A forma mais protegida e segura de utilizar a Litecoin.", R.raw.portugues),
            IntroLanguage(Language.RUSSIAN.title, "Самый надежный и безопасный способ использования биткойна.", R.raw.russian)
        )
    }
}