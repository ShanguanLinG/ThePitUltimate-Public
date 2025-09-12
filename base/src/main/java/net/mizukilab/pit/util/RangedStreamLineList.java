package net.mizukilab.pit.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class RangedStreamLineList<T> extends ConcurrentLinkedDeque<T> {
    AtomicInteger ATOMIC = new AtomicInteger();
    private final Predicate<T> predicate;
    private final int maxElement;

    public RangedStreamLineList(int maxElement, Predicate<T> t) {
        super();
        this.maxElement = maxElement;
        this.predicate = t;
    }

    public RangedStreamLineList(int maxElement, Predicate<T> t, Collection<T> t2) {
        super(t2);
        this.maxElement = maxElement;
        this.predicate = t;
    }

    @Override
    public boolean offerFirst(T t) {
        recycle();
        boolean b = super.offerFirst(t);
        ATOMIC.decrementAndGet();
        return b;
    }

    @Override
    public boolean offerLast(T t) {
        throw new UnsupportedOperationException("Only can offer at first");
    }

    @Override
    public void addFirst(T t) {
        this.offerFirst(t);
        ATOMIC.incrementAndGet();
    }

    @Override
    public void addLast(T t) {
        this.offerLast(t);
        ATOMIC.incrementAndGet();
    }

    @Override
    public boolean add(T t) {

        ATOMIC.incrementAndGet();
        this.offerFirst(t);
        return true;
    }

    @Override
    public T pollFirst() {

        ATOMIC.incrementAndGet();
        return super.pollFirst();
    }

    @Override
    public T pollLast() {
        ATOMIC.incrementAndGet();
        return super.pollLast();
    }

    public void recycle() {
        //stage 1
        while (peekLast() != null) {
            int acquire = ATOMIC.getAcquire();
            if (!(acquire > maxElement || predicate.test(peekLast()))) break;
            pollLast();
        }
    }

}
