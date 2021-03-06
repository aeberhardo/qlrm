package ch.simas.sqlresultmapper;

import ch.simas.generator.ClassGenerator;
import ch.simas.sqlresultmapper.to.EmployeeTO;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class JdbcSqlResultMapperTest {

    private static Connection con;

    @BeforeClass
    public static void init() throws SQLException, FileNotFoundException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        con = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate("DROP TABLE EMPLOYEE");
        } catch (Exception e) {
        }
        stmt.executeUpdate("CREATE TABLE EMPLOYEE (ID INTEGER NOT NULL, NAME VARCHAR, PRIMARY KEY (ID))");
        stmt.executeUpdate("INSERT INTO EMPLOYEE (ID , NAME) VALUES (1, 'Peter Muster')");
        stmt.close();

        ClassGenerator.generateFromTables("src/test/java/", "ch.simas.sqlresultmapper.to", "TO", false, con, "EMPLOYEE");
    }

    @Test
    public void testSql() throws SQLException {
        Statement stmt = con.createStatement();
        boolean ok = stmt.execute("SELECT ID, NAME FROM EMPLOYEE");
        Assert.assertTrue(ok);

        List<EmployeeTO> list = JdbcSqlResultMapper.list(stmt.getResultSet(), EmployeeTO.class);
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() > 0);

        for (EmployeeTO rec : list) {
            System.out.println(rec);
        }
    }

    @Test
    public void uniqueResult() throws SQLException {
        Statement stmt = con.createStatement();
        boolean ok = stmt.execute("SELECT ID, NAME FROM EMPLOYEE WHERE ID = 1");
        Assert.assertTrue(ok);

        EmployeeTO to = JdbcSqlResultMapper.uniqueResult(stmt.getResultSet(), EmployeeTO.class);
        Assert.assertNotNull(to);

        System.out.println(to);
    }
}
