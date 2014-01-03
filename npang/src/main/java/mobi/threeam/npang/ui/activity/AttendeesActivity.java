package mobi.threeam.npang.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.SystemService;

import mobi.threeam.npang.R;
import mobi.threeam.npang.ui.fragment.AttendeeFragment_;

@EActivity(R.layout.activity_attendees)
public class AttendeesActivity extends Activity {
	private static final String FRAG_TAG = "frag";
	
	@SystemService
	LayoutInflater inflater;
	

	@Extra
	long paymentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getFragmentManager();
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
		getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
