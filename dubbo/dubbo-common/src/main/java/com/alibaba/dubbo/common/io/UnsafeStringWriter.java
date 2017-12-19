package com.alibaba.dubbo.common.io;

import java.io.IOException;
import java.io.Writer;

/**
 * 非线程安全
 */
public class UnsafeStringWriter extends Writer {
    /**
     * 自定义Writer，将字符串的输出写入进指定的字符串缓冲中；
     */
    private StringBuilder mBuffer;

    //-------------------------------------------------  Constructors
    public UnsafeStringWriter() {
        lock = mBuffer = new StringBuilder();
    }

    public UnsafeStringWriter(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }

        lock = mBuffer = new StringBuilder();
    }

    @Override
    public void write(char[] cs, int off, int len) throws IOException {
        if ((off < 0) || (off > cs.length) || (len < 0)
                || (off + len > cs.length) || ((off + len) < 0))
            throw new IndexOutOfBoundsException();

        if (len > 0)
            mBuffer.append(cs, off, len);
    }

    /**
     * 整数当做字符编码，转换对应的字符
     *
     * @param c
     * @throws IOException
     */
    public void write(int c) throws IOException {
        mBuffer.append((char) c);
    }

    @Override
    public void write(char[] cs) throws IOException {
        mBuffer.append(cs, 0, cs.length);
    }

    @Override
    public void write(String str) throws IOException {
        mBuffer.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        mBuffer.append(str.substring(off, off + len));
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(csq.subSequence(start, end).toString());
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        mBuffer.append(c);
        return this;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public String toString() {
        return mBuffer.toString();
    }
}
