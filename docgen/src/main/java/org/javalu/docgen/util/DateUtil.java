package org.javalu.docgen.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String getUTCTimeStr() {
        StringBuffer UTCTimeBuffer = new StringBuffer();
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day) ;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.parse(UTCTimeBuffer.toString()) ;
            return UTCTimeBuffer.toString() ;
        }catch(ParseException e)
        {
            e.printStackTrace() ;
        }
        return null ;
    }

    public static String getYear(){
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        int year = cal.get(Calendar.YEAR);
        return year+"";
    }

    public static String getMonth(){
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        int month = cal.get(Calendar.MONTH)+1;
        return String.format("%02d", month);
    }

    public static String getDay(){
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format("%02d", day);
    }

    public static String getspDateStr(){
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance() ;
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return year+String.format("%03d", month)+String.format("%02d", day-3)+"-"+String.format("%03d", month)+String.format("%02d", day+1);
    }

}
