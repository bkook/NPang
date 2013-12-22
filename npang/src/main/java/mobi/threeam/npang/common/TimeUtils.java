package mobi.threeam.npang.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
	public static final String titlePattern = "yy. MM. dd. (E)";
	public static final SimpleDateFormat titleFormat = new SimpleDateFormat(titlePattern, Locale.KOREA);
	public static String buildTitle(Date date) {
		return titleFormat.format(date);
	}
/////

	public static final String pattern = "yyyy. MM. dd. a h:m";
	public static final SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.KOREA);

	public static String format(Date time) {
		return formatter.format(time);
	}

//	public static String format(long time) {
//		return formatter.format(new Date(time));
//	}
	
//	public static long parse(String timeStr) {
//		try {
//			return formatter.parse(timeStr).getTime();
//		} catch (ParseException e) {
//			Logger.e(e);
//		}
//		return 0;
//	}

}