package com.example.hustle;

public class LoggedTime {
    long duration;

    LoggedTime(){
        duration = -1000;
    }

    LoggedTime(long duration) {
        this.duration = duration;
    }

    public LoggedTime addTime(long time) {
        return new LoggedTime(this.duration + time);
    }

    @Override
    public String toString() {
        return "USER DURATION: " + duration;
    }
}
