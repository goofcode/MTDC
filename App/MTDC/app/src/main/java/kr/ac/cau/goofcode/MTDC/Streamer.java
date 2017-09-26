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

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Streamer extends SurfaceView  implements SurfaceHolder.Callback, OnVideoDataRecv, Runnable {

    private static final String TAG = "Streamer";

    private SurfaceHolder surfaceHolder;

    private static final BlockingQueue<VideoPacket> videoList = new LinkedBlockingQueue<>(5);
    private Thread drawThread;
    private ByteBuffer buffer;
    private Bitmap bm = null;

    private CameraManagel cameraManagel;

    public Streamer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        cameraManagel = ((ControlModeActivity)context).getCameraManagel();
    }

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder){
        cameraManagel.setOnDataRecvLisenner(this);
        drawThread = new Thread(this);
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
    private void DrawVideo(Bitmap bitmap, VideoPacket videoPacket) {
        try {
            buffer = ByteBuffer.wrap(videoPacket.mDataBuf);
            bitmap.copyPixelsFromBuffer(this.buffer);

            Canvas canvas = surfaceHolder.lockCanvas();

            Rect drawRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());

            // canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(bitmap, null, drawRect, null);
            surfaceHolder.unlockCanvasAndPost(canvas);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "Video drawThread started");

        while(true) {
            if (videoList.isEmpty()) {
                // Log.e(TAG, "videoList empty");
                continue;
            }

            VideoPacket videoPacket = videoList.poll();

            Object graphicMatrix = new Matrix();
            ((Matrix) graphicMatrix).postRotate(180.0F);

            graphicMatrix = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), (Matrix) graphicMatrix, true);

            DrawVideo((Bitmap) graphicMatrix, videoPacket);

            if (!((Bitmap) graphicMatrix).isRecycled()) continue;
            ((Bitmap) graphicMatrix).recycle();
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