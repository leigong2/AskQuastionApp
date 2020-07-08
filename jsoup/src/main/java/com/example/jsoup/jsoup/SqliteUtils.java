package com.example.jsoup.jsoup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SqliteUtils {
    private static SqliteUtils sqlUtils;
    private static String DB_NAME;
    private static Connection conn = null;
    private static Statement stat = null;

    private SqliteUtils() {
    }

    public void onDestroy() throws Exception {
        stat.close();
        conn.close();
        conn = null;
        stat = null;
        sqlUtils = null;
    }

    public static SqliteUtils getInstance(String dbFilePath) {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
            stat = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sqlUtils == null || !dbFilePath.equals(DB_NAME)) {
            synchronized (SqliteUtils.class) {
                if (sqlUtils == null || !dbFilePath.equals(DB_NAME)) {
                    DB_NAME = dbFilePath;
                    sqlUtils = new SqliteUtils();
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

    public <T> int createTable(String tableName, Class<T> clz) {
        try {
            // 拿到该类
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
            int i = stat.executeUpdate(sql);
            stat.close();
            return i;
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
            stat.close();
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
    public synchronized <T> boolean insertData(String tableName, T t) {
        createTable(tableName, t.getClass());
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
            boolean execute = stat.execute(sql);
            stat.close();
            return execute;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized <T> boolean insertData(String tableName, List<T> ts) {
        long time = System.currentTimeMillis();
        if (ts == null || ts.isEmpty()) {
            return false;
        }
        createTable(tableName, ts.get(0).getClass());
        try {
            for (int i = 0; i < ts.size(); i++) {
                // 拿到该类
                Class<?> clz = ts.get(i).getClass();
                // 获取实体类的所有属性，返回Field数组
                Field[] fields = clz.getDeclaredFields();
                StringBuilder keys = new StringBuilder();
                StringBuilder values = new StringBuilder();
                for (Field field : fields) {
                    byte[] items = field.getName().getBytes();
                    items[0] = (byte) ((char) items[0] - 'a' + 'A');
                    Method m = ts.get(i).getClass().getMethod("get" + new String(items));
                    Object value = m.invoke(ts.get(i));
                    Type type = field.getGenericType();
                    keys.append(new String(items)).append(",");
                    if ("class java.lang.String".equals(type.toString())) {
                        values.append("\'").append(value).append("\'").append(",");
                    } else {
                        values.append(i).append(",");
                    }
                }
                String sql = "insert into " + tableName + "(" + keys.substring(0, keys.length() - 1) + ") values(" + values.substring(0, values.length() - 1) + ")";
                stat.addBatch(sql);
            }
            System.out.println("sql 语句加载完毕....耗时：" + (System.currentTimeMillis() - time));
            int[] execute = stat.executeBatch();
            System.out.println( "总耗时 ：" + (System.currentTimeMillis() - time));
            stat.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> List<T> queryAllData(String tableName, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        String sql = "select * from " + tableName;
        try {
            ResultSet rs = stat.executeQuery(sql);
            while (rs.next()) {
                T t = clazz.newInstance();
                for (Field field : t.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(t, rs.getObject(field.getName()));
                }
                result.add(t);
            }
            rs.close();
            stat.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public <T> List<T> queryData(String tableName, Map<String, Object> param, Class<T> clazz) {
        return queryData(tableName, param, clazz, 1);
    }

    public <T> List<T> queryData(String tableName, Map<String, Object> param, Class<T> clazz, int size) {
        List<T> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String s : param.keySet()) {
            Object o = param.get(s);
            if (o instanceof String) {
                sb.append(s).append(" like '%").append(o).append("%'").append(" ");
            } else if (o instanceof Integer && "page".equals(s)) {
                sb.append(s).append(">").append((int) o - 1).append(" and ").append(s).append("<").append((int) o + size).append(" ");
            } else {
                sb.append(s).append("=").append(o).append(" ");
            }
            sb.append("or ");
        }
        if (sb.length() > 0) {
            String sql = "select * from " + tableName + " where " + sb.substring(0, sb.length() - 3);
            try {
                ResultSet rs = stat.executeQuery(sql);
                while (rs.next()) {
                    T t = clazz.newInstance();
                    for (Field field : t.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(t, rs.getObject(field.getName()));
                    }
                    result.add(t);
                }
                rs.close();
                stat.close();
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
                boolean execute = stat.execute(sql);
                stat.close();
                return execute;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTable(String tableName) {
        try {
            String sql = "delete from " + tableName;
            boolean execute = stat.execute(sql);
            stat.close();
            return execute;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> int updateData(String tableName, Map<String, Object> oldMap, T t) {
//        update 表名 set 字段名1=值1，字段2=值2 where 条件;
        StringBuilder sb = new StringBuilder();
        for (String s : oldMap.keySet()) {
            sb.append(s).append("=").append(oldMap.get(s)).append(",");
        }
        sb = new StringBuilder(sb.subSequence(0, sb.length() - 1));
        sb.append(" where ");
        try {
            // 拿到该类
            Class<?> clz = t.getClass();
            // 获取实体类的所有属性，返回Field数组
            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
                byte[] items = field.getName().getBytes();
                items[0] = (byte) ((char) items[0] - 'a' + 'A');
                Method m = t.getClass().getMethod("get" + new String(items));
                Object value = m.invoke(t);
                Type type = field.getGenericType();
                sb.append(new String(items)).append("=");
                if ("class java.lang.String".equals(type.toString())) {
                    sb.append("\'").append(value).append("\'");
                } else {
                    sb.append(value);
                }
                sb.append(" and ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql = "update " + tableName + " set " + sb.substring(0, sb.length() - 5);
        try {
            int i = stat.executeUpdate(sql);
            stat.close();
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean checkUrlEnable(String wma) {
        if ("".equals(wma)) {
            return false;
        }
        URL url;
        int responseCode = 0;
        try {
            url = new URL(wma);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(5000);
            http.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            responseCode = http.getResponseCode();
            if (responseCode == 200) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void insertColumn(String video_bean, String id) {
        try {
            String sql = "alter table " + video_bean + " add " + id + " int IDENTITY(1,1) not null";
            stat.execute(sql);
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dropColumn(String video_bean, String id) {
        String sql = "alter table " + video_bean + " drop column " + id + ";";
        try {
            stat.execute(sql);
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
