package mejje;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class Utils {
	static Vector split(String original, String separator) {
		Vector nodes = new Vector();
		// Parse nodes into vector
		int index = original.indexOf(separator);
		while(index >= 0) {
			nodes.addElement( original.substring(0, index) );
			original = original.substring(index+separator.length());
			index = original.indexOf(separator);
		}
		// Get the last node
		nodes.addElement( original );
		return nodes;
	}

	static String trimLines(String original) {
		StringBuffer buffer = new StringBuffer();
		Enumeration lines = split(original, "\n").elements();
		while (lines.hasMoreElements()) {
			String line = (String) lines.nextElement();
			buffer.append((buffer.length() > 0 ? "\n" : "") + line.trim());
		}
		return buffer.toString();
	}

	static String getDateString(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String dateString = year + "-" +
			(month < 10 ? "0" : "") + month + "-" +
			(day < 10 ? "0" : "") + day;
		return dateString;
	}

	static String getTimeString(Date time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		String timeString = (hour < 10 ? "0" : "") + hour + ":" +
			(minute < 10 ? "0" : "") + minute;
		return timeString;
	}
}
