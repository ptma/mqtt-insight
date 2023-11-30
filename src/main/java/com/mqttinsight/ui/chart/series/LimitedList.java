package com.mqttinsight.ui.chart.series;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * 有数量或时间限制的 List， 线程安全的
 *
 * @param <E>
 * @author ptma
 */
public class LimitedList<E extends TimeBasedElement> implements List<E> {

    private final List<E> delegate;

    private Limit limit;

    public LimitedList() {
        this(Limit.of(0, 0, ""));
    }

    public LimitedList(final Limit limit) {
        this.limit = limit;
        this.delegate = Collections.synchronizedList(new ArrayList<>());
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    private void clearExpiredElements() {
        if (limit.getTimeLimit() > 0) {
            boolean removed = delegate.removeIf(element -> element.getTimestamp() + limit.getTimeLimit() < System.currentTimeMillis());
        }
    }

    private boolean isMaximum() {
        return limit.getSizeLimit() > 0 && size() >= limit.getSizeLimit();
    }

    private boolean isMaximum(int tobeAdded) {
        return limit.getSizeLimit() > 0 && size() >= limit.getSizeLimit() + tobeAdded;
    }

    @Override
    public int size() {
        clearExpiredElements();
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        clearExpiredElements();
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        clearExpiredElements();
        return delegate.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        clearExpiredElements();
        return delegate.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        clearExpiredElements();
        delegate.forEach(action);
    }

    @Override
    public Object[] toArray() {
        clearExpiredElements();
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        clearExpiredElements();
        return delegate.toArray(a);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        clearExpiredElements();
        return delegate.toArray(generator);
    }

    @Override
    public boolean add(E e) {
        clearExpiredElements();
        while (isMaximum()) {
            remove(0);
        }
        return delegate.add(e);
    }

    @Override
    public boolean remove(Object o) {
        clearExpiredElements();
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        clearExpiredElements();
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        clearExpiredElements();
        while (isMaximum(c.size())) {
            remove(0);
        }
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        clearExpiredElements();
        while (isMaximum(c.size())) {
            remove(0);
        }
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        clearExpiredElements();
        return delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        clearExpiredElements();
        return delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        clearExpiredElements();
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        clearExpiredElements();
        delegate.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        clearExpiredElements();
        delegate.sort(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public E get(int index) {
        clearExpiredElements();
        return delegate.get(index);
    }

    @Override
    public E set(int index, E element) {
        clearExpiredElements();
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        clearExpiredElements();
        delegate.add(index, element);
        while (isMaximum()) {
            remove(0);
        }
    }

    @Override
    public E remove(int index) {
        clearExpiredElements();
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        clearExpiredElements();
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        clearExpiredElements();
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        clearExpiredElements();
        return delegate.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        clearExpiredElements();
        return delegate.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        clearExpiredElements();
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        clearExpiredElements();
        return delegate.spliterator();
    }

    @Override
    public Stream<E> stream() {
        clearExpiredElements();
        return delegate.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        clearExpiredElements();
        return delegate.parallelStream();
    }
}
