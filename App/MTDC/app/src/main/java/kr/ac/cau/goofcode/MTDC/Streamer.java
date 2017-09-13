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

public class Streamer extends SurfaceView  implements SurfaceHolder.Callback, OnVideoDataRecv {

    private static final String TAG = "Streamer";

    private SurfaceHolder holder = getHolder();

    private static final BlockingQueue<AudioPacket> audioList = new LinkedBlockingQueue<>(5);
    private static final BlockingQueue<VideoPacket> videoList = new LinkedBlockingQueue<>(5);
    private ByteBuffer buffer;
    private Bitmap bm = null;
    private boolean isStartAudio = false;

    private CameraManagel cameraManagel;
    private Thread videoPreviewThread;

    public Streamer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cameraManagel = ((ControlModeActivity)context).getCameraManagel();
        this.holder.addCallback(this);
    }

    private void DrawVideo(Bitmap bitmap, VideoPacket videoPacket) {
        try {
            this.buffer = ByteBuffer.wrap(videoPacket.mDataBuf);
            bitmap.copyPixelsFromBuffer(this.buffer);
            Canvas canvas = this.holder.lockCanvas(null);
            if (videoPacket != null) {
                Rect localRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap, null, localRect, null);
                this.holder.unlockCanvasAndPost(canvas);
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.toString());
            exception.printStackTrace();
        }
    }

    public void audioData(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3) {}
    public void dataRecv(int width, int height, int bit, byte[] data, int frameType) {
        if (this.holder == null) {
            return;
        }
        VideoPacket receivedVideoPacket;
        do {
            receivedVideoPacket = new VideoPacket(bit, data.length, data, frameType, height, width);
            if (this.bm == null) {
                this.bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            }
        } while (videoList.offer(receivedVideoPacket));
        videoList.poll();
        videoList.offer(receivedVideoPacket);
    }

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
        this.cameraManagel.setOnDataRecvLisenner(this);
        this.holder = paramSurfaceHolder;
        this.videoPreviewThread = new Thread(new videoPreviewThread(), "videoThread");
        this.videoPreviewThread.start();
    }
    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
    }
    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
        this.cameraManagel.RemoveDataRecvLisenner(this);
        videoPreviewThread.interrupt();
        this.isStartAudio = false;
        this.holder = null;
    }

    public class AudioPacket {
        public byte[] audioData;
        public int sizeInBytes;

        public AudioPacket() {
        }
    }
    public class VideoPacket {
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

    private class videoPreviewThread implements Runnable {
        videoPreviewThread() {}
        public void run() {
            Log.i(TAG, "Video preview thread started");
            for (; ; ) {
                try {
                    if (Streamer.videoList.isEmpty()){
                        // Log.e(TAG, "videoList empty");
                        continue;
                    }
                    VideoPacket videoPacket = Streamer.videoList.poll();
                    Object graphicMatrix = new Matrix();
                    ((Matrix) graphicMatrix).postRotate(180.0F);

                    graphicMatrix = Bitmap.createBitmap(Streamer.this.bm,
                            0, 0, Streamer.this.bm.getWidth(), Streamer.this.bm.getHeight(),
                            (Matrix) graphicMatrix, true);

                    Streamer.this.DrawVideo((Bitmap) graphicMatrix, videoPacket);
                    if (!((Bitmap) graphicMatrix).isRecycled()) {
                        continue;
                    }
                    ((Bitmap) graphicMatrix).recycle();
                    continue;
                } catch (Exception localException) {
                    Log.e(TAG, localException.toString());
                }
                Streamer.videoList.clear();
                return;
            }
        }
    }
}