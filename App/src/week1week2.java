import java.util.*;

public class week1week2 {

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
        String query;
        int frequency;
    }

    private TrieNode root;
    private final int TOP_K = 10;

    public week1week2() {
        root = new TrieNode();
    }

    // Insert a query with frequency
    public void insertQuery(String query, int frequency) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
        node.query = query;
        node.frequency += frequency; // accumulate frequency
    }

    // Update frequency when user searches a query
    public void updateFrequency(String query) {
        insertQuery(query, 1);
    }

    // Autocomplete top K suggestions for a given prefix
    public List<String> autocomplete(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        PriorityQueue<TrieNode> minHeap = new PriorityQueue<>(TOP_K, Comparator.comparingInt(n -> n.frequency));
        dfs(node, minHeap);

        List<String> result = new ArrayList<>();
        while (!minHeap.isEmpty()) result.add(minHeap.poll().query);
        Collections.reverse(result); // highest frequency first
        return result;
    }

    // Depth-first search to collect top K queries
    private void dfs(TrieNode node, PriorityQueue<TrieNode> heap) {
        if (node.isEnd) {
            if (heap.size() < TOP_K) {
                heap.offer(node);
            } else if (node.frequency > heap.peek().frequency) {
                heap.poll();
                heap.offer(node);
            }
        }
        for (TrieNode child : node.children.values()) {
            dfs(child, heap);
        }
    }

    // Optional: simple typo correction using edit distance (Levenshtein)
    public List<String> suggestCorrections(String query, int maxDistance) {
        List<String> suggestions = new ArrayList<>();
        for (String candidate : collectAllQueries()) {
            if (levenshteinDistance(query, candidate) <= maxDistance) {
                suggestions.add(candidate);
            }
        }
        return suggestions;
    }

    private List<String> collectAllQueries() {
        List<String> queries = new ArrayList<>();
        collectQueries(root, queries);
        return queries;
    }

    private void collectQueries(TrieNode node, List<String> queries) {
        if (node.isEnd) queries.add(node.query);
        for (TrieNode child : node.children.values()) collectQueries(child, queries);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[a.length()][b.length()];
    }

    public static void main(String[] args) {
        week1week2 autocomplete = new week1week2();

        // Load sample queries
        autocomplete.insertQuery("java tutorial", 1234567);
        autocomplete.insertQuery("javascript", 987654);
        autocomplete.insertQuery("java download", 456789);
        autocomplete.insertQuery("java 21 features", 10);

        // Autocomplete prefix "jav"
        System.out.println("Autocomplete for 'jav':");
        List<String> suggestions = autocomplete.autocomplete("jav");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.println((i + 1) + ". " + suggestions.get(i));
        }

        // Update frequency for trending query
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");

        System.out.println("\nAutocomplete after updating 'java 21 features':");
        suggestions = autocomplete.autocomplete("jav");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.println((i + 1) + ". " + suggestions.get(i));
        }

        // Typo correction example
        System.out.println("\nSuggestions for typo 'javascritp':");
        List<String> corrections = autocomplete.suggestCorrections("javascritp", 2);
        for (String s : corrections) {
            System.out.println(s);
        }
    }
}