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
import mobi.threeam.npang.database.dao.ContactsLoader.Contact;

@EViewGroup(R.layout.item_contact)
public class ContactItemView extends RelativeLayout {

	@ViewById
	ImageView photo;

	@ViewById
	TextView name;

	public ContactItemView(Context context) {
		super(context);
	}

	public void bind(Contact contact) {
		if (!TextUtils.isEmpty(contact.photoUri)) {
			UrlImageViewHelper.setUrlDrawable(photo, contact.photoUri);
		} else {
			photo.setImageResource(R.drawable.ic_action_user);
		}
		name.setText(contact.name);
	}
}
