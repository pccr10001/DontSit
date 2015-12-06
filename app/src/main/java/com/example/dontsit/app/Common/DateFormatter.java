package com.example.dontsit.app.Common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static SimpleDateFormat short_formatter =
            new SimpleDateFormat("MM/dd HH", Locale.getDefault());

    public static Date parse(String date) throws ParseException {
        return formatter.parse(date);
    }

    public static String format(Date date) throws ParseException {
        return formatter.format(date);
    }

    public static String short_format(Date date) throws ParseException {
        return short_formatter.format(date);
    }
}
