package mobi.threeam.npang.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.androidannotations.annotations.EBean;

import java.util.HashSet;
import java.util.List;

import mobi.threeam.npang.common.AnimUtils;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.ui.view.PaymentGroupItemView;
import mobi.threeam.npang.ui.view.PaymentGroupItemView_;

@EBean
public class PaymentGroupAdapter extends BaseAdapter {
	HashSet<Long> animatedIds;
	Context context;
	List<PaymentGroup> data;

	public PaymentGroupAdapter(Context context) {
		this.context = context;
		this.animatedIds = new HashSet<Long>();
	}
	
	public void setData(List<PaymentGroup> data) {
		this.data = data;
		notifyDataSetChanged();
	}

	public void addDataAt(int index, PaymentGroup group) {
		this.data.add(index, group);
		notifyDataSetChanged();
	}

    public void refresh(PaymentGroup group) {
        int index = 0;
        for (PaymentGroup item : this.data) {
            if (item.id == group.id) {
                break;
            }
            index ++;
        }
        if (index < data.size()) {
            data.add(index, group);
            data.remove(index + 1);
        }
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        PaymentGroupItemView view = (PaymentGroupItemView) convertView;
        if (view == null) {
        	view = PaymentGroupItemView_.build(context);
        } 
        PaymentGroup group = getItem(position);
        view.bind(group);
        if (!animatedIds.contains(group.id)) {
        	AnimUtils.slideIn(view);
        	animatedIds.add(group.id);
        }
		return view;
	}
	
	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public PaymentGroup getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).id;
	}

}
