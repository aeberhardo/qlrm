package ch.simas.generator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class ClassGenerator {

    public static void generateFromTables(String path, String pkg, String suffix, boolean publicFields, Connection con, String... tables) throws SQLException, FileNotFoundException {

        DatabaseMetaData metadata = con.getMetaData();
        for (String table : tables) {
            String className = generateClassName(table, suffix);
            PrintWriter outputStream = new PrintWriter(new FileOutputStream(createFileName(path, pkg, className)));

            createClassHeader(outputStream, pkg, className);

            ResultSet colResults = metadata.getColumns(null, null, table, null);
            createClassBody(colResults, outputStream, className, publicFields);

            outputStream.close();
            colResults.close();
        }
    }

    public static void generateFromResultSet(String path, String pkg, String className, boolean publicFields, ResultSet resultSet) throws SQLException, FileNotFoundException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        PrintWriter outputStream = new PrintWriter(new FileOutputStream(createFileName(path, pkg, className)));

        createClassHeader(outputStream, pkg, className);

        createClassBody(metaData, outputStream, className, publicFields);

        outputStream.close();
    }

    private static void createClassHeader(PrintWriter outputStream, String pkg, String className) {
        if (pkg != null) {
            outputStream.println("package " + pkg + ";\n");
        }
        outputStream.println("import java.sql.*;");
        outputStream.println("import java.util.*;");
        outputStream.println("import java.math.*;");
        outputStream.println("\n");
        outputStream.println("public class " + className + " {\n");
    }

    private static String createFileName(String path, String pkg, String className) {
        if (pkg == null) {
            return path + "/" + className + ".java";
        } else {
            return path + "/" + pkg.replaceAll("\\.", "/") + "/" + className + ".java";
        }
    }

    private static void createClassBody(ResultSet colResults, PrintWriter outputStream, String className, boolean publicFields) throws SQLException {
        StringBuilder ctrArgs = new StringBuilder();
        StringBuilder ctrBody = new StringBuilder();
        StringBuilder getters = new StringBuilder();
        boolean first = true;
        while (colResults.next()) {
            if (!first) {
                ctrArgs.append(", ");
            }
            String name = colResults.getString("COLUMN_NAME").toLowerCase();
            short colType = colResults.getShort("DATA_TYPE");
            generateCtrAndGetters(colType, outputStream, publicFields, name, ctrArgs, ctrBody, getters);
            first = false;
        }
        writeCtrAndGetters(outputStream, className, ctrArgs, ctrBody, getters);
    }

    private static void createClassBody(ResultSetMetaData metaData, PrintWriter outputStream, String className, boolean publicFields) throws SQLException {
        StringBuilder ctrArgs = new StringBuilder();
        StringBuilder ctrBody = new StringBuilder();
        StringBuilder getters = new StringBuilder();
        boolean first = true;
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (!first) {
                ctrArgs.append(", ");
            }
            String name = metaData.getColumnName(i).toLowerCase();
            int colType = metaData.getColumnType(i);
            generateCtrAndGetters(colType, outputStream, publicFields, name, ctrArgs, ctrBody, getters);
            first = false;
        }
        writeCtrAndGetters(outputStream, className, ctrArgs, ctrBody, getters);
    }

    private static String sqlTypeToJavaTypeString(int dataType) {
        String typeString;
        switch (dataType) {
            case Types.TINYINT:
                typeString = "byte";
                break;
            case Types.BIGINT:
                typeString = "long";
                break;
            case Types.INTEGER:
                typeString = "int";
                break;
            case Types.SMALLINT:
                typeString = "short";
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                typeString = "String";
                break;
            case Types.DOUBLE:
            case Types.FLOAT:
                typeString = "double";
                break;
            case Types.REAL:
                typeString = "float";
                break;
            case Types.NUMERIC:
            case Types.DECIMAL:
                typeString = "java.math.BigDecimal";
                break;
            case Types.DATE:
                typeString = "java.sql.Date";
                break;
            case Types.BIT:
                typeString = "boolean";
                break;
            case Types.OTHER:
                typeString = "Object";
                break;
            case Types.TIMESTAMP:
                typeString = "java.sql.Timestamp";
                break;
            case Types.TIME:
                typeString = "java.sql.Time";
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                typeString = "byte[]";
                break;
            default:
                typeString = "Object";
                break;
        }
        return typeString;
    }

    private static String generateClassName(String table, String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        return table.substring(0, 1).toUpperCase() + table.substring(1, table.length()).toLowerCase() + suffix;
    }

    private static void generateCtrAndGetters(int colType, PrintWriter outputStream, boolean publicFields, String name, StringBuilder ctrArgs, StringBuilder ctrBody, StringBuilder getters) {
        String type = sqlTypeToJavaTypeString(colType);
        outputStream.println(publicFields ? "  public " : "  private " + type + " " + name + ";");
        ctrArgs.append(type).append(" ").append(name);
        ctrBody.append("    this.").append(name).append(" = ").append(name).append(";\n");
        if (!publicFields) {
            getters.append("  public ").append(type).append(" get").append(name.substring(0, 1).toUpperCase()).append(name.substring(1, name.length())).append("() {\n");
            getters.append("    return ").append(name).append(";\n }\n");
        }
    }

    private static void writeCtrAndGetters(PrintWriter outputStream, String className, StringBuilder ctrArgs, StringBuilder ctrBody, StringBuilder getters) {
        outputStream.println("\n");
        outputStream.println("  public " + className + " (" + ctrArgs.toString() + ") {\n");
        outputStream.println(ctrBody.toString());
        outputStream.println("  }\n");
        outputStream.println(getters.toString());
        outputStream.println("}");
    }
}
