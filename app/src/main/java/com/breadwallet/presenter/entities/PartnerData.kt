package com.breadwallet.presenter.entities

import android.os.Bundle
import com.breadwallet.tools.manager.AnalyticsManager
import com.breadwallet.tools.util.BRConstants
import org.json.JSONObject
import java.io.File

class PartnerData {

/// Returns Partner Key
/// - Parameter name: Enum for the different partners
/// - Returns: Key string
//  static func partnerKeyPath(name: PartnerNames) -> String {
    fun partnerKeyPath(name: PartnerNames): String {

        val jsonString: String = File("./app/partner-keys.json").readText(Charsets.UTF_8)

        val jsonObject = JSONObject(jsonString)

        when (name) {
            PartnerNames.INFURA -> {
                return jsonObject.getString("infura-api")
            }

            PartnerNames.LITEWALLETOPS -> {
                return jsonObject.getString("litewallet-ops")
            }

            PartnerNames.LITEWALLETSTART -> {
                return jsonObject.getString("litewallet-start")
            }

            PartnerNames.PUSHER -> {
                return jsonObject.getString("pusher-instance-id")
            }

            PartnerNames.PUSHERSTAGING -> {
                return jsonObject.getString("pusher-staging-instance-id")
            }

            else -> {
                val params = Bundle()
                params.putString("error", "Partner Data key not found")
                AnalyticsManager.logCustomEventWithParams(BRConstants._20200112_ERR, params)
                return ""
            }
        }
    }
}