package com.example.android.askquastionapp.xmlparse;

import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
    private static final String SHAREDSTRINGS = "xl/sharedStrings.xml";
    private static final String DIRSHEET = "xl/worksheets/";
    private static final String ENDXML = ".xml";

    private List<String> listCells;

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
}
