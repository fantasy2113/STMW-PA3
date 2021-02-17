import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {

  private static final int[] lastOcc = new int[256];
  private static String pat = "";
  private static String text = "";
  private static List<String> L = new ArrayList<>();
  private static final Map<Integer, String> LCP = new HashMap<>();
  private static final List<Integer> results = new ArrayList<>();

  public static void main(String... args) throws Exception {
    pat = args[0];
    text = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);

    int runs = 10;
    long searchRuntime = 0;
    long searchStartTime = 0;
    long preprocessingStartTime = 0;
    long preprocessingRuntime = 0;

    System.out.println("Pattern search: " + pat);
    System.out.println("File: " + args[1]);
    System.out.println();

    for (int measurement1 = 0; measurement1 < runs; measurement1++) {
      searchStartTime = System.nanoTime();
      naiveSlidingWindow(false);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("Sliding Window = " + results);
    System.out.println("Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println();

    results.clear();
    searchRuntime = 0;
    for (int measurement2 = 0; measurement2 < runs; measurement2++) {
      searchStartTime = System.nanoTime();
      naiveSlidingWindow(true);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("Sliding Window with last-occ = " + results);
    System.out.println("Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println();

    results.clear();
    searchRuntime = 0;
    for (int measurement3 = 0; measurement3 < runs; measurement3++) {
      text = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
      searchStartTime = System.nanoTime();
      preprocessingStartTime = searchStartTime;
      offlinePreprocessing();
      preprocessingRuntime += (System.nanoTime() - preprocessingStartTime);
      simpleSearch();
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("Simple Search = " + results);
    System.out.println("Preprocessing runtime (Nano): " + (preprocessingRuntime / runs));
    System.out.println("Search runtime (Nano): " + ((searchRuntime / runs) - (preprocessingRuntime / runs)));
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
        results.add(i);
        return true;
      }
      if (intelligent) {
        i += lastOcc[text.charAt(i + m - 1)];
      } else {
        i++;
      }
    }
    return false;
  }

  public static int simpleSearch() {
    int d = -1;
    int f = L.size();
    final int m = pat.length();
    while ((d + 1) < f) {
      int i = (d + f) / 2;
      final int l = LCP.get(i).length();
      if (l == m && l == L.get(i).length()) {
        results.add(i);
        return i;
      } else if (l == L.get(i).length() || (l != m && L.get(i).charAt(l) < pat.charAt(l))) {
        d = i;
      } else {
        f = i;
      }
    }
    results.add(-1);
    return -1;
  }

  public static void offlinePreprocessing() {
    prepareTextForOffline();
    L = new ArrayList<>(Arrays.asList(text.split(" ")));
    Collections.sort(L);
    buildPatLCP();
  }

  private static void buildPatLCP() {
    LCP.put(-1, "");
    for (int i = 0; i < L.size(); i++) {
      LCP.put(i, lcp(pat, L.get(i)));
    }
  }

  private static String lcp(String u, String v) {
    int lcp = 0;
    int end = Math.min(u.length(), v.length());
    for (int i = 0; i < end; i++) {
      if (u.charAt(i) == v.charAt(i)) {
        lcp++;
      }
    }
    return u.substring(0, lcp);
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

  private static void prepareTextForOffline() {
    text = text.replaceAll("\\.", "").replaceAll(",", "")
        .replaceAll(":", "").replaceAll("!", "")
        .replaceAll("\\?", "").replaceAll(";", "")
        .replaceAll("\n", "").replaceAll("\r", "")
        .replaceAll("\r\n", "");
  }
}
