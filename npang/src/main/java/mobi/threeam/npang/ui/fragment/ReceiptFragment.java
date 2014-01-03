package mobi.threeam.npang.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.AlarmUtils;
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
public class ReceiptFragment extends Fragment {

	@Pref
	NPangPreference_ prefs;

	@SystemService
	LayoutInflater inflater;

    @SystemService
    InputMethodManager inputMethodManager;

    @Bean
    AlarmUtils alarmUtils;

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
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        configureActionBar();

		loadPaymentGroup();
	}

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    MenuItem item = menu.findItem(R.id.action_share);

        final Intent intent = buildShareIntent();
	    ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
	    shareActionProvider.setShareIntent(intent);
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean enabled = ! ((MainActivity)getActivity()).isDrawerOpened();
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
			Notifier.toast(R.string.msg_unknown_error);
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
        alarmPeriod.setSelection(paymentGroup.alarmPeriod - 1);

        alarmPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                paymentGroup.alarmPeriod = i + 1;
                try {
                    paymentGroupDao.update(paymentGroup);
                    alarmUtils.refresh();
                } catch (SQLException e) {
                    Logger.e(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(paymentGroup.alarmTime);
        alarmTime.setText(TimeUtils.timeFormat(paymentGroup.alarmTime));

        setAlarmAreaEnabled(paymentGroup.alarmEnabled);
        alarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                paymentGroup.alarmEnabled = isChecked;
                try {
                    paymentGroupDao.update(paymentGroup);
                    alarmUtils.refresh();
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
        Calendar cal = Calendar.getInstance();
        cal.setTime(paymentGroup.alarmTime);
        int hour = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);

        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.MINUTE, min);

                alarmTime.setText(TimeUtils.timeFormat(paymentGroup.alarmTime));

                paymentGroup.alarmTime = cal.getTime();
                try {
                    paymentGroupDao.update(paymentGroup);
                    alarmUtils.refresh();
                } catch (SQLException e) {
                    Logger.e(e);
                }
            }
        }, hour, min, false).show();
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
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_receipt);
	}

	@OptionsItem(R.id.action_input)
	void menuActionInput() {
        paymentGroupDao.setPaymentGroupState(paymentGroup, PaymentGroup.STATE_NONE);

        alarmUtils.refresh();

		InputFragment fragment = InputFragment_.builder().paymentGroupId(paymentGroup.id).build();

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.contents, fragment);
		ft.commit();
	}



    private String makeReceiptText() {
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
            int count = payment.attendees.size();

            String paymentItem = getString(R.string.receipt_format_item_payment, paymentIndex,
                    CurrencyUtils.format(payment.amount), count, CurrencyUtils.format(payment.amount / count));

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
