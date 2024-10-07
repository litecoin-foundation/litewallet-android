package com.platform.interfaces;

import com.platform.kvstore.CompletionObject;

public interface KVStoreAdaptor {

    public CompletionObject ver(String key);

    public CompletionObject put(String key, byte[] value, long version);

    public CompletionObject del(String key, long version);

    public CompletionObject get(String key, long version);

    public CompletionObject keys();

}
