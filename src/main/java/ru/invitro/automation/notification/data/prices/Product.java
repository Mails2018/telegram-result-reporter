package ru.invitro.automation.notification.data.prices;

import java.util.Objects;

public class Product {

    private String id;

    private String priceWeb;

    private String price1C;

    public Product(String id, String priceWeb, String price1C) {
        this.id = id;
        this.priceWeb = priceWeb;
        this.price1C = price1C;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPriceWeb() {
        return priceWeb;
    }

    public void setPriceWeb(String priceWeb) {
        this.priceWeb = priceWeb;
    }

    public String getPrice1C() {
        return price1C;
    }

    public void setPrice1C(String price1C) {
        this.price1C = price1C;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
            Objects.equals(priceWeb, product.priceWeb) &&
            Objects.equals(price1C, product.price1C);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, priceWeb, price1C);
    }

    @Override
    public String toString() {
        return "Product{" +
            "id='" + id + '\'' +
            ", priceWeb='" + priceWeb + '\'' +
            ", price1C='" + price1C + '\'' +
            '}';
    }
}
