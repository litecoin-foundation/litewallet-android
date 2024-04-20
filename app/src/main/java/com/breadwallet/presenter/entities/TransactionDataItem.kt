package com.breadwallet.presenter.entities

import com.platform.entities.TxMetaData

///Refactored Transaction data item. Replaces TxItem
class TransactionDataItem {

    private var timeStamp = 0L; private var sentAmount = 0L; private var receivedAmount = 0L;
    private var networkFee = 0L; private var opsFee = 0L; private var balanceAfterTransaction = 0L
    private var toAddresses = arrayOf<String>(); private var fromAddresses = arrayOf<String>()
    private var blockHeight = 0;  private var transactionSize = 0
    private var isValidTransaction = false
    private var transactionHash = ByteArray(0)
    private var outputAmounts = LongArray(0)
    private var transactionHashHexReversed: String? = null
    private var metaData: TxMetaData? = null
    private val tag: String = TransactionDataItem::class.java.name

    init {
        this.timeStamp = timeStamp
        this.sentAmount = sentAmount
        this.receivedAmount = receivedAmount
        this.networkFee = networkFee
        this.opsFee = opsFee
        this.balanceAfterTransaction = balanceAfterTransaction
        this.toAddresses = toAddresses
        this.fromAddresses = fromAddresses
        this.blockHeight = blockHeight
        this.transactionSize = transactionSize
        this.isValidTransaction = isValidTransaction
        this.transactionHash = transactionHash
        this.outputAmounts = outputAmounts
        this.transactionHashHexReversed = transactionHashHexReversed
        this.metaData = metaData
    }

    fun getTransactionBlockHeight(): Int {
        return blockHeight
    }

    fun getNetworkFee(): Long {
        return networkFee
    }

    fun getTransactionSize(): Int {
        return transactionSize
    }

    fun getFromAddresses(): Array<String> {
        return fromAddresses
    }

    fun getTransactionHash(): ByteArray {
        return transactionHash
    }

    fun getTxHashHexReversed(): String? {
        return transactionHashHexReversed
    }

    fun getReceivedAmount(): Long {
        return receivedAmount
    }

    fun getSentAmount(): Long {
        return sentAmount
    }

    fun getTimeStamp(): Long {
        return timeStamp
    }

    fun getToAddresses(): Array<String> {
        return toAddresses
    }

    fun getBalanceAfterTransaction(): Long {
        return balanceAfterTransaction
    }

    fun getOutputAmounts(): LongArray {
        return outputAmounts
    }

    fun isValidTransaction(): Boolean {
        return isValidTransaction
    }

    fun getTAG(): String {
        return tag
    }
}
