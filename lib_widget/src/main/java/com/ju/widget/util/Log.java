package com.ju.widget.util;

public class Log {

    private static boolean isDebug = true;

    public static final void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static final void e(String tag, Object... args) {
        android.util.Log.e(tag, generateMessage(args));
    }

    public static final void w(String tag, Object... args) {
        android.util.Log.w(tag, generateMessage(args));
    }

    public static final void i(String tag, Object... args) {
        android.util.Log.i(tag, generateMessage(args));
    }

    public static final void d(String tag, Object... args) {
        if (isDebug) {
            android.util.Log.i(tag, generateMessage(args));
        }
    }

    public static final void v(String tag, Object... args) {
        if (isDebug) {
            android.util.Log.v(tag, generateMessage(args));
        }
    }

    private static final String generateMessage(Object... args) {
        final StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg).append(" ");
        }
        return sb.toString();
    }

}
