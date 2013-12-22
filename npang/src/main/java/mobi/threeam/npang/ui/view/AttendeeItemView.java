package mobi.threeam.npang.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import mobi.threeam.npang.R;
import mobi.threeam.npang.database.model.Attendee;

@EViewGroup(R.layout.item_attendee)
public class AttendeeItemView extends RelativeLayout {

	@ViewById
	ImageView photo;

	@ViewById
	TextView name;

	Context context;
	public AttendeeItemView(Context context) {
		super(context);
		this.context = context;
	}

	public void bind(Attendee attendee) {
		if (!TextUtils.isEmpty(attendee.photoUri)) {
			UrlImageViewHelper.setUrlDrawable(photo, attendee.photoUri);
		} else {
			photo.setImageResource(R.drawable.ic_action_user);
		}
		name.setText(attendee.name);
	}

}
