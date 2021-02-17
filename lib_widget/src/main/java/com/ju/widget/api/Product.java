package com.ju.widget.api;

import java.util.Objects;

/**
 * 产品分类定义
 */
public class Product implements Comparable<Product> {

    public final int mID;
    public final String mTitle;

    public Product(int id, String title) {
        mID = id;
        mTitle = title == null ? "UnknownProduct" : title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Product product = (Product) o;
        return Objects.equals(mTitle, product.mTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTitle);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Product{");
        sb.append("mTitle='").append(mTitle).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Product o) {
        return mTitle.compareTo(o.mTitle);
    }
}
