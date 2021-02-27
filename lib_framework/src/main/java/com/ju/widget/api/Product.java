package com.ju.widget.api;

import android.text.TextUtils;

import java.util.Objects;

/**
 * 产品分类定义
 */
public class Product implements Comparable<Product> {

    public final int mVersion;
    public final String mID;
    public final String mTitle;
    public final String mDescription;

    public Product(int version, String id, String title) {
        this(version, id, title, null);
    }

    public Product(int version, String id, String title, String description) {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Product ID must not empty.");
        }

        mVersion = version;
        mID = id;
        mTitle = (title == null ? "UnknownProduct" : title);
        mDescription = (description == null ? "UnknownDescription" : description);
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
        return mID == product.mID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTitle);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Product{");
        sb.append("mID='").append(mID).append('\'');
        sb.append(", mTitle='").append(mTitle).append('\'');
        sb.append(", mDescription='").append(mDescription).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Product o) {
        return mTitle.compareTo(o.mTitle);
    }
}
