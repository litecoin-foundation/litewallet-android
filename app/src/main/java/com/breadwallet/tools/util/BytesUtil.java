package com.breadwallet.tools.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import timber.log.Timber;

public class BytesUtil {

    public static byte[] readBytesFromStream(InputStream in) {

        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        try {
            while ((len = in.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            try {
                byteBuffer.close();
            } catch (IOException e) {
                Timber.e(e);
            }
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                Timber.e(e);
            }
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();

    }
}
