package ru.invitro.automation.notification.data.prices;

public class City implements Comparable {

    private String name;

    private String guid;

    public City(String name, String guid) {
        this.name = name;
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public int compareTo(Object o) {
        return this.name.compareTo(((City) o).getName());
    }
}
