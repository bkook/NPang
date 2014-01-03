package mobi.threeam.npang.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ItemSelect;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.AlarmUtils;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.NPangPreference_;
import mobi.threeam.npang.common.Notifier;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.common.TimeUtils;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.PayAttRelationDao;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.ChangeAmountEvent;
import mobi.threeam.npang.event.ChangeAttendeesEvent;
import mobi.threeam.npang.event.DeletePaymentEvent;
import mobi.threeam.npang.event.OpenDrawerEvent;
import mobi.threeam.npang.event.SetPaymentGroupEvent;
import mobi.threeam.npang.ui.activity.MainActivity;
import mobi.threeam.npang.ui.view.PaymentItemView;
import mobi.threeam.npang.ui.view.PaymentItemView_;

@OptionsMenu(R.menu.input)
@EFragment(R.layout.fragment_input)
public class InputFragment extends Fragment {

	Handler handler = new Handler();

	@Pref
	NPangPreference_ prefs;

    @Bean
    AlarmUtils alarmUtils;

	@SystemService
	LayoutInflater inflater;

	@ViewById
	EditText title;

//	@ViewById
//	AutoCompleteTextView bankNameText;

	@ViewById
	Spinner bankNameSpinner;

	@ViewById
	EditText accountNumber;

	@ViewById
	LinearLayout paymentViewList;
	
	@ViewById
	TextView totalAmount;
	
	@OrmLiteDao(helper=DBHelper.class, model=Payment.class)
	PaymentDao paymentDao;

	@OrmLiteDao(helper=DBHelper.class, model=PaymentGroup.class)
	PaymentGroupDao paymentGroupDao;

	@OrmLiteDao(helper=DBHelper.class, model=PayAttRelation.class)
	PayAttRelationDao relationDao;

    @FragmentArg
    long paymentGroupId;

	PaymentGroup paymentGroup;

    static final int STATE_DEFAULT = 0;
    static final int STATE_DETELE = 1;
    int fragmentState = STATE_DEFAULT;


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
        getActivity().getActionBar().setTitle(R.string.title_input);

