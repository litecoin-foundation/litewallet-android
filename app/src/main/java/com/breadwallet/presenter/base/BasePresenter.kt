package com.breadwallet.presenter.base


/** Litewallet
 * Created by Mohamed Barry on 6/30/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
abstract class BasePresenter<out V : BaseView>(var view: BaseView?) {


    init {
        inject()
    }

    private fun inject() {
    }

    abstract fun subscribe()
    abstract fun unsubscribe()

    fun detach() {
        view = null
    }

    fun isAttached() = view != null
}
