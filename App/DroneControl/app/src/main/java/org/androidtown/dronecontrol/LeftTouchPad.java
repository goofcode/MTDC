package org.androidtown.dronecontrol;

/**
 * Created by HeeSu on 2017-08-02.
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by HeeSu on 2017-07-03.
 */

public class LeftTouchPad extends TouchPad {

    public LeftTouchPad(Context context) {super(context,true);}
    public LeftTouchPad(Context context, @Nullable AttributeSet attrs){
        super(context, true, attrs);
    }
}