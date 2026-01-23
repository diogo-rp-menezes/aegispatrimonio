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
}
