package io.irina.backports.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.util.arithmetic.IntegerUtils;
import org.apache.commons.lang3.Validate;

import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.function.*;

// trimmed down version of SWMRHashTable
public class SWMRLong2ObjectHashTable<V> {

    protected int size;

    protected TableEntry<V>[] table;

    protected final float loadFactor;

    protected static final VarHandle SIZE_HANDLE = ConcurrentUtil.getVarHandle(SWMRLong2ObjectHashTable.class, "size", int.class);
    protected static final VarHandle TABLE_HANDLE = ConcurrentUtil.getVarHandle(SWMRLong2ObjectHashTable.class, "table", TableEntry[].class);

    /* size */

    protected final int getSizePlain() {
        return (int)SIZE_HANDLE.get(this);
    }

    protected final int getSizeOpaque() {
        return (int)SIZE_HANDLE.getOpaque(this);
    }

    protected final int getSizeAcquire() {
        return (int)SIZE_HANDLE.getAcquire(this);
    }

    protected final void setSizePlain(final int value) {
        SIZE_HANDLE.set(this, value);
    }

    protected final void setSizeOpaque(final int value) {
        SIZE_HANDLE.setOpaque(this, value);
    }

    protected final void setSizeRelease(final int value) {
        SIZE_HANDLE.setRelease(this, value);
    }

    /* table */

    protected final TableEntry<V>[] getTablePlain() {
        //noinspection unchecked
        return (TableEntry<V>[])TABLE_HANDLE.get(this);
    }

    protected final TableEntry<V>[] getTableAcquire() {
        //noinspection unchecked
        return (TableEntry<V>[])TABLE_HANDLE.getAcquire(this);
    }

    protected final void setTablePlain(final TableEntry<V>[] table) {
        TABLE_HANDLE.set(this, table);
    }

    protected final void setTableRelease(final TableEntry<V>[] table) {
        TABLE_HANDLE.setRelease(this, table);
    }

    protected static final int DEFAULT_CAPACITY = 16;
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected static final int MAXIMUM_CAPACITY = Integer.MIN_VALUE >>> 1;

