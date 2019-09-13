package com.example.nav;

public class History {

    public  static String email,search,id;

    public History(){

    }

    public History(String search,String email,  String id) //, String search, String id
    {
        this.search = search;
        this.email=email;
        this.id = id;
    }

    public String getSearch() {
        return search;
    }
    public static void setSearch(String search) {
        History.search = search;
    }
    public String getEmail() {
        return email;
    }
    public static void setEmail(String email) {
        History.email = email;
    }
    public String getId() {
        return id;
    }
    public static void setId(String id) {
        History.id = id;
    }



}