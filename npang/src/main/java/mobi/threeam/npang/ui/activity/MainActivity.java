package mobi.threeam.npang.ui.activity;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.CreatePaymentGroupEvent;
import mobi.threeam.npang.event.PaymentGroupChangedEvent;
import mobi.threeam.npang.event.SetPaymentGroupEvent;
import mobi.threeam.npang.ui.adapter.PaymentGroupAdapter;
import mobi.threeam.npang.ui.fragment.InputFragment_;
import mobi.threeam.npang.ui.fragment.ReceiptFragment_;

@EActivity(R.layout.activity_main)
public class MainActivity extends SherlockFragmentActivity implements OnClickListener {
	private static final String FRAG_TAG = "frag";
	
	@SystemService
	LayoutInflater inflater;
	
	@OrmLiteDao(helper=DBHelper.class, model=PaymentGroup.class)
	PaymentGroupDao paymentGroupDao;

	@OrmLiteDao(helper=DBHelper.class, model=Payment.class)
	PaymentDao paymentDao;
	

    @ViewById
	DrawerLayout drawerLayout;

    @ViewById
	View drawer;

	ActionBarDrawerToggle drawerToggle;

    PaymentGroupAdapter adapter;

	Fragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(FRAG_TAG) == null) {

			FragmentTransaction ft = fm.beginTransaction();
            PaymentGroup group = paymentGroupDao.queryForAllSorted().get(0);
            switch (group.state) {
            case PaymentGroup.STATE_NONE:
                fragment = InputFragment_.builder().paymentGroupId(group.id).build();
                break;
            case PaymentGroup.STATE_CALCULATED:
                fragment = ReceiptFragment_.builder().paymentGroupId(group.id).build();
                break;
            }

			ft.replace(R.id.contents, fragment, FRAG_TAG);
			ft.commit();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);//, CreatePaymentGroupEvent.class);
	}
	
	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);//, CreatePaymentGroupEvent.class);
		super.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (drawerToggle != null) {
			drawerToggle.syncState();
		}
	}

	public void onEventMainThread(CreatePaymentGroupEvent event) {
		adapter.addDataAt(0, event.group);
	}

    public void onEventMainThread(PaymentGroupChangedEvent event) {
       adapter.refresh(event.group);
    }
	
	@AfterViews
	void bindDrawer() {
		getSupportActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawerLayout == null) {
			return;
		}
		drawer = findViewById(R.id.drawer);

		View headerView = inflater.inflate(R.layout.drawer_listview_header, null);
		headerView.findViewById(R.id.create_payment_group).setOnClickListener(this);

		ListView drawerListView = (ListView) drawer.findViewById(R.id.drawer_listview);
		drawerListView.addHeaderView(headerView);
		
		List<PaymentGroup> paymentGroups = paymentGroupDao.queryForAllSorted();

		adapter = new PaymentGroupAdapter(this);
		adapter.setData(paymentGroups);
		
		drawerListView.setAdapter(adapter);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
                adapter.notifyDataSetChanged();
                supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
                adapter.notifyDataSetChanged();
                supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);
	}

    public boolean isDrawerOpened() {
        return drawerLayout.isDrawerOpen(drawer);
    }
	

	@ItemClick
	public void drawerListviewItemClicked(PaymentGroup group) {
		EventBus.getDefault().post(new SetPaymentGroupEvent(group));
		drawerLayout.closeDrawers();
	}

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        } else {
            super.onBackPressed();
        }
    }

    @Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.create_payment_group: {
			try {
				PaymentGroup group = paymentGroupDao.createDefault(this);
				paymentDao.addPaymentTo(group);
				if (group.payments == null) {
					paymentGroupDao.refresh(group);
				}
			} catch (SQLException e) {
				Logger.e(e);
			}
			break;
		}
		}
	}

    @OptionsItem({ android.R.id.home })
	void menuHome() {
		if (drawerLayout.isDrawerOpen(drawer)) {
			drawerLayout.closeDrawer(drawer);
		} else {
			drawerLayout.openDrawer(drawer);
		}
	}

}
