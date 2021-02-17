import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {

  private static final int[] lastOcc = new int[256];
  private static final Map<Integer, String> LCP = new HashMap<>();
  private static List<Integer> indices = new ArrayList<>();
  private static List<String> L = new ArrayList<>();
  private static String pat = null;
  private static String text = null;

  public static void main(String... args) throws Exception {
    pat = args[0];
    text = readText(args[1]);

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
      indices = naiveSlidingWindow(false);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("Sliding Window = " + indices);
    System.out.println("Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println();

    searchRuntime = 0;
    for (int measurement2 = 0; measurement2 < runs; measurement2++) {
      searchStartTime = System.nanoTime();
      indices = naiveSlidingWindow(true);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("Sliding Window with last-occ = " + indices);
    System.out.println("Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println();

    searchRuntime = 0;
    for (int measurement3 = 0; measurement3 < runs; measurement3++) {
      text = readText(args[1]);
      searchStartTime = System.nanoTime();
      preprocessingStartTime = searchStartTime;
      offlineSearchPreprocessing();
      preprocessingRuntime += (System.nanoTime() - preprocessingStartTime);
      indices = simpleSearch();
      searchRuntime += (System.nanoTime() - searchStartTime);
    }

    System.out.println("Simple Search = " + indices);
    System.out.println("Preprocessing runtime (Nano): " + (preprocessingRuntime / runs));
    System.out.println("Search runtime (Nano): " + ((searchRuntime / runs) - (preprocessingRuntime / runs)));
  }

  public static List<Integer> naiveSlidingWindow(boolean intelligent) {
    List<Integer> indices = new ArrayList<>();
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
        indices.add(i);
      }
      if (intelligent) {
        i += lastOcc[text.charAt(i + m - 1)];
      } else {
        i++;
      }
    }
    return indices;
  }

  public static List<Integer> simpleSearch() {
    List<Integer> indices = new ArrayList<>();
    int d = -1;
    int f = L.size();
    final int m = pat.length();
    while ((d + 1) < f) {
      int i = (d + f) / 2;
      final int l = LCP.get(i).length();
      if (l == m && l == L.get(i).length()) {
        indices.add(i);
        f = i;
      } else if (l == L.get(i).length() || (l != m && L.get(i).charAt(l) < pat.charAt(l))) {
        d = i;
      } else {
        f = i;
      }
    }
    return indices;
  }

  public static void offlineSearchPreprocessing() {
    prepareTextForOfflineSearch();
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
    Arrays.fill(lastOcc, m);
    for (int k = 0; k < m - 2; k++) {
      lastOcc[pat.charAt(k)] = m - 1 - k;
    }
  }

  private static void prepareTextForOfflineSearch() {
    text = text.replaceAll("\\.", "").replaceAll(",", "")
        .replaceAll(":", "").replaceAll("!", "")
        .replaceAll("\\?", "").replaceAll(";", "")
        .replaceAll("\n", "").replaceAll("\r", "")
        .replaceAll("\r\n", "");
  }

  private static String readText(String arg) throws IOException {
    return new String(Files.readAllBytes(Paths.get(arg)), StandardCharsets.UTF_8);
  }
}
