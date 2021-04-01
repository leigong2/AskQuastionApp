package com.example.android.askquastionapp.utils;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class ViewPagerUtils {
    public static RecyclerView getRecyclerView(ViewPager2 viewPager) {
        return (RecyclerView) viewPager.getChildAt(0);
    }
}
