package com.example.android.askquastionapp.contacts;

public class ContactBean {
    public String name;
    public String phone;
    public String note;
    public long contactId;
    public String address;

    public String toCurString() {
        return "ContactBean{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", note='" + note + '\'' +
                ", contactId=" + contactId +
                ", address='" + address + '\'' +
                '}';
    }
}
