package mobi.threeam.npang.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.SystemService;

import mobi.threeam.npang.R;
import mobi.threeam.npang.ui.fragment.AttendeeFragment_;

@EActivity(R.layout.activity_attendees)
public class AttendeesActivity extends SherlockFragmentActivity {
	private static final String FRAG_TAG = "frag";
	
	@SystemService
	LayoutInflater inflater;
	

	@Extra
	long paymentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(FRAG_TAG) == null) {
			FragmentTransaction ft = fm.beginTransaction();

            Fragment fragment = AttendeeFragment_.builder()
                    .paymentId(paymentId)
                    .build();
            ft.replace(R.id.contents, fragment, FRAG_TAG);
			ft.commit();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
	
	@AfterViews
	void bindDrawer() {
		getSupportActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
