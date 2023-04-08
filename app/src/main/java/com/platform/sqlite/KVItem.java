package com.platform.sqlite;

import com.platform.kvstore.CompletionObject;

import timber.log.Timber;

public class KVItem {
    public long version;
    public long remoteVersion;
    public String key;
    public byte[] value;
    public long time;
    public int deleted;
    public CompletionObject.RemoteKVStoreError err;

    public KVItem(long version, long remoteVersion, String key, byte[] value, long time, int deleted, CompletionObject.RemoteKVStoreError err) {
        this.version = version;
        this.remoteVersion = remoteVersion;
        this.key = key;
        this.value = value;
        this.time = time;
        this.deleted = deleted;
        this.err = err;
    }

    public KVItem(long version, long remoteVersion, String key, byte[] value, long time, int deleted) {
        this.version = version;
        this.remoteVersion = remoteVersion;
        this.key = key;
        this.value = value;
        this.time = time;
        this.deleted = deleted;
    }

    public void printValues() {
        Timber.d("timber: KVItem values: \nversion: %s\nremoteVersion: %s\nkey: %s\nvalue.length: %s\ntime: %s\ndeleted: %s"
                , version
                , remoteVersion
                , key
                , value.length
                , time
                , deleted);
    }

    private KVItem() {
    }

}
