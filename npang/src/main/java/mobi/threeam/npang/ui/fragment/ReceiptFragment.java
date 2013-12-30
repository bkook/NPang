package mobi.threeam.npang.ui.fragment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.actionbarsherlock.widget.ShareActionProvider.OnShareTargetSelectedListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.CurrencyUtils;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.NPangPreference_;
import mobi.threeam.npang.common.Notifier;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.common.TimeUtils;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.AttendeeDao;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.PaymentGroupChangedEvent;
import mobi.threeam.npang.event.SetPaymentGroupEvent;
import mobi.threeam.npang.ui.activity.MainActivity;
import mobi.threeam.npang.ui.view.ReceiptAttendeeView;
import mobi.threeam.npang.ui.view.ReceiptAttendeeView_;
import mobi.threeam.npang.ui.view.ReceiptPaymentView;
import mobi.threeam.npang.ui.view.ReceiptPaymentView_;

@OptionsMenu(R.menu.receipt)
@EFragment(R.layout.fragment_receipt)
public class ReceiptFragment extends SherlockFragment {

	@Pref
	NPangPreference_ prefs;

	@SystemService
	LayoutInflater inflater;

	@ViewById
	TextView title;

	@ViewById
	LinearLayout attendeeViewList;

	@ViewById
	LinearLayout paymentViewList;

	@ViewById
	TextView bankName;

	@ViewById
	TextView bankAccount;

    @ViewById
    ViewGroup alarmArea;

    @ViewById
    Spinner alarmPeriod;

    @ViewById
    Button alarmTime;

    @ViewById
    ToggleButton alarmToggle;

    @ColorRes
    int alarmEnabledColor;

    @ColorRes
    int alarmDisabledColor;

	@OrmLiteDao(helper = DBHelper.class, model = PaymentGroup.class)
	PaymentGroupDao paymentGroupDao;

	@OrmLiteDao(helper = DBHelper.class, model = Payment.class)
	PaymentDao paymentDao;

	@OrmLiteDao(helper = DBHelper.class, model = Attendee.class)
	AttendeeDao attendeeDao;

	@FragmentArg
	long paymentGroupId;

