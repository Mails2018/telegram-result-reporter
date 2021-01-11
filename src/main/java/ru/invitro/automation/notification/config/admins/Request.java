package ru.invitro.automation.notification.config.admins;

import org.jvnet.hk2.annotations.Optional;

import java.util.Objects;

public class Request {

    Long id;

    @Optional
    String LastName;

    @Optional
    String UserName;

    @Optional
    String FirstName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    @Override
    public String toString() {
        return "Request{" +
            "id=" + id +
            ", LastName='" + LastName + '\'' +
            ", UserName='" + UserName + '\'' +
            ", FirstName='" + FirstName + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Request request = (Request) o;
        return id.equals(request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
