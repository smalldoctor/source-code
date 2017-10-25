java.util.concurrent  
**Class ThreadPoolExecutor**  

java.lang.Object  
　　java.lang.concurrent.AbstractExecutorService  
　　　　java.lang.concurrent.ThreadPoolExecutor  

##### All Implemented Interface:  
　　Executor, ExecutorService  
##### Direct Known Subclasses:
　　ScheduledThreadPoolExecutor
***
public class **ThreadPoolExecutor**  
extends AbstractExecutorService
***
### Field Summary
- COUNT_BITS
  - 修饰符
    - private static final
  - 类型
    - int
  - 说明
    - 线程池状态控制是通过一个int值控制，int值包含两部分
      - workerCount:当前活动线程的数量；workerCount可能是短暂的与实际的线程数量不等，如线程工厂创建线程失败，或者线程的统计刚好发生在线程被terminated前一刻，用户取到的workerCount代表的是当前work set的大小。
      - runState:线程池的当前状态
    - int值中的(**Integer.SIZE-3**)位用来控制workerCount的最大值
    - 低位是workerCount，高位是runState
    - 线程池的runState被存放在int的高位，workerCount被存放在低位
- ctl
  - 修饰符
    - private final
  - 类型
    - AtomicInteger
  - 说明
    - 用来存储控制线程池状态的整数
- corePoolSize
  - 修饰符
    - private volatile int
    - volatile用来保证线程可见性
  - 类型
    - int
  - 说明
   - 用来设定核心线程数；线程池最小的alive线程数量，通常情况下是不会超时。但是可以通过allowCoreThreadTimeOut设定超时，即CoreThread等待任务超过指定时长则会自动销毁。
- maximumPoolSize
  - 修饰符
    - private volatile int
    - volatile用来保证线程的可见性
  - 类型
    - int
  - 说明
   - 用来设定线程池最大的线程数；线程池的最终线程数量不一定就是这个值，还需要参考队列的类型，只有在线程池达到`corePoolSize`且队列已经满了，才会依据`maximumPoolSize`的大小，创建新的线程。
- mainLock
  - 修饰符
    - private final
  - 类型
    - ReentrantLock
  - 说明
    - 用来控制对worker set的访问
***
### Constructor Summary
- **ThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
long keepAliveTime,
TimeUnit unit,
BlockingQueue<Runnable> workQueue)**
  - 说明
    - 使用默认线程池工厂构建线程池
  - 修饰符
    -  
  - 入参
    -  
  - 出参
    -  
  - 方法体
    -  
***
### Method Summary
- **ctlOf(int rs, int wc)**
  - 概要  
    用来封装代表线程池状态的int
  - 修饰符
    -  private static
  - 入参
    - rs:线程池当前的状态
    - wc:当前线程池的workerCount
  - 出参
    -  int:返回的是代表线程池当前状态的整数
  - 方法体
    -  因为int前三位代表线程池的状态，int后29位代表线程池的workerCount，通过位或（二进制位有一个1则是1）的方式计算线程池状态的int
- **runStateOf(int c)**
  - 概要
    通过此方法计算当前线程池的状态
  - 修饰符
    - private statics
  - 入参
    - ctl：当前线程池的状态int
  - 出参
    - int：当前线程池的状态
  - 方法体
    - 通过将CAPACITY位反之后，再与ctl进行位与运算，从而得到当前的线程状态
- **workerCountOf(int c)**
  - 概要
    通过此方法计算当前线程池的线程数量
  - 修饰符
    - private static
  - 入参
    - int:当前线程池的的状态int
  - 出参
    - int：当前线程池的workerCount
  - 方法体
    - 通过与CAPACITY进行位与，计算得到当前线程的数量
- **execute(Runnable command)**
  - 概要
    执行用户提交的任务
  - 修饰符
    - public
  - 入参
    - Runnable:用户提交的任务
  - 出参
    - void
  - 异常
    - NullPointerException：当用户提交的任务Runnable==null时，报空指针的
  - 方法体
    1.  调用workerCountOf方法获取当前的workerCount，如果小于`corePoolSize`，则调用addWorker方法增加线程
- **addWorker(Runnable firstTask, boolean core)**
  - 概要
    在bound范围内创建新的worker，创建之后并启动执行firstTask（workerCount也会调整）；如果ThreadPool是stop或者shutdown状态则会返回false；如果线程创建失败也会返回失败，如线程工厂返回null或者创建的新线程在启动时异常。
  - 修饰符
    - private
  - 入参
    - firstTask：新线程需要执行的任务；当线程池的线程数量少于corePoolSize时，此时firstTask不会进入队列，直接启动新的线程；当queue已满且线程池的线程数量少于maximumPoolSize时，则创建新的线程启动线程，处理firstTask。
    - core:boolean:用来表示是corePoolSize还是maximumPoolSize作为bound，用boolean值便于后面区分并且每次取最新的size。
  - 出参
    - boolean:处理成功是否
  - 方法体
    - 使用Java标签（retry）（goto方式）实现的无限循环
      1. 获取线程池状态int，并获取线程池当前状态rs
      2. 判断如果当前线程池状态是大于ShutDown状态，或者如果是ShutDown状态且firstTask等于null且queue是空，则返回false。
      3. 内部无限循环
        1. 获取当前的workerCount
        2. 当前的workerCount超过`CAPACITY`,或者`corePoolSize`或者`maximumPoolSize`，return false；
        3.  使用AtomicInteger的CAS方法增加线程池的workerCount，如果增加成功则break retry退出循环。
        4.  获取当前的线程池状态int
        5.  计算当前的线程池状态，如果不等于原有的线程状态rs，则continue retry，再次进行外循环。否则进行内循环。（如果线程池的workerCount的CAS增加失败，则进行内循环）
      4. 新建Worker对象，Worker本身是Runnable的实现；在新建Worker时，会新建一个Thread。
      5.  判断Worker是否新建线程是否成功
        1. 如果Worker新建线程成功,
***
### Nested Class Summary（三级标题）
|Modifier and Type|Class and Description|
|:---|
|private final class|ThreadPoolExecutor.Worker<br>封装了线程池的工作线程.|
