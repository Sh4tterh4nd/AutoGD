package io.kellermann;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Test {
    public static void main(String[] args) {
        LocalTime localTime = LocalTime.ofNanoOfDay(1882293333000L);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        System.out.println(dtf.format(localTime));
    }
}
