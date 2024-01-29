package com.breadwallet.presenter.entities

import com.breadwallet.tools.security.BRKeyStore
import org.json.JSONObject
import java.util.regex.Pattern
import java.io.File

enum class PartnerNames(val key: String) {
    MOONPAY("moonpay"),
    BITREFILL("bitrefill"),
    INFURA("infura-api"),
    LITEWALLETOPS("litewallet-ops"),
    LITEWALLETSTART("litewallet-start"),
    PUSHER("pusher-instance-id"),
    PUSHERSTAGING("pusher-staging-instance-id")
}