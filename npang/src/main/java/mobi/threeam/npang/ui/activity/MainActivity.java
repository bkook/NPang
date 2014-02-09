package mobi.threeam.npang.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.AnimUtils;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.Notifier;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.AttendeeDao;
import mobi.threeam.npang.database.dao.PayAttRelationDao;
import mobi.threeam.npang.database.dao.PaymentDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PayAttRelation;
import mobi.threeam.npang.database.model.Payment;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.CreatePaymentGroupEvent;
import mobi.threeam.npang.event.OpenDrawerEvent;
import mobi.threeam.npang.event.PaymentGroupChangedEvent;
import mobi.threeam.npang.event.SetPaymentGroupEvent;
import mobi.threeam.npang.ui.adapter.PaymentGroupAdapter;
import mobi.threeam.npang.ui.fragment.InputFragment_;
import mobi.threeam.npang.ui.fragment.ReceiptFragment_;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements OnClickListener {
	private static final String FRAG_TAG = "frag";
	
	@SystemService
	LayoutInflater inflater;

    @OrmLiteDao(helper=DBHelper.class, model=Attendee.class)
    AttendeeDao attendeeDao;

    @OrmLiteDao(helper=DBHelper.class, model=PayAttRelation.class)
    PayAttRelationDao relationDao;

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

    @Extra
    long paymentGroupId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(FRAG_TAG) == null) {

			FragmentTransaction ft = fm.beginTransaction();
            PaymentGroup group = null;
            if (paymentGroupId != -1) {
                try {
                    group = paymentGroupDao.queryForId(paymentGroupId);
                } catch (SQLException e) {
                    Logger.e(e);
                }
            }
            if (group == null) {
                group = paymentGroupDao.queryForAllSorted().get(0);
            }
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


		getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

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
                invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
                adapter.notifyDataSetChanged();
                invalidateOptionsMenu();
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

    <T> void deleteByPaymentGroup(Dao<T, Long> dao, PaymentGroup group) {
        DeleteBuilder<T, Long> builder = dao.deleteBuilder();
        try {
            builder.where().eq("paymentGroup_id", group.id);
            dao.delete(builder.prepare());
        } catch (SQLException e) {
            Logger.e(e);
        }
    }

    @Background
    void deletePaymentGroup(PaymentGroup group) {
        try {
            deleteByPaymentGroup(relationDao, group);
            deleteByPaymentGroup(paymentDao, group);
            deleteByPaymentGroup(attendeeDao, group);
            paymentGroupDao.delete(group);

            List<PaymentGroup> paymentGroups = paymentGroupDao.queryForAllSorted();
            setUpListview(paymentGroups);
            EventBus.getDefault().post(new SetPaymentGroupEvent(paymentGroups.get(0)));
        } catch (SQLException e) {
            Logger.e(e);
        }
    }

    @UiThread
    void setUpListview(List<PaymentGroup> paymentGroups) {
        adapter.setData(paymentGroups);
    }

    @ItemLongClick
    public void drawerListviewItemLongClicked(final int position) {
        if (adapter.getCount() == 1) {
            Notifier.toast(R.string.msg_more_than_one);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete)
                .setMessage(R.string.delete_desc)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ListView drawerListView = (ListView) drawer.findViewById(R.id.drawer_listview);
                        int headerViewsCount = drawerListView.getHeaderViewsCount();

                        final PaymentGroup group = adapter.getItem(position - headerViewsCount);

                        int firstPosition = drawerListView.getFirstVisiblePosition();
                        int lastPosition = drawerListView.getLastVisiblePosition();

                        if (firstPosition <= position && lastPosition >= position) {
                            int selectedIndex = position - firstPosition;
                            final View view = drawerListView.getChildAt(selectedIndex);
                            AnimUtils.fadeOut(view, new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    deletePaymentGroup(group);
                                    view.setAlpha(1);
                                }
                            });
                        } else {
                            deletePaymentGroup(group);
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
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
            EventBus.getDefault().post(new OpenDrawerEvent());
            List<PaymentGroup> paymentGroups = paymentGroupDao.queryForAllSorted();
            setUpListview(paymentGroups);
			drawerLayout.openDrawer(drawer);
		}
	}

}
