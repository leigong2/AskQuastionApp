package com.example.jsoup.xmlparse;


import com.example.jsoup.TranslateUiUtil;
import com.example.jsoup.bean.LanguageWords;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslateUtils {
    public static void startTranslate(File srcFile, File descDir) {
        Map<String, List<List<String>>> map = ExcelManager.getInstance().analyzeXls(srcFile.getPath());
        if (map == null || map.isEmpty()) {
            map = ExcelManager.getInstance().analyzeXlsx(srcFile.getPath());
        }
//        [{xxx:"xxx", "xx":xx}, {yyy:"yyy", yy:"yy"}]
//        {xxx:"xxx", xxx:"xxx"}
        for (String key : map.keySet()) {
            List<List<String>> lists = map.get(key);
            if (lists == null) {
                return;
            }
            List<LanguageWords> languageWords = new ArrayList<>();
            for (int i = 1; i < lists.size(); i++) {
                List<String> strings = lists.get(i);
                LanguageWords languageWord = new LanguageWords();
                for (int j = 2; j < strings.size(); j++) {
                    LanguageWords.KeyWord keyWord = new LanguageWords.KeyWord();
                    keyWord.key = strings.get(0);
                    keyWord.word = strings.get(j);
                    switch (j) {
                        case 2:
                            languageWord.rCN = keyWord;
                            break;
                        case 3:
                            languageWord.en = keyWord;
                            break;
                        case 4:
                            languageWord.ar = keyWord;
                            break;
                        case 5:
                            languageWord.de = keyWord;
                            break;
                        case 6:
                            languageWord.es = keyWord;
                            break;
                        case 7:
                            languageWord.fr = keyWord;
                            break;
                        case 8:
                            languageWord.hi = keyWord;
                            break;
                        case 9:
                            languageWord.in = keyWord;
                            break;
                        case 10:
                            languageWord.it = keyWord;
                            break;
                        case 11:
                            languageWord.ja = keyWord;
                            break;
                        case 12:
                            languageWord.ko = keyWord;
                            break;
                        case 13:
                            languageWord.pt = keyWord;
                            break;
                        case 14:
                            languageWord.ru = keyWord;
                            break;
                        case 15:
                            languageWord.th = keyWord;
                            break;
                        case 16:
                            languageWord.vi = keyWord;
                            break;
                        case 17:
                            languageWord.rHK = keyWord;
                            break;
                    }
                }
                languageWords.add(languageWord);
            }
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    appendXml(languageWords, descDir);
                }
            }.start();
        }
    }

    private static void appendXml(List<LanguageWords> languageWords, File descDir) {
        try {
            long start = System.currentTimeMillis();
            for (LanguageWords languageWord : languageWords) {
                for (int i = 2; i < 18; i++) {
                    switch (i) {
                        case 2:
                            startWrite(languageWord.rCN, "values-zh-rCN", descDir);
                            break;
                        case 3:
                            startWrite(languageWord.en, "values", descDir);
                            break;
                        case 4:
                            startWrite(languageWord.ar, "values-ar", descDir);
                            break;
                        case 5:
                            startWrite(languageWord.de, "values-de", descDir);
                            break;
                        case 6:
                            startWrite(languageWord.es, "values-es", descDir);
                            break;
                        case 7:
                            startWrite(languageWord.fr, "values-fr", descDir);
                            break;
                        case 8:
                            startWrite(languageWord.hi, "values-hi", descDir);
                            break;
                        case 9:
                            startWrite(languageWord.in, "values-in", descDir);
                            break;
                        case 10:
                            startWrite(languageWord.it, "values-it", descDir);
                            break;
                        case 11:
                            startWrite(languageWord.ja, "values-ja", descDir);
                            break;
                        case 12:
                            startWrite(languageWord.ko, "values-ko", descDir);
                            break;
                        case 13:
                            startWrite(languageWord.pt, "values-pt", descDir);
                            break;
                        case 14:
                            startWrite(languageWord.ru, "values-ru", descDir);
                            break;
                        case 15:
                            startWrite(languageWord.th, "values-th", descDir);
                            break;
                        case 16:
                            startWrite(languageWord.vi, "values-vi", descDir);
                            break;
                        case 17:
                            startWrite(languageWord.rHK, "values-zh-rHK", descDir);
                            break;
                    }
                }
            }
            long l = System.currentTimeMillis() - start;
            TranslateUiUtil.getInstance().setText("写入完毕！耗时：" + l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startWrite(LanguageWords.KeyWord keyWord, String country, File descDir) throws IOException {
        File parentDir = new File(descDir + File.separator + country + File.separator);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        File file = new File(parentDir, "strings.xml");
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
        StringBuilder temp = new StringBuilder();
        while (true) {
            String s = fileReader.readLine();
            if (s == null) {
                break;
            }
            temp.append(s).append("\n");
        }
        temp = new StringBuilder(temp.toString().trim());
        int index = temp.toString().lastIndexOf("\n");
        BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
        if (index > 0) {
            boolean contains = false;
            String normal = temp.toString().substring(0, index);
            String last = temp.toString().substring(index, temp.length());
            String key = keyWord.key;
            String value = resetWord(keyWord.word);
            String string = String.format("    <string name=\"%s\">%s</string>", key, value);
            String[] split = normal.split("\n");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s == null) {
                    continue;
                }
                if (equals(s, keyWord)) {
                    fos.write(string);
                    if (i < split.length - 1) {
                        fos.write("\n");
                    }
                    contains = true;
                    continue;
                }
                fos.write(s);
                if (i < split.length - 1) {
                    fos.write("\n");
                }
            }
            if (!contains) {
                fos.write("\n");
                fos.write(string);
            }
            fos.write(last);
        } else {
            boolean contains = false;
            String key = keyWord.key;
            String value = resetWord(keyWord.word);
            String string = String.format("    <string name=\"%s\">%s</string>", key, value);
            if (!temp.toString().contains("<resources>")) {
                fos.write("<resources>");
            }
            String[] split = temp.toString().split("\n");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s == null) {
                    continue;
                }
                if (equals(s, keyWord)) {
                    fos.write(string);
                    if (i < split.length - 1) {
                        fos.write("\n");
                    }
                    contains = true;
                    continue;
                }
                fos.write(s);
                if (i < split.length - 1) {
                    fos.write("\n");
                }
            }
            if (!contains) {
                fos.write("\n");
                fos.write(string);
            }
            fos.write("\n</resources>");
        }
        fileReader.close();
        fos.close();
    }

    private static boolean equals(String string, LanguageWords.KeyWord word) {
        //<string name="more">更多</string>
        String[] split = string.split("=|>|<");
        if (split.length < 3) {
            return false;
        }
        String value = split[split.length - 2];
        String key = string.replace("<string name=\"", "").replace("\">" + value + "</string>", "").trim();
        if (word.key.equals(key)) {
            return true;
        }
        return false;
    }

    private static String resetWord(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '\'') {
                sb.append("\\");
            }
            sb.append(word.charAt(i));
        }
        return sb.toString().replaceAll("XXX", "%s");
    }
}
