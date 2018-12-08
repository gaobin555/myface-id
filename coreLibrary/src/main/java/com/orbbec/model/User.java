package com.orbbec.model;

/**
 *
 * @author mac
 * @date 16/7/4
 */
public class User {
    private String userId;
    private String personId;
    private String name;
    private String age;
    private String gender;
    private String  birdthday;


    public String getServerPersonId() {
        return serverPersonId;
    }

    public void setServerPersonId(String serverPersonId) {
        this.serverPersonId = serverPersonId;
    }

    private String serverPersonId = "serverPersonId";

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    private String score;
    private String head;

    public User(String personId, String name, String age, String gender,String birdthday) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.birdthday = birdthday;
    }

    public User() {

    }

    public String getBirdthday() {
        return birdthday;
    }

    public void setBirdthday(String birdthday) {
        this.birdthday = birdthday;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }


    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", personId='" + personId + '\'' +
                ", name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", gender='" + gender + '\'' +
                ", serverPersonId='" + serverPersonId + '\'' +
                ", score='" + score + '\'' +
                ", head='" + head + '\'' +
                '}';
    }
}
