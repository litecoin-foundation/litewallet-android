package com.platform.middlewares;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.breadwallet.BreadApp;
import com.platform.APIClient;
import com.platform.BRHTTPHelper;
import com.platform.HTTPServer;
import com.platform.interfaces.Middleware;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import timber.log.Timber;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 10/19/16.
 * Copyright (c) 2016 breadwallet LLC
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
public class HTTPIndexMiddleware implements Middleware {

    @Override
    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        Timber.d("handling: " + target + " " + baseRequest.getMethod());
        Context app = BreadApp.getBreadContext();
        if (app == null) {
            Timber.i("handle: app is null!");
            return true;
        }

        String indexFile = APIClient.getInstance(app).getExtractedPath(app, rTrim(target, "/") + "/index.html");

        File temp = new File(indexFile);
        if (!temp.exists()) {
            return false;
        }

        try {
            byte[] body = FileUtils.readFileToByteArray(temp);
            Assert.assertNotNull(body);
            Assert.assertNotSame(body.length, 0);
            response.setHeader("Content-Length", String.valueOf(body.length));
            return BRHTTPHelper.handleSuccess(200, body, baseRequest, response, "text/html;charset=utf-8");
        } catch (IOException e) {
            Timber.e(e, "handle: error sending response: ");
            return BRHTTPHelper.handleError(500, null, baseRequest, response);
        }
    }

    public String rTrim(String str, String piece) {
        if (str.endsWith(piece)) {
            return str.substring(str.lastIndexOf(piece), str.length());
        }
        return str;
    }
}