		String[] bankNameArray = getResources().getStringArray(R.array.banks);
		ArrayAdapter<String> bankNameAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item,
				bankNameArray);
		bankNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bankNameSpinner.setAdapter(bankNameAdapter);

		accountNumber.setText(prefs.accountNumber().get());
		accountNumber.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				prefs.accountNumber().put(accountNumber.getText().toString());
			}
		});
		
		loadPaymentGroup();
	}
	
	@Background
	void loadPaymentGroup() {
		try {
			paymentGroup = paymentGroupDao.queryForId(paymentGroupId);
		} catch (SQLException e) {
			Logger.e(e);
		}	
		
		refresh();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		save();
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean enabled = ! ((MainActivity)getActivity()).isDrawerOpened();
        for (int i=0; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(enabled);
        }
    }

    @ItemSelect
	public void bankNameSpinner(boolean selected, int position) {
		prefs.bankName().put(bankNameSpinner.getSelectedItem().toString());
	}


	@Click(R.id.add_payment)
	public void addPaymentButton() {
		Payment lastPayment = null;
		if (paymentViewList.getChildCount() > 0) {
			int lastIndex = paymentViewList.getChildCount() - 1;
			PaymentItemView view = (PaymentItemView) paymentViewList.getChildAt(lastIndex);
			lastPayment = view.payment;
//			if (view.amount.getNumber().intValue() == 0) {
//				Notifier.toast(R.string.
//				return;
//			}
		}

		try {
			Payment payment = paymentDao.addPaymentTo(paymentGroup);
			if (relationDao.copyAttendees(lastPayment, payment)) {
				paymentDao.refresh(payment);
			}
			addPaymentView(payment);
		} catch (SQLException e) {
			Logger.e(e);
		}
	}

	void addPaymentView() {
		PaymentItemView view = PaymentItemView_.build(getActivity());
		int index = paymentViewList.getChildCount() + 1;
		view.setUpEventListener(getActivity(), index);
		
		paymentViewList.addView(view);	
	}
	
	void addPaymentView(Payment payment) {
		PaymentItemView view = PaymentItemView_.build(getActivity());
		int index = paymentViewList.getChildCount();

		view.setUpEventListener(getActivity(), index);
		view.setUp(index, payment, paymentDao);
		
		paymentViewList.addView(view);	
	}
	
	void removePaymentView() {
		paymentViewList.removeViewAt(paymentViewList.getChildCount()-1);
	}
	
	void calcTotalAmount() {
		double total = 0;

		for (int i=0; i < paymentViewList.getChildCount(); i++) {
			PaymentItemView holder = (PaymentItemView) paymentViewList.getChildAt(i);
			Number amount = holder.amount.getNumber();
			String val = holder.amount.getStrippedText();
			try {
				if (val.contains(".")) {
					total += amount.doubleValue();
				} else {
					total += amount.longValue();
				}
			} catch (Exception e) {
				continue;
			}
		}

		TextViewUtils.currency(totalAmount, total);
	}

    public void onEventMainThread(OpenDrawerEvent event) {
        save();
    }

    public void onEventMainThread(DeletePaymentEvent event) {
        int count = paymentViewList.getChildCount();
        boolean afterDeleted = false;
        for (int i=0; i < count; i++ ) {
            PaymentItemView holder = (PaymentItemView) paymentViewList.getChildAt(i);

            Payment payment = holder.payment;
            if (payment.id == event.payment.id) {
                paymentViewList.removeView(holder);
                afterDeleted = true;
            }

            if (afterDeleted) {
                holder.place.setHint(i);
            }
        }

        if (paymentViewList.getChildCount() == 1) {
            setFragmentState(STATE_DEFAULT);
        }

        calcTotalAmount();
    }

	public void onEventMainThread(SetPaymentGroupEvent event) {
        save();

        if (fragmentState != STATE_DEFAULT) {
            setFragmentState(STATE_DEFAULT);
        }

        FragmentManager fm = getFragmentManager();
        switch (event.group.state) {
            case PaymentGroup.STATE_NONE:
                paymentGroup = event.group;
                paymentGroupId = event.group.id;

                refresh();
                break;
            case PaymentGroup.STATE_CALCULATED:
                ReceiptFragment fragment = ReceiptFragment_.builder().paymentGroupId(event.group.id).build();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.contents, fragment);
                ft.commit();
                break;
        }
	}
	
	public void onEventMainThread(ChangeAmountEvent event) {
		calcTotalAmount();
	}
	
	public void onEventMainThread(ChangeAttendeesEvent event) {
		for (int i=0; i < paymentViewList.getChildCount(); i++) {
			PaymentItemView view = (PaymentItemView) paymentViewList.getChildAt(i);
			if (view.payment.id == event.payment.id) {
				TextViewUtils.attendees(view.attendee, event.payment);
				break;
			}
		}
	}
	
	private void save() {
		try {
			paymentGroupDao.refresh(paymentGroup);
		} catch (SQLException e) {
			Logger.e(e);
		}
		paymentGroup.title = title.getText().toString();
		paymentGroup.bankName = bankNameSpinner.getSelectedItem().toString();
		paymentGroup.bankAccount = accountNumber.getText().toString();

		try {
			paymentGroupDao.update(paymentGroup);
		} catch (SQLException e) {
			Logger.e(e);
		}

		int i=0;
		for (Payment payment : paymentGroup.payments) {
			PaymentItemView holder = (PaymentItemView) paymentViewList.getChildAt(i);
			holder.fill(payment);
			try {
				paymentDao.update(payment);
			} catch (SQLException e) {
				Logger.e(e);
			}
			i++;
		}
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

		title.setText(paymentGroup.title);
		title.setHint(TimeUtils.buildTitle(paymentGroup.createdAt));

		// Payment 관련 셋팅
		if (paymentGroup.payments == null) {
			paymentViewList.removeAllViews();
			return;
		}

		int viewCount = paymentViewList.getChildCount();
		int willBeAdded = paymentGroup.payments.size() - viewCount;
		for (int i=0; i < willBeAdded; i++) {
			addPaymentView();
		}

		int willBeRemoved = viewCount - paymentGroup.payments.size();
		for (int i=0; i < willBeRemoved; i++) {
			removePaymentView();
		}

		int i=0;
		for (Payment payment : paymentGroup.payments) {
			PaymentItemView holder = (PaymentItemView) paymentViewList.getChildAt(i);
			holder.setUp(i, payment, paymentDao);
			i++;
		}

		SpinnerAdapter adapter = bankNameSpinner.getAdapter();
		for (i=0 ; i < adapter.getCount(); i++) {
			String bankName = (String) adapter.getItem(i);
			if (bankName.equals(paymentGroup.bankName)) {
				bankNameSpinner.setSelection(i);
				break;
			}
		}

		accountNumber.setText(paymentGroup.bankAccount);
	}

	boolean isValid() {
		for (Payment payment: paymentGroup.payments) {
			Logger.i("payment " + payment.amount + " " + payment.attendees.size());
			if (payment.amount <= 0) {
				return false;
			}
			if (payment.attendees.size() <= 0) {
				return false;
			}
		}
		return true;
	}

    void setFragmentState(int state) {
        fragmentState = state;

        for (int i=0; i < paymentViewList.getChildCount(); i++) {
            PaymentItemView itemView = (PaymentItemView) paymentViewList.getChildAt(i);
            itemView.showDeleteView(fragmentState == STATE_DETELE);
        }
    }

    @OptionsItem(R.id.action_delete)
	void menuActionDelete() {
        save();
        if (paymentViewList.getChildCount() == 1) {
            Notifier.toast(R.string.account_info);
            return;
        }

        setFragmentState(fragmentState == STATE_DEFAULT ? STATE_DETELE : STATE_DEFAULT);
    }

	@OptionsItem(R.id.action_calculate)
	void menuActionCalculate() {
		save();
        alarmUtils.refresh();

		if ( ! isValid()) {
            Notifier.toast(R.string.msg_fill_in_the_blanks);
			return;
		}

        paymentGroupDao.setPaymentGroupState(paymentGroup, PaymentGroup.STATE_CALCULATED);

		ReceiptFragment fragment = ReceiptFragment_.builder().paymentGroupId(paymentGroup.id).build();
		
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.contents, fragment);
		ft.commit();
	}

}
