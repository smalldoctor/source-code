package algo;

/**
 * @ClassName: algo.BinarySearch
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2019-05-19 08:28
 */
public class BinarySearch {

  public int binarySearch(int[] a, int n, int search) {
    return binarySearchInternally(a, 0, a.length - 1, search);
  }

  public int binarySearchInternally(int[] a, int p, int q, int search) {
    if (p > q) {
      return -1;
    }
    int mid = p + ((q - p) >> 1);
    if (a[mid] == search) {
      return mid;
    } else if (a[mid] > search) {
//     新的坐标需要加减1，否则死循环
      return binarySearchInternally(a, p, mid - 1, search);
    } else {
      return binarySearchInternally(a, mid + 1, q, search);
    }
  }

  public static void main(String[] args) {
    int[] a = new int[]{1, 4, 6, 7, 10, 30, 40};
    System.out.println(new BinarySearch().binarySearch(a, a.length, 10));
    System.out.println(new BinarySearch().binarySearch(a, a.length, 9));
  }
}
