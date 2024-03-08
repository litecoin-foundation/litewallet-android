package com.breadwallet.entities

import com.breadwallet.R

class IntroLanguageResource {

    fun loadResources() : Array<IntroLanguage>{
        return arrayOf<IntroLanguage> (
            IntroLanguage(Language.ENGLISH.title, "The most secure and safest way to use Litecoin.", R.raw.english, "Are you sure you want to change the language to English?"),
            IntroLanguage(Language.SPANISH.title, "La forma más segura de usar Litecoin.", R.raw.spanish, "¿Estás seguro de que quieres cambiar el idioma a español?"),
            IntroLanguage(Language.INDONESIAN.title, "Cara paling aman dan teraman untuk menggunakan Litecoin.", R.raw.bahasaindonesia, "Yakin ingin mengubah bahasanya ke bahasa Indonesia?"),
            IntroLanguage(Language.GERMAN.title, "Die sicherste Option zur Nutzung von Litecoin.", R.raw.deutsch, "Sind Sie sicher, dass Sie die Sprache auf Deutsch ändern möchten?"),
            IntroLanguage(Language.UKRAINIAN.title, "Найбезпечніший і найбезпечніший спосіб використання Litecoin.", R.raw.ukrainian, "Ви впевнені, що хочете змінити мову на українську?"),
            IntroLanguage(Language.CHINESE_TRADITIONAL.title, "使用萊特幣最安全、最有保障的方式。", R.raw.traditionalchinese, "您確定要將語言改為中文嗎？"),
            IntroLanguage(Language.ITALIAN.title, "Il modo più sicuro per usare i Litecoin.", R.raw.italiano, "Sei sicuro di voler cambiare la lingua in italiano?"),
            IntroLanguage(Language.KOREAN.title, "Litecoin을 사용하는 가장 안정되고 안전한 방법.", R.raw.korean, "언어를 한국어로 변경하시겠습니까?"),
            IntroLanguage(Language.FRENCH.title, "La façon la plus sécurisée et sûre d'utiliser Litecoin.", R.raw.french, "Êtes-vous sûr de vouloir changer la langue en français ?"),
            IntroLanguage(Language.TURKISH.title, "Litecoin'i kullanmanın en güvenli ve en güvenli yolu.", R.raw.turkish, "Dili türkçeye değiştirmek istediğinizden emin misiniz?"),
            IntroLanguage(Language.JAPANESE.title, "最も安全にリテコインを使う手段。", R.raw.japanese, "言語を日本語に変更してもよろしいですか?"),
            IntroLanguage(Language.PORTUGUESE.title, "A forma mais protegida e segura de utilizar a Litecoin.", R.raw.portugues, "Tem certeza de que deseja alterar o idioma para português?"),
            IntroLanguage(Language.RUSSIAN.title, "Самый надежный и безопасный способ использования биткойна.", R.raw.russian, "Вы уверены, что хотите сменить язык на русский?")
        )
    }
}