package basic.skiplist;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: basic.skiplist.SkipListNode
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018-09-24 20:53
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018-09-24      xuecy           v1.0.0               修改原因
 */
public class SkipListNode<T> {
    public int key;
    public T value;

    public SkipListNode<T> up, left, down, right;

    public static final int HEAD_KEY = Integer.MIN_VALUE; // 负无穷
    public static final int TAIL_KEY = Integer.MAX_VALUE; // 正无穷

    public SkipListNode(int k, T v) {
        key = k;
        value = v;
    }

    @Override
    public String toString() {
        return "key-value:" + key + "-" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof SkipListNode<?>)) {
            return false;
        }
        SkipListNode<T> ent;
        try {
            ent = (SkipListNode<T>) o; // 检测类型
        } catch (ClassCastException ex) {
            return false;
        }
        return (ent.getKey() == key) && (ent.getValue() == value);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
