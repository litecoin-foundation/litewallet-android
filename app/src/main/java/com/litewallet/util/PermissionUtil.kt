package com.litewallet.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object PermissionUtil {

    @JvmStatic
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @JvmStatic
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun requestPermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) = launcher.launch(permissions)

    @JvmStatic
    fun requestPermission(
        launcher: ActivityResultLauncher<String>,
        permission: String
    ) = launcher.launch(permission)

}