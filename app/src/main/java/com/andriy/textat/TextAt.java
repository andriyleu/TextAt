package com.andriy.textat;

import android.app.Application;

public class TextAt extends Application {

    private String usernick;

    public String getUsernick() {
        return usernick;
    }

    public void setUsernick(String nick) {
        this.usernick = nick;
    }
}