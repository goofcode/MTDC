package kr.ac.cau.goofcode.MTDC;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class PowerButton extends android.support.v7.widget.AppCompatImageButton {

    public PowerButton(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        setOnClickListener(new clickListener());
    }

    private class clickListener implements OnClickListener{
        @Override
        public void onClick(View view) {((ControlModeActivity)getContext()).power();}
    }
}