	PaymentGroup paymentGroup;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		EventBus.getDefault().register(this);
	}

	@Override
	public void onDetach() {
		EventBus.getDefault().unregister(this);
		super.onDetach();
	}

	@AfterViews
	void afterViews() {
		configureActionBar();

		loadPaymentGroup();
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean enabled = ! ((MainActivity)getSherlockActivity()).isDrawerOpened();
        for (int i=0; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(enabled);
        }
    }

    public void onEventMainThread(SetPaymentGroupEvent event) {
        FragmentManager fm = getFragmentManager();
        switch (event.group.state) {
            case PaymentGroup.STATE_NONE:
                InputFragment fragment = InputFragment_.builder().paymentGroupId(event.group.id).build();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.contents, fragment);
                ft.commit();
                break;
            case PaymentGroup.STATE_CALCULATED:
                paymentGroup = event.group;
                paymentGroupId = event.group.id;
                refresh();
                break;
        }
    }

    public void onEventMainThread(PaymentGroupChangedEvent event) {
        setAlarmAreaEnabled(event.group.alarmEnabled);
    }

	void loadPaymentGroup() {
		try {
			paymentGroup = paymentGroupDao.queryForId(paymentGroupId);
            Logger.e("paymentGroup " + paymentGroup);
            if (paymentGroup == null) {
                throw new RuntimeException();
            }
		} catch (SQLException e) {
			Logger.e(e);
		}

		refresh();
	}

	@UiThread
	void refresh() {
		if (paymentGroup == null) {
			Notifier.toast("null");
			return;
		}

		try {
			paymentGroupDao.refresh(paymentGroup);
		} catch (SQLException e) {
			Logger.e(e);
		}

		setUpTitle();
		setUpAttendees();
		setUpBankAccount();
		setUpPayments();
        setUpAlarm();

		if (paymentGroup.payments == null) {
			paymentViewList.removeAllViews();
			return;
		}
	}

	void setUpTitle() {
		title.setText(TextUtils.isEmpty(paymentGroup.title) ? TimeUtils
				.buildTitle(paymentGroup.createdAt) : paymentGroup.title);
	}

    void setUpAlarm() {
        alarmPeriod.setSelection(1);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(paymentGroup.alarmTime);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:m a");
        alarmTime.setText(dateFormatter.format(paymentGroup.alarmTime));

        setAlarmAreaEnabled(paymentGroup.alarmEnabled);
        alarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                paymentGroup.alarmEnabled = isChecked;
                try {
                    paymentGroupDao.update(paymentGroup);
                } catch (SQLException e) {
                    Logger.e(e);
                }
                setAlarmAreaEnabled(isChecked);
            }
        });
    }

    void setAlarmAreaEnabled(boolean enabled) {
        alarmToggle.setChecked(enabled);
        alarmArea.setBackgroundColor(enabled ? alarmEnabledColor : alarmDisabledColor);
    }

    @Click
    void alarmTimeClicked() {
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                paymentGroup.alarmTime = new Date();
            }
        }, 11, 11, false).show();
    }

	void setUpAttendees() {
		List<Attendee> attendees = null;
		try {
			attendees = attendeeDao.queryForEq("paymentGroup_id",
					paymentGroup.id);
		} catch (SQLException e) {
			Logger.e(e);
			return;
		}

		HashMap<Long, Integer> map = new HashMap<Long, Integer>();

		for (Payment payment : paymentGroup.payments) {
			int attendeeCount = payment.attendees.size();
			for (PayAttRelation relation : payment.attendees) {
				Attendee attendee = relation.attendee;

				if (!map.containsKey(attendee.id)) {
					map.put(attendee.id, 0);
				}
				Integer amount = map.get(attendee.id);
				map.put(attendee.id, amount + (payment.amount / attendeeCount));
			}
		}

		int viewCount = attendeeViewList.getChildCount();
		int willBeAdded = attendees.size() - viewCount;
		for (int i = 0; i < willBeAdded; i++) {
			attendeeViewList.addView(ReceiptAttendeeView_.build(getActivity()));
		}

		int willBeRemoved = viewCount - attendees.size();
		for (int i = 0; i < willBeRemoved; i++) {
			attendeeViewList.removeViewAt(0);
		}

		int i = 0;
		for (Attendee attendee : attendees) {
            attendee.paymentGroup = paymentGroup;
			ReceiptAttendeeView view = (ReceiptAttendeeView) attendeeViewList
					.getChildAt(i);
			view.bind(attendee, map.get(attendee.id));
			i++;
		}
	}

	void setUpPayments() {
		int viewCount = paymentViewList.getChildCount();
		int willBeAdded = paymentGroup.payments.size() - viewCount;
		for (int i = 0; i < willBeAdded; i++) {
			paymentViewList.addView(ReceiptPaymentView_.build(getActivity()));
		}

		int willBeRemoved = viewCount - paymentGroup.payments.size();
		for (int i = 0; i < willBeRemoved; i++) {
			paymentViewList.removeViewAt(0);
		}

		int i = 0;
		for (Payment payment : paymentGroup.payments) {
			ReceiptPaymentView view = (ReceiptPaymentView) paymentViewList
					.getChildAt(i);
			view.bind(i, payment);
			i++;
		}
	}

	void setUpBankAccount() {
		bankName.setText(paymentGroup.bankName);
		bankAccount.setText(paymentGroup.bankAccount);
	}

	void configureActionBar() {
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@OptionsItem(R.id.action_input)
	void menuActionInput() {
        paymentGroupDao.setPaymentGroupState(paymentGroup, PaymentGroup.STATE_NONE);

		InputFragment fragment = InputFragment_.builder().paymentGroupId(paymentGroup.id).build();

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.contents, fragment);
		ft.commit();
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    MenuItem item = menu.findItem(R.id.action_share);

	    Intent intent = buildShareIntent();
	    ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
	    shareActionProvider.setShareHistoryFileName(null);
	    shareActionProvider.setShareIntent(intent);
	    shareActionProvider.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {
			@Override
			public boolean onShareTargetSelected(ShareActionProvider source,
					Intent intent) {
				startActivity(intent);
				return true;
			}
		});
	}

    private String makeReceiptText() {
        Logger.e("paymentgroup000 " + paymentGroup);
         StringBuilder builder = new StringBuilder();
        builder.append("[").append(TextViewUtils.title(paymentGroup)).append("]").append("\r\n\r\n");

        // attendees
        fillAttendees(builder);

        // bank
        builder.append(getString(R.string.receipt_format_title, getString(R.string.account_info)))
                .append(paymentGroup.bankName).append(paymentGroup.bankAccount).append("\r\n")
                .append("\r\n");

        // payments
        fillPayments(builder);
        builder.append("\r\n");

        return builder.toString();
    }

    private void fillAttendees(StringBuilder builder) {
        builder.append(getString(R.string.receipt_format_title, getString(R.string.by_attendee)));
        builder.append(getString(R.string.receipt_format_title, getString(R.string.by_attendee)));

        List<Attendee> attendees = null;
        try {
            attendees = attendeeDao.queryForEq("paymentGroup_id",
                    paymentGroup.id);
        } catch (SQLException e) {
            Logger.e(e);
            return;
        }

        HashMap<Long, Integer> map = new HashMap<Long, Integer>();

        for (Payment payment : paymentGroup.payments) {
            int attendeeCount = payment.attendees.size();
            for (PayAttRelation relation : payment.attendees) {
                Attendee attendee = relation.attendee;

                if (!map.containsKey(attendee.id)) {
                    map.put(attendee.id, 0);
                }
                Integer amount = map.get(attendee.id);
                map.put(attendee.id, amount + (payment.amount / attendeeCount));
            }
        }

        int i = 0;
        for (Attendee attendee : attendees) {
            attendee.paymentGroup = paymentGroup;
            int resId = R.string.receipt_format_item_attendee;
            if (attendee.paidAt != null) {
                resId = R.string.receipt_format_item_attendee_checked;
            }
            Logger.i("attendee1 " + attendee.id);
            Logger.i("attendee2 " + map);
            builder.append(getString(resId, attendee.name, CurrencyUtils.format(map.get(attendee.id))));
            i++;
        }
                paymentGroup.totalAmount = 0;
        for (Payment payment : paymentGroup.payments) {
            paymentGroup.totalAmount += payment.amount;
        }

        builder.append("------------------").append("\r\n")
                .append(getString(R.string.total)).append(" : ").append(CurrencyUtils.format(paymentGroup.totalAmount)).append("\r\n")
                .append("\r\n");

    }

    private void fillPayments(StringBuilder builder) {
        builder.append(getString(R.string.receipt_format_title, getString(R.string.items)));

        int index = 1;
        for (Payment payment : paymentGroup.payments) {
            String paymentIndex = getString(R.string.payment_title_hint, index);
            if (!TextUtils.isEmpty(payment.place)) {
                paymentIndex = paymentIndex + "-" + payment.place;
            }

            String paymentItem = getString(R.string.receipt_format_item_payment, paymentIndex,
                    NumberFormat.getCurrencyInstance(Locale.KOREA).format(payment.amount), payment.attendees.size(), "10");

            builder.append(paymentItem);
            index++;
        }
    }

	private Intent buildShareIntent() {
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, makeReceiptText());
		return intent;
	}

}
