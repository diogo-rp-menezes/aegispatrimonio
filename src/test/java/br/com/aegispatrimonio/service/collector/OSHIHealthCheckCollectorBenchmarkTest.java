package br.com.aegispatrimonio.service.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OSHIHealthCheckCollectorBenchmarkTest {

    @Test
    void testCollectPerformance() {
        OSHIHealthCheckCollector collector = new OSHIHealthCheckCollector(new ObjectMapper());

        long startTime = System.currentTimeMillis();
        collector.collect();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Execution time: " + duration + "ms");
        assertTrue(duration < 200, "Expected execution time to be less than 200ms but was " + duration + "ms");
    }
}
