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

public class RightTouchPad extends TouchPad {

    public RightTouchPad(Context context) {
        super(context, false);
    }
    public RightTouchPad(Context context, @Nullable AttributeSet attrs){
        super(context, false, attrs);
    }

}