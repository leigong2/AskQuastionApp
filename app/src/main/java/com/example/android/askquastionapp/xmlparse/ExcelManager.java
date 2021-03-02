package com.example.android.askquastionapp.xmlparse;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.android.askquastionapp.bean.Company;
import com.example.android.askquastionapp.utils.FileUtil;
import com.example.jsoup.GsonGetter;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * Created by 请叫我张懂 on 2017/9/25.
 */

public class ExcelManager {
    private static ExcelManager instance;

    private ExcelManager() {

    }

    public static ExcelManager getInstance() {
        if (instance == null) {
            synchronized (ExcelManager.class) {
                if (instance == null) {
                    instance = new ExcelManager();
                }
            }
        }
        return instance;
    }

    public Map<String, List<List<String>>> analyzeXlsx(File fileName) {
        long start = System.currentTimeMillis();
        Map<String, List<List<String>>> data = new HashMap<>();
        try {
            InputStream stream = new FileInputStream(fileName);
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(0);
                int rowsCount = sheet.getPhysicalNumberOfRows();
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                List<List<String>> list = new ArrayList<>();
                for (int r = 0; r < rowsCount; r++) {
                    Row row = sheet.getRow(r);
                    int cellsCount = row.getPhysicalNumberOfCells();
                    List<String> list3 = new ArrayList<>();
                    for (int c = 0; c < cellsCount; c++) {
                        String cellAsString = getCellAsString(row, c, formulaEvaluator);
                        list3.add(cellAsString);
                    }
                    list.add(list3);
                }
                data.put(workbook.getSheetName(i), list);
            }
        } catch (Exception e) {
            /* proper exception handling to be here */
        }
        Log.i("zune: ", "analyzeXlsx耗时: " + (System.currentTimeMillis() - start));
        return data;
    }

    //Map转Object
    public <T> T mapToObject(Map<Object, Object> map, Class<T> beanClass) throws Exception {
        if (map == null)
            return null;
        T obj = beanClass.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            if (map.containsKey(field.getName())) {
                field.set(obj, map.get(field.getName()));
            }
        }
        return obj;
    }

    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        value = "" + numericValue;
                    }
                    break;
                case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
        }
        return value;
    }

    public Map<String, List<List<String>>> analyzeXls(String fileName) {
        long start = System.currentTimeMillis();
        Map<String, List<List<String>>> map = new HashMap<>();
        List<List<String>> rows;
        List<String> columns = null;
        try {
            Workbook workbook = Workbook.getWorkbook(new File(fileName));
            Sheet[] sheets = workbook.getSheets();
            for (Sheet sheet : sheets) {
                rows = new ArrayList<>();
                String sheetName = sheet.getName();
                for (int i = 0; i < sheet.getRows(); i++) {
                    Cell[] sheetRow = sheet.getRow(i);
                    int columnNum = sheet.getColumns();
                    for (int j = 0; j < sheetRow.length; j++) {
                        if (j % columnNum == 0) {
                            columns = new ArrayList<>();
                        }
                        columns.add(sheetRow[j].getContents());
                    }
                    rows.add(columns);
                }
                map.put(sheetName, rows);
            }

            Iterator<Map.Entry<String, List<List<String>>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<List<String>>> next = iterator.next();
                Iterator<List<String>> iterator1 = next.getValue().iterator();
                while (iterator1.hasNext()) {
                    Log.i("zzz", "analyzeXls: sheet --> " + next.getKey() + " row --> " + iterator1.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("zune: ", "analyzeXls耗时: " + (System.currentTimeMillis() - start));
        return map;
    }

    @NotNull
    public Map<String, List<List<String>>> getStringListMap(Context context, String[] assets) {
        Map<String, List<List<String>>> map = new HashMap<>();
        for (String asset : assets) {
            Map<String, List<List<String>>> temp = null;
            File fileName = FileUtil.assetsToFile(context, asset);
            if (fileName != null) {
                temp = ExcelManager.getInstance().analyzeXls(fileName.getPath());
            }
            if (temp == null || temp.isEmpty()) {
                temp = ExcelManager.getInstance().analyzeXlsx(fileName);
            }
            if (temp == null || temp.isEmpty()) {
                continue;
            }
            for (String s : temp.keySet()) {
                List<List<String>> value = temp.get(s);
                if (value != null) {
                    List<List<String>> sheet = map.get("sheet");
                    if (sheet == null || sheet.isEmpty()) {
                        map.put("sheet", value);
                    } else {
                        sheet.addAll(value);
                        map.put("sheet", sheet);
                    }
                }
            }
        }
        return map;
    }


    public String getJson(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public <T> List<T> getData(Class<T> clazz, @NonNull Map<String, List<List<String>>> map) {
        List<T> ts = new ArrayList<>();
        for (String key : map.keySet()) {
            List<List<String>> lists = map.get(key);
            if (lists == null) {
                continue;
            }
            for (int i = 1; i < lists.size(); i++) {
                List<String> values = lists.get(i);
                if (values == null) {
                    continue;
                }
                StringBuilder json = new StringBuilder();
                for (int j = 0; j < values.size(); j++) {
                    String value = values.get(j);
                    if (value == null) {
                        value = "";
                    }
                    String str = value.replaceAll("\\\\", "\\\\\\\\").trim();
                    if (j == 0) {
                        json.append("{\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\",");
                    } else if (j == values.size() - 1) {
                        json.append("\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\"}");
                    } else {
                        json.append("\"").append(lists.get(0).get(j)).append("\":").append("\"").append(str).append("\",");
                    }
                }
                T t = GsonGetter.getInstance().getGson().fromJson(json.toString(), clazz);
                if (t instanceof Company) {
                    float[] moneys = isMoneyEnable(((Company) t).money);
                    if (moneys == null || moneys.length == 1 || moneys[1] < 11) {
                        continue;
                    }
                    ((Company) t).minMoney = moneys[0];
                    ((Company) t).maxMoney = moneys[1];
                }
                if (t != null) {
                    ts.add(t);
                }
            }
        }
        return ts;
    }

    public float[] isMoneyEnable(String money) {
        /*zune：6K-10K, 8-12K,  8-12k·13薪,   1.5-2万/月,  薪资面议, 6-9千/月, 8千-1.2万/月**/
        float min = 0;
        float max = 0;
        String[] split = money.split("[-·]");
        if (split.length == 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split[0].length(); i++) {
            char c = split[0].charAt(i);
            if (c == '.' || (c >= '0' && c <= '9')) {
                sb.append(c);
            }
        }
        try {
            min = Float.parseFloat(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.setLength(0);
        for (int i = 0; i < split[1].length(); i++) {
            char c = split[1].charAt(i);
            if (c == '.' || (c >= '0' && c <= '9')) {
                sb.append(c);
            }
            if ('万' == c) {
                try {
                    max = Float.parseFloat(sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                min *= split[0].contains("千") ? 1 : 10;
                max *= 10;
                break;
            }
            if ('千' == c || 'K' == c || 'k' == c) {
                try {
                    max = Float.parseFloat(sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new float[]{min, max};
    }
}
