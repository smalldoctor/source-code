package algo;

/**
 * @ClassName: algo.KthSmall
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-05-18 20:01
 */
public class KthSmall {

  public int kthSmall(int[] a, int k) {
    if (a == null || a.length < k) {
      return -1;
    }

    int partition = partition(a, 0, a.length - 1);
//    分区之后，下表则代表的第几个小
    while (partition + 1 != k) {
      if (partition + 1 > k) {
        partition = partition(a, 0, partition - 1);
      } else {
        partition = partition(a, partition + 1, a.length - 1);
      }
    }
    return a[partition];
  }

  public int partition(int[] a, int p, int q) {
    int lower = p;
    int upper = p;
    int pivot = a[q];
    for (; lower < q; lower++) {
      if (a[lower] < pivot) {
        if (upper < lower) {
          int tmp = a[upper];
          a[upper] = a[lower];
          a[lower] = tmp;
        }
        upper++;
      }
    }

    int tmp = a[upper];
    a[upper] = a[q];
    a[q] = tmp;

    return upper;
  }

  /***
   * 重复元素可能不是想要的结果
   * */
  public static void main(String[] args) {
    int[] a = new int[]{5, 3, 7, 1, 9, 4, 20, 12, 2};
    QuickStore quickStore = new QuickStore();

    quickStore.quickStore(a, a.length);
    for (int i = 0; i < a.length; i++) {
      int i1 = a[i];
      System.out.println(i1);
    }

    int[] b = new int[]{5, 3, 7, 1, 9, 4, 20, 12, 2};
    System.out.println(new KthSmall().kthSmall(b, 4));
  }

}
