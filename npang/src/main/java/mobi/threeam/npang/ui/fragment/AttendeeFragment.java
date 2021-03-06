package mobi.threeam.npang.ui.fragment;

import android.app.Fragment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ListView;

import com.meetme.android.horizontallistview.HorizontalListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.NPangPreference_;
import mobi.threeam.npang.common.Notifier;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.AttendeeDao;
import mobi.threeam.npang.database.dao.ContactsLoader;
import mobi.threeam.npang.database.dao.ContactsLoader.Contact;
import mobi.threeam.npang.database.dao.PayAttRelationDao;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.ChangeAttendeesEvent;
import mobi.threeam.npang.ui.adapter.AttendeeAdapter;
import mobi.threeam.npang.ui.adapter.ContactAdapter;

@EFragment(R.layout.fragment_attendee)
public class AttendeeFragment extends Fragment {

	@Pref
	NPangPreference_ prefs;

	@SystemService
	LayoutInflater inflater;

	@ViewById
	ListView listview;

	@ViewById
	EditText search;

	@ViewById
	HorizontalListView attendeeListview;

	@OrmLiteDao(helper = DBHelper.class, model = PaymentGroup.class)
	PaymentGroupDao paymentGroupDao;

	@OrmLiteDao(helper = DBHelper.class, model = Payment.class)
	PaymentDao paymentDao;

	@OrmLiteDao(helper = DBHelper.class, model = Attendee.class)
	AttendeeDao attendeeDao;
	
	@OrmLiteDao(helper = DBHelper.class, model = PayAttRelation.class)
	PayAttRelationDao relationDao;

	@Bean
	AttendeeAdapter attendeeAdapter;

	@Bean
	ContactAdapter contactAdapter;

	@Bean
	ContactsLoader contactsLoader;

	ArrayList<Contact> contacts;
	
	@FragmentArg
	long paymentId;
	
	Payment payment;
	HashMap<String, Attendee> attendeeMap;

	Handler handler = new Handler();

	@AfterViews
	void afterViews() {
        getActivity().getActionBar().setTitle(R.string.attendee);
		List<Contact> contacts = contactsLoader.getAll();

		contactAdapter.setData(contacts);
		listview.setAdapter(contactAdapter);
		
		attendeeListview.setAdapter(attendeeAdapter);

		try {
			payment = paymentDao.queryForId(paymentId);

			List<Attendee> attendees = attendeeDao.queryForEq("paymentGroup_id", payment.paymentGroup.id);
			attendeeMap = new HashMap<String, Attendee>();
			if (attendees != null) {
				for (Attendee attendee : attendees) {
					attendeeMap.put(attendee.name, attendee);
				}
			}
		} catch (SQLException e) {
			Logger.e(e);
		}
		
		ArrayList<Attendee> attendees = new ArrayList<Attendee>();
		if (payment.attendees != null) {
			for (PayAttRelation relation: payment.attendees) {
				try {
					attendeeDao.refresh(relation.attendee);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				attendees.add(relation.attendee);
			}
		}

		attendeeAdapter.setData(attendees);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                contactAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
	}

	@ItemClick
	void attendeeListviewItemClicked(int position) {
		Attendee attendee = attendeeAdapter.getItem(position);

		attendeeMap.remove(attendee.name);
		attendeeAdapter.removeItemAt(position);
		
		relationDao.delete(payment, attendee);
		List<PayAttRelation> relations = null;
		try {
            long count = relationDao.queryBuilder().where().eq("attendee_id", attendee.id).countOf();
            if (count == 0) {
                attendeeDao.deleteById(attendee.id);
            }
		} catch (SQLException e) {
			Logger.e(e);
			return;
		}
		EventBus.getDefault().post(new ChangeAttendeesEvent(payment));
	}
	
	@ItemClick 
	void listviewItemClicked(Contact contact) {
		Attendee attendee = new Attendee();
		attendee.name = contact.name;
		attendee.photoUri = contact.photoUri;
		addAttendee(attendee);
	}
	
	@Click
	void addAttendeeClicked() {
		Attendee attendee = new Attendee();
		attendee.name = search.getText().toString();
		if (TextUtils.isEmpty(attendee.name)) {
			return;
		}
		search.setText("");
		addAttendee(attendee);
	}
	
	void addAttendee(Attendee attendee) {
		if (attendeeAdapter.contains(attendee.name)) {
            Notifier.toast(R.string.msg_already_exist_attendee);
			return;
		}
		if (!attendeeMap.containsKey(attendee.name)) {
			try {
				attendee.paymentGroup = payment.paymentGroup;
				attendeeDao.create(attendee);
			} catch (SQLException e) {
				Logger.e(e);
				return;
			}
		} else {
			attendee = attendeeMap.get(attendee.name);
		}
		PayAttRelation relation = null;
		try {
			relation = new PayAttRelation(payment, attendee);
			relationDao.create(relation);
		} catch (SQLException e) {
			Logger.e(e);
			return;
		}
		
		attendeeMap.put(attendee.name, attendee);
		attendeeAdapter.addItem(attendee);
		EventBus.getDefault().post(new ChangeAttendeesEvent(payment));
	}
}
