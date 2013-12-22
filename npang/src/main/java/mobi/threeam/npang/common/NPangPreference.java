package mobi.threeam.npang.common;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

@SharedPref(value=Scope.APPLICATION_DEFAULT)
public interface NPangPreference {
	
	@DefaultString("")
	String bankName();

	@DefaultString("")
	String accountNumber();
}
