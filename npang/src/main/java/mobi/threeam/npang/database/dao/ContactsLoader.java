package mobi.threeam.npang.database.dao;

import java.util.ArrayList;

import com.googlecode.androidannotations.annotations.EBean;

import mobi.threeam.npang.NPangApp;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;

@EBean
public class ContactsLoader {

	public static class Contact {
		public long id;
		public String name;
		public String photoUri;
	}

	public String[] projection = new String[] { Contacts._ID,
			Contacts.DISPLAY_NAME, Contacts.PHOTO_ID,
			
	};

	public ArrayList<Contact> contacts;

	public ArrayList<Contact> getAll() {
		Cursor cursor = NPangApp.get().getContentResolver().query(Contacts.CONTENT_URI, projection,
						null, null, null);
		contacts = new ArrayList<ContactsLoader.Contact>();
		
		if (cursor != null && cursor.moveToFirst()) {
			do {
				Contact contact = new Contact();
				contact.id = cursor.getLong(0);
				contact.name = cursor.getString(1);
				String contactId = cursor.getString(2);
				if (!TextUtils.isEmpty(contactId)) {
//					long photoId = Long.parseLong(contactId);
//					contact.photoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, photoId).toString();
					contact.photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id).toString();

					
				}
				
				contacts.add(contact);

				cursor.moveToNext();
			} while(!cursor.isAfterLast());
		}
		return contacts;
	}

}
