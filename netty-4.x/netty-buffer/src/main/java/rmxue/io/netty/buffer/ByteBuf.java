package rmxue.io.netty.buffer;

/**
 * @Author: xuecy
 * @Date: 2016/11/3
 * @RealUser: Chunyang Xue
 * @Time: 23:50
 * @Package: rmxue.io.netty.buffer
 * @Email: xuecy@live.com
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * ByteBuf的实现:
 * 1.   通过使用两个指针实现读写的处理,readerIndex用于读操作,writerIndex用于写操作
 * ----1.1 writerIndex和readerIndex都是从0开始增长,读操作时readerIndex增长,写操作时writerIndex增长,但是
 * readerIndex不能大于writerIndex;读取之后,0-readerIndex之间的内容被视为discard,可以通过discardReadBytes
 * 释放这些空间,类似于ByteBuffer的compact方法;
 * ----1.2 readerIndex和writerIndex之间是可读取的内容,相当于ByteBuffer的position和limit之间;writerIndex和Capacity
 * 之间是可写区间,相当于ByteBuffer的limit和capacity之间。
 * ----1.3 读写都不能修改对方的index,所以不需要调整index的指针来实现读写;
 * 1)分为三块区域
 * +-------------------+------------------+------------------+
 * | discardable bytes |  readable bytes  |  writable bytes  |
 * |                   |     (CONTENT)    |                  |
 * +-------------------+------------------+------------------+
 * |                   |                  |                  |
 * 0      <=      readerIndex   <=   writerIndex    <=    capacity
 * 2) 调用discardReadBytes()方法之后
 * +------------------+--------------------------------------+
 * |  readable bytes  |    writable bytes (got more space)   |
 * +------------------+--------------------------------------+
 * |                  |                                      |
 * readerIndex (=0) <= writerIndex (decreased)        <=        capacity
 * writerIndex=oldWriterIndex-oldReaderIndex
 * ----1.4 JDK的ByteBuffer写入时需要自己控制是否超过最大容量,否则会抛出BufferOverFlowException;但是Netty的ByteBuf
 * 在write方法中已经进行控制。无需额外控制;但是不能超过ByteBuf最大可以容量;
 * ----1.5 Netty提供了接口进行ByteBuf和原生的JDK的ByteBuffer的转换功能
 */
public abstract class ByteBuf {

    //********************顺序读操作

    /**
     * 方法需要控制是否超过容量,进行自动的扩容。
     *
     * @param value
     * @return
     */
    public abstract ByteBuf writeByte(int value);

    /**
     * 从readerIndex处获取一个boolean值,readerIndex加1;
     *
     * @return
     */
    public abstract boolean readBoolean();

    /**
     * 从readerIndex处获取一个byte值,readerIndex加1;
     *
     * @return
     */
    public abstract byte readByte();

    /**
     * 从readerIndex处读取一个无符号字节值,readerIndex加1;
     *
     * @return
     */
    public abstract byte readUnsignedByte();

    /**
     * 从readerIndex处读取一个short值,readerIndex加2;
     * 因为short是2字节
     *
     * @return
     */
    public abstract short readShort();

    /**
     * 从readerIndex处读取一个无符号的short值,readerIndex加2;
     *
     * @return
     */
    public abstract short readUnsignedShort();

    /**
     * 从readerIndex处读取一个24位(3字节)的整型值,readerIndex增加3
     * (非Java的基本数据类型,通常不会使用)
     *
     * @return
     */
    public abstract int readMedium();

    /**
     * 从readerIndex处读取一个无符号的24位(3字节)的整型值,readerIndex增加3
     *
     * @return
     */
    public abstract int readUnsignedMedium();

    /**
     * 从readerIndex处读取一个整型值,readerIndex增加4
     *
     * @return
     */
    public abstract int readInt();

    /**
     * 从readerIndex处读取一个无符号整型值,readerIndex增加4
     *
     * @return
     */
    public abstract int readUnsignedInt();

    /**
     * 从readerIndex处读取一个long值,readerIndex增加8
     *
     * @return
     */
    public abstract long readLong();

    /**
     * 从readerIndex处读取一个char值,readerIndex增加2;
     * char型是2个字节
     *
     * @return
     */
    public abstract char readChar();

    /**
     * 从readerIndex处读取一个float值,readerIndex增加4;
     *
     * @return
     */
    public abstract float readFloat();

    /**
     * 从readerIndex处读取一个double值,readerIndex增加8
     *
     * @return
     */
    public abstract double readDouble();

    /**
     * 从当前的readerIndex处开始读取数据,长度为length,放入到新的ByteBuf中,新的ByteBuf
     * 的readerIndex为0,writerIndex为length;
     *
     * @param length the number of bytes to transfer
     * @return the newly created ByteBuf which contains the transferred bytes
     * @throws IndexOutOfBoundsException 当length大于当前可读的长度,则抛出越界异常
     */
    public abstract ByteBuf readBytes(int length);

