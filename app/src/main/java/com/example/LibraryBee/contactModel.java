package com.example.LibraryBee;

public class contactModel {

    int img;
    String name,number;

    public contactModel(int img,String name,String num){

        this.name=name;
        this.number=num;
        this.img=img;
    }

    public int getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return  number;
    }


    public contactModel(String name,String number){

        this.name=name;
        this.number=number;
    }
}
