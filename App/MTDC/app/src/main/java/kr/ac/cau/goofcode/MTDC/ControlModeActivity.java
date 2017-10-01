package kr.ac.cau.goofcode.MTDC;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.logic.ffcamlib.CameraManagel;
import com.logic.ffcamlib.Ipcameral;

import java.util.Timer;
import java.util.TimerTask;

import static android.view.Window.FEATURE_NO_TITLE;

public class ControlModeActivity extends BaseActivity implements CameraManagel.new_live_ui_listerner {

    static final String TAG = "ControlModeActivity";

    private CameraManagel cameraManagel = new CameraManagel();

    //components
    private ControlPad leftControlPad, rightControlPad;
    private PowerButton powerButton;
    private TakeoffButton takeoffButton;
    private LandButton landButton;

    private Timer sendControlDataTimer;
    private TimerTask sendControlDataTimerTask;

    private byte[] controlData = new byte[10];
    private int tuneRudd = 0;
    private int tuneElev = -1;
    private int tuneAile = -4;
    private int msg_mode = 0;//????
    private int msg_onestop = 0;
    private int msg_speed = 0;
    private int msg_photo = 0;
    private int msg_record = 0;
    private int msg_takeoff = 0;
    private int msg_head = 0;
    private int msg_landing = 0;
    private int msg_tinyreset = 0;

    private boolean trackingMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_NO_TITLE);
        setContentView(R.layout.activity_control_mode);

        // get components
        leftControlPad = (ControlPad) findViewById(R.id.left_control_pad);
        rightControlPad = (ControlPad) findViewById(R.id.right_control_pad);
        powerButton = (PowerButton) findViewById(R.id.power_button);
        takeoffButton = (TakeoffButton) findViewById(R.id.takeoff_button);
        landButton = (LandButton) findViewById(R.id.land_button);

        // start streaming
        cameraManagel.startPlay();
        cameraManagel.play_video();
        cameraManagel.getCrt(this);
        cameraManagel.set_live_listener(this);
        cameraManagel.setContext(this);
        findViewById(R.id.streamer).setVisibility(View.VISIBLE);

        // start sending control message
        startSendMsg();
    }
    @Override
    protected void onDestroy() {
        stopSendMsg();
        cameraManagel.stopGetCrt();
        cameraManagel.set_live_listener(null);
        cameraManagel.setContext(null);
        cameraManagel.stopPlay();
        cameraManagel.exitApk();
        super.onDestroy();
    }
    @Override
    protected void onPause() {

        super.onPause();
    }
    @Override
    protected void onResume() {super.onResume();}

    public CameraManagel getCameraManagel() {
        return cameraManagel;
    }
    public boolean isTrackingMode(){return trackingMode;}

    public void startSendMsg() {
        if ((this.sendControlDataTimer != null) && (this.sendControlDataTimerTask != null)) {
            stopSendMsg();
        }

        Log.e(TAG, "Start Send Control Data");
        Log.e(TAG, "Enabling Send Data");
        Ipcameral.setEnableSendData(1);

        this.sendControlDataTimer = new Timer();
        this.sendControlDataTimerTask = new TimerTask() {
            public void run() {
                int msgTuneElev = tuneElev + 32;
                int msgTuneRudd = tuneRudd + 32;
                int msgTuneAile = tuneAile + 32;
                msgTuneAile = msgTuneAile > 31?msgTuneAile:32-msgTuneAile;
                msgTuneElev = msgTuneElev > 31?msgTuneElev:32-msgTuneElev;

                //get values from controlpad
                int msgThro = leftControlPad.getVertical();
                int msgRudd = leftControlPad.getHorizontal();
                int msgElev = rightControlPad.getVertical();
                int msgAile = rightControlPad.getHorizontal();

                if (msgElev == 128) msgElev = Byte.MIN_VALUE;
                else msgElev = msgElev > 128 ? msgElev - 128 : 255 - msgElev;
                if (msgRudd < 128) msgRudd = 127 - msgRudd;
                if (msgAile < 128) msgAile = 127 - msgAile;

                controlData[0] = (byte) msgThro;
                controlData[1] = (byte) msgElev;
                controlData[2] = (byte) msgRudd;
                controlData[3] = (byte) msgAile;
                controlData[4] = 32;
                controlData[5] = (byte) (msg_mode + msg_speed + msgTuneElev);
                controlData[6] = (byte) (msg_photo + msg_record + msgTuneRudd);
                controlData[7] = (byte) (msg_head + msg_takeoff + msgTuneAile);
                controlData[8] = (byte) (msg_tinyreset + msg_onestop + msg_landing);
                controlData[9] = (byte) ((controlData[0] ^ controlData[1] ^ controlData[2] ^ controlData[3]
                        ^ controlData[4] ^ controlData[5] ^ controlData[6] ^ controlData[7] ^ controlData[8]) + 85);
                cameraManagel.sendCtrlData(controlData, controlData.length);
            }
        };

        // send control data every 40 ms
        sendControlDataTimer.schedule(sendControlDataTimerTask, 0L, 40L);
    }
    private void stopSendMsg() {
        Ipcameral.setEnableSendData(0);
        if (this.sendControlDataTimer != null) {
            Log.e(TAG, "Stop Sending Control Data");
            this.sendControlDataTimer.cancel();
            this.sendControlDataTimer = null;
        }
        if (this.sendControlDataTimerTask != null) {
            this.sendControlDataTimerTask.cancel();
            this.sendControlDataTimerTask = null;
        }
    }

    public void power() {
        msg_onestop = 16;
        Runnable endTurnOff = new Runnable() {
            @Override
            public void run() {
                msg_onestop = 0;
            }
        };
        new Handler().postDelayed(endTurnOff, 1000L);
    }
    public void takeoff() {
        msg_takeoff = 64;
        msg_landing = 0;
        Runnable endTakeOff = new Runnable() {
            @Override
            public void run() {
                msg_takeoff = 0;
            }
        };
        new Handler().postDelayed(endTakeOff, 1000L);
    }
    public void land() {
        msg_landing = 8;
        msg_takeoff = 0;
        Runnable endLanding = new Runnable() {
            @Override
            public void run() {
                msg_landing = 0;
            }
        };
        new Handler().postDelayed(endLanding, 1000L);
    }
    public void track(){
        if(!trackingMode) {
            leftControlPad.setVisibility(View.INVISIBLE);
            rightControlPad.setVisibility(View.INVISIBLE);
            takeoffButton.setVisibility(View.INVISIBLE);
            landButton.setVisibility(View.INVISIBLE);
            findViewById(R.id.control_layout).invalidate();
            trackingMode = true;
        }
        else{
            leftControlPad.setVisibility(View.VISIBLE);
            rightControlPad.setVisibility(View.VISIBLE);
            takeoffButton.setVisibility(View.VISIBLE);
            landButton.setVisibility(View.VISIBLE);
            findViewById(R.id.control_layout).invalidate();

            trackingMode = false;
        }
    }


    /* unused methods */
    @Override
    public void on_connect(int paramInt) {
    }
    @Override
    public void on_record(int paramInt) {
    }
    @Override
    public void on_video(int paramInt) {
    }
}
