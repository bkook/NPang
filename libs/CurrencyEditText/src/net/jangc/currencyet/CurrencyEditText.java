package net.jangc.currencyet;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class CurrencyEditText extends EditText {
	Pattern pattern = Pattern.compile("[^0-9.]");

	NumberFormat formatter;
	String formatted;
	TextWatcher watcher;
	
	int fractionDigits = 0;
	
	public CurrencyEditText(Context context) {
		super(context);
		init();
	}

	public CurrencyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CurrencyEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	
	private void init() {
		formatter = NumberFormat.getCurrencyInstance(Locale.KOREA);
		fractionDigits = formatter.getCurrency().getDefaultFractionDigits();

		super.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (watcher != null) {
					watcher.onTextChanged(s, start, before, count); 
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (watcher != null) {
					watcher.beforeTextChanged(s, start, count, after);

				}
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.equals(formatted, s)) {
					return;
				}
				Matcher matcher = pattern.matcher(s);
				String replaced = matcher.replaceAll("");

				boolean prevHasDot = replaced.contains(".");

				try {
					if (prevHasDot) {
						formatted = formatter.format(Double.parseDouble(replaced));
					} else {
						formatted = formatter.format(Long.parseLong(replaced));
					}
				} catch (Exception e) {
					return;
				}
				int position = getSelectionStart();
				int length = s.length();
				setText(formatted);
				boolean nextHasDot = formatted.contains(".");

				if (prevHasDot == true &&  nextHasDot == true) {
					setSelection(formatted.length() - (length - position));
				} else if (prevHasDot == true && formatted.contains(".") == false) {
//					setSelection(formatted.length() - (length - position));
					// TODO 
				} else if (prevHasDot == false && formatted.contains(".") == true) {
					setSelection(formatted.length() - (length - position) - fractionDigits - 1);
				} else {
					setSelection(formatted.length() - (length - position));
				}

				if (watcher != null) {
					watcher.afterTextChanged(s);
				}
			}
		});
	}
	
	public Number getNumber() {
		String val = getStrippedText();
		if (TextUtils.isEmpty(val)) {
			return 0;
		}
		try {
			if (val.contains(".")) {
				return Double.parseDouble(val);
			} else {
				return Long.parseLong(val);
			}
		} catch (Exception e) {
//			Logger.e(e);
		}

		return 0;
	}
	
	public String getStrippedText() {
		Matcher matcher = pattern.matcher(getText());
		return matcher.replaceAll("");
	}
	
	@Override
	public void addTextChangedListener(final TextWatcher watcher) {
		this.watcher = watcher;
	}
	
//	@Override
//	public void setText(CharSequence text, BufferType type) {
//		if (!TextUtils.isEmpty(text)) {
//			try {
//				String formatted = formatter.format(text);
//				super.setText(formatted, type);
//			} catch (IllegalArgumentException e) {
//				super.setText(text, type);
//			}
//		} else {
//			super.setText(text, type);
//		}
//	}

}
