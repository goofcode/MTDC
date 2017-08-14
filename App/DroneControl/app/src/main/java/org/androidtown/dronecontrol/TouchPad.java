package org.androidtown.dronecontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Created by HeeSu on 2017-08-10.
 */

public class TouchPad extends View {

    public static final int MOVE_RADIUS = 160;  //움직일 수 있는 범위
    public static final int OUTER_RADIUS = 200;
    public static final int RADIUS = 80;        //빨간 원

    public static final int INNER_COLOR = Color.rgb(255,133,133);
    public static final int OUTER_COLOR = Color.rgb(255,238,173);

    protected Paint outerPaint;
    protected Paint innerPaint;

    float originX;
    float originY;

    float curX;
    float curY;

    double power = 0;
    double angle = 0;
    double positive_angle=0;


    boolean dragging = false;

    boolean isLeft;

    public TouchPad(Context context, boolean isLeft) {
        super(context);
        init(context);

        this.isLeft = isLeft;
        originX = isLeft?400:550;
        originY = 270;
        curX = originX;
        curY = originY;

    }
    public TouchPad(Context context, boolean isLeft,@Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

        this.isLeft = isLeft;

        originX = isLeft?400:550;
        originY = 270;
        curX = originX;
        curY = originY;
    }
    protected void init(Context context){
        outerPaint = new Paint();
        outerPaint.setStyle(Paint.Style.FILL);
        outerPaint.setColor(OUTER_COLOR);

        innerPaint = new Paint();
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(INNER_COLOR);
    }

    public double getPower(){return power;}
    public double getAngle(){return angle;}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle((int)originX, (int)originY, OUTER_RADIUS, outerPaint);
        canvas.drawCircle((int)curX, (int)curY, RADIUS, innerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int touchX = (int)event.getX();
        int touchY = (int)event.getY();
        float dx = 0;
        float dy = 0;

        double dist = Math.sqrt(Math.pow(originX - touchX,2) + Math.pow(originY - touchY, 2));

        dx = touchX - originX;
        dy = touchY - originY;


        if(action == MotionEvent.ACTION_DOWN){
            //원 안을 클릭했을 경우
            if ( dist < MOVE_RADIUS) {
                dragging = true;
                curX = touchX;
                curY = touchY;

                power = (dist>MOVE_RADIUS?MOVE_RADIUS:dist)*100.0/160;
                angle = Math.atan(dy / dx) +(dx>0?0:Math.PI);
            }
            invalidate();
        }
        else if(action == MotionEvent.ACTION_MOVE){
            if(dragging) {
                // 원 안
                if( dist < MOVE_RADIUS){
                    curX = touchX;
                    curY = touchY;
                }
                // 원 밖
                else {
                    curX = originX + MOVE_RADIUS * (float) Math.cos(angle);
                    curY = originY + MOVE_RADIUS * (float) Math.sin(angle);
                }
                power = (dist>MOVE_RADIUS?MOVE_RADIUS:dist)*100.0/160;
                angle = Math.atan(dy / dx) +(dx>0?0:Math.PI);

                invalidate();
            }
        }
        else if (action == MotionEvent.ACTION_UP){
            dragging = false;
            curX = originX;
            curY = originY;
            power=0;
            angle=0;

            invalidate();
        }

        if(angle<0)
        {
            positive_angle=angle+Math.PI*2;
        }
        else
        {
            positive_angle=angle;
        }

        DecimalFormat df = new DecimalFormat("000.00");


        if(isLeft) {
            MainActivity.leftPowerTextView.setText("leftPower:\t" + df.format(power));
            MainActivity.leftAngleTextView.setText("leftAngle:\t" + df.format(positive_angle));
        }
        else{
            MainActivity.rightPowerTextView.setText("rightPower:\t" + df.format(power));
            MainActivity.rightAngleTextView.setText("rightAngle:\t" + df.format(positive_angle));

        }

        return true;
    }
}