    /**
     * 创建当前ByteBuf的一个子区域,子区域与当前ByteBuf共享缓冲区,但是独立维护writerIndex和readerIndex;
     * 新创建的子区域readerIndex为0,writerIndex为length
     *
     * @param length 构建子区域的字节数
     * @return 新的子区域的ByteBuf
     * @throws IndexOutOfBoundsException 当length大约当前ByteBuf可读字节数时抛出此异常
     */
    public abstract ByteBuf readSlice(int length);

    /**
     * 将当前的ByteBuf的数据读取到目标ByteBuf中,直到目标ByteBuf没有可写空间;
     * 操作完成之后,当前的ByteBuf的readerIndex+=读取的字节数
     *
     * @param dis 目标ByteBuf
     * @return
     * @throws IndexOutOfBoundsException 如果目标ByteBuf可写的字节数大于当前可读的则抛出越界异常;
     */
    public abstract ByteBuf readBytes(ByteBuf dis);

    /**
     * 将当前的ByteBuf读取到目标ByteBuf中,读取长度为length;操作完成之后,当前的ByteBuf
     * 的readerIndex+=length;
     *
     * @param dst    目标ByteBuf
     * @param length 读取的长度
     * @return
     * @throws IndexOutOfBoundsException 如果length大于当前ByteBuf可读的或者目标ByteBuf可写的长度,则抛出越界异常
     */
    public abstract ByteBuf readBytes(ByteBuf dst, int length);

    /**
     * 将当前ByteBuf读取到目标ByteBuf中,读取长度为Length;操作完成之后,当前的ByteBuf的
     * readerIndex+=length;目标的ByteBuf不是从writerIndex开始写,而是从dstIndex处开始
     * 写
     *
     * @param dst      目标ByteBuf
     * @param dstIndex 目标ByteBuf起始写位置
     * @param length   读取的length
     * @return
     * @throws IndexOutOfBoundsException 如果length大于当前的ByteBuf可读字节数或者dstIndex小于0或者
     *                                   length+dstIndex大于目标ByteBuf的capacity则抛出越界异常
     */
    public abstract ByteBuf readBytes(ByteBuf dst, int dstIndex, int length);

    /**
     * 将当前的ByteBuf读取到目标字节数组byte[]中,读取的长度为dst.length;
     * 操作完成之后,ByteBuf的readerIndex+=dst.length;
     *
     * @param dst 目标byte[]
     * @return
     * @throws IndexOutOfBoundsException 如果dst.length大于当前ByteBuf可以读取的字节数则抛出越界异常
     */
    public abstract ByteBuf readBytes(byte[] dst);

    /**
     * 将当前ByteBuf读取到目标的byte[]中,读取字节数为length,目标byte[]的起始位置为dstIndex;
     *
     * @param dst      目标byte[]
     * @param dstIndex 目标byte[]的起始位置
     * @param length   读取的字节数
     * @return
     * @throws IndexOutOfBoundsException 如果length大于当前ByteBuf的可读字节数,dst小于0或者
     *                                   dstIndex+length大于byte[]的长度则抛出越界异常
     */
    public abstract ByteBuf readBytes(byte[] dst, int dstIndex, int length);

    /**
     * 将当前的ByteBuf读取到目标ByteBuffer中,直到位置指针到达ByteBuffer的limit;
     * 操作完成之后,ByteBuf的readerIndex+=dst.remaining
     *
     * @param dst
     * @return
     * @throws IndexOutOfBoundsException 如果ByteBuffer的可写字节数大于ByteBuf的可读字节数则
     *                                   抛出越界异常
     */
    public abstract ByteBuf readBytes(ByteBuffer dst);

    /**
     * 将当前的ByteBuf读取到目标输出流中,读取的字节数为length;读取完成之后,当前ByteBuf的
     * readerIndex+=length
     *
     * @param dst    目标输出流
     * @param length 读取的字节数
     * @return
     * @throws IndexOutOfBoundsException 如果length大于当前ByteBuf可读取的字节数,则抛出越界异常
     * @throws java.io.IOException       如果读取过程中OutputStream自身发生异常,则抛出IOException
     */
    public abstract ByteBuf readBytes(OutputStream dst, int length);

    /**
     * 将当前ByteBuf写入到GatheringByteChannel中,写入的最大字节数为length;
     * PS:
     * 由于GatheringByteChannel是非阻塞Channel,调用它的write方法是无法保证一次将
     * 所有所需的数据写入成功,所以存在写半包的情况。因此实际的写入的字节范围为[0,length];
     * 操作完成之后,当前ByteBuf的readerIndex+=实际写入的字节数
     *
     * @param out
     * @param length 最大的字节数
     * @return 实际写入GatheringByteChannel中的字节数
     * @throws IndexOutOfBoundsException 如果需要写入的length大于当前的ByteBuf的可读字节数,则抛出越界异常
     * @throws java.io.IOException       如果写入过程中GatheringByteChannel发生异常,则抛出IOException
     */
    public abstract int readBytes(GatheringByteChannel out, int length);

