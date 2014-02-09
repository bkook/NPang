package mobi.threeam.npang.database.dao;

import android.content.ContentUris;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;

import com.j256.ormlite.stmt.QueryBuilder;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mobi.threeam.npang.NPangApp;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.model.Attendee;

@EBean
public class ContactsLoader {

	@OrmLiteDao(helper = DBHelper.class, model = Attendee.class)
	AttendeeDao attendeeDao;

//    public ArrayList<Contact> contacts;

	public static class Contact {
		public long id;
		public String name;
		public String photoUri;
	}

	public String[] projection = new String[] { Contacts._ID,
			Contacts.DISPLAY_NAME, Contacts.PHOTO_ID,
			
	};

    public ArrayList<Contact> getAll() {
        ArrayList<Contact> contacts = new ArrayList<ContactsLoader.Contact>();

        loadLatestAttendees(10, contacts);
        loadDeviceContacts(contacts);
        return contacts;
    }


    void loadLatestAttendees(long count, ArrayList<Contact> contacts) {
        QueryBuilder<Attendee, Long> queryBuilder = attendeeDao.queryBuilder ();
        try {
            List<Attendee> attendeeList = queryBuilder.distinct().selectColumns(new String[] {"name", "photoUri"}).limit(count).query();
            if (attendeeList != null && attendeeList.size() > 0)
            for (Attendee attendee: attendeeList) {
                Contact contact = new Contact();
                contact.name = attendee.name;
                contact.photoUri = attendee.photoUri;
                contacts.add(contact);
            }

        } catch (SQLException e) {
            Logger.e(e);
        }
    }

	void loadDeviceContacts(ArrayList<Contact> contacts) {
		Cursor cursor = NPangApp.get().getContentResolver().query(Contacts.CONTENT_URI, projection,
						null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			do {
				Contact contact = new Contact();
				contact.id = cursor.getLong(0);
				contact.name = cursor.getString(1);
				String contactId = cursor.getString(2);
				if (!TextUtils.isEmpty(contactId)) {
					contact.photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id).toString();
				}
				contacts.add(contact);

				cursor.moveToNext();
			} while(!cursor.isAfterLast());
		}
//		return contacts;
	}

}
