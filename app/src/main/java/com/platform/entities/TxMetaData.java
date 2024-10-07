package com.platform.entities;

public class TxMetaData {

    /**
     * Key: “txn-<txHash>”
     * <p>
     * {
     * “classVersion”: 5, //used for versioning the schema
     * “bh”: 47583, //blockheight
     * “er”: 2800.1, //exchange rate
     * “erc”: “USD”, //exchange currency
     * “fr”: 300, //fee rate
     * “s”: fd, //size
     * “c”: 123475859 //created
     * “dId”: ”<UUID>” //DeviceId - This is a UUID that gets generated and then persisted so it can get sent with every tx
     * “comment”: “Vodka for Mihail”
     * }
     */

    public String deviceId;
    public String comment;
    public String exchangeCurrency;
    public int classVersion;
    public int blockHeight;
    public double exchangeRate;
    public long fee;
    public int txSize;
    public int creationTime;

}
