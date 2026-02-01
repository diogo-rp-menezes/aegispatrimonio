package br.com.aegispatrimonio.service;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchOptimizationServiceTest {

    private final SearchOptimizationService service = new SearchOptimizationService();

    @Test
    void testLevenshteinDistance() {
        // Standard Levenshtein examples
        assertEquals(3, service.calculateLevenshteinDistance("kitten", "sitting"));
        assertEquals(0, service.calculateLevenshteinDistance("hello", "hello"));
        assertEquals(1, service.calculateLevenshteinDistance("test", "tent"));
        assertEquals(5, service.calculateLevenshteinDistance("", "abcde"));
        assertEquals(5, service.calculateLevenshteinDistance("abcde", ""));
    }

    @Test
    void testSimilarity() {
        assertEquals(1.0, service.calculateSimilarity("abc", "abc"), 0.001);
        assertEquals(0.0, service.calculateSimilarity("", "abc"), 0.001);

        // "test" vs "tent" -> distance 1, maxLength 4 -> 1 - (1/4) = 0.75
        assertEquals(0.75, service.calculateSimilarity("test", "tent"), 0.001);
    }

    @Test
    void testRanking_ShouldPrioritizeMatches() {
        List<String> assets = Arrays.asList("Monitor Dell", "Laptop HP", "Mouse Logitech", "Cadeira Gamer");

        // User types "Laptp" (Typo) -> Should match "Laptop HP" best
        List<String> results = service.rankResults("Laptp", assets, s -> s);

        assertFalse(results.isEmpty(), "Should return matches");
        assertTrue(results.get(0).contains("Laptop"), "First result should be Laptop (closest match)");
    }

    @Test
    void testRanking_ShouldFilterNoise() {
        List<String> assets = Arrays.asList("Notebook", "Server", "Switch");

        // User searches something completely unrelated
        List<String> results = service.rankResults("Xyz123CompleteGarbage", assets, s -> s);

        // Should be filtered out by the 0.2 threshold
        assertTrue(results.isEmpty(), "Results with very low similarity should be filtered out");
    }

    @Test
    void testPerformance_ShouldBeFast() {
        // Generate a large list of items
        List<String> bulkAssets = java.util.stream.IntStream.range(0, 5000)
                .mapToObj(i -> "Asset Number " + i + " - Description " + (i * 2))
                .toList();

        String target = "Asset Number 4999 - Description 9998";
        long start = System.currentTimeMillis();
        // Search for the exact string to ensure it comes first (Score 1.0)
        List<String> results = service.rankResults(target, bulkAssets, s -> s);
        long end = System.currentTimeMillis();

        long duration = end - start;
        // Assert that searching 5000 items takes less than 500ms (Adjusted for CI variability)
        // This validates the "Shift Left" claim of low latency while being robust
        assertTrue(duration < 500, "Fuzzy search took too long: " + duration + "ms");
        assertFalse(results.isEmpty());
        assertEquals(target, results.get(0));
    }
}
