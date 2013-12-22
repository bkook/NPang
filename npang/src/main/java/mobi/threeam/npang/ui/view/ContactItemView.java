package mobi.threeam.npang.ui.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import mobi.threeam.npang.R;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.database.dao.ContactsLoader.Contact;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

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
		Logger.i("bind " + contact.photoUri);
		if (!TextUtils.isEmpty(contact.photoUri)) {
			UrlImageViewHelper.setUrlDrawable(photo, contact.photoUri);
		} else {
			photo.setImageResource(R.drawable.ic_action_user);
		}
		name.setText(contact.name);
	}
}
