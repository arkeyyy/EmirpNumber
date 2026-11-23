package org.example.emirp_number;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class EmirpSearchTest {
    @Test
    void sampleRangesAndResults() {
        var ranges = EmirpSearch.ranges(100, 4);
        assertEquals(List.of(
                new EmirpSearch.Range(1, 11),
                new EmirpSearch.Range(12, 31),
                new EmirpSearch.Range(32, 61),
                new EmirpSearch.Range(62, 100)), ranges);

        var matches = ranges.stream()
                .flatMapToInt(range -> IntStream.rangeClosed(range.first(), range.last()))
                .filter(EmirpSearch::isEmirp)
                .boxed().toList();
        assertEquals(List.of(13, 17, 31, 37, 71, 73, 79, 97), matches);
    }

    @Test
    void emirpsRequireTwoDistinctPrimes() {
        assertFalse(EmirpSearch.isEmirp(1));
        assertFalse(EmirpSearch.isEmirp(11));
        assertFalse(EmirpSearch.isEmirp(23));
        assertFalse(EmirpSearch.isEmirp(32));
        assertTrue(EmirpSearch.isEmirp(13));
        // 1,500,000,077 reverses to the prime 7,700,000,051.
        assertTrue(EmirpSearch.isEmirp(1_500_000_077));
    }

    @Test
    void rangesCoverEachNumberOnceAndDifferInSizeWhenPossible() {
        for (int limit = 1; limit <= 130; limit++) {
            for (int count = 1; count <= Math.min(limit, 64); count++) {
                var ranges = EmirpSearch.ranges(limit, count);
                assertEquals(count, ranges.size());
                int next = 1;
                for (var range : ranges) {
                    assertEquals(next, range.first());
                    assertTrue(range.size() >= 1);
                    next = range.last() + 1;
                }
                assertEquals(limit + 1, next);
                if (count > 1 && count < limit) {
                    assertTrue(ranges.stream().map(EmirpSearch.Range::size).distinct().count() > 1);
                }
            }
        }
    }

    @Test
    void maximumLimitFitsAcrossAllWorkerCounts() {
        for (int count = 1; count <= 64; count++) {
            var ranges = EmirpSearch.ranges(Integer.MAX_VALUE, count);
            long next = 1;
            for (var range : ranges) {
                assertEquals(next, range.first());
                assertTrue(range.size() >= 1);
                next = (long) range.last() + 1;
            }
            assertEquals(2_147_483_648L, next);
        }
    }

    @Test
    void invalidRangeRequestsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> EmirpSearch.ranges(0, 1));
        assertThrows(IllegalArgumentException.class, () -> EmirpSearch.ranges(10, 0));
        assertThrows(IllegalArgumentException.class, () -> EmirpSearch.ranges(2, 3));
        assertThrows(IllegalArgumentException.class, () -> EmirpSearch.ranges(100, 65));
    }
}
