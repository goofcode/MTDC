
package com.logic.ffcamlib;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.WIFI_SERVICE;

public class CameraManagel implements Ipcameral.cameraManageListener {

    static final String TAG = "CameraManagel";

    Ipcameral ipcameral;
    private new_live_ui_listerner listener;
    private Context context;
    public static List<OnVideoDataRecv> mList = new ArrayList<>();
    private Context mcontext;

    private Timer crtTimer;
    private TimerTask crtTask;

    public CameraManagel(){
        this.ipcameral = Ipcameral.getInstance();
        this.ipcameral.set_manage_listener(this);
    }
    public void getCrt(Context context) {
        this.mcontext = context;
        if(this.crtTimer == null){
            this.crtTimer = new Timer();

            this.crtTask = new TimerTask() {
                public void run() {
                    if ((Ipcameral.getLogicPrivilegeState() == 1) && (Ipcameral.GetProtocol() == 3)) {
                        Log.i(TAG, "set Logic Privilege to 0");
                        Ipcameral.SetLogicPrivilege(0, getIpAddress());
                    }
                }
            };
            crtTimer.schedule(this.crtTask, 0L, 500L);
        }
    }
    public void stopGetCrt() {
        if (this.crtTimer != null) {
            this.crtTimer.cancel();
            this.crtTimer = null;
        }
        if (this.crtTask != null) {
            this.crtTask.cancel();
            this.crtTask = null;
        }

        Log.e(TAG, "stop Get Crt, protocol:" + Ipcameral.GetProtocol() );

        if ((getIpAddress() != null) && (Ipcameral.GetProtocol() == 3)) {
            Log.e(TAG, "set Logic Privilege to 1");
            Ipcameral.SetLogicPrivilege(1, getIpAddress());
        }
    }
    private String getIpAddress() {
        WifiManager wm = (WifiManager) this.context.getApplicationContext().getSystemService(WIFI_SERVICE);
        int intIp = wm.getConnectionInfo().getIpAddress();
        return (intIp & 0xff)+"."+(intIp >> 8 & 0xff)+"."+(intIp >> 16 & 0xff)+"."+(intIp >> 24 & 0xff);
    }

    public void set_live_listener(new_live_ui_listerner live_ui_listerner) {
        if (this.listener != null) {
            synchronized (this.listener) {
                this.listener = live_ui_listerner;
                return;
            }
        }
        this.listener = live_ui_listerner;
    }
    public void setContext(Context paramContext) {
        this.context = paramContext;
    }
    public void setOnDataRecvLisenner(OnVideoDataRecv paramOnVideoDataRecv) {
        if (!mList.contains(paramOnVideoDataRecv)) {
            mList.add(paramOnVideoDataRecv);
        }
    }
    public void RemoveDataRecvLisenner(OnVideoDataRecv paramOnVideoDataRecv) {
        if (mList.contains(paramOnVideoDataRecv)) {
            mList.remove(paramOnVideoDataRecv);
        }
    }

    public void startPlay() {
        this.ipcameral.startPlay(0);
    }
    public void play_video() {
        this.ipcameral.playVideo(0);
    }

    public void stopPlay() {
        this.ipcameral.stopPlay();
    }

    public void sendCtrlData(byte[] ctrlData, int length) {
        this.ipcameral.sendCtrlData(ctrlData, length);
    }

    public void exitApk() {
        this.ipcameral.deinitIpcamLib();
    }

    @Override
    public void on_connect(int paramInt) { }
    @Override
    public void on_record(int paramInt) { }
    @Override
    public void on_video(int paramInt) { }
    @Override
    public void video_data(byte[] paramArrayOfByte, int paramInt1, int paramInt2) { }

    public interface new_live_ui_listerner {
        void on_connect(int paramInt);
        void on_record(int paramInt);
        void on_video(int paramInt);
    }
}