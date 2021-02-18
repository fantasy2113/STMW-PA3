import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {

  private static final int[] lastOcc = new int[256];
  private static final Map<Integer, String> LCP = new HashMap<>();
  private static final int runs = 10;
  private static List<String> L = new ArrayList<>();
  private static List<Integer> resultIndices = new ArrayList<>();
  private static long searchRuntime = 0;
  private static long searchStartTime = 0;
  private static long preprocessingStartTime = 0;
  private static long preprocessingRuntime = 0;
  private static int resultIndex = -1;
  private static String pat = null;
  private static String text = null;

  public static void main(String... args) throws Exception {
    setArgs(args);
    printStart(args[1]);
    for (int measurement = 0; measurement < runs; measurement++) {
      searchStartTime = System.nanoTime();
      resultIndices = naiveSlidingWindow(false);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }
    printSearchResultOnline("Sliding Window");
    for (int measurement = 0; measurement < runs; measurement++) {
      searchStartTime = System.nanoTime();
      resultIndices = naiveSlidingWindow(true);
      searchRuntime += (System.nanoTime() - searchStartTime);
    }
    printSearchResultOnline("Sliding Window with last-occ");
    for (int measurement = 0; measurement < runs; measurement++) {
      searchStartTime = System.nanoTime();
      preprocessingStartTime = searchStartTime;
      offlineSearchPreprocessing();
      preprocessingRuntime += (System.nanoTime() - preprocessingStartTime);
      resultIndex = simpleSearch(-1, L.size());
      searchRuntime += (System.nanoTime() - searchStartTime);
    }
    printOfflineSearch();
  }

  private static void setArgs(String[] args) throws IOException {
    pat = args[0];
    text = readText(args[1]);
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

  public static int simpleSearch(int d, int f) {
    final int m = pat.length();
    while ((d + 1) < f) {
      int i = (d + f) / 2;
      final int l = LCP.get(i).length();
      if (l == m && l == L.get(i).length()) {
        return i;
      } else if (l == L.get(i).length() || (l != m && L.get(i).compareTo(pat) < pat.compareTo(L.get(i)))) {
        d = i;
      } else {
        f = i;
      }
    }
    return -1;
  }

  public static void offlineSearchPreprocessing() {
    Set<String> set = new HashSet<>(new ArrayList<>(Arrays.asList(text.split("\\W+"))));
    set.remove("");
    L = new ArrayList<>(set);
    Collections.sort(L);
    buildPatLCP();
  }

  private static void buildPatLCP() {
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
    for (int k = 0; k <= m - 2; k++) {
      lastOcc[pat.charAt(k)] = m - 1 - k;
    }
  }

  private static String readText(String arg) throws IOException {
    return new String(Files.readAllBytes(Paths.get(arg)), StandardCharsets.UTF_8);
  }

  private static void printSearchResultOnline(String search) {
    System.out.println(search + " = " + resultIndices);
    System.out.println("Hits = " + resultIndices.size());
    System.out.println("Search runtime (Nano): " + (searchRuntime / runs));
    System.out.println();
    searchRuntime = 0;
    resultIndices.clear();
  }

  private static void printStart(String arg) {
    System.out.println("Pattern search: " + pat);
    System.out.println("File: " + arg);
    System.out.println();
    searchRuntime = 0;
    preprocessingRuntime = 0;
  }

  private static void printOfflineSearch() {
    System.out.println("Simple Search = [" + resultIndex + "]");
    System.out.println("Preprocessing runtime (Nano): " + (preprocessingRuntime / runs));
    System.out.println("Search runtime (Nano): " + ((searchRuntime / runs) - (preprocessingRuntime / runs)));
    searchRuntime = 0;
    preprocessingRuntime = 0;
  }
}
