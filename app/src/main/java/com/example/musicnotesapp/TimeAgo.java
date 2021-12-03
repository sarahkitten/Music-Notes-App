/*
Source Code for the initialization used in the Audio Recording, CIS422 FA21
Author(s): Kale Satta-Hutton
Last Edited: 12/2/21
Sources:
    Base version of the code:
    https://www.youtube.com/watch?v=z--VaNj6l1U&ab_channel=TVACStudio
*/

package com.example.musicnotesapp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    public String getTimeAgo(long duration) {
        Date now = new Date(); // create Time object

        // get the seconds of how long the file has been alive for
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        // get how many minutes the file has been alive for
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        // get how many hours the file has been alive for
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        // get how many days the file has been alive for
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);


        if(seconds < 60){ // if file was created less than a minute ago
            return "just now";
        } else if (minutes == 1) {// if file was created exactly a minute ago
            return "a minute ago";
        } else if (minutes > 1 && minutes < 60) { // if file was created more than one minute but less than an hour ago
            return minutes + " minutes ago";
        } else if (hours == 1) { // if file was created an hour ago
            return "an hour ago";
        } else if (hours > 1 && hours < 24) { // if file was created more than an hour ago but less than a day ago
            return hours + " hours ago";
        } else if (days == 1) { // if file was created a day ago
            return "a day ago";
        } else { // if not the others above return how many days ago it was created
            return days + " days ago";
        }

    }

}
