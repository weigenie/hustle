package com.example.hustle;

import java.util.ArrayList;
import java.util.List;

public class User {
    long duration;

    User(){
        duration = -1000;
    }

    User(long duration) {
        this.duration = duration;
    }

    public User addTime(long time) {
        return new User(this.duration + time);
    }

    @Override
    public String toString() {
        return "USER DURATION: " + duration;
    }
}
