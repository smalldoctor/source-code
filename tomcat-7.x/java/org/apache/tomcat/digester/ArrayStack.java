package org.apache.tomcat.digester;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Stack，只用在Digester包中
 *
 * @param <E>
 */
public class ArrayStack<E> extends ArrayList<E> {
    /**
     * Ensure serialization compatibility
     */
    private static final long serialVersionUID = 2130079159931574599L;

    public ArrayStack() {
        super();
    }

    public ArrayStack(int initialSize) {
        super(initialSize);
    }

    public boolean empty() {
        return isEmpty();
    }

    /**
     * Pushes a new item onto the top of this stack.
     *
     * @param item
     * @return
     */
    public E push(E item) {
        add(item);
        return item;
    }

    /**
     * 获取stack的顶层元素，但是不出栈
     *
     * @return
     * @throws EmptyStackException
     */
    public E peek() throws EmptyStackException {
        int n = size();
        if (n < 1) {
            throw new EmptyStackException();
        } else {
            return get(n - 1);
        }
    }

    /**
     * 元素是从0开始算；
     * 栈顶是第0个元素，依次为1，2，3...
     *
     * @param n
     * @return
     * @throws EmptyStackException
     */
    public E peek(int n) throws EmptyStackException {
        int m = (size() - n) - 1;
        if (m <= 0)
            throw new EmptyStackException();
        else
            return get(m);
    }


    public E pop() throws EmptyStackException {
        int n = size();
        if (n < 1)
            throw new EmptyStackException();
        return remove(n - 1);
    }
}
