package com.alibaba.dubbo.common.io;

import java.io.IOException;
import java.io.Writer;

/**
 * 非线程安全
 */
public class UnsafeStringWriter extends Writer {
    private StringBuilder mBuffer;

    //-------------------------------------------------  Constructors
    public UnsafeStringWriter() {
        lock = mBuffer = new StringBuilder();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
