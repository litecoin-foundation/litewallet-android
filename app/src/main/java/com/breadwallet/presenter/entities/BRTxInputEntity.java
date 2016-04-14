package com.breadwallet.presenter.entities;

import java.io.Serializable;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on 9/25/15.
 * Copyright (c) 2016 breadwallet llc <mihail@breadwallet.com>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
class BRTxInputEntity implements Serializable {

    private int id;
    private int index;
    private byte[] prevOutTxHash;
    private int prevOutIndex;
    private int sequence;
    private byte[] signatures = new byte[32];
    private byte[] txHash = new byte[32];

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPrevOutIndex() {
        return prevOutIndex;
    }

    public void setPrevOutIndex(int prevOutIndex) {
        this.prevOutIndex = prevOutIndex;
    }

    public byte[] getPrevOutTxHash() {
        return prevOutTxHash;
    }

    public void setPrevOutTxHash(byte[] prevOutTxHash) {
        this.prevOutTxHash = prevOutTxHash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public byte[] getSignatures() {
        return signatures;
    }

    public void setSignatures(byte[] signatures) {
        this.signatures = signatures;
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }
}
