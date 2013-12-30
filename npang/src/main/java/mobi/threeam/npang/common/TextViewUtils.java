package mobi.threeam.npang.common;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import mobi.threeam.npang.R;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;

public class TextViewUtils {
	public static void strike(TextView tv, boolean enable) {
		if (enable) {
			tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			tv.setPaintFlags(tv.getPaintFlags() &~ Paint.STRIKE_THRU_TEXT_FLAG);
		}
	}

    public static String title(PaymentGroup paymentGroup) {
        Logger.i("paymentGroupTt0 " + paymentGroup);
        Logger.i("paymentGroupTt1 " + paymentGroup.title);
        Logger.i("paymentGroupTt2 " + paymentGroup.createdAt);
        return TextUtils.isEmpty(paymentGroup.title) ? TimeUtils.buildTitle(paymentGroup.createdAt) : paymentGroup.title;
    }

	public static void currency(TextView view, double amount) {
        String val = CurrencyUtils.format(amount);
		view.setText(val);
	}
	
	public static void attendees(BootstrapButton view, Payment payment) {
		view.setText(getAttendeesString(view.getContext(), payment));
	}

    public static void place(TextView view, int index, String place) {
        if (TextUtils.isEmpty(place)) {
            view.setText(view.getContext().getString(R.string.payment_title_hint, index+1));
        } else {
            view.setText(place);
        }
    }

	private static String getAttendeesString(Context context, Payment payment) {
		if (payment.attendees == null || payment.attendees.size() == 0) {
			return context.getString(R.string.attendee);
		} else {
			return context.getString(R.string.attendee_count, payment.attendees.size());
		}
	}

}
