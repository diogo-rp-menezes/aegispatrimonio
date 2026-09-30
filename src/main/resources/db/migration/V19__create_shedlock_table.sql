-- H3 (audit 2026-09-30): tabela de lock distribuído (ShedLock) para jobs @Scheduled.
-- Garante que o job mensal de depreciação rode em apenas uma réplica do k8s.
CREATE TABLE shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
