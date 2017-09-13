package com.logic.ffcamlib;

import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class Ipcameral {
    private static final String TAG = "ipcam";

    public static int mbit;
    public static int mheight;
    public static int mwidth;
    public static Ipcameral singleton;

    private ByteBuffer buffer;
    private cameraManageListener cam_mgr_listener;

    static {
        try {
            System.loadLibrary("ffmpeg");
            System.loadLibrary("logicvideo");
        } catch (UnsatisfiedLinkError ule) {
            Log.e("LoadJniLib", "Error: Could not load native library: " + ule.getMessage());
        }
    }

    private Ipcameral() {
        initIpcamLib(2, 0, new int[]{0}, 1);
    }

    public static Ipcameral getInstance() {
        if (singleton == null) {
            singleton = new Ipcameral();
        }
        return singleton;
    }

    public void set_manage_listener(cameraManageListener cameraManageListener) {
        this.cam_mgr_listener = cameraManageListener;
    }

    public static native int GetProtocol();

    public static native int IsSdCarPathNewPro();

    public static native int LogicSdVOD(String paramString, int paramInt);

    public static native int SetLogicPrivilege(int paramInt, String paramString);

    public static native int SetStreamBoardInfo();

    public static native void StartCtrlConnect();

    public static native void StopCtrlConnect();

    public static native double currentTime();

    public static native double duration();

    public static native int getLogicPrivilegeState();

    public static native float getPlayProgress();

    public static native int sdcarGeneralPhoto(int paramInt);

    public static native void setEnableSendData(int paramInt);

    public static native int setStreamBrightness(int paramInt);

    public static native int setStreamContrasts(int paramInt);

    public static native int setStreamResolution(int paramInt);

    public static native int setStreamSaturation(int paramInt);

    public static native int setStreamSharpness(int paramInt);

    public static native int setWiFiChannel(int paramInt);

    public native int DeleteSdCarFile(String paramString);

    public native int GetSdCarBasicInfo();

    public native int GetSdCarPathInfo(int paramInt);

    public native int IsMjxPre();

    public native void OnPause();

    public native void OnRePlay();

    public native void OnStop();

    public native void Onplay(String paramString, int paramInt1, int paramInt2, Surface paramSurface);

    public native int SdCarFileDownloadContinue(String paramString);

    public native int SdCarFileDownloadPause(String paramString);

    public native int SdCarFileDownloadStart(String paramString, int paramInt);

    public native int SdCarFileDownloadStart1(String paramString);

    public native int SdCarFileDownloadStop(String paramString);

    public native int SdCarFormat(int paramInt);

    public native int SdCarRecordThumbnail(String paramString);

    public native void deinitIpcamLib();

    public native int initIpcamLib(int paramInt1, int paramInt2, int[] paramArrayOfInt, int paramInt3);

    public native void playVideo(int paramInt);

    public native void seekTime(double paramDouble);

    public native void sendCtrlData(byte[] paramArrayOfByte, int paramInt);

    public native int setParameter(String paramString, int paramInt, char paramChar1, char paramChar2);

    public native void startPlay(int paramInt);

    public native void startRecord(String paramString, int paramInt);

    public native int startSDCarRecord();

    public native void stopPlay();

    public native void stopRecord();

    public native int stopSDCarRecord();

    public native void stopVideo();

    public void videoDataStream(int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte) {}
    public void vodDataStream(int paramInt1, int paramInt2, byte[] paramArrayOfByte) {}
    public void softDecodeStream(int paramInt1, int paramInt2, int index, byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) {
            return;
        }
        mwidth = paramInt1;
        mheight = paramInt2;
        this.cam_mgr_listener.video_data(paramArrayOfByte, paramInt1, paramInt2);
        index = 0;
        while(index < CameraManagel.mList.size()) {
            CameraManagel.mList.get(index).dataRecv(paramInt1, paramInt2, 0, paramArrayOfByte, 0);
            index += 1;
        }
    }
    public void hardDecodeStream(int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4) {}
    public void setAppCallBack(int paramInt1, int paramInt2) {}
    public void callBackSerialData(byte[] paramArrayOfByte, int paramInt) {}
    public void callBackSerialInfo(int paramInt1, int paramInt2) {}
    public void callBackStreamInfo(int paramInt1, int paramInt2) {}
    public void cameraStatusChangedCallback(int paramInt) {this.cam_mgr_listener.on_connect(paramInt);}
    public void callBackBoardInfo(int paramInt1, int paramInt2) {}
    public void callBackSdCarBasicInfo(byte paramByte1, byte paramByte2, int paramInt1, int paramInt2, int paramInt3) {}
    public void callBackSdCarDataInfo(int paramInt){}
    public void callBackSdCarpathInfo(int paramInt1, int paramInt2, long paramLong, int paramInt3, String paramString) {}
    public void recordResultCallback(int paramInt) {this.cam_mgr_listener.on_record(paramInt);}
    public void RcSendMsg(byte[] paramArrayOfByte, int paramInt) {}
    public void FHNPStateCallBack(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7) {}
    public void BoardStateInfo(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        Log.e("boardinfo", "BoardID: " + paramInt1 + " IcType:" + paramInt2 + " Resolution:" + paramInt3 + " IsSupportSdCar" + paramInt4);
    }


    public interface cameraManageListener {
        void on_connect(int paramInt);
        void on_record(int paramInt);
        void on_video(int paramInt);
        void video_data(byte[] paramArrayOfByte, int paramInt1, int paramInt2);
    }
}