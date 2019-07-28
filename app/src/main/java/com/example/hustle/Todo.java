package com.example.hustle;

public class Todo {

    String ID;
    String text;

    Todo() {}

    Todo(String ID, String text) {
        this.ID = ID;
        this.text = text;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Todo)) {
            return false;
        }
        if (((Todo) obj).ID.equals(this.ID)
            && ((Todo) obj).text.equals(this.text)) {
            return true;
        } else {
            return false;
        }
    }
}
