package org.androidtown.dronecontrol;

/**
 * Created by HeeSu on 2017-08-02.
 */

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    ArrayList<Drawable> imageList = new ArrayList<Drawable>();
    ImageView imageView;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView=(ImageView)findViewById(R.id.imageView);

        Resources res = getResources();
        imageList.add(res.getDrawable(R.drawable.and_icon1));
        imageList.add(res.getDrawable(R.drawable.and_icon2));
        imageList.add(res.getDrawable(R.drawable.and_icon3));


        Button button = (Button) findViewById(R.id.controller_mode_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });

        AnimThread thread = new AnimThread();
        thread.start();
    }

    class AnimThread extends Thread{
        public void run(){
            int index = 0;
            for(int i=0;i<100;i++) {
                index = i % 3;

                final Drawable drawable = imageList.get(index);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        imageView.setImageDrawable(drawable);
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                ;
            }
        }
    }

}
