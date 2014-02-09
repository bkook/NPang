package mobi.threeam.npang.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.HashSet;

import mobi.threeam.npang.common.AnimUtils;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.ui.view.AttendeeItemView;
import mobi.threeam.npang.ui.view.AttendeeItemView_;

@EBean
public class AttendeeAdapter extends BaseAdapter {

	HashSet<Long> animatedIds;

    @RootContext
    Context context;

	ArrayList<Attendee> data;

	@AfterInject
	public void afterInject() {
		data = new ArrayList<Attendee>();
		animatedIds = new HashSet<Long>();
	}

	public void addItem(Attendee item) {
		data.add(0, item);
		notifyDataSetChanged();
	}
	
	public void removeItemAt(int position) {
		Attendee attendee = getItem(position);
		animatedIds.remove(attendee.id);
		data.remove(position);
		notifyDataSetChanged();
	}

	public void setData(ArrayList<Attendee> list) {
		data.clear();
		data.addAll(list);
		notifyDataSetChanged();
	}
	
	public boolean contains(String name) {
		for (Attendee attendee: data) {
			if (name.equals(attendee.name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Attendee getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AttendeeItemView view = (AttendeeItemView) convertView;
		if (view == null) {
			view = AttendeeItemView_.build(context);
		}
		Attendee attendee = getItem(position);
		view.bind(attendee);

		if (!animatedIds.contains(attendee.id)) {
			animatedIds.add(attendee.id);
			AnimUtils.dropDown(view);
		}
		return view;
	}

}
