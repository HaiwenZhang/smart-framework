package com.haiwen.smart.framework.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static final String SEPARATOR = String.valueOf((char) 29);

    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public static String[] splitString(String str, String separator) {
        return StringUtils.splitByWholeSeparator(str, separator);
    }

}
