package mobi.threeam.npang.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.stmt.QueryBuilder;

import net.jangc.currencyet.CurrencyEditText;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.AttendeeDao;
import mobi.threeam.npang.database.dao.PayAttRelationDao;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.event.ChangeAmountEvent;
import mobi.threeam.npang.event.DeletePaymentEvent;
import mobi.threeam.npang.ui.activity.AttendeesActivity_;

@EViewGroup(R.layout.item_payment)
public class PaymentItemView extends RelativeLayout {

    @ViewById
    public LinearLayout itemContainer;

	@ViewById
	public EditText place;
	@ViewById
	public CurrencyEditText amount;
	@ViewById
	public BootstrapButton attendee;
    @ViewById
    public BootstrapButton delete;

    @OrmLiteDao(helper = DBHelper.class, model = Payment.class)
    PaymentDao paymentDao;

    @OrmLiteDao(helper = DBHelper.class, model = Attendee.class)
    AttendeeDao attendeeDao;

    @OrmLiteDao(helper = DBHelper.class, model = PayAttRelation.class)
    PayAttRelationDao relationDao;

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
//        isSwiping = false;
//
//        ViewConfiguration vc = ViewConfiguration.get(getContext());
//
//        slop = vc.getScaledTouchSlop();
//        minFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
//        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
//        animationTime = getContext().getResources().getInteger(
//                android.R.integer.config_shortAnimTime);
    }

	public void setUp(int index, Payment payment, PaymentDao paymentDao) {
		int paymentIndex = index + 1;
		this.payment = payment;
        this.paymentDao = paymentDao;

        setPlaceHint(paymentIndex);
		place.setText(payment.place);


		TextViewUtils.attendees(attendee, payment);

		amount.setText("" + payment.amount);
	}

    public void setPlaceHint(int paymentIndex) {
        String paymentTitleHint = getResources().getString(
                R.string.payment_title_hint, paymentIndex);
        place.setHint(paymentTitleHint);
    }

	public void setUpEventListener(final Activity activity, int paymentIndex) {
		String paymentTitleHint = getResources().getString(
				R.string.payment_title_hint, paymentIndex + 1);
		place.setHint(paymentTitleHint);

		amount.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				EventBus.getDefault().post(new ChangeAmountEvent());
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

    public void showDeleteView(boolean show) {
        if (show) {
            itemContainer.animate().translationX(delete.getWidth() + getChildAt(0).getPaddingLeft());
        } else {
            itemContainer.animate().translationX(0);
        }
    }

    @Click
    void deleteClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());;
        builder.setTitle(R.string.delete)
                .setMessage(R.string.search)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete.animate().alpha(0);
                        itemContainer.animate().translationX(getWidth()).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                delete();
                            }
                        });

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.show();
    }

    @Background
    void delete() {
        try {
            paymentDao.deleteById(payment.id);
            for (PayAttRelation relation : payment.attendees) {
                Attendee attendee = relation.attendee;

                QueryBuilder<PayAttRelation, Long> builder = relationDao.queryBuilder();
                long count = builder.setCountOf(true).where().eq("attendee_id", relation.attendee.id).countOf();
                if (count == 1) {
                    attendeeDao.delete(attendee);
                }
            }

            relationDao.delete(payment.attendees);

            EventBus.getDefault().post(new DeletePaymentEvent(payment));
        } catch (SQLException e) {
            Logger.e(e);
        }
    }
}
