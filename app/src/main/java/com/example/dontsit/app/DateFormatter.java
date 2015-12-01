package com.example.dontsit.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static SimpleDateFormat formatter =
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.TAIWAN);

    public static Date parse(String date) throws ParseException {
        return formatter.parse(date);
    }

    public static String format(Date date) throws ParseException {
        return formatter.format(date);
    }
}
