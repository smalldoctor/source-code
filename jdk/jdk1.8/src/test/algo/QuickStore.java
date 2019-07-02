package algo;

/**
 * @ClassName: algo.QuickStore
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-05-15 14:02
 */
public class QuickStore {

  /**
   * nlogn
   * 原地排序
   * 非稳定排序
   */
  public void quickStore(int[] a, int n) {
    quickStoreInternally(a, 0, n - 1);
  }

  public void quickStoreInternally(int[] a, int p, int q) {
    if (p >= q) {
      return;
    }

    int r = partition(a, p, q);
//    pivot的值是已经排序好的
    quickStoreInternally(a, p, r - 1);
    quickStoreInternally(a, r + 1, q);
  }

  public int partition(int[] a, int p, int q) {
    int lower = p;
    int upper = p;
    int pivot = a[q];
    for (; lower <= q; lower++) {
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

  public static void main(String[] args) {
    int[] a = new int[]{5, 3, 7, 1, 1, 4, 20, 12, 2};
    QuickStore quickStore = new QuickStore();

    quickStore.quickStore(a, a.length);
    for (int i = 0; i < a.length; i++) {
      int i1 = a[i];
      System.out.println(i1);
    }
  }
}
