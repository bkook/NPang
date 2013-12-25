package mobi.threeam.npang.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.common.TimeUtils;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;

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

        TextViewUtils.strike(title, group.completed);
        TextViewUtils.strike(amount, group.completed);
        TextViewUtils.strike(date, group.completed);

        boolean showAmount = group.state == PaymentGroup.STATE_CALCULATED;
        amount.setVisibility(showAmount ? View.VISIBLE : View.INVISIBLE);
	}

}
