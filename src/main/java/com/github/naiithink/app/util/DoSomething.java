package com.github.naiithink.app.util;

public final class DoSomething {
    private DoSomething() {}

    @FunctionalInterface
    public interface DoublyParam<T, S, U> {

        U doIt(T param1, S param2);
    }
}
