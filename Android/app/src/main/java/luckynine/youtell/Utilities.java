package luckynine.youtell;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Weiliang on 7/7/2015.
 */
public class Utilities {
    public static Timestamp ConvertStringToTimestamp(String str_date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date date = formatter.parse(str_date);
            Timestamp timeStampDate = new Timestamp(date.getTime());

            return timeStampDate;
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static String GetTimeDifference(Timestamp timestamp){

        Timestamp timeNow = new Timestamp(new Date().getTime());
        if(timeNow.getYear() != timestamp.getYear()){
            int yearDiff = timeNow.getYear() - timestamp.getYear();
            if(yearDiff == 1) return "1 year ago";
            else return yearDiff + " years ago";
        }

        if(timeNow.getMonth() != timestamp.getMonth()){
            int monthDiff = timeNow.getMonth() - timestamp.getMonth();
            if(monthDiff == 1) return "1 month ago";
            else return monthDiff + " months ago";
        }

        if(timeNow.getDay() != timestamp.getDay()){
            int dayDiff = timeNow.getDay() - timestamp.getDay();
            if(dayDiff == 1) return "1 day ago";
            else return dayDiff + " days ago";
        }

        if(timeNow.getHours() != timestamp.getHours()){
            int hourDiff = timeNow.getHours() - timestamp.getHours();
            if(hourDiff == 1) return "1 hour ago";
            else return hourDiff + " hours ago";
        }

        if(timeNow.getMinutes() != timestamp.getMinutes()){
            int minuteDiff = timeNow.getMinutes() - timestamp.getMinutes();
            if(minuteDiff == 1) return "1 minute ago";
            else return minuteDiff + " minutes ago";
        }

        if(timeNow.getSeconds() != timestamp.getSeconds()){
            int secondDiff = timeNow.getSeconds() - timestamp.getSeconds();
            if(secondDiff == 1) return "1 second ago";
            else return secondDiff + " seconds ago";
        }

        return "Just now";
    }
}
