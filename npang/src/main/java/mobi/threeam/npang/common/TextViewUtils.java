package mobi.threeam.npang.common;

import java.text.NumberFormat;
import java.util.Locale;

import com.beardedhen.androidbootstrap.BootstrapButton;

import mobi.threeam.npang.R;
import mobi.threeam.npang.database.model.Payment;

import android.content.Context;
import android.graphics.Paint;
import android.widget.TextView;

public class TextViewUtils {
	public static void strike(TextView tv, boolean enable) {
		if (enable) {
			tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			tv.setPaintFlags(tv.getPaintFlags() &~ Paint.STRIKE_THRU_TEXT_FLAG);
		}
	}
	
	public static void currency(TextView view, double amount) {
		String val = NumberFormat.getCurrencyInstance(Locale.KOREA).format(amount);
		view.setText(val);
	}
	
	public static void attendees(BootstrapButton view, Payment payment) {
		view.setText(getAttendeesString(view.getContext(), payment));
	}

	private static String getAttendeesString(Context context, Payment payment) {
		if (payment.attendees == null || payment.attendees.size() == 0) {
			return context.getString(R.string.attendee);
		} else {
			return context.getString(R.string.attendee_count, payment.attendees.size());
		}
	}

}
