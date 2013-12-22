package mobi.threeam.npang.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.actionbarsherlock.widget.ShareActionProvider.OnShareTargetSelectedListener;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.NPangPreference_;
import mobi.threeam.npang.common.Notifier;
import mobi.threeam.npang.common.TimeUtils;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.AttendeeDao;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.SetPaymentGroupEvent;
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
		EventBus.getDefault().register(this, SetPaymentGroupEvent.class);
	}

	@Override
	public void onDetach() {
		EventBus.getDefault().unregister(this, SetPaymentGroupEvent.class);
		super.onDetach();
	}

	@AfterViews
	void afterViews() {
		configureActionBar();

		loadPaymentGroup();
	}

    public void onEventMainThread(SetPaymentGroupEvent event) {
//        save();

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


    @Background
	void loadPaymentGroup() {
		try {
			paymentGroup = paymentGroupDao.queryForId(paymentGroupId);
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

		if (paymentGroup.payments == null) {
			paymentViewList.removeAllViews();
			return;
		}
	}

	void setUpTitle() {
		title.setText(TextUtils.isEmpty(paymentGroup.title) ? TimeUtils
				.buildTitle(paymentGroup.createdAt) : paymentGroup.title);
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
			ReceiptAttendeeView view = (ReceiptAttendeeView) attendeeViewList
					.getChildAt(i);
			Logger.e("view " + view + " " + attendee.id + " : " + map.containsKey(attendee.id));

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
			view.bind(payment);

			// ViewHolder holder = paymentViewHolderList.get(i);
			// holder.setUp(i, payment);
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

	private Intent buildShareIntent() {
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("text/plain");
	    intent.putExtra(Intent.EXTRA_TEXT, "hello");
		return intent;
	}

}
