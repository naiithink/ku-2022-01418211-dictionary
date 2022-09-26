package com.github.naiithink.app.util;

@FunctionalInterface
public interface Filterer<T, S> {

    T filter(T data, S filter);
}
