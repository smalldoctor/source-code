package java.util;

import java.io.Serializable;

/**
 * 用于key/value映射；不能存在相同key，一个key至多映射到一个value;
 * <p>
 * Map提供三种试图：key，value，key-value；Map的顺序依据不同的实现而不同，即有些Map的实现保证顺序，
 * 有的Map的实现不保证顺序;
 * <p>
 * Map的key需要注意，如果作为key的对象，在不通场景下equals返回不同值，则会带来不确定性;
 * Map的具体实现需要提供至少两个构造器：无参构造器和只有一个Map类型参数的构造器
 */
public interface Map<K, V> {
    /**
     * 返回key-value的数目；
     * 最大为 {@link Integer#MAX_VALUE}
     *
     * @return
     */
    int size();

    /**
     * 如果没有key-value,返回true
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 判断是否包含指定的key：
     * 1. key==null，则Map中存在一个null作为key的key-value
     * 2. key.equals(k)
     *
     * @param key
     * @return
     */
    boolean containsKey(Object key);

    /**
     * 判断是否包含指定的value：
     * 1. value=null, 则Map中存在一个null作为value的key-value
     * 2. value.equals(v)
     *
     * @param value
     * @return
     */
    boolean containsValue(Object value);

    /**
     * 1. 如果key为null，则返回Map中null作为key所映射的value
     * 2. 在允许null作为value的Map中，如果返回null，不代表Map不包含Given Key作为key的映射；
     * 因为存在Given Key作为key的映射的，只是value是null;
     *
     * @param key
     * @return
     */
    V get(Object key);

    /**
     * 如果key存在，则替换老的value；
     * key存在标准：
     * containsKey返回true
     *
     * @param key
     * @return 1. 如果put之前，Map不存在key，则返回null
     * 2. 如果put之前，Map存在key，则返回put之前的值
     * 3. 如果返回null，也可能是之前key所映射的value是null
     * @throws UnsupportedOperationException
     * @throws NullPointerException          如果给定的key或者value是null，但是Map不支持null
     */
    V put(K key, V value);

    /**
     * 移除Given key的Map:
     * key必须满足的条件：
     * key == null ？k=null : key.equals(K)
     *
     * @param key
     * @return 1. 如果key不存在，则返回null
     * 2. 如果key存在，则返回对应key的value
     * 3. 如果返回null，也可能是之前key所映射的value是null
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this map
     * @throws ClassCastException            if the key is of an inappropriate type for
     *                                       this map
     * @throws NullPointerException          if the specified key is null and this
     *                                       map does not permit null keys
     */
    V remove(K key);

    /**
     * 如果在putAll的过程中，Given Map发生修改，则最终putAll的结果是不确定的;
     *
     * @param m
     * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
     *                                       is not supported by this map
     * @throws ClassCastException            if the class of a key or value in the
     *                                       specified map prevents it from being stored in this map
     * @throws NullPointerException          if the specified map is null, or if
     *                                       this map does not permit null keys or values, and the
     *                                       specified map contains null keys or values
     * @throws IllegalArgumentException      if some property of a key or value in
     *                                       the specified map prevents it from being stored in this map
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *                                       is not supported by this map
     */
    void clear();

    /**
     * 返回Map的key的Set，对Set的移除操作，会对应的移除Mapping；
     * 但是Set不支持add和addAll操作;
     * <p>
     * 1. 在对Set进行迭代的时候，如果同时发生Set的修改（除迭代器自己的remove之外），结果是不确定的；
     *
     * @return
     */
    Set<K> keySet();

    /**
     * 所有的Value的集合,对Collection的移除操作，会对应的移除Mapping；
     * 但是Collection不支持add和addAll操作;
     *
     * @return
     */
    Collection<V> values();

    /**
     * key-value pair;
     * 除了通过setValue进行的操作，其他方式对Map的修改，会使得已经迭代返回的Entry的结果
     * 变的不确定;
     *
     * @return
     */
    Set<Map.Entry<K, V>> entrySet();

    interface Entry<K, V> {
        K getKey();

        V getValue();

        /**
         * 如果Mapping已经从Map中移除，则可能结果是不确定的
         *
         * @param value new value to be stored in this entry
         * @return set之前key对应的value
         * @throws UnsupportedOperationException if the <tt>put</tt> operation
         *                                       is not supported by the backing map
         * @throws ClassCastException            if the class of the specified value
         *                                       prevents it from being stored in the backing map
         * @throws NullPointerException          if the backing map does not permit
         *                                       null values, and the specified value is null
         * @throws IllegalArgumentException      if some property of this value
         *                                       prevents it from being stored in the backing map
         * @throws IllegalStateException         implementations may, but are not
         *                                       required to, throw this exception if the entry has been
         *                                       removed from the backing map.
         */
        V setValue(V value);

        /**
         * 如果满足以下条件：
         * e1.getKey() == null ?
         * e2.getKey() == null : e1.getKey.equals(e2.getKey());
         * e1.getValue() == null ?
         * e2.getValue() == null ? e2.getValue.equals(e2.getValue());
         *
         * @param o object to be compared for equality with this map entry
         * @return <tt>true</tt> if the specified object is equal to this map
         * entry
         */
        boolean equals(Object o);

        /**
         * hash code的计算规则：
         * ((e.getKey() == null)? 0 : e.getKey().hashCode()) ^
         * ((e.getValue() == null)? 0 : e.getValue().hashCode())
         *
         * @return
         */
        int hashCode();

        /**
         * 返回一个 {@link Comparator},用于比较Entry；按照Key进行比较;
         * 如果key为空，则返回 {@link NullPointerException}
         *
         * @param <K>
         * @param <V>
         * @return
         * @since 1.8
         */
        public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K, V>> comparingByKey() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                    (c1, c2) -> c1.getKey().compareTo(c2.getKey());
        }

        /**
         * @param <K>
         * @param <V>
         * @return
         */
        public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValue() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                    (c1, c2) -> c1.getValue().compareTo(c2.getValue());
        }


        /**
         * @param cmp
         * @param <K>
         * @param <V>
         * @return
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                    (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
        }

    }
}
