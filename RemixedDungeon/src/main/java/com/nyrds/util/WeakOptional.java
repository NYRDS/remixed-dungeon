package com.nyrds.util;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class WeakOptional<T> {

    private WeakReference<T> reference;


    private WeakOptional(T value) {
        reference = new WeakReference<>(Objects.requireNonNull(value));
    }

    private WeakOptional() {
    }

    public void clear() {
        if(reference != null) {
            reference.clear();
        }
    }

    public static<T> WeakOptional<T> empty() {
        return new WeakOptional<>();
    }

    public static<T> WeakOptional<T> of(T value) {
        return new WeakOptional<>(value);
    }

    public interface Action<T> {
        void apply(T value);
    }

    public void ifPresent(Action<T> action) {
        if (reference != null) {
            T value = reference.get();
            if(value!=null) {
                action.apply(value);
            }
        }
    }

}
