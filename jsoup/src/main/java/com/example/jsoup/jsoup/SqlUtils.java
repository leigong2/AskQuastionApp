package com.example.jsoup.jsoup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlUtils {
    private static SqlUtils sqlUtils;
    private static String DB_NAME;
    Connection conn = null;
    Statement stat = null;

    private SqlUtils() {
        try {
            // 1.建立驱动，加载驱动程序
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 2.建立连接，与数据库进行连接
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + DB_NAME + "?" +
                            "&useUnicode=true" +
                            "&characterEncoding=utf-8" +
                            "&useJDBCCompliantTimezoneShift=true" +
                            "&useLegacyDatetimeCode=false" +
                            "&serverTimezone=UTC" +
                            "&&useSSL=false",
                    "root", "long0715");
            stat = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() throws Exception {
        stat.close();
        conn.close();
        conn = null;
        stat = null;
        sqlUtils = null;
    }

    public static SqlUtils getInstance(String dbName) {
        if (sqlUtils == null || !dbName.equals(DB_NAME)) {
            synchronized (SqlUtils.class) {
                if (sqlUtils == null || !dbName.equals(DB_NAME)) {
                    DB_NAME = dbName;
                    sqlUtils = new SqlUtils();
                }
            }
        }
        return sqlUtils;
    }

    /**
     * Create table.
     *
     * @param tableName the table name
     * @param params    the params, key: filed, value, varchar(255)
     *                  String sql = "CREATE TABLE " + tableName + "\n" +
     *                  "(\n" +
     *                  "Id_P int,\n" +
     *                  "LastName varchar(255),\n" +
     *                  "FirstName varchar(255),\n" +
     *                  "Address varchar(255),\n" +
     *                  "City varchar(255)\n" +
     * @return
     */
    public int createTable(String tableName, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (String s : params.keySet()) {
            sb.append(s).append(" ").append(params.get(s)).append(",");
        }
        String substring = sb.substring(0, sb.length() - 1);
        String sql = "create table if not exists" + tableName + "\n" +
                "(\n" +
                substring +
                ")";
        try {
            return stat.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public <T> int createTable(String tableName, T t) {
        try {
            // 拿到该类
            Class<?> clz = t.getClass();
            // 获取实体类的所有属性，返回Field数组
            Field[] fields = clz.getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            for (Field field : fields) {
                String name = field.getName();
                Type type = field.getGenericType();
                if ("class java.lang.String".equals(type.toString())) {
                    sb.append(name).append(" ").append("varchar(255)").append(",");
                } else if ("Integer".equals(type.toString()) || "int".equals(type.toString())) {
                    sb.append(name).append(" ").append("int(4)").append(",");
                } else if ("Float".equals(type.toString()) || "float".equals(type.toString())) {
                    sb.append(name).append(" ").append("float(6,2)").append(",");
                } else if ("Double".equals(type.toString()) || "double".equals(type.toString())) {
                    sb.append(name).append(" ").append("double(6,2)").append(",");
                } else {
                    sb.append(name).append(" ").append("varchar(255)").append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            String sql = "create table if not exists " + tableName + "(" + sb + ")";
            return stat.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Is table exit boolean.
     *
     * @param table the table
     * @return the boolean
     */
    public boolean isTableExit(String table) {
        String sql = "SHOW TABLES LIKE '%" + table + "%'";
        try {
            ResultSet rs = stat.executeQuery(sql);
            ResultSetMetaData m = rs.getMetaData();
            int columns = m.getColumnCount();
            rs.close();
            return columns > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Insert data boolean.
     *
     * @param <T>       the type parameter
     * @param tableName the table name
     * @param t         the t
     * @return the boolean
     */
    public <T> boolean insertData(String tableName, T t) {
        createTable(tableName, t);
        try {
            // 拿到该类
            Class<?> clz = t.getClass();
            // 获取实体类的所有属性，返回Field数组
            Field[] fields = clz.getDeclaredFields();
            StringBuilder keys = new StringBuilder();
            StringBuilder values = new StringBuilder();
            for (Field field : fields) {
                byte[] items = field.getName().getBytes();
                items[0] = (byte) ((char) items[0] - 'a' + 'A');
                Method m = t.getClass().getMethod("get" + new String(items));
                Object value = m.invoke(t);
                Type type = field.getGenericType();
                keys.append(new String(items)).append(",");
                if ("class java.lang.String".equals(type.toString())) {
                    values.append("\'").append(value).append("\'").append(",");
                } else {
                    values.append(value).append(",");
                }
            }
            String sql = "insert into " + tableName + "(" + keys.substring(0, keys.length() - 1) + ") values(" + values.substring(0, values.length() - 1) + ")";
            return stat.executeUpdate(sql) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> List<T> queryData(String tableName, Map<String, Object> param, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String s : param.keySet()) {
            Object o = param.get(s);
            if (o instanceof String) {
                sb.append(s).append("=").append("\"").append(o).append("\"");
            } else {
                sb.append(s).append("=").append(o);
            }
            sb.append("and");
        }
        if (sb.length() > 0) {
            String sql = "select * from " + tableName + " where " + sb.substring(0, sb.length() - 3);
            try {
                ResultSet rs = stat.executeQuery(sql);
                while (rs.next()) {
                    T t = clazz.newInstance();
                    for (Field field : t.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(t,rs.getObject(field.getName()));
                    }
                    result.add(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean deleteData(String tableName, Map<String, Object> param) {
//        delete from 表名 where 条件;
        try {
            StringBuilder sb = new StringBuilder();
            for (String s : param.keySet()) {
                Object o = param.get(s);
                if (o instanceof String) {
                    sb.append(s).append("=").append("\"").append(o).append("\"");
                } else {
                    sb.append(s).append("=").append(o);
                }
                sb.append("and");
            }
            if (sb.length() > 0) {
                String sql = "delete from " + tableName + " where " + sb.substring(0, sb.length() - 3);
                return stat.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTable(String tableName) {
        try {
            String sql = "delete from " + tableName;
            return stat.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateData(String tableName, Map<String, Object> params) {
//        update 表名 set 字段名1=值1，字段2=值2 where 条件;

    }

}
