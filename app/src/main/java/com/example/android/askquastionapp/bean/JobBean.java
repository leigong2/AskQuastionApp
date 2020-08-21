package com.example.android.askquastionapp.bean;

import android.text.TextUtils;

public class JobBean {
    public String companyName;
    public String area;
    public String jobName;
    public String minYears;
    public String maxYears;
    public String minMoney;
    public String maxMoney;
    public String companyDesc;
    public String peoples;
    public String workDesc;

    public static JobBean companyToJob(Company company) {
        JobBean jobBean = new JobBean();
        jobBean.companyName = company.company;
        jobBean.area = company.address;
        String money = company.money;
        String[] split = money.split("-|K");
        jobBean.minMoney = split[0];
        jobBean.maxMoney = split.length < 2 ? split[0] : split[1];
        jobBean.jobName = company.os;
        String years = company.years;
        String[] split1 = years.split("\\d");
        StringBuilder sb = new StringBuilder();
        for (String s : split1) {
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            sb.append(s).append("|");
        }
        sb.delete(sb.length() - 1, sb.length());
        String[] year = years.split(sb.toString());
        jobBean.minYears = !split1[0].equals(company.years) && year.length > 0 ? year[0] : "0";
        jobBean.maxYears = !split1[0].equals(company.years) && year.length > 1 ? year[1] : "0";
        jobBean.workDesc = company.companyDetail;
        return jobBean;
    }
}
