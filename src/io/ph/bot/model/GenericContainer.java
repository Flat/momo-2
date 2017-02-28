package io.ph.bot.model;

public class GenericContainer<T> {
    private T t;

    public GenericContainer() {
    	
    }

    public GenericContainer(T t) {
        this.t = t;
    }

    public T getVal() {
        return t;
    }

    public void setVal(T t) {
        this.t = t;
    }
}
