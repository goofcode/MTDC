package kr.ac.cau.goofcode.MTDC;

import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class BaseActivity extends AppCompatActivity {
    PowerManager.WakeLock wakeLock = null;
    private int wifiSleepPolicy = 0;

    private void setWifiNeverSleep()
    {
        this.wifiSleepPolicy = Settings.System.getInt(getContentResolver(), "wifi_sleep_policy", 0);
        Settings.System.putInt(getContentResolver(), "wifi_sleep_policy", 2);
    }

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    protected void onResume()
    {
        super.onResume();
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onStop()
    {
        super.onStop();
    }
}
