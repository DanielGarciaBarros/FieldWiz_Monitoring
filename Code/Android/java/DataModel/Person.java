package com.example.danielgarcia.fieldwiz_monitoring.DataModel;

import java.io.Serializable;

/**
 * Created by danielgarcia on 06.05.17.
 */

public class Person implements Serializable {
    private String firstname;
    private String name;

    public Person(String firstname, String name) {
        this.firstname = firstname;
        this.name = name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
