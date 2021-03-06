package kr.ac.cau.goofcode.MTDC;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ControlPad extends View {

    public static final int MOVE_RADIUS = 200;
    public static final int OUTER_RADIUS = 220;
    public static final int INNER_RADIUS = 70;

    private Paint innerPaint = new Paint(), outerPaint = new Paint();

    private int originX, originY;
    private int curX, curY;
    private boolean dragging = false;

    public ControlPad(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setColor(Color.rgb(255, 133, 133));
        outerPaint.setStyle(Paint.Style.FILL);
        outerPaint.setColor(Color.rgb(230, 230, 230));
}

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        originX = getWidth() / 2;
        originY = getHeight() / 2;
        curX = originX;
        curY = originY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(originX, originY, OUTER_RADIUS, outerPaint);
        canvas.drawCircle(curX, curY, INNER_RADIUS, innerPaint);
    }

    private int getPadValue(boolean isVertical) {
        final int MAX_VALUE = 256;

        double diff = isVertical ? (originY - curY) : (curX - originX);
        double normDiff = diff / (2 * MOVE_RADIUS) + 0.5;

        return (int) (normDiff == 1 ? MAX_VALUE - 1 : normDiff * MAX_VALUE);
    }
    public int getVertical() {
        return getPadValue(true);
    }
    public int getHorizontal() {
        return getPadValue(false);
    }
    public void resetPad(){curX = originX; curY = originY;}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        double dist = Math.hypot(touchX - originX, touchY - originY);

        if (action == MotionEvent.ACTION_DOWN) {
            if (dist < MOVE_RADIUS) {
                dragging = true;
                curX = touchX;
                curY = touchY;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (dragging) {
                if (dist < MOVE_RADIUS) {curX = touchX; curY = touchY;}
                else {
                    curX = (int) (MOVE_RADIUS * (touchX - originX) / dist + originX);
                    curY = (int) (MOVE_RADIUS * (touchY - originY) / dist + originY);
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            dragging = false;
            curX = originX;
            curY = originY;
        }
        invalidate();
        return true;
    }
}