package com.example.jsoup;

public class NumberUtils {
    public static int chineseToNumber(String chineseNum) {
        try {
            if (chineseNum.length() ==1) {
                return singleNumber(chineseNum);
            }
            if (chineseNum.length() == 2) {
                return twoNumber(chineseNum);
            }
            if (chineseNum.length() == 3) {
                return threeNumber(chineseNum);
            }
            return mulNumber(chineseNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int mulNumber(String chineseNum) {
        if (chineseNum.contains("亿") || chineseNum.contains("万") || chineseNum.contains("千")) {
            //Todo
            return 1000;
        }
        int baiWei = singleNumber(chineseNum.split("百")[0]);
        String shiGeWei = chineseNum.split("百")[1];
        if (shiGeWei.endsWith("十")) {
            return baiWei * 100 + singleNumber(shiGeWei.replaceAll("十", "")) * 10;
        }
        String[] shiGes = shiGeWei.split("十");
        if (shiGes.length == 1) {
            return baiWei * 100 + singleNumber(shiGeWei.substring(shiGeWei.length() - 1));
        }
        return baiWei * 100 + singleNumber(shiGes[0]) * 10 + singleNumber(shiGes[1].replaceAll("零", ""));
    }

    private static int threeNumber(String chineseNum) {
        if (!chineseNum.contains("十")) {
            return -1;
        }
        String shiWei = chineseNum.substring(0, 1);
        String geWei = chineseNum.substring(2, 3);
        return singleNumber(shiWei) * 10 + singleNumber(geWei);
    }

    private static int twoNumber(String chineseNum) {
        if (chineseNum.startsWith("十")) {
            return 10 + singleNumber(chineseNum.replaceAll("十", ""));
        }
        if (chineseNum.endsWith("十")) {
            return 10 * singleNumber(chineseNum.replaceAll("十", ""));
        }
        if (chineseNum.endsWith("百")) {
            return 10 * singleNumber(chineseNum.replaceAll("百", ""));
        }
        if (chineseNum.endsWith("千")) {
            return 10 * singleNumber(chineseNum.replaceAll("千", ""));
        }
        if (chineseNum.endsWith("万")) {
            return 10 * singleNumber(chineseNum.replaceAll("万", ""));
        }
        if (chineseNum.endsWith("亿")) {
            return 10 * singleNumber(chineseNum.replaceAll("亿", ""));
        }
        return 10;
    }

    private static int singleNumber(String singleNum) {
        switch (singleNum) {
            case "零":
                return 0;
            case "一":
                return 1;
            case "二":
                return 2;
            case "三":
                return 3;
            case "四":
                return 4;
            case "五":
                return 5;
            case "六":
                return 6;
            case "七":
                return 7;
            case "八":
                return 8;
            case "九":
                return 9;
            case "十":
                return 10;
        }
        return 0;
    }
}
