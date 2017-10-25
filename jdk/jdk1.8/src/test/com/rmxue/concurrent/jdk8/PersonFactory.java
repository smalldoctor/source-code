package com.rmxue.concurrent.jdk8;

public interface PersonFactory<P extends Person> {
    P createPerson(String firstName, String lastName);
}
