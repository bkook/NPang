package mobi.threeam.npang.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mobi.threeam.npang.common.search.SoundSearcher;
import mobi.threeam.npang.database.dao.ContactsLoader.Contact;
import mobi.threeam.npang.ui.view.ContactItemView;
import mobi.threeam.npang.ui.view.ContactItemView_;

@EBean
public class ContactAdapter extends BaseAdapter implements Filterable {

    @RootContext
    Context context;

    ArrayList<Contact> original;
	ArrayList<Contact> data;

    ContactFilter filter;

	@AfterInject
	public void afterInject() {
		data = new ArrayList<Contact>();
        original = new ArrayList<Contact>();
        filter = new ContactFilter();
	}

//	public AttendeeAdapter(Context context) {
//		this.context = context;
//	}

	public void addItem(Contact item) {
		data.add(item);
        original.add(item);
	}

	public void setData(List<Contact> list) {
        original.clear();
        original.addAll(list);
        getFilter().filter(filter.lastKey);
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

    @Override
    public Filter getFilter() {
        return new ContactFilter();
    }


    public class ContactFilter extends Filter {
        public String lastKey = "";

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String key = constraint.toString();
            lastKey = key;
            FilterResults results = new FilterResults();
            ArrayList<Contact> filteredContacts = new ArrayList<Contact>();
            for (int i = 0; i < original.size(); i++) {
                Contact contact = original.get(i);
                if (SoundSearcher.matchString(contact.name, key)) {
                    filteredContacts.add(contact);
                }
            }

            results.count = filteredContacts.size();
            results.values = filteredContacts;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            data.clear();
            data.addAll((Collection<Contact>) results.values);
            notifyDataSetChanged();
        }
    }

}
