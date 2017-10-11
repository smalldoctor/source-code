package io.netty.util.internal.logging;

/**
 * @Author xuecy
 * @Date: 15/11/4
 * @RealUser:Chunyang Xue
 * @Time: 13:41
 * @Package:io.netty.util.internal.logging
 * @Email:xuecy@asiainfo.com
 */

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * 创建slf4j logger
 */
public class Slf4JLoggerFactory extends InternalLoggerFactory {
    public Slf4JLoggerFactory() {
    }

    Slf4JLoggerFactory(boolean failIfNOP) {
        assert failIfNOP;// 应该一直是true

        final StringBuffer buf = new StringBuffer();
        final PrintStream err = System.err;
        try {
            // PrintStream 待研究
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    buf.append(b);
                }
            }, true, "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        try {
            // slf4j 待研究
            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
                throw new NoClassDefFoundError(buf.toString());
            } else {
                err.print(buf);
                err.flush();
            }
        } finally {
            System.setErr(err);
        }
    }

    @Override
    protected InternalLogger newInstance(String name) {
        return null;
    }
}
