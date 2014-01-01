package mobi.threeam.npang.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.CurrencyUtils;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.database.model.Payment;

@EViewGroup(R.layout.item_receipt_payment)
public class ReceiptPaymentView extends RelativeLayout {

	@ViewById
	TextView place;

	@ViewById
	TextView description;

	@ViewById
	TextView amount;

	public ReceiptPaymentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ReceiptPaymentView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ReceiptPaymentView(Context context) {
		super(context);
	}

	public void bind(int index, Payment payment) {
		int attendeeCount = payment.attendees.size();

        TextViewUtils.place(place, index, payment.place);
        description.setText(String.format("%s ÷ %d", CurrencyUtils.format(payment.amount), attendeeCount));
        TextViewUtils.currency(amount, payment.amount / attendeeCount);
	}
}