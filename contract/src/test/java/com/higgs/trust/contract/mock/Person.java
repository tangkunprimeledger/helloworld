package com.higgs.trust.contract.mock;

import com.higgs.trust.contract.mock.serialization.Deserializer;
import com.higgs.trust.contract.mock.serialization.SerializableEntity;
import com.higgs.trust.contract.mock.serialization.Serializer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Person implements Externalizable, SerializableEntity {
    private String name;
    private int age;

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


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeInt(age);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String)in.readObject();
        age = in.readInt();
    }

    @Override
    public String toString() {
        return String.format("name:%s, age:%", name, age);
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.writeString("name", name);
        serializer.writeInt("age", age);
    }

    @Override
    public void deserialize(Deserializer deserializer) {
        name = deserializer.readString("name");
        age = deserializer.readInt("age");
    }
}
