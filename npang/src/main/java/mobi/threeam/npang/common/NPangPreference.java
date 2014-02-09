package mobi.threeam.npang.common;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value=SharedPref.Scope.APPLICATION_DEFAULT)
public interface NPangPreference {
	
	@DefaultString("")
	String bankName();

	@DefaultString("")
	String accountNumber();
}
