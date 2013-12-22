package mobi.threeam.npang.common;

import mobi.threeam.npang.NPangApp;
import android.widget.Toast;

public class Notifier {

	public static void toast(int resId) {
		String msg = NPangApp.get().getString(resId);
		toast(msg);
	}

	public static void toast(String msg) {
		Toast.makeText(NPangApp.get(), msg, Toast.LENGTH_SHORT).show();
	}

}