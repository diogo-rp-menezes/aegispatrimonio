package db.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * V17: Criação idempotente do índice idx_alertas_ativo_id (M-12).
 *
 * Motivação: nenhuma guarda SQL única é portável entre H2 2.3 (SeedDataIT/dev,
 * MODE=MySQL) e MySQL 8 (prod). H2 não suporta information_schema.statistics nem
 * PREPARE FROM @var; MySQL não suporta CREATE INDEX IF NOT EXISTS nem
 * EXECUTE IMMEDIATE. A verificação via JDBC DatabaseMetaData é portável para
 * qualquer banco.
 *
 * Em produção legada, onde a tabela alertas foi criada historicamente pelo
 * Hibernate (ddl-auto=update) e o índice já existe, a migration não falha por
 * índice duplicado. Em banco novo, o índice é criado normalmente.
 *
 * Nota: o pacote precisa ser exatamente `db.migration` porque o Flyway mapeia a
 * location `classpath:db/migration` para este pacote ao escanear migrations Java.
 */
public class V17__create_alertas_index extends BaseJavaMigration {

    private static final String TABLE_NAME = "alertas";
    private static final String INDEX_NAME = "idx_alertas_ativo_id";
    private static final String CREATE_INDEX_SQL = "CREATE INDEX " + INDEX_NAME + " ON " + TABLE_NAME + "(ativo_id)";

    @Override
    public void migrate(Context context) throws Exception {
        // Não fechar a Connection: ela pertence ao Flyway (fechá-la quebra o commit da migration).
        Connection connection = context.getConnection();
        if (!indexExists(connection)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_INDEX_SQL);
            }
        }
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
