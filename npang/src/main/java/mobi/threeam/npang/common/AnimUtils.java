package mobi.threeam.npang.common;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

public class AnimUtils {
	public static void slideIn(final View view) {
		ViewHelper.setX(view, -view.getWidth());
		ViewHelper.setAlpha(view, 0);
		view.animate().translationX(0).alpha(1).setStartDelay(100);
	}
	
	public static void dropDown(final View view) {
		ViewHelper.setY(view, -100);
		ViewHelper.setAlpha(view, 0);
		ViewHelper.setScaleX(view, 0);
		view.animate().translationY(0).alpha(1).scaleX(1);
	}
}
