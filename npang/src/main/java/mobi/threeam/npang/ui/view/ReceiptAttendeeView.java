package mobi.threeam.npang.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.threeam.npang.R;
import mobi.threeam.npang.common.Logger;
import mobi.threeam.npang.common.TextViewUtils;
import mobi.threeam.npang.database.DBHelper;
import mobi.threeam.npang.database.dao.AttendeeDao;
import mobi.threeam.npang.database.dao.PaymentGroupDao;
import mobi.threeam.npang.database.model.Attendee;
import mobi.threeam.npang.database.model.PaymentGroup;
import mobi.threeam.npang.event.PaymentGroupChangedEvent;

@EViewGroup(R.layout.item_receipt_attendee)
public class ReceiptAttendeeView extends RelativeLayout {
//	Attendee attendee;

	@ViewById(R.id.attendee)
	CheckBox attendeeView;

	@ViewById(R.id.amount)
	TextView amountView;
	
	@OrmLiteDao(helper = DBHelper.class, model = Attendee.class)
	AttendeeDao attendeeDao;

    @OrmLiteDao(helper = DBHelper.class, model = PaymentGroup.class)
    PaymentGroupDao paymentGroupDao;


	public ReceiptAttendeeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ReceiptAttendeeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ReceiptAttendeeView(Context context) {
		super(context);
	}
	
	public void bind(final Attendee attendee, int amount) {
        TextViewUtils.currency(amountView, amount);

		boolean isPaid = attendee.paidAt != null;
		this.attendeeView.setText(attendee.name);
		this.attendeeView.setChecked(isPaid);
		TextViewUtils.strike(attendeeView, isPaid);
        TextViewUtils.strike(amountView, isPaid);

		this.attendeeView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                attendee.paidAt = isChecked ? new Date() : null;
                attendeeView.setChecked(isChecked);
                TextViewUtils.strike(attendeeView, isChecked);
                TextViewUtils.strike(amountView, isChecked);
                try {
                    PaymentGroup paymentGroup = attendee.paymentGroup;

                    attendeeDao.update(attendee);
                    List<Attendee> attendees = attendeeDao.queryForEq ("paymentGroup_id", attendee.paymentGroup.id);

                    boolean completed = true;
                    for (Attendee attendee: attendees) {
                        if (attendee.paidAt == null) {
                            completed = false;
                            break;
                        }
                    }
                    if (paymentGroup.completed != completed) {
                        paymentGroup.completed = completed;
                        if (completed) {
                            paymentGroup.alarmEnabled = false;
                        }
                        paymentGroupDao.update(paymentGroup);
                        EventBus.getDefault().post(new PaymentGroupChangedEvent(paymentGroup));
                    }
                } catch (SQLException e) {
                    Logger.e(e);
                }
            }
        });
	}

}
