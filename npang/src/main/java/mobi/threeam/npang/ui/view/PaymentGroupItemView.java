package mobi.threeam.npang.ui.view;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.common.TimeUtils;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import android.content.Context;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_payment_group)
public class PaymentGroupItemView extends RelativeLayout {

	@ViewById
	TextView title;
	
	@ViewById
	TextView amount;

	@ViewById
	TextView date;

	public PaymentGroupItemView(Context context) {
		super(context);
	}

	public void bind(PaymentGroup group) {
		if (TextUtils.isEmpty(group.title)) {
			title.setText(TimeUtils.buildTitle(group.createdAt));
		} else {
			title.setText(group.title);
		}
		
		group.totalAmount = 0;
		for (Payment payment : group.payments) {
			group.totalAmount += payment.amount;
		}

		TextViewUtils.currency(amount, group.totalAmount);
		date.setText(TimeUtils.format(group.createdAt));
	}

}
