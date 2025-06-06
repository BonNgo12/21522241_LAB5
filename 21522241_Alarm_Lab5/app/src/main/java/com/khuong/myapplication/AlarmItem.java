package com.khuong.myapplication;

public class AlarmItem {
    public String time;
    public String label;
    public String days;

    public AlarmItem(String time, String label, String days) {
        this.time = time;
        this.label = label;
        this.days = days;
    }

    public String getTime() {
        return time;
    }

    public String getLabel() {
        return label;
    }

    public String getDays() {
        return days;
    }
}
