package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * M4 (audit 2026-09-30): criação do índice FULLTEXT ft_ativos_nome sobre
 * ativos(nome), usado pela busca por nome via MATCH ... AGAINST
 * (IN NATURAL LANGUAGE MODE) no AtivoRepository.
 *
 * Segue o padrão da V17 (Java migration + JDBC DatabaseMetaData) porque não
 * existe guarda SQL única portável entre H2 2.3 (dev/e2e, MODE=MySQL) e
 * MySQL 8 (prod): H2 não suporta CREATE FULLTEXT INDEX nem
 * information_schema.statistics; MySQL não suporta CREATE INDEX IF NOT EXISTS.
 *
 * Em H2 a migration é um no-op (a busca por nome cai no fallback LIKE do
 * AtivoService). Em MySQL o índice é criado apenas se ainda não existir,
 * mantendo a migration idempotente e segura para re-execução após rollback
 * parcial. Rollback: DROP INDEX ft_ativos_nome ON ativos.
 */
public class V20__create_ativos_fulltext_index extends BaseJavaMigration {

    private static final String TABLE_NAME = "ativos";
    private static final String INDEX_NAME = "ft_ativos_nome";
    private static final String CREATE_INDEX_SQL =
            "CREATE FULLTEXT INDEX " + INDEX_NAME + " ON " + TABLE_NAME + " (nome)";

    @Override
    public void migrate(Context context) throws Exception {
        // Não fechar a Connection: ela pertence ao Flyway (fechá-la quebra o commit da migration).
        Connection connection = context.getConnection();
        if (!isMysql(connection)) {
            return; // H2 (dev/e2e): FULLTEXT não é suportado; service usa fallback LIKE.
        }
        if (!indexExists(connection)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_INDEX_SQL);
            }
        }
    }

    private boolean isMysql(Connection connection) throws SQLException {
        String product = connection.getMetaData().getDatabaseProductName();
        return "MySQL".equalsIgnoreCase(product) || "MariaDB".equalsIgnoreCase(product);
    }

    private boolean indexExists(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = null; // H2 usa schema PUBLIC; MySQL usa catalog — null aceita ambos
        try (ResultSet indexes = metaData.getIndexInfo(catalog, schema, TABLE_NAME, false, false)) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if (indexName != null && indexName.equalsIgnoreCase(INDEX_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }
}
