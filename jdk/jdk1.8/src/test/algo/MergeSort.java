package algo;

/**
 * @ClassName: algo.MergeSort
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-05-15 13:14
 */
public class MergeSort {

  /**
   * nlogn
   * 稳定排序
   * 非原地排序
   */
  public void mergeSort(int[] a, int n) {
    mergeSortInternally(a, 0, n - 1);
  }

  public void mergeSortInternally(int[] a, int p, int q) {

//    递归结束条件
    if (p >= q) {
      return;
    }

    int r = p + (q - p) / 2;
    mergeSortInternally(a, p, r);
    mergeSortInternally(a, r + 1, q);
//    合并
    merge(a, p, r, q);
  }

  /*两个数组进行合并*/
  public void merge(int[] a, int p, int r, int q) {
    int[] tmp = new int[q - p + 1];
    int i = p;
    int j = r + 1;
    int k = 0;
    while (i <= r && j <= q) {
      if (a[i] <= a[j]) {
        tmp[k++] = a[i++];
      } else {
        tmp[k++] = a[j++];
      }
    }

//    找出剩余数据的数组；j和i都是++，所以理论没有剩余数据时，j应该大于q
    int start = i;
    int end = r;
    if (j <= q) {
      start = j;
      end = q;
    }

    while (start <= end) {
      tmp[k++] = a[start++];
    }

    for (i = 0; i <= q - p; i++) {
      a[p + i] = tmp[i];
    }
  }

  public static void main(String[] args) {
    int[] a = new int[]{5, 3, 7, 1, 9, 4, 20, 12, 2};
    MergeSort mergeSort = new MergeSort();

    mergeSort.mergeSort(a, a.length);
    for (int i = 0; i < a.length; i++) {
      int i1 = a[i];
      System.out.println(i1);
    }
  }
}
