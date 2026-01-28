package br.com.aegispatrimonio.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service dedicated to Search Optimization using deterministic algorithms (Shift Left).
 * Provides Fuzzy Search capabilities without external AI dependencies.
 */
@Service
public class SearchOptimizationService {

    /**
     * Calculates the Levenshtein distance between two CharSequences.
     * Uses a memory-optimized approach (two rows).
     *
     * @param left  The first string
     * @param right The second string
     * @return The edit distance (number of changes required to transform left to right)
     */
    public int calculateLevenshteinDistance(CharSequence left, CharSequence right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        int n = left.length();
        int m = right.length();

        if (n == 0) return m;
        if (m == 0) return n;

        if (n > m) {
            // Swap the strings to consume less memory
            final CharSequence tmp = left;
            left = right;
            right = tmp;
            n = m;
            m = right.length();
        }

        Buffers buffers = BUFFERS.get();
        buffers.ensureCapacity(n + 1);
        int[] p = buffers.p;
        int[] d = buffers.d;
        int[] _d; // placeholder

        for (int i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (int j = 1; j <= m; j++) {
            char rightJ = right.charAt(j - 1);
            d[0] = j;

            for (int i = 1; i <= n; i++) {
                int cost = left.charAt(i - 1) == rightJ ? 0 : 1;
                // minimum of deletion, insertion, substitution
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            _d = p;
            p = d;
            d = _d;
        }

        return p[n];
    }

    /**
     * Calculates similarity score between 0.0 (completely different) and 1.0 (exact match).
     *
     * @param left  First string
     * @param right Second string
     * @return Similarity score
     */
    public double calculateSimilarity(String left, String right) {
        if (left == null || right == null) return 0.0;
        if (left.equals(right)) return 1.0;

        int maxLength = Math.max(left.length(), right.length());
        if (maxLength == 0) return 1.0;

        // Optimization: If length difference is too large, it's impossible to reach > 0.2 score
        // Score = 1.0 - (dist / maxLen) > 0.2  =>  dist < 0.8 * maxLen
        // Since dist >= abs(len(left) - len(right)), if diff >= 0.8 * maxLen, we can skip
        int lengthDiff = Math.abs(left.length() - right.length());
        if ((double) lengthDiff / maxLength >= 0.8) {
            return 0.0;
        }

        int distance = calculateLevenshteinDistance(left, right);

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Ranks a list of items based on how similar a specific field is to the query.
     * Filters out results with very low similarity (e.g., < 0.2).
     *
     * @param query The search term
     * @param items The list of candidate items
     * @param fieldExtractor Function to extract the string field to compare from the item
     * @return Sorted list of items (most similar first)
     */
    public <T> List<T> rankResults(String query, List<T> items, Function<T, String> fieldExtractor) {
        if (query == null || query.isEmpty()) return items;
        if (items == null || items.isEmpty()) return List.of();

        String safeQuery = query.toLowerCase().trim();

        return items.stream()
            .map(item -> {
                String val = fieldExtractor.apply(item);
                double score = calculateSimilarity(safeQuery, val != null ? val.toLowerCase() : "");
                return new ItemWithScore<>(item, score);
            })
            .filter(i -> i.score > 0.2) // Threshold to remove complete noise
            .sorted((a, b) -> Double.compare(b.score, a.score)) // Descending
            .map(ItemWithScore::item)
            .collect(Collectors.toList());
    }

    private record ItemWithScore<T>(T item, double score) {}

    private static class Buffers {
        int[] p = new int[128];
        int[] d = new int[128];

        void ensureCapacity(int size) {
            if (p.length < size) {
                int newSize = Math.max(p.length * 2, size);
                p = new int[newSize];
                d = new int[newSize];
            }
        }
    }

    private static final ThreadLocal<Buffers> BUFFERS = ThreadLocal.withInitial(Buffers::new);
}
