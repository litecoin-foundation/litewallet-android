package com.breadwallet.entities

import com.breadwallet.R

class IntroLanguageResource {
    private val USD: String = "USD"
    private val EUR: String = "EUR"
    private val RMB: String = "RMB"
    private val JPY: String = "JPY"
    fun loadResources(): Array<IntroLanguage> {
        return arrayOf<IntroLanguage>(
            IntroLanguage(
                Language.ENGLISH.code,
                Language.ENGLISH.title,
                "The most secure and safest way to use Litecoin.",
                R.raw.english,
                "Are you sure you want to change the language to English?",
                Language.ENGLISH
            ),
            IntroLanguage(
                Language.SPANISH.code,
                Language.SPANISH.title,
                "La forma más segura de usar Litecoin.",
                R.raw.spanish,
                "¿Estás seguro de que quieres cambiar el idioma a español?",
                Language.SPANISH,
            ),
            IntroLanguage(
                Language.INDONESIAN.code,
                Language.INDONESIAN.title,
                "Cara paling aman dan teraman untuk menggunakan Litecoin.",
                R.raw.bahasaindonesia,
                "Yakin ingin mengubah bahasanya ke bahasa Indonesia?",
                Language.INDONESIAN
            ),
            IntroLanguage(
                Language.GERMAN.code,
                Language.GERMAN.title,
                "Die sicherste Option zur Nutzung von Litecoin.",
                R.raw.deutsch,
                "Sind Sie sicher, dass Sie die Sprache auf Deutsch ändern möchten?",
                Language.GERMAN
            ),
            IntroLanguage(
                Language.UKRAINIAN.code,
                Language.UKRAINIAN.title,
                "Найбезпечніший і найбезпечніший спосіб використання Litecoin.",
                R.raw.ukrainian,
                "Ви впевнені, що хочете змінити мову на українську?",
                Language.UKRAINIAN
            ),
            IntroLanguage(
                Language.CHINESE_TRADITIONAL.code,
                Language.CHINESE_TRADITIONAL.title,
                "使用萊特幣最安全、最有保障的方式。",
                R.raw.traditionalchinese,
                "您確定要將語言改為中文嗎？",
                Language.CHINESE_TRADITIONAL
            ),
            IntroLanguage(
                Language.ITALIAN.code,
                Language.ITALIAN.title,
                "Il modo più sicuro per usare i Litecoin.",
                R.raw.italiano,
                "Sei sicuro di voler cambiare la lingua in italiano?",
                Language.ITALIAN
            ),
            IntroLanguage(
                Language.KOREAN.code,
                Language.KOREAN.title,
                "Litecoin을 사용하는 가장 안정되고 안전한 방법.",
                R.raw.korean,
                "언어를 한국어로 변경하시겠습니까?",
                Language.KOREAN
            ),
            IntroLanguage(
                Language.FRENCH.code,
                Language.FRENCH.title,
                "La façon la plus sécurisée et sûre d'utiliser Litecoin.",
                R.raw.french,
                "Êtes-vous sûr de vouloir changer la langue en français ?",
                Language.FRENCH
            ),
            IntroLanguage(
                Language.TURKISH.code,
                Language.TURKISH.title,
                "Litecoin'i kullanmanın en güvenli ve en güvenli yolu.",
                R.raw.turkish,
                "Dili türkçeye değiştirmek istediğinizden emin misiniz?",
                Language.TURKISH
            ),
            IntroLanguage(
                Language.JAPANESE.code,
                Language.JAPANESE.title,
                "最も安全にリテコインを使う手段。",
                R.raw.japanese,
                "言語を日本語に変更してもよろしいですか?",
                Language.JAPANESE
            ),
            IntroLanguage(
                Language.PORTUGUESE.code,
                Language.PORTUGUESE.title,
                "A forma mais protegida e segura de utilizar a Litecoin.",
                R.raw.portugues,
                "Tem certeza de que deseja alterar o idioma para português?",
                Language.PORTUGUESE
            ),
            IntroLanguage(
                Language.PORTUGUESE.code,
                Language.RUSSIAN.title,
                "Самый надежный и безопасный способ использования биткойна.",
                R.raw.russian,
                "Вы уверены, что хотите сменить язык на русский?",
                Language.RUSSIAN
            ),
            IntroLanguage(
                Language.ARABIC.code,
                Language.ARABIC.title,
                "حدد مناقشة الطريقة الأكثر أمانًا وأمانًا لاستخدام Litecoin.",
                R.raw.arabic,
                "هل أنت متأكد أنك تريد تغيير اللغة إلى الإندونيسية؟",
                Language.ARABIC
            )
        )
    }

    fun findLanguageIndex(language: Language): Int {
        return loadResources().map { intro -> intro.lang }.indexOf(language)
    }
}
