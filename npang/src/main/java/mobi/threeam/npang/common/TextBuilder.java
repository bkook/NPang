package mobi.threeam.npang.common;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import android.content.Context;
import android.text.TextUtils;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;

@EBean
public class TextBuilder {
	@RootContext
	Context context;

	PaymentGroup group;
	
	public void setGroup(PaymentGroup group) {
		this.group = group;
	}

	public String getPlace(int index) {
		Payment payment = null;
		int i=0;
		for (Payment item: group.payments) {
			if (i == index) {
				payment = item;
				break;
			}
			i++;
		}
		if (TextUtils.isEmpty(payment.place)) {
			return "";
		}
		return payment.place;
	}
}
