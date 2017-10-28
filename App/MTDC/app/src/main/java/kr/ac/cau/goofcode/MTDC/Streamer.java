package kr.ac.cau.goofcode.MTDC;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.logic.ffcamlib.CameraManagel;
import com.logic.ffcamlib.OnVideoDataRecv;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Streamer extends SurfaceView  implements SurfaceHolder.Callback, OnVideoDataRecv {

    private static final String TAG = "Streamer";

    private CameraManagel cameraManagel;

    private Mat rawMat = new Mat();
    private Mat detectMat = new Mat();

    private Thread drawThread;
    private SurfaceHolder surfaceHolder;
    private Bitmap bm = null;

    private static final BlockingQueue<VideoPacket> videoList = new LinkedBlockingQueue<>(5);

    static {
        try {
            System.loadLibrary("opencv_java3");
            System.loadLibrary("marker-detect");
        } catch (UnsatisfiedLinkError ule) {
            Log.e("LoadJniLib", "Error: Could not load native library: " + ule.getMessage());
        }
    }

    public Streamer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        cameraManagel = ((ControlModeActivity)context).getCameraManagel();
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder){
        cameraManagel.setOnDataRecvLisenner(this);
        drawThread = new Thread(new DrawingThread());
        drawThread.start();
    }
    public void surfaceChanged(SurfaceHolder surfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
    }
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.e(TAG, "onSurfaceDestroy");
        drawThread.interrupt();
        cameraManagel.RemoveDataRecvLisenner(this);
        drawThread.interrupt();

    }

    // On data receive
    public void dataRecv(int width, int height, int bit, byte[] data, int frameType) {
        if (surfaceHolder == null) return;
        if (bm == null) bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        // push video packet to blocking queue
        videoList.offer(new VideoPacket(bit, data.length, data, frameType, height, width));
    }

    public native void convertRGBtoGray(long matAddrInput, long matAddrResult);
    public native int[] getTrackingControlData(long matAddrInput, long matAddrResult);

    private class DrawingThread implements Runnable{
        public void run() {

            while(bm == null) {
                try { Thread.sleep(50); }
                catch (InterruptedException e) { e.printStackTrace();}
            }

            Log.i(TAG, "Video drawThread started");

            Matrix matrix = new Matrix();
            matrix.postRotate(180.0F);

            Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            while(true) {
                if (videoList.isEmpty()) continue;

                // get one video packet
                VideoPacket videoPacket = videoList.poll();

                // make raw bitmap from video packet
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(videoPacket.mDataBuf));

                //if tracking mode, show markers
                if(((ControlModeActivity)getContext()).isTrackingMode())
                {
                    // detect marker on bitmap
                    Utils.bitmapToMat(bitmap, rawMat);
                    ((ControlModeActivity) getContext()).trackCtrlData
                            = getTrackingControlData(rawMat.getNativeObjAddr(), detectMat.getNativeObjAddr());
                    ((ControlModeActivity) getContext()).isTrackCtrlDataUsable = true;
                    Utils.matToBitmap(detectMat, bitmap);
                }
                else
                    ((ControlModeActivity) getContext()).isTrackCtrlDataUsable = false;

                Canvas canvas = surfaceHolder.lockCanvas();
                //canvas.drawColor(Color.BLACK);
                if(canvas != null) {
                    canvas.drawBitmap(bitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    // unused implement
    public void audioData(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3) {}

    private class VideoPacket {
        public int mBufLen;
        public byte[] mDataBuf;
        public int mFrameType;
        public int mVideoHeight;
        public int mVideoWidth;
        public int mbit;

        public VideoPacket(int bit, int bufLen, byte[] dataBuf, int frameType, int videoHeight, int videoWidth) {
            this.mBufLen = bufLen;
            this.mDataBuf = dataBuf;
            this.mFrameType = frameType;
            this.mVideoHeight = videoHeight;
            this.mVideoWidth = videoWidth;
            this.mbit = bit;
        }
    }

}