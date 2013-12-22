package mobi.threeam.npang.ui.view;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.event.AmountChangedEvent;
import mobi.threeam.npang.ui.activity.AttendeesActivity_;
import net.jangc.currencyet.CurrencyEditText;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EViewGroup(R.layout.item_payment)
public class PaymentItemView extends RelativeLayout implements OnTouchListener {
	@ViewById
	public EditText place;
	@ViewById
	public CurrencyEditText amount;
	@ViewById
	public BootstrapButton attendee;

	public Payment payment;

	public PaymentItemView(Context context) {
		super(context);
		init();
	}

	public PaymentItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PaymentItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	void init() {
		this.setOnTouchListener(this);
		
	}

	public void setUp(int index, Payment payment) {
		int paymentIndex = index + 1;
		this.payment = payment;

		String paymentTitleHint = getResources().getString(
				R.string.payment_title_hint, paymentIndex);
		place.setHint(paymentTitleHint);
		place.setText(payment.place);

		TextViewUtils.attendees(attendee, payment);

		amount.setText("" + payment.amount);
	}

	public void setUpEventListener(final Activity activity, int paymentIndex) {
		String paymentTitleHint = getResources().getString(
				R.string.payment_title_hint, paymentIndex + 1);
		place.setHint(paymentTitleHint);

		amount.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				EventBus.getDefault().post(new AmountChangedEvent());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		attendee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				AttendeesActivity_.intent(activity).paymentId(payment.id).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();

			}
		});
	}

	public void fill(Payment payment) {
		payment.place = place.getText().toString();
		payment.amount = amount.getNumber().intValue();
	}

	public boolean isValid() {
		// attendee.getText
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
}
