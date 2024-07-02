package com.example.myapplication;

public class Card {
    private int id;
    private String name;
    private String qrValue;
    private String password;
    private int balance;

    public Card(int id, String name, String qrValue, String password, int balance) {
        this.id = id;
        this.name = name;
        this.qrValue = qrValue;
        this.password = password;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQrValue() {
        return qrValue;
    }

    public String getPassword() {
        return password;
    }

    public int getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", qrValue='" + qrValue + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                '}';
    }
}
