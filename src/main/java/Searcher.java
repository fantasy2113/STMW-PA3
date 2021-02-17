public class Searcher {

  private static final int[] lastOcc = new int[256];
  private static final String PAT = "hallo";
  private static final String TEXT = "fsdfsdfsdfsdfsdhalldfsdfdsfdshallodfsdgdfg";

  public static void main(String... args) {
    int runs = 10;

    System.out.println("- Online Search:");
    System.out.println("\t- Sliding Window:");

    long runtime = 0;
    for (int measurement1 = 0; measurement1 < runs; measurement1++) {
      long startTime = System.nanoTime();
      naiveSlidingWindow(PAT, TEXT, false);
      runtime += (System.nanoTime() - startTime);
    }

    System.out.println("\t\t- Nano Runtime: " + (runtime / runs));
    System.out.println("\t- Sliding Window with last-occ:");

    runtime = 0;
    for (int measurement2 = 0; measurement2 < runs; measurement2++) {
      long startTime = System.nanoTime();
      naiveSlidingWindow(PAT, TEXT, true);
      runtime += (System.nanoTime() - startTime);
    }

    System.out.println("\t\t- Nano Runtime: " + (runtime / runs));
    System.out.println("- Offline Search:");
    System.out.println("\t- Simple Search:");

    runtime = 0;
    for (int measurement3 = 0; measurement3 < runs; measurement3++) {
      long startTime = System.nanoTime();
      simpleSearch(PAT, TEXT);
      runtime += (System.nanoTime() - startTime);
    }
    System.out.println("\t\t- Nano Runtime: " + (runtime / runs));
  }


  public static boolean naiveSlidingWindow(String pat, String text, boolean intelligent) {
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

  public static boolean simpleSearch(String pat, String text) {
    System.out.println("\t\t- PATTERN \"" + pat + "\" NO MATCH");
    return false;
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
