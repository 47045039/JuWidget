/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ju.widget.impl.launcher3.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.inputmethod.InputMethodManager;

import com.ju.widget.util.Tools;

/**
 * Utility class for offloading some class from UI thread
 */
public class UiThreadHelper {

    private static final int MSG_HIDE_KEYBOARD = 1;

    private static Handler getHandler(Context context) {
        return new Handler(Tools.getWorkLooper(), new UiCallbacks(context));
    }

    public static void hideKeyboardAsync(Context context, IBinder token) {
        Message.obtain(getHandler(context), MSG_HIDE_KEYBOARD, token).sendToTarget();
    }

    private static class UiCallbacks implements Handler.Callback {

        private final InputMethodManager mIMM;

        UiCallbacks(Context context) {
            mIMM = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_HIDE_KEYBOARD:
                    mIMM.hideSoftInputFromWindow((IBinder) message.obj, 0);
                    return true;
            }
            return false;
        }
    }
}
