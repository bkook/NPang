package mobi.threeam.npang.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import mobi.threeam.npang.database.dao.ContactsLoader.Contact;
import mobi.threeam.npang.ui.view.ContactItemView;
import mobi.threeam.npang.ui.view.ContactItemView_;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

@EBean
public class ContactAdapter extends BaseAdapter {

    @RootContext
    Context context;

	ArrayList<Contact> data;

	@AfterInject
	public void afterInject() {
		data = new ArrayList<Contact>();
	}

//	public AttendeeAdapter(Context context) {
//		this.context = context;
//	}

	public void addItem(Contact item) {
		data.add(item);
	}

	public void setData(List<Contact> list) {
		data.clear();
		data.addAll(list);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Contact getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactItemView view = (ContactItemView) convertView;
		if (view == null) {
			view = ContactItemView_.build(context);
		}
		Contact contact = getItem(position);
		view.bind(contact);
		return view;
	}

}
