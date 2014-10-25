package ru.gagauz.utils.collection;

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

    public static boolean contains(Object[] objects, Object object) {
        for (Object o : objects) {
            if (o == object || (o != null && o.equals(object))) {
                return true;
            }
        }
        return false;
    }
}