    //*************顺序写操作

    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=1
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于1,则抛出越界异常
     */
    public abstract ByteBuf writeBoolean(boolean value);

    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=1
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于1,则抛出越界异常
     */
    public abstract ByteBuf writeByte(byte value);

    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=2
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于2,则抛出越界异常
     */
    public abstract ByteBuf writeShort(int value);


    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=3
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于3,则抛出越界异常
     */
    public abstract ByteBuf writeMedium(int value);


    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=4
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于4,则抛出越界异常
     */
    public abstract ByteBuf writeInt(int value);

    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=8
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于8,则抛出越界异常
     */
    public abstract ByteBuf writeLong(long value);

    /**
     * 将value写入当前的ByteBuf中;
     * 操作完成之后,writerIndex+=2
     *
     * @param value
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于2,则抛出越界异常
     */
    public abstract ByteBuf writeChar(int value);

    /**
     * 将源ByteBuf写入到当前的ByteBuf中;
     * 当前ByteBuf的writeIndex+=src.readableBytes
     *
     * @param src
     * @return
     * @throws IndexOutOfBoundsException 如果当前的ByteBuf可写的字节数小于源ByteBuf的可读字节数,
     *                                   则抛出越界异常
     */
    public abstract ByteBuf writeBytes(ByteBuf src);

    /**
     * 将源ByteBuf中的可读字节字节写入当前ByteBuf中,写入的字节数长度为length;
     * 操作完成之后,当前ByteBuf的writerIndex+=length
     *
     * @param src
     * @param length
     * @return
     * @throws IndexOutOfBoundsException 如果length大于源ByteBuf的可读字节数或者当前ByteBuf的可写字节数
     *                                   ,则抛出越界异常
     */
    public abstract ByteBuf writeBytes(ByteBuf src, int length);

    /**
     * 将源ByteBuf的可读字节写入到当前的ByteBuf中,写入的字节数长度为length,起始索引为srcIndex;
     * 操作成功之后,当前的ByteBuf的writerIndex+=length
     *
     * @param src
     * @param srcIndex
     * @param length
     * @return
     * @throws IndexOutOfBoundsException 如果srcIndex小于0,src+length大于源ByteBuf的可读字节数,或者
     *                                   length大于当前ByteBuf的可写字节数,则抛出越界异常
     */
    public abstract ByteBuf writeBytes(ByteBuf src, int srcIndex, int length);

    /**
     * 将源字节数组写入到当前ByteBuf中,成功写入之后,当前ByteBuf的writerIndex+=src.length
     *
     * @param src
     * @return
     * @throws IndexOutOfBoundsException 如果源字节数组的长度大于当前ByteBuf的写入字节数,则抛出越界异常
     */
    public abstract ByteBuf writeBytes(byte[] src);

    /**
     * 将源byte[]的可读字节写入到当前的ByteBuf中,写入的字节数长度为length,起始索引为srcIndex;
     * 操作成功之后,当前的ByteBuf的writerIndex+=length
     *
     * @param src
     * @param srcIndex
     * @param length
     * @return
     * @throws IndexOutOfBoundsException 如果srcIndex小于0,src+length大于源byte[]的可读字节数,或者
     *                                   length大于当前ByteBuf的可写字节数,则抛出越界异常
     */
    public abstract ByteBuf writeBytes(byte[] src, int srcIndex, int length);

    /**
     * 将源ByteBuffer的可读字节写入当前ByteBuf中,写入长度为src.remaining;
     * 操作成功之后,当前ByteBuf的writerIndex+=src.remaining
     *
     * @param src
     * @return
     * @throws IndexOutOfBoundsException 如果源ByteBuffer src的可读字节长度大于当前ByteBuf的可写入长度
     *                                   则抛出越界异常
     */
    public abstract ByteBuf writeBytes(ByteBuffer src);

    /**
     * 将源InputStream src的内容写入到当前的ByteBuf中,写入的最大字节数是length;
     * 操作成功之后,当前ByteBuf的writerIndex+=实际写入字节数
     *
     * @param in
     * @param length
     * @return
     * @throws IndexOutOfBoundsException 如果length大于源InputStream的可读字节数或者
     *                                   大于当前ByteBuf的可写入字节数,则抛出越界异常。
     * @throws java.io.IOException       如果源InputStream发生异常,则抛出IO异常
     */
    public abstract int writeBytes(InputStream in, int length);

    /**
     * 将源ScatteringByteChannel src的内容写入到当前的ByteBuf中,写入的最大字节数是length;
     * 操作成功之后,当前ByteBuf的writerIndex+=实际写入字节数
     *
     * @param src
     * @param length
     * @return
     * @throws IndexOutOfBoundsException 如果length大于源ScatteringByteChannel的可读字节数或者
     *                                   大于当前ByteBuf的可写入字节数,则抛出越界异常。
     * @throws java.io.IOException       如果源ScatteringByteChannel发生异常,则抛出IO异常
     */
    public abstract int writeBytes(ScatteringByteChannel src, int length);

    /**
     * 将当前的ByteBuf的缓冲区填充为NUL(0x00),从writerIndex开始;
     * 操作成功之后,当前ByteBuf的writerIndex+=length
     *
     * @param length
     * @return
     * @throws IndexOutOfBoundsException 如果length大于当前ByteBuf的可写入长度则抛出越界异常
     */
    public abstract ByteBuf writeZero(int length);
}
