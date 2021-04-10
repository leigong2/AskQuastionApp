package com.example.android.askquastionapp.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SqlliteUtils {
    private static SqlliteUtils sqlUtils;
    private static String DB_NAME;
    private static SQLiteDatabase db;

    private SqlliteUtils() {
    }

    public void onDestroy() throws Exception {
        db.close();
        db = null;
        sqlUtils = null;
    }

    public static SqlliteUtils getInstance(String dbFilePath) {
        try {
            db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sqlUtils == null || !dbFilePath.equals(DB_NAME)) {
            synchronized (SqlliteUtils.class) {
                if (sqlUtils == null || !dbFilePath.equals(DB_NAME)) {
                    DB_NAME = dbFilePath;
                    sqlUtils = new SqlliteUtils();
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
            db.execSQL(sql);
            return 1;
        } catch (Exception e) {
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
            db.execSQL(sql);
            return 1;
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
            Cursor cursor = db.rawQuery(sql, null);
            return cursor.getCount() > 0;
        } catch (Exception e) {
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
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> List<T> queryData(String tableName, Map<String, Object> param, Class<T> clazz) {
        return queryData(tableName, param, clazz, 1);
    }

    @NotNull
    public <T> List<T> queryData(String tableName, Map<String, Object> param, Class<T> clazz, int size) {
        List<T> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String s : param.keySet()) {
            Object o = param.get(s);
            if (o instanceof String) {
                sb.append(s).append(" like '%").append(o).append("%'").append(" ");
            } else if (o instanceof Integer && "page".equals(s)) {
                sb.append(s).append(">").append((int) o * size - 1).append(" and ").append(s).append("<").append((int) o * size + size).append(" ");
            } else {
                sb.append(s).append("=").append(o).append(" ");
            }
            sb.append("or").append(" ");
        }
        if (sb.length() > 0) {
            String sql = "select * from " + tableName + " where " + sb.substring(0, sb.length() - 3);
            try {
                Cursor rs = db.rawQuery(sql, null);
                while (rs.moveToNext()) {
                    T t = cursor2Model(rs, clazz);
                    if (t != null) {
                        result.add(t);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public <T> List<T> queryAllData(String tableName, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        String sql = "select * from " + tableName;
        try {
            Cursor rs = db.rawQuery(sql, null);
            while (rs.moveToNext()) {
                T t = cursor2Model(rs, clazz);
                if (t != null) {
                    result.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public <T> T cursor2Model(Cursor cursor, Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
            Field[] fields = t.getClass().getFields();
            for (Field field : fields) {
                Type type = field.getType();
                String fieldName = field.getName();
                field.setAccessible(true);
                if (type == Long.class || (type == Long.TYPE)) {
                    field.set(t, cursor.getLong(cursor.getColumnIndex(fieldName)));
                } else if (Integer.class == type || (type == Integer.TYPE)) {
                    field.set(t, cursor.getInt(cursor.getColumnIndex(fieldName)));
                } else if (type == String.class) {
                    field.set(t, cursor.getString(cursor.getColumnIndex(fieldName)));
                } else if (type == byte[].class) {
                    field.set(t, cursor.getBlob(cursor.getColumnIndex(fieldName)));
                } else if (type == Float.class || type == Float.TYPE) {
                    field.set(t, cursor.getFloat(cursor.getColumnIndex(fieldName)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
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
                db.execSQL(sql);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTable(String tableName) {
        try {
            String sql = "delete from " + tableName;
            db.execSQL(sql, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateData(String tableName, Map<String, Object> params) {
//        update 表名 set 字段名1=值1，字段2=值2 where 条件;

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
            http.setConnectTimeout(50000000);
            http.setReadTimeout(50000000);
            http.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            http.setRequestMethod("GET");
            http.setInstanceFollowRedirects(false);
            http.connect();
            responseCode = http.getResponseCode();
            if (responseCode == 200) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
