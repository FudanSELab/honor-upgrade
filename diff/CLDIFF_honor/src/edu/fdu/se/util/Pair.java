package edu.fdu.se.util;

import java.util.Objects;

public class Pair<T, E> {
    public final T first;
    public final E second;

    public Pair(T first, E second) {
        this.first = first;
        this.second = second;
    }

    public static <T, E> Pair<T, E> of(T first, E second) {
        return new Pair<>(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    public T getFirst() {
        return first;
    }

    public E getSecond() {
        return second;
    }
}
