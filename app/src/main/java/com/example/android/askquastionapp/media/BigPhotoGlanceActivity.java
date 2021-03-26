package com.example.android.askquastionapp.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.android.askquastionapp.BaseApplication;
import com.example.android.askquastionapp.R;
import com.example.android.askquastionapp.utils.MemoryCache;

import java.util.ArrayList;
import java.util.List;

public class BigPhotoGlanceActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<PictureCheckManager.MediaData> mediaData = new ArrayList<>();
    private int position;

    public static void start(Context context, List<PictureCheckManager.MediaData> mediaData, int position) {
        Intent intent = new Intent(context, BigPhotoGlanceActivity.class);
        MemoryCache.getInstance().put("mediaData", mediaData);
        MemoryCache.getInstance().put("position", position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_photo_glance);
        List<PictureCheckManager.MediaData> mediaData = MemoryCache.getInstance().remove("mediaData");
        if (mediaData != null) {
            this.mediaData.addAll(mediaData);
        }
        position = MemoryCache.getInstance().remove("position");
        viewPager = findViewById(R.id.view_pager2);
        viewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return BigPhotoGlanceFragment.getInstance();
            }

            @Override
            public int getItemCount() {
                return BigPhotoGlanceActivity.this.mediaData.size();
            }
        });
        viewPager.setOffscreenPageLimit(1);
        RecyclerView childAt = (RecyclerView) viewPager.getChildAt(0);
        if (childAt.getLayoutManager() != null) {
            childAt.getLayoutManager().setItemPrefetchEnabled(false);
        }
        childAt.setItemViewCacheSize(0);
        viewPager.registerOnPageChangeCallback(callback);
        viewPager.setCurrentItem(position);
        if (position > 0) {
            BaseApplication.getInstance().getHandler().postDelayed(() -> {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(position));
                if (fragment instanceof BigPhotoGlanceFragment) {
                    ((BigPhotoGlanceFragment)fragment).loadPicture(BigPhotoGlanceActivity.this.mediaData.get(position));
                }
            }, 500);
        }
    }

    private ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (viewPager.getAdapter() == null) {
                return;
            }
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(position));
            if (fragment instanceof BigPhotoGlanceFragment) {
                ((BigPhotoGlanceFragment)fragment).loadPicture(mediaData.get(position));
            }
        }
    };

    public void removeItem(PictureCheckManager.MediaData mediaData) {
        this.mediaData.remove(mediaData);
        if (viewPager.getAdapter() != null) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }
}