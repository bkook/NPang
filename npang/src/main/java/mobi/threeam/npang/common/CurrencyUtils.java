package mobi.threeam.npang.common;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by jangc on 2013. 12. 25..
 */
public class CurrencyUtils {
    public static String format(double amount) {
        String val = NumberFormat.getCurrencyInstance(Locale.KOREA).format(amount);
        return val;
    }

}
