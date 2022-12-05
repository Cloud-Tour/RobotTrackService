package com.zkd.robotTrack.service;

public interface Function<T, E> {

    T callback(E e);

}