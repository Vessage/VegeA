package cn.bahamut.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by alexchow on 16/4/1.
 */
public class DateHelper {
    private static final SimpleDateFormat accurateDateTimeFomatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final  SimpleDateFormat dateFomatter = new SimpleDateFormat("yyyy-MM-dd");

    private static final  SimpleDateFormat dateTimeFomatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final  SimpleDateFormat localDateTimeSimpleFomatter = new SimpleDateFormat("yy/MM/dd HH:mm");

    private static final  SimpleDateFormat localDateTimeFomatter  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final  SimpleDateFormat localDateFomatter = new SimpleDateFormat("yyyy-MM-dd");

    static{
        accurateDateTimeFomatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFomatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateTimeFomatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        localDateTimeSimpleFomatter.setTimeZone(TimeZone.getDefault());
        localDateTimeFomatter.setTimeZone(TimeZone.getDefault());
        localDateFomatter.setTimeZone(TimeZone.getDefault());
    }

    public static String toDateString(Date date)
    {
        return dateFomatter.format(date);
    }

    public static String toAccurateDateTimeString(Date date)
    {
        return accurateDateTimeFomatter.format(date);
    }

    public static String toDateTimeString(Date date)
    {
        return dateTimeFomatter.format(date);
    }

    public static Date stringToAccurateDate(String accurateDateTimeString)
    {
        try {
            return accurateDateTimeFomatter.parse(accurateDateTimeString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date stringToDateTime(String dateTimeString)
    {
        try {
            return dateTimeFomatter.parse(dateTimeString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date stringToDate(String dateString)
    {
        try {
            return dateTimeFomatter.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String toLocalDateTimeSimpleString(Date date)
    {
        return localDateTimeSimpleFomatter.format(date);
    }

    public static String toLocalDateTimeString(Date date)
    {
        return localDateTimeFomatter.format(date);
    }

    public static String toLocalDateString(Date date)
    {
        return localDateFomatter.format(date);
    }

    private static final int[] monthDays = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
    public static int daysOfMonth(int year,int month)
    {
        int monthIndex = month > 0 ? month - 1 : 0;
        if(monthIndex == 1)
        {
            return monthDays[monthIndex] + (isLeapYear(year) ? 1 : 0);
        }
        return monthDays[monthIndex];
    }

    public static boolean isLeapYear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            return true;
        } else {
            return false;
        }
    }
}
