package org.lostcityinterfaceeditor.helpers;

public class StringUtils {

	public static String capitalizeFirstLetter(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
}
