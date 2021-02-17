import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Searcher {

  private static final int[] lastOcc = new int[256];
  private static final String pat = "hallo";
  private static final String text = "fsdfsdfsdfsdfsdhalldfsdfdsfdshallodfsdgdfg fgdfgdfg hell asdasdasdasdg halloaddasd hallo";
  private static List<String> L = new ArrayList<>();

  public static void main(String... args) {
    int runs = 10;

    System.out.println("- Online Search:");
    System.out.println("\t- Sliding Window:");

    long searchRuntime = 0;
    long searchStartTime;

    for (int measurement1 = 0; measurement1 < runs; measurement1++) {
      searchStartTime = System.nanoTime();
      naiveSlidingWindow(false);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("\t\t- Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println("\t- Sliding Window with last-occ:");

    searchRuntime = 0;
    for (int measurement2 = 0; measurement2 < runs; measurement2++) {
      searchStartTime = System.nanoTime();
      naiveSlidingWindow(true);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("\t\t-Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println("- Offline Search:");
    System.out.println("\t- Simple Search:");

    searchRuntime = 0;
    for (int measurement3 = 0; measurement3 < runs; measurement3++) {
      searchStartTime = System.nanoTime();
      L = new ArrayList<>(Arrays.asList(text.split(" ")));
      Collections.sort(L);
      simpleSearch();
      searchRuntime += (System.nanoTime() - searchStartTime);
    }
    System.out.println("\t\t-Search runtime (Nano): " + (searchRuntime / runs));
  }


  public static boolean naiveSlidingWindow(boolean intelligent) {
    if (intelligent) {
      initLastOcc(pat);
    }

    final int n = text.length();
    final int m = pat.length();

    int i = 0;
    while (i <= (n - m)) {

      int j = 0;
      while (j < m && pat.charAt(j) == text.charAt(i + j)) {
        j++;
      }

      if (j == m) {
        System.out.println("\t\t- PATTERN \"" + pat + "\" MATCH AT [" + i + ", " + (i + (m - 2)) + "]");
        return true;
      }

      if (intelligent) {
        i += lastOcc[text.charAt(i + m - 1)];
      } else {
        i++;
      }

    }

    System.out.println("\t\t- PATTERN \"" + pat + "\" NO MATCH");
    return false;
  }

  public static String simpleSearch() {
    int d = -1;
    int f = L.size();
    final int m = pat.length();

    while ((d + 1) < f) {
      int i = (d + f) / 2;
      final int l = lcp(pat, L.get(i));

      if (l == m && l == L.get(i).length()) {
        System.out.println("\t\t- PATTERN \"" + pat + "\" MATCH AT INDEX: " + i);
        return String.valueOf(i);
      } else if (l == L.get(i).length() || (l != m && L.get(i).charAt(l) < pat.charAt(l))) {
        d = i;
      } else {
        f = i;
      }
    }

    System.out.println("\t\t- PATTERN \"" + pat + "\" NO MATCH");
    return "(" + d + "," + f + ")";
  }

  private static int lcp(String u, String v) {
    int lcp = 0;
    int end = Math.min(u.length(), v.length());
    for (int i = 0; i < end; i++) {
      if (u.charAt(i) == v.charAt(i)) {
        lcp++;
      }
    }
    return lcp;
  }

  private static void initLastOcc(String pat) {
    final int m = pat.length();
    for (int a = 0; a < lastOcc.length; a++) {
      lastOcc[a] = m;
    }
    for (int k = 0; k < m - 2; k++) {
      lastOcc[pat.charAt(k)] = m - 1 - k;
    }
  }
}
