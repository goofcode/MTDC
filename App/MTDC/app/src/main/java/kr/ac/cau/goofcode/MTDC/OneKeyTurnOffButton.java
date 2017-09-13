package kr.ac.cau.goofcode.MTDC;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class OneKeyTurnOffButton extends android.support.v7.widget.AppCompatButton {

    public OneKeyTurnOffButton(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        setOnClickListener(new clickListener());
        setText(R.string.turnoff_btn_text);
    }

    private class clickListener implements OnClickListener{
        @Override
        public void onClick(View view) {((ControlModeActivity)getContext()).oneKeyTurnOff();}
    }
}
