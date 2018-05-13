package com.higgs.trust.rs.custom.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by young001 on 17/3/24.
 */
public class TimeUtil {
    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String unixtimeToDateString(Integer unixtime) {
        Long timestamp = Long.valueOf(unixtime) * 1000;
        String dateString = new SimpleDateFormat(DATE_FORMAT).format(new Date(timestamp));
        return dateString;
    }

    public static Integer dateStringToUnixtime(String dateString) {
        try {
            String dataStringTrim = dateString.trim();
            Date date = new SimpleDateFormat(DATE_FORMAT).parse(dataStringTrim);
            int unixTime = (int) (long) (date.getTime() / 1000);
            return unixTime;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String testDateString = "2017-03-23 11:10:21\n";
        System.out.println(dateStringToUnixtime(testDateString));
    }
}
