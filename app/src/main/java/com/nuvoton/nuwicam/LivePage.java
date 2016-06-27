package com.nuvoton.nuwicam;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import android.app.FragmentManager;
import android.view.Surface;
import android.view.View;

import java.util.ArrayList;

public class LivePage extends AppCompatActivity implements LiveFragment.OnHideBottomBarListener {
    // live view callbacks
    public void onHideBottomBar(boolean isHide){
        if (isHide){
            bottomNavigation.hideBottomNavigation(true);
        } else {
            bottomNavigation.restoreBottomNavigation(true);
        }
    }

    private int index=0;
    private String platform = "NuWicam";
    private String cameraSerial = "5";
    private boolean clicked = false;
    private boolean isLandscape = false;
    private static final String TAG = "SkyEye", FRAGMENT_TAG = "CURRENT_FRAGMENT_INDEX";
    private AHBottomNavigation bottomNavigation;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private FragmentManager fragmentManager = getFragmentManager();
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        View decorView = getWindow().getDecorView();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if (index != 1){
                bottomNavigation.hideBottomNavigation(true);
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                isLandscape = true;
            }
        } else {
            bottomNavigation.restoreBottomNavigation(true);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            isLandscape = false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        View decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_live_page);
        Log.d(TAG, "onCreate:" + platform + ", " + cameraSerial);
        initUI();
        switch (orientation){
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (index != 1){
                    bottomNavigation.hideBottomNavigation(true);
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                    isLandscape = true;
                }
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putInt(FRAGMENT_TAG, index);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: " + String.valueOf(savedInstanceState.getInt(FRAGMENT_TAG)));
        changeFragment(savedInstanceState.getInt(FRAGMENT_TAG));
    }

    private void initUI(){
        final Bundle bundle = new Bundle();
        bundle.putString("Platform", platform);
        bundle.putString("CameraSerial", cameraSerial);

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem liveItem = new AHBottomNavigationItem("Live", R.drawable.livetab);
        AHBottomNavigationItem settingItem = new AHBottomNavigationItem("Setting", R.drawable.geartab);

        bottomNavigationItems.add(liveItem);
        bottomNavigationItems.add(settingItem);

        bottomNavigation.addItems(bottomNavigationItems);
        bottomNavigation.setAccentColor(Color.parseColor("#007DFF"));

        bottomNavigation.setNotification(0, 0);
        LiveFragment fragment = new LiveFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected){
                index = position;
                if (position == 0){
                    LiveFragment fragment = LiveFragment.newInstance(bundle);
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_content, fragment)
                            .commit();
                }else{
                    SettingFragment fragment = SettingFragment.newInstance(bundle);
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_content, fragment)
                            .commit();
                }
                bottomNavigation.setNotification(0, position);
                return true;
            }
        });
        changeFragment(0);
    }

    private void changeFragment(int savedIndex){
        Bundle bundle = new Bundle();
        bundle.putString("Platform", platform);
        bundle.putString("CameraSerial", cameraSerial);
        index = savedIndex;
        if (index == 0){
            LiveFragment fragment = LiveFragment.newInstance(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, fragment)
                    .commit();
        }else{
            SettingFragment fragment = new SettingFragment();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, fragment)
                    .commit();
        }
        bottomNavigation.setNotification(0, index);
        bottomNavigation.setCurrentItem(index);
    }

    public void showBottomBar(boolean option){
        if (option == false){
            bottomNavigation.hideBottomNavigation(true);
        }else {
            bottomNavigation.restoreBottomNavigation(true);
        }
    }
}
