package com.higgs.trust.contract.mock;

import com.alibaba.fastjson.JSONObject;

public class Person {
    private String name;
    private int age;
    private Colors color;
    private JSONObject state;

    public Person() {}

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Colors getColor() {
        return color;
    }

    public void setColor(Colors color) {
        this.color = color;
    }

    public JSONObject getState() {
        return state;
    }

    public void setState(JSONObject state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("name:%s, age:%d", name, age);
    }
}
