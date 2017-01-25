package jooqdb;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.using;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JooqNameTest {
    static final String TABLE_NAME = "TEST";
    DSLContext context;

    @After
    public void after() {
        dropTable();
        context.close();
    }

    public void openH2() throws Exception {
        String userName = "sa";
        String password = "sa";
        String url      = "jdbc:h2:mem:";

        Connection conn = DriverManager.getConnection(url, userName, password);
        context = using(conn, SQLDialect.H2);
    }

    public void openPostgres() throws Exception {
        String userName = "postgres";
        String password = "cristal";
        String url      = "jdbc:postgresql://localhost:5432/integtest";

        Connection conn = DriverManager.getConnection(url, userName, password);
        context = using(conn, SQLDialect.H2);
    }

    public int createTable() {
        return context.createTableIfNotExists(table(name(TABLE_NAME)))
            .column(field(name("UUID"),  UUID.class),    SQLDataType.UUID.nullable(false))
            .column(field(name("NAME"),  String.class),  SQLDataType.VARCHAR.length(128).nullable(false))
            .column(field(name("VALUE"), String.class),  SQLDataType.VARCHAR.length(4096).nullable(true))
            .constraints(constraint("PK_"+TABLE_NAME).primaryKey(field(name("UUID")), field(name("NAME"))))
        .execute();
    }

    public int dropTable() {
        return context.dropTableIfExists(table(name(TABLE_NAME))).execute();
    }

    public int insert(UUID uuid, String name, String value) {
        return context
                .insertInto(table(name(TABLE_NAME)))
                    .set(field(name("UUID")),  uuid)
                    .set(field(name("NAME")),  name)
                    .set(field(name("VALUE")), value)
                .execute();
    }

    public String fetch(UUID uuid, String name) {
        Record result = context
                .select().from(table(name(TABLE_NAME)))
                .where(field(name("UUID")).equal(uuid))
                  .and(field(name("NAME")).equal(name))
                .fetchOne();

        if(result != null) return result.get(field(name("VALUE")), String.class);
        return null;
    }

    @Test
    public void testWithH2() throws Exception {
        openH2();
        UUID uuid = UUID.randomUUID();
        createTable();
        assert insert(uuid, "Type", "Serious") == 1;
        Assert.assertEquals("Serious", fetch(uuid, "Type"));
    }

    @Test @Ignore("Postgres test cannot run in Travis")
    public void testWithPostgres() throws Exception {
        openPostgres();
        UUID uuid = UUID.randomUUID();
        createTable();
        assert insert(uuid, "Type", "Serious") == 1;
        Assert.assertEquals("Serious", fetch(uuid, "Type"));
    }
}
