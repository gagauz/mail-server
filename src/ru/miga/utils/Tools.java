package ru.miga.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Tools {
    private Tools() {
    }

    public static <T> ArrayList<T> arrayList() {
        return new ArrayList<T>();
    }

    public static <T> LinkedList<T> linkedList() {
        return new LinkedList<T>();
    }

    public static <K, V> HashMap<K, V> hasMap() {
        return new HashMap<K, V>();
    }
}
