package com.higgs.trust.contract.rhino.function;

import org.apache.commons.lang3.time.DateUtils;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.Calendar;

/**
 * @author liuyu
 * @description
 * @date 2018-11-09
 */
public class DateFuncs {
    private static DateFuncs instance = new DateFuncs();

    public static DateFuncs getInstance() {
        return instance;
    }

    /**
     * is same day
     *
     * @ first param by date-long value
     * @ second param by date-long value
     */
    public BaseFunction isSameDay = new IsSameDayFunc();

    private static class IsSameDayFunc extends BaseFunction {
        @Override public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            return DateUtils.isSameDay(formatDate(args[0]), formatDate(args[1]));
        }
    }

    private static Calendar formatDate(Object obj) {
        if (obj == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(String.valueOf(obj)));
        return calendar;
    }

    public static void main(String[] args) {
        Calendar leftNow = Calendar.getInstance();
        leftNow.set(Calendar.DAY_OF_YEAR, -1);
        Calendar rightNow = Calendar.getInstance();
        boolean r = DateUtils.isSameDay(leftNow, rightNow);
        System.out.println(r);
    }
}
