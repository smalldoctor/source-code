package java.util.concurrent;

/**
 * Future用于表示异步计算，间接表示与其相关的异步计算；
 * Future提供方法检查异步计算是否完成，获取异步计算的结果；
 * {@code get}用于获取异步计算的结果，是一个阻塞的方法；
 * {@code cancel}用于取消异步任务;
 * 可以通过Future的方法判断是正常完成还是cancel结束；
 * <p>
 * <b>Sample Usage</b> (Note that the following classes are all
 * made-up.)
 * <pre> {@code
 * interface ArchiveSearcher { String search(String target); }
 * class App {
 *   ExecutorService executor = ...
 *   ArchiveSearcher searcher = ...
 *   void showSearch(final String target)
 *       throws InterruptedException {
 *     Future<String> future
 *       = executor.submit(new Callable<String>() {
 *         public String call() {
 *             return searcher.search(target);
 *         }});
 *     displayOtherThings(); // do other things while searching
 *     try {
 *       displayText(future.get()); // use future
 *     } catch (ExecutionException ex) { cleanup(); return; }
 *   }
 * }}</pre>
 *
 */
public interface Future<V> {
}
