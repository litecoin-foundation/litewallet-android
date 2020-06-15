package com.breadwallet.tools.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.breadwallet.R

/** Litewallet
 * Created by Mohamed Barry on 5/1/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */


/**
 * String extension
 */
fun String.Companion.join(array: Array<String>, separator: Char?): String {
    if (array.isEmpty()) {
        return ""
    }
    val stringBuilder = StringBuilder()
    for (i in 0 until array.size - 1) {
        stringBuilder.append(array[i])
        stringBuilder.append(separator)
    }
    stringBuilder.append(array[array.size - 1])
    return stringBuilder.toString()
}


/**
 * FragmentActivity extension
 */
fun FragmentActivity.addFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        containerId: Int = R.id.fragment_container
) {
    addFragment(this.supportFragmentManager, fragment, null, addToBackStack, containerId)
}

fun FragmentActivity.replaceFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        containerId: Int = R.id.fragment_container
) {
    replaceFragment(this.supportFragmentManager, fragment, null, addToBackStack, containerId)
}

/**
 * Fragment extension
 */
fun Fragment.addFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        containerId: Int = R.id.fragment_container,
        transition: Int? = null
) {
    addFragment(this.childFragmentManager, fragment, transition, addToBackStack, containerId)
}

private fun addFragment(fragmentManager: FragmentManager,
                        fragment: Fragment,
                        transition: Int?,
                        addToBackStack: Boolean = true,
                        containerId: Int = R.id.fragment_container) {
    val transaction = fragmentManager.beginTransaction()
    transition?.let { transaction.setTransition(it) }
    transaction.add(containerId, fragment)
    if (addToBackStack) {
        transaction.addToBackStack(fragment.tag)
    }
    transaction.commit()
}

private fun replaceFragment(fragmentManager: FragmentManager,
                            fragment: Fragment,
                            transition: Int?,
                            addToBackStack: Boolean = true,
                            containerId: Int = R.id.fragment_container) {
    val transaction = fragmentManager.beginTransaction()
    transition?.let { transaction.setTransition(it) }
    transaction.replace(containerId, fragment)
    if (addToBackStack) {
        transaction.addToBackStack(fragment.tag)
    }
    transaction.commit()
}
