package mobi.threeam.npang.common;

import android.util.Log;

public class Logger {
	public static final String TAG = "NPang";
	public static boolean DEBUG = true;

	public static void i(Object o) {
		if (DEBUG)
			Log.i(TAG, String.valueOf(o));
	}

	public static void i(String i) {
		if (DEBUG)
			Log.i(TAG, i);
	}

	public static void d(Object o) {
		if (DEBUG)
			Log.d(TAG, String.valueOf(o));
	}

	public static void w(String w) {
		if (DEBUG)
			Log.w(TAG, w);
	}

	public static void w(Object w) {
		if (DEBUG)
			Log.w(TAG, String.valueOf(w));
	}

	public static void e(String e) {
		if (DEBUG)
			Log.e(TAG, e);
	}

	public static void e(Throwable e) {
		if (DEBUG)
			Log.e(TAG, Log.getStackTraceString(e));
	}
}
