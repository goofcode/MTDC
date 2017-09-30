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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Streamer extends SurfaceView  implements SurfaceHolder.Callback, OnVideoDataRecv, Runnable {

    private static final String TAG = "Streamer";

    private CameraManagel cameraManagel;

    private SurfaceHolder surfaceHolder;
    private static final BlockingQueue<VideoPacket> videoList = new LinkedBlockingQueue<>(5);
    private Thread drawThread;
    private Bitmap bm = null;

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

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder){
        cameraManagel.setOnDataRecvLisenner(this);
        drawThread = new Thread(Streamer.this);
        drawThread.start();
    }
    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
    }
    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
        cameraManagel.RemoveDataRecvLisenner(this);
        drawThread.interrupt();
        surfaceHolder = null;
    }

    // On data receive
    public void dataRecv(int width, int height, int bit, byte[] data, int frameType) {
        if (surfaceHolder == null) return;
        if (bm == null) bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        // push video packet to blocking queue
        videoList.offer(new VideoPacket(bit, data.length, data, frameType, height, width));
    }
    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);


    @Override
    public void run() {
        Log.i(TAG, "Video drawThread started");

        while(true) {
            if (videoList.isEmpty())
                continue;

            // get one video packet
            VideoPacket videoPacket = videoList.poll();

            // make raw bitmap from video packet
            Matrix rawMatrix = new Matrix();
            rawMatrix.postRotate(180.0F);
            Bitmap rawBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), rawMatrix, true);
            Bitmap detectBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), rawMatrix, true);

            rawBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(videoPacket.mDataBuf));

            // convert raw bitmap to gray bitmap
            Mat rawMat = new Mat();
            Mat detectMat = new Mat();

            Utils.bitmapToMat(rawBitmap, rawMat);
            ConvertRGBtoGray(rawMat.getNativeObjAddr(), detectMat.getNativeObjAddr());
            Utils.matToBitmap(detectMat, detectBitmap);

            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            //canvas.drawBitmap(rawBitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
            canvas.drawBitmap(detectBitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
            surfaceHolder.unlockCanvasAndPost(canvas);

            if (!(rawBitmap.isRecycled())) continue;
            rawBitmap.recycle();
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