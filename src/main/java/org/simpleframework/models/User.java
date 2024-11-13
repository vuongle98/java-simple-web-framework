package org.simpleframework.models;

import org.simpleframework.annotations.GeneratedValue;
import org.simpleframework.annotations.Id;
import org.simpleframework.annotations.Table;

@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;

    public User() {}

    public User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String toString() {
        return "User [id=" + id + ", name=" + name + "]";
    }
}
