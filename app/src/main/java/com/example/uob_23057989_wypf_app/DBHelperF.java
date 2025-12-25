package com.example.uob_23057989_wypf_app;

public class DBHelperF {
    String userName,number,email;
    long pass;
    public DBHelperF(String userName, String number, String email, long pass) {
        this.userName = userName;
        this.number = number;
        this.email = email;
        this.pass = pass;
    }
    public String getUserName() {
        return userName;
    }
    public String getNumber() {
        return number;
    }
    public String getEmail() {
        return email;
    }
    public long getPass() {
        return pass;
    }
}

