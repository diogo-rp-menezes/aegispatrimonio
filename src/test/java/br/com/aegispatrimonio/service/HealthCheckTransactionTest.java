package br.com.aegispatrimonio.service;

import br.com.aegispatrimonio.BaseIT;
import br.com.aegispatrimonio.model.HealthCheckHistory;
import br.com.aegispatrimonio.repository.HealthCheckHistoryRepository;
import br.com.aegispatrimonio.service.collector.OSHIHealthCheckCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

class HealthCheckTransactionTest extends BaseIT {

    @Autowired
    private HealthCheckService healthCheckService;

    @MockBean
    private OSHIHealthCheckCollector oshiCollector;

    @MockBean
    private HealthCheckHistoryRepository healthCheckHistoryRepository;

    @Test
    void testPerformSystemHealthCheck_transactionStatus() {
        AtomicBoolean transactionActiveInCollect = new AtomicBoolean(false);

        when(oshiCollector.collect()).thenAnswer(invocation -> {
            transactionActiveInCollect.set(TransactionSynchronizationManager.isActualTransactionActive());
            return new HealthCheckHistory();
        });

        healthCheckService.performSystemHealthCheck();

        // VERIFICATION: We expect the transaction to be INACTIVE (false) after optimization.
        // This confirms that the blocking I/O in collect() is not holding a database connection.
        assertFalse(transactionActiveInCollect.get(), "Transaction should NOT be active inside collect() (Optimization successful)");
    }
}
