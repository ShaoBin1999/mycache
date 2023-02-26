package com.bsren.mycache.interceptor;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

public class SimpleKey implements Serializable {

    public static final SimpleKey EMPTY = new SimpleKey();

    private final Object[] params;

    private transient int hashCode;

    public SimpleKey(Object... elements){
        Assert.notNull(elements, "Elements must not be null");
        this.params = elements.clone();
        this.hashCode = Arrays.deepHashCode(this.params);
    }

    @Override
    public boolean equals(Object o) {
        return this==o ||
                (o instanceof SimpleKey && Arrays.deepEquals(this.params,((SimpleKey) o).params));
    }



    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + StringUtils.arrayToCommaDelimitedString(this.params) + "]";
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // Re-calculate hashCode field on deserialization
        this.hashCode = Arrays.deepHashCode(this.params);
    }

}
