package org.androidtown.dronecontrol;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {


    public static TextView leftPowerTextView;
    public static TextView rightPowerTextView;
    public static TextView leftAngleTextView;
    public static TextView rightAngleTextView;

    public static TouchPad leftTouchPad, rightTouchPad;

    private Socket sock = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        class ConnectThread extends Thread {

            String host;
            int port;

            TouchPad left, right;

            public ConnectThread(String host, int port, TouchPad left, TouchPad right){
                this.host= host;
                this.port = port;
                this.left = left;
                this.right = right;
            }

            @Override
            public void run(){
                System.out.println("run");

                DataOutputStream outputStream = null;

                double lpower, langle, rpower, rangle;
                DecimalFormat df = new DecimalFormat("000.00");

               /* try {
                    Socket sock = new Socket(host, port);
                    outputStream = new DataOutputStream(sock.getOutputStream());

                    while(true){
                        lpower = left.getPower();
                        langle = left.getAngle();
                        rpower = right.getPower();
                        rangle = right.getAngle();

                        //concat
                        String data = "";
                        data = df.format(lpower)+":"+ df.format(langle)+":"+ df.format(rpower)+ ":"+ df.format(rangle)+"\n";

                        //send
                        outputStream.write(data.getBytes());
                        outputStream.flush();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        leftPowerTextView = (TextView) findViewById(R.id.textView1);
        rightPowerTextView = (TextView) findViewById(R.id.textView2);
        leftAngleTextView = (TextView) findViewById(R.id.textView3);
        rightAngleTextView = (TextView) findViewById(R.id.textView4);

        leftPowerTextView.setText("leftPower");
        rightPowerTextView.setText("rightPower");
        leftAngleTextView.setText("leftAngle");
        rightAngleTextView.setText("rightAngle");

        leftTouchPad = (TouchPad) findViewById(R.id.LeftTouchPad);
        rightTouchPad = (TouchPad) findViewById(R.id.RightTouchPad);


        Button button = (Button) findViewById(R.id.tracking_mode_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                startActivityForResult(intent, 101);
            }
        });

        Button button1 = (Button) findViewById(R.id.connect_button);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectThread connectThread = new ConnectThread("172.20.10.5", 12345,leftTouchPad,rightTouchPad);
                connectThread.start();
            }

        });

    }
}
