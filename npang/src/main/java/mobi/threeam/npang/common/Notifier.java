package mobi.threeam.npang.common;

import android.widget.Toast;

import mobi.threeam.npang.NPangApp;

public class Notifier {

	public static void toast(int resId) {
		String msg = NPangApp.get().getString(resId);
		toast(msg);
	}

	public static void toast(String msg) {
		Toast.makeText(NPangApp.get(), msg, Toast.LENGTH_SHORT).show();
	}

    public static void notiBar() {

    }

    public static void clearNotiBar() {

    }

}