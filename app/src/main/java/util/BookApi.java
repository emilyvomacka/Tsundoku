package util;

import android.app.Application;

public class BookApi extends Application {
    private String username;
    private String userId;
    private static BookApi instance;

    public static  BookApi getInstance() {
        if (instance == null) {
            instance = new BookApi();
        }
        return instance;
    }

    public BookApi(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