    /**
     * Constructs this map with a capacity of {@code 16} and load factor of {@code 0.75f}.
     */
    public SWMRLong2ObjectHashTable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs this map with the specified capacity and load factor of {@code 0.75f}.
     * @param capacity specified initial capacity, > 0
     */
    public SWMRLong2ObjectHashTable(final int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs this map with the specified capacity and load factor.
     * @param capacity specified capacity, > 0
     * @param loadFactor specified load factor, > 0 and finite
     */
    public SWMRLong2ObjectHashTable(final int capacity, final float loadFactor) {
        final int tableSize = getCapacityFor(capacity);

        if (loadFactor <= 0.0 || !Float.isFinite(loadFactor)) {
            throw new IllegalArgumentException("Invalid load factor: " + loadFactor);
        }

        //noinspection unchecked
        final TableEntry<V>[] table = new TableEntry[tableSize];
        this.setTablePlain(table);

        if (tableSize == MAXIMUM_CAPACITY) {
            this.threshold = -1;
        } else {
            this.threshold = getTargetCapacity(tableSize, loadFactor);
        }

        this.loadFactor = loadFactor;
    }
    public boolean containsValue(final Object value) {
        Validate.notNull(value, "Null value");

        final TableEntry< V>[] table = this.getTableAcquire();
        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                final V currVal = curr.getValueAcquire();
                if (currVal == value || currVal.equals(value)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(final Object key, final Object value) {
        Validate.notNull(key, "Null key");

        //noinspection unchecked
        final TableEntry<V> entry = this.getEntryForOpaque((Long) key);

        if (entry == null) {
            return false;
        }

        final V entryVal = entry.getValueAcquire();
        return entryVal == value || entryVal.equals(value);
    }

    protected final int removeFromSizePlain(final int num) {
        final int newSize = this.getSizePlain() - num;

        this.setSizePlain(newSize);

        return newSize;
    }

    public int removeEntryIf(final Predicate<? super TableEntry<V>> predicate) {
        Validate.notNull(predicate, "Null predicate");

        int removed = 0;

        final TableEntry< V>[] table = this.getTablePlain();

        bin_iteration_loop:
        for (int i = 0, len = table.length; i < len; ++i) {
            TableEntry<V> curr = table[i];
            if (curr == null) {
                continue;
            }

            /* Handle bin nodes first */
            while (predicate.test(curr)) {
                ++removed;
                this.removeFromSizePlain(1); /* required in case predicate throws an exception */

                ArrayUtil.setRelease(table, i, curr = curr.getNextPlain());

                if (curr == null) {
                    continue bin_iteration_loop;
                }
            }

            TableEntry<V> prev;

            /* curr at this point is the bin node */

            for (prev = curr, curr = curr.getNextPlain(); curr != null;) {
                /* If we want to remove, then we should hold prev, as it will be a valid entry to link on */
                if (predicate.test(curr)) {
                    ++removed;
                    this.removeFromSizePlain(1); /* required in case predicate throws an exception */

                    prev.setNextRelease(curr = curr.getNextPlain());
                } else {
                    prev = curr;
                    curr = curr.getNextPlain();
                }
            }
        }

        return removed;
    }
    /**
     * Constructs this map with a capacity of {@code 16} or the specified map's size, whichever is larger, and
     * with a load factor of {@code 0.75f}.
     * All of the specified map's entries are copied into this map.
     * @param other The specified map.
     */
    public SWMRLong2ObjectHashTable(final SWMRLong2ObjectHashTable<V> other) {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, other);
    }

    /**
     * Constructs this map with a minimum capacity of the specified capacity or the specified map's size, whichever is larger, and
     * with a load factor of {@code 0.75f}.
     * All of the specified map's entries are copied into this map.
     * @param capacity specified capacity, > 0
     * @param other The specified map.
     */
    public SWMRLong2ObjectHashTable(final int capacity, final SWMRLong2ObjectHashTable<V> other) {
        this(capacity, DEFAULT_LOAD_FACTOR, other);
    }

    /**
     * Constructs this map with a min capacity of the specified capacity or the specified map's size, whichever is larger, and
     * with the specified load factor.
     * All of the specified map's entries are copied into this map.
     * @param capacity specified capacity, > 0
     * @param loadFactor specified load factor, > 0 and finite
     * @param other The specified map.
     */
    public SWMRLong2ObjectHashTable(final int capacity, final float loadFactor, final SWMRLong2ObjectHashTable<V> other) {
        this(Math.max(Validate.notNull(other, "Null map").size(), capacity), loadFactor);
        this.putAll(other);
    }

    public final float getLoadFactor() {
        return this.loadFactor;
    }

    protected static int getCapacityFor(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }
        if (capacity >= MAXIMUM_CAPACITY) {
            return MAXIMUM_CAPACITY;
        }
        return IntegerUtils.roundCeilLog2(capacity);
    }

    /** Callers must still use acquire when reading the value of the entry. */
    protected final TableEntry<V> getEntryForOpaque(final long key) {
        final int hash = SWMRLong2ObjectHashTable.getHash(key);
        final TableEntry<V>[] table = this.getTableAcquire();

        for (TableEntry<V> curr = ArrayUtil.getOpaque(table, hash & (table.length - 1)); curr != null; curr = curr.getNextOpaque()) {
            if (key == curr.key) {
                return curr;
            }
        }

        return null;
    }

    protected final TableEntry<V> getEntryForPlain(final long key) {
        final int hash = SWMRLong2ObjectHashTable.getHash(key);
        final TableEntry<V>[] table = this.getTablePlain();

        for (TableEntry<V> curr = table[hash & (table.length - 1)]; curr != null; curr = curr.getNextPlain()) {
            if (key == curr.key) {
                return curr;
            }
        }

        return null;
    }

    /* MT-Safe */

    /** must be deterministic given a key */
    protected static int getHash(final long key) {
        return (int)it.unimi.dsi.fastutil.HashCommon.mix(key);
    }

    // rets -1 if capacity*loadFactor is too large
    protected static int getTargetCapacity(final int capacity, final float loadFactor) {
        final double ret = (double)capacity * (double)loadFactor;
        if (Double.isInfinite(ret) || ret >= ((double)Integer.MAX_VALUE)) {
            return -1;
        }

        return (int)ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        /* Make no attempt to deal with concurrent modifications */
        if (!(obj instanceof SWMRLong2ObjectHashTable<?> other)) {
            return false;
        }

        if (this.size() != other.size()) {
            return false;
        }

        final TableEntry<V>[] table = this.getTableAcquire();

        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                final V value = curr.getValueAcquire();

                final Object otherValue = other.get(curr.key);
                if (otherValue == null || (value != otherValue && value.equals(otherValue))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        /* Make no attempt to deal with concurrent modifications */
        int hash = 0;
        final TableEntry<V>[] table = this.getTableAcquire();

        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                hash += curr.hashCode();
            }
        }

        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("SingleWriterMultiReaderHashMap:{");

        this.forEach((final long key, final V value) -> {
            builder.append("{key: \"").append(key).append("\", value: \"").append(value).append("\"}");
        });

        return builder.append('}').toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SWMRLong2ObjectHashTable<V> clone() {
        return new SWMRLong2ObjectHashTable<>(this.getTableAcquire().length, this.loadFactor, this);
    }

    /**
     * {@inheritDoc}
     */
    public void forEach(final Consumer<? super TableEntry<V>> action) {
        Validate.notNull(action, "Null action");

        final TableEntry<V>[] table = this.getTableAcquire();
        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                action.accept(curr);
            }
        }
    }
    protected Set<Long> keyset;
    protected Collection<V> values;
    protected Set<TableEntry<V>> entrySet;
    public Set<Long> keySet() {
        return this.keyset == null ? this.keyset = new KeySet<>(this) : this.keyset;
    }

    public Collection<V> values() {
        return this.values == null ? this.values = new ValueCollection<>(this) : this.values;
    }

    public Set<TableEntry<V>> entrySet() {
        return this.entrySet == null ? this.entrySet = new EntrySet<>(this) : this.entrySet;
    }
    @FunctionalInterface
    public interface BiLongObjectConsumer<V> {
        void accept(final long key, final V value);
    }

    /**
     * {@inheritDoc}
     */
    public void forEach(final BiLongObjectConsumer<? super V> action) {
        Validate.notNull(action, "Null action");

        final TableEntry<V>[] table = this.getTableAcquire();
        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                final V value = curr.getValueAcquire();

                action.accept(curr.key, value);
            }
        }
    }

    /**
     * Provides the specified consumer with all keys contained within this map.
     * @param action The specified consumer.
     */
    public void forEachKey(final LongConsumer action) {
        Validate.notNull(action, "Null action");

        final TableEntry<V>[] table = this.getTableAcquire();
        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                action.accept(curr.key);
            }
        }
    }
    public void forEachKey(final Consumer<? super Long> action) {
        Validate.notNull(action, "Null action");

        final TableEntry<V>[] table = this.getTableAcquire();
        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                action.accept(curr.key);
            }
        }
    }
    /**
     * Provides the specified consumer with all values contained within this map. Equivalent to {@code map.values().forEach(Consumer)}.
     * @param action The specified consumer.
     */
    public void forEachValue(final Consumer<? super V> action) {
        Validate.notNull(action, "Null action");

        final TableEntry<V>[] table = this.getTableAcquire();
        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> curr = ArrayUtil.getOpaque(table, i); curr != null; curr = curr.getNextOpaque()) {
                final V value = curr.getValueAcquire();

                action.accept(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public V get(final long key) {
        final TableEntry<V> entry = this.getEntryForOpaque(key);
        return entry == null ? null : entry.getValueAcquire();
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final long key) {
        // note: we need to use getValueAcquire, so that the reads from this map are ordered by acquire semantics
        return this.get(key) != null;
    }

    /**
     * {@inheritDoc}
     */
    public V getOrDefault(final long key, final V defaultValue) {
        final TableEntry<V> entry = this.getEntryForOpaque(key);

        return entry == null ? defaultValue : entry.getValueAcquire();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return this.getSizeAcquire();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return this.getSizeAcquire() == 0;
    }

    /* Non-MT-Safe */

    protected int threshold;

    protected final void checkResize(final int minCapacity) {
        if (minCapacity <= this.threshold || this.threshold < 0) {
            return;
        }

        final TableEntry<V>[] table = this.getTablePlain();
        int newCapacity = minCapacity >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : IntegerUtils.roundCeilLog2(minCapacity);
        if (newCapacity < 0) {
            newCapacity = MAXIMUM_CAPACITY;
        }
        if (newCapacity <= table.length) {
            if (newCapacity == MAXIMUM_CAPACITY) {
                return;
            }
            newCapacity = table.length << 1;
        }

        //noinspection unchecked
        final TableEntry<V>[] newTable = new TableEntry[newCapacity];
        final int indexMask = newCapacity - 1;

        for (int i = 0, len = table.length; i < len; ++i) {
            for (TableEntry<V> entry = table[i]; entry != null; entry = entry.getNextPlain()) {
                final long key = entry.key;
                final int hash = SWMRLong2ObjectHashTable.getHash(key);
                final int index = hash & indexMask;

                /* we need to create a new entry since there could be reading threads */
                final TableEntry<V> insert = new TableEntry<>(key, entry.getValuePlain());

                final TableEntry<V> prev = newTable[index];

                newTable[index] = insert;
                insert.setNextPlain(prev);
            }
        }

        if (newCapacity == MAXIMUM_CAPACITY) {
            this.threshold = -1; /* No more resizing */
        } else {
            this.threshold = getTargetCapacity(newCapacity, this.loadFactor);
        }
        this.setTableRelease(newTable); /* use release to publish entries in table */
    }

    protected final int addToSize(final int num) {
        final int newSize = this.getSizePlain() + num;

        this.setSizeOpaque(newSize);
        this.checkResize(newSize);

        return newSize;
    }

    protected final int removeFromSize(final int num) {
        final int newSize = this.getSizePlain() - num;

        this.setSizeOpaque(newSize);

        return newSize;
    }

    protected final V put(final long key, final V value, final boolean onlyIfAbsent) {
        final TableEntry<V>[] table = this.getTablePlain();
        final int hash = SWMRLong2ObjectHashTable.getHash(key);
        final int index = hash & (table.length - 1);

        final TableEntry<V> head = table[index];
        if (head == null) {
            final TableEntry<V> insert = new TableEntry<>(key, value);
            ArrayUtil.setRelease(table, index, insert);
            this.addToSize(1);
            return null;
        }

        for (TableEntry<V> curr = head;;) {
            if (key == curr.key) {
                if (onlyIfAbsent) {
                    return curr.getValuePlain();
                }

                final V currVal = curr.getValuePlain();
                curr.setValueRelease(value);
                return currVal;
            }

            final TableEntry<V> next = curr.getNextPlain();
            if (next != null) {
                curr = next;
                continue;
            }

            final TableEntry<V> insert = new TableEntry<>(key, value);

            curr.setNextRelease(insert);
            this.addToSize(1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public V put(final long key, final V value) {
        Validate.notNull(value, "Null value");

        return this.put(key, value, false);
    }

    /**
     * {@inheritDoc}
     */
    public V putIfAbsent(final long key, final V value) {
        Validate.notNull(value, "Null value");

        return this.put(key, value, true);
    }

    protected final V remove(final long key, final int hash) {
        final TableEntry<V>[] table = this.getTablePlain();
        final int index = (table.length - 1) & hash;

        final TableEntry<V> head = table[index];
        if (head == null) {
            return null;
        }

        if (head.key == key) {
            ArrayUtil.setRelease(table, index, head.getNextPlain());
            this.removeFromSize(1);

            return head.getValuePlain();
        }

        for (TableEntry<V> curr = head.getNextPlain(), prev = head; curr != null; prev = curr, curr = curr.getNextPlain()) {
            if (key == curr.key) {
                prev.setNextRelease(curr.getNextPlain());
                this.removeFromSize(1);

                return curr.getValuePlain();
            }
        }

        return null;
    }

    protected final V remove(final long key, final int hash, final V expect) {
        final TableEntry<V>[] table = this.getTablePlain();
        final int index = (table.length - 1) & hash;

        final TableEntry<V> head = table[index];
        if (head == null) {
            return null;
        }

        if (head.key == key) {
            final V val = head.value;
            if (val == expect || val.equals(expect)) {
                ArrayUtil.setRelease(table, index, head.getNextPlain());
                this.removeFromSize(1);

                return head.getValuePlain();
            } else {
                return null;
            }
        }

        for (TableEntry<V> curr = head.getNextPlain(), prev = head; curr != null; prev = curr, curr = curr.getNextPlain()) {
            if (key == curr.key) {
                final V val = curr.value;
                if (val == expect || val.equals(expect)) {
                    prev.setNextRelease(curr.getNextPlain());
                    this.removeFromSize(1);

                    return curr.getValuePlain();
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public V remove(final long key) {
        return this.remove(key, SWMRLong2ObjectHashTable.getHash(key));
    }

    public boolean remove(final long key, final V expect) {
        return this.remove(key, SWMRLong2ObjectHashTable.getHash(key), expect) != null;
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(final SWMRLong2ObjectHashTable<? extends V> map) {
        Validate.notNull(map, "Null map");

        final int size = map.size();
        this.checkResize(Math.max(this.getSizePlain() + size/2, size)); /* preemptively resize */
        map.forEach(this::put);
    }
    /**
     * Removes a key-value pair from this map if the specified predicate returns true. The specified predicate is
     * tested with every entry in this map. Returns the number of key-value pairs removed.
     * @param predicate The predicate to test key-value pairs against.
     * @return The total number of key-value pairs removed from this map.
     */
    public int removeIf(final BiPredicate<Long, V> predicate) {
        Validate.notNull(predicate, "Null predicate");

        int removed = 0;

        final TableEntry<V>[] table = this.getTablePlain();

        bin_iteration_loop:
        for (int i = 0, len = table.length; i < len; ++i) {
            TableEntry<V> curr = table[i];
            if (curr == null) {
                continue;
            }

            /* Handle bin nodes first */
            while (predicate.test(curr.key, curr.getValuePlain())) {
                ++removed;
                this.removeFromSizePlain(1); /* required in case predicate throws an exception */

                ArrayUtil.setRelease(table, i, curr = curr.getNextPlain());

                if (curr == null) {
                    continue bin_iteration_loop;
                }
            }

            TableEntry<V> prev;

            /* curr at this point is the bin node */

            for (prev = curr, curr = curr.getNextPlain(); curr != null;) {
                /* If we want to remove, then we should hold prev, as it will be a valid entry to link on */
                if (predicate.test(curr.key, curr.getValuePlain())) {
                    ++removed;
                    this.removeFromSizePlain(1); /* required in case predicate throws an exception */

                    prev.setNextRelease(curr = curr.getNextPlain());
                } else {
                    prev = curr;
                    curr = curr.getNextPlain();
                }
            }
        }

        return removed;
    }
    /**
     * {@inheritDoc}
     * <p>
     * This call is non-atomic and the order that which entries are removed is undefined. The clear operation itself
     * is release ordered, that is, after the clear operation is performed a release fence is performed.
     * </p>
     */
    public void clear() {
        Arrays.fill(this.getTablePlain(), null);
        this.setSizeRelease(0);
    }

    public static final class TableEntry<V> {

        protected final long key;
        protected V value;

        protected TableEntry<V> next;

        protected static final VarHandle VALUE_HANDLE = ConcurrentUtil.getVarHandle(TableEntry.class, "value", Object.class);
        protected static final VarHandle NEXT_HANDLE = ConcurrentUtil.getVarHandle(TableEntry.class, "next", TableEntry.class);

        /* value */

        protected final V getValuePlain() {
            //noinspection unchecked
            return (V)VALUE_HANDLE.get(this);
        }

        protected final V getValueAcquire() {
            //noinspection unchecked
            return (V)VALUE_HANDLE.getAcquire(this);
        }

        protected final void setValueRelease(final V to) {
            VALUE_HANDLE.setRelease(this, to);
        }

        /* next */

        protected final TableEntry<V> getNextPlain() {
            //noinspection unchecked
            return (TableEntry<V>)NEXT_HANDLE.get(this);
        }

        protected final TableEntry<V> getNextOpaque() {
            //noinspection unchecked
            return (TableEntry<V>)NEXT_HANDLE.getOpaque(this);
        }

        protected final void setNextPlain(final TableEntry<V> next) {
            NEXT_HANDLE.set(this, next);
        }

        protected final void setNextRelease(final TableEntry<V> next) {
            NEXT_HANDLE.setRelease(this, next);
        }

        protected TableEntry(final long key, final V value) {
            this.key = key;
            this.value = value;
        }
        public long getKey() {
            return this.key;
        }

        public V getValue() {
            return this.getValueAcquire();
        }

        /**
         * {@inheritDoc}
         */
        public V setValue(final V value) {
            if (value == null) {
                throw new NullPointerException();
            }

            final V curr = this.getValuePlain();

            this.setValueRelease(value);
            return curr;
        }

        protected static int hash(final long key, final Object value) {
            return SWMRLong2ObjectHashTable.getHash(key) ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return hash(this.key, this.getValueAcquire());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof TableEntry<?> other)) {
                return false;
            }
            final long otherKey = other.getKey();
            final long thisKey = this.getKey();
            final Object otherValue = other.getValueAcquire();
            final V thisVal = this.getValueAcquire();
            return (thisKey == otherKey) && (thisVal == otherValue || thisVal.equals(otherValue));
        }
    }
    protected static abstract class ViewCollection<V,T> implements Collection<T> {

        protected final SWMRLong2ObjectHashTable<V> map;

        protected ViewCollection(final SWMRLong2ObjectHashTable<V> map) {
            this.map = map;
        }

        @Override
        public boolean add(final T element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends T> collections) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> collection) {
            Validate.notNull(collection, "Null collection");

            boolean modified = false;
            for (final Object element : collection) {
                modified |= this.remove(element);
            }
            return modified;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        public boolean isEmpty() {
            return this.size() == 0;
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public boolean containsAll(final Collection<?> collection) {
            Validate.notNull(collection, "Null collection");

            for (final Object element : collection) {
                if (!this.contains(element)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Object[] toArray() {
            final List<T> list = new ObjectArrayList<>(this.size());

            list.addAll(this);

            return list.toArray();
        }

        @Override
        public <E> E[] toArray(final E[] array) {
            final List<T> list = new ObjectArrayList<>(this.size());

            list.addAll(this);

            return list.toArray(array);
        }

        @Override
        public <E> E[] toArray(final IntFunction<E[]> generator) {
            final List<T> list = new ObjectArrayList<>(this.size());

            list.addAll(this);

            return list.toArray(generator);
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (final T element : this) {
                hash += element == null ? 0 : element.hashCode();
            }
            return hash;
        }

        @Override
        public Spliterator<T> spliterator() { // TODO implement
            return Spliterators.spliterator(this, Spliterator.NONNULL);
        }
    }
    protected static abstract class ViewSet<V,T> extends ViewCollection<V,T> implements Set<T> {

        protected ViewSet(final SWMRLong2ObjectHashTable<V> map) {
            super(map);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Set)) {
                return false;
            }

            final Set<?> other = (Set<?>)obj;
            if (other.size() != this.size()) {
                return false;
            }

            return this.containsAll(other);
        }
    }
    protected static final class EntrySet<V> extends ViewSet<V, TableEntry<V>> implements Set<TableEntry<V>> {

        protected EntrySet(final SWMRLong2ObjectHashTable<V> map) {
            super(map);
        }

        @Override
        public boolean remove(final Object object) {
            if (!(object instanceof Map.Entry<?, ?> entry)) {
                return false;
            }

            final Object key;
            final Object value;

            try {
                key = entry.getKey();
                value = entry.getValue();
            } catch (final IllegalStateException ex) {
                return false;
            }

            return this.map.remove((long) key, (V) value);
        }

        @Override
        public boolean removeIf(final Predicate<? super TableEntry<V>> filter) {
            Validate.notNull(filter, "Null filter");

            return this.map.removeEntryIf(filter) != 0;
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            Validate.notNull(collection, "Null collection");

            return this.map.removeEntryIf((final TableEntry<V> entry) -> !collection.contains(entry)) != 0;
        }

        @Override
        public Iterator<TableEntry<V>> iterator() {
            return new EntryIterator<>(this.map.getTableAcquire(), this.map);
        }

        @Override
        public void forEach(final Consumer<? super TableEntry<V>> action) {
            this.map.forEach(action);
        }

        @Override
        public boolean contains(final Object object) {
            if (!(object instanceof Map.Entry<?, ?> entry)) {
                return false;
            }

            final Object key;
            final Object value;

            try {
                key = entry.getKey();
                value = entry.getValue();
            } catch (final IllegalStateException ex) {
                return false;
            }

            return this.map.contains(key, value);
        }

        @Override
        public String toString() {
            return CollectionUtil.toString(this, "SWMRHashTableEntrySet");
        }
    }
    protected static final class EntryIterator<V> extends TableEntryIterator<V, TableEntry<V>> {

        protected EntryIterator(final TableEntry<V>[] table, final SWMRLong2ObjectHashTable<V> map) {
            super(table, map);
        }

        @Override
        public TableEntry<V> next() {
            final TableEntry<V> curr = this.advanceEntry();

            if (curr == null) {
                throw new NoSuchElementException();
            }

            return curr;
        }
    }
    protected static final class ValueIterator<V> extends TableEntryIterator< V, V> {

        protected ValueIterator(final TableEntry<V>[] table, final SWMRLong2ObjectHashTable< V> map) {
            super(table, map);
        }

        @Override
        public V next() {
            final TableEntry<V> entry = this.advanceEntry();

            if (entry == null) {
                throw new NoSuchElementException();
            }

            return entry.getValueAcquire();
        }
    }
    protected static final class ValueCollection<V> extends ViewSet<V, V> implements Collection<V> {

        protected ValueCollection(final SWMRLong2ObjectHashTable<V> map) {
            super(map);
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator<>(this.map.getTableAcquire(), this.map);
        }

        @Override
        public void forEach(final Consumer<? super V> action) {
            Validate.notNull(action, "Null action");

            this.map.forEachValue(action);
        }

        @Override
        public boolean contains(final Object object) {
            Validate.notNull(object, "Null object");

            return this.map.containsValue(object);
        }

        @Override
        public boolean remove(final Object object) {
            Validate.notNull(object, "Null object");

            final Iterator<V> itr = this.iterator();
            while (itr.hasNext()) {
                final V val = itr.next();
                if (val == object || val.equals(object)) {
                    itr.remove();
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean removeIf(final Predicate<? super V> filter) {
            Validate.notNull(filter, "Null filter");

            return this.map.removeIf((final Long key, final V value) -> {
                return filter.test(value);
            }) != 0;
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            Validate.notNull(collection, "Null collection");

            return this.map.removeIf((final Long key, final V value) -> !collection.contains(value)) != 0;
        }

        @Override
        public String toString() {
            return CollectionUtil.toString(this, "SWMRHashTableValues");
        }
    }
    protected static final class KeySet<V> extends ViewSet<V, Long> {

        protected KeySet(final SWMRLong2ObjectHashTable<V> map) {
            super(map);
        }

        @Override
        public Iterator<Long> iterator() {
            return new KeyIterator<>(this.map.getTableAcquire(), this.map);
        }

        @Override
        public void forEach(final Consumer<? super Long> action) {
            Validate.notNull(action, "Null action");

            this.map.forEachKey(action);
        }

        @Override
        public boolean contains(final Object key) {
            Validate.notNull(key, "Null key");

            return this.map.containsKey((long) key);
        }

        @Override
        public boolean remove(final Object key) {
            Validate.notNull(key, "Null key");

            return this.map.remove((long) key) != null;
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            Validate.notNull(collection, "Null collection");

            return this.map.removeIf((final Long key, final V value) -> !collection.contains(key)) != 0;
        }

        @Override
        public boolean removeIf(final Predicate<? super Long> filter) {
            Validate.notNull(filter, "Null filter");

            return this.map.removeIf((final Long key, final V value) -> filter.test(key)) != 0;
        }

        @Override
        public String toString() {
            return CollectionUtil.toString(this, "SWMRHashTableKeySet");
        }
    }
    protected static final class KeyIterator<K, V> extends TableEntryIterator<V, Long> {

        protected KeyIterator(final TableEntry<V>[] table, final SWMRLong2ObjectHashTable<V> map) {
            super(table, map);
        }

        @Override
        public Long next() {
            final TableEntry<V> curr = this.advanceEntry();

            if (curr == null) {
                throw new NoSuchElementException();
            }

            return curr.key;
        }
    }
    protected static abstract class TableEntryIterator<V, T> implements Iterator<T> {

        protected final TableEntry<V>[] table;
        protected final SWMRLong2ObjectHashTable<V> map;

        /* bin which our current element resides on */
        protected int tableIndex;

        protected TableEntry<V> currEntry; /* curr entry, null if no more to iterate or if curr was removed or if we've just init'd */
        protected TableEntry<V> nextEntry; /* may not be on the same bin as currEntry */

        protected TableEntryIterator(final TableEntry<V>[] table, final SWMRLong2ObjectHashTable<V> map) {
            this.table = table;
            this.map = map;
            int tableIndex = 0;
            for (int len = table.length; tableIndex < len; ++tableIndex) {
                final TableEntry< V> entry = ArrayUtil.getOpaque(table, tableIndex);
                if (entry != null) {
                    this.nextEntry = entry;
                    this.tableIndex = tableIndex + 1;
                    return;
                }
            }
            this.tableIndex = tableIndex;
        }

        @Override
        public boolean hasNext() {
            return this.nextEntry != null;
        }

        protected final TableEntry< V> advanceEntry() {
            final TableEntry<V>[] table = this.table;
            final int tableLength = table.length;
            int tableIndex = this.tableIndex;
            final TableEntry<V> curr = this.nextEntry;
            if (curr == null) {
                return null;
            }

            this.currEntry = curr;

            // set up nextEntry

            // find next in chain
            TableEntry<V> next = curr.getNextOpaque();

            if (next != null) {
                this.nextEntry = next;
                return curr;
            }

            // nothing in chain, so find next available bin
            for (;tableIndex < tableLength; ++tableIndex) {
                next = ArrayUtil.getOpaque(table, tableIndex);
                if (next != null) {
                    this.nextEntry = next;
                    this.tableIndex = tableIndex + 1;
                    return curr;
                }
            }

            this.nextEntry = null;
            this.tableIndex = tableIndex;
            return curr;
        }

        @Override
        public void remove() {
            final TableEntry<V> curr = this.currEntry;
            if (curr == null) {
                throw new IllegalStateException();
            }

            this.map.remove(curr.key);

            this.currEntry = null;
        }
    }

}
