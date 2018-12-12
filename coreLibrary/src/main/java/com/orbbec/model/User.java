package com.orbbec.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户业务Bean，实现了Parcelable接口
 */
public class User implements Parcelable {

    private String userId;
    private String personId;
    private String name;
    private String age;
    private String serverPersonId = "server_person_id";
    private String gender;
    private String score = "score";
    private String head;

    public User() {
    }

    public User(String personId, String name, String age, String gender) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public User(String userId, String personId, String name, String age, String serverPersonId, String gender, String score, String head) {
        this.userId = userId;
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.serverPersonId = serverPersonId;
        this.gender = gender;
        this.score = score;
        this.head = head;
    }

    protected User(Parcel in) {
        userId = in.readString();
        personId = in.readString();
        name = in.readString();
        age = in.readString();
        serverPersonId = in.readString();
        gender = in.readString();
        score = in.readString();
        head = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(personId);
        parcel.writeString(name);
        parcel.writeString(age);
        parcel.writeString(serverPersonId);
        parcel.writeString(gender);
        parcel.writeString(score);
        parcel.writeString(head);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getServerPersonId() {
        return serverPersonId;
    }

    public void setServerPersonId(String serverPersonId) {
        this.serverPersonId = serverPersonId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }
}
