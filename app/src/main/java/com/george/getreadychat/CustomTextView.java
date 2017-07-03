package com.george.getreadychat;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    public CustomTextView (Context context) {
        super(context);
        init();
    }

    public CustomTextView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "paint.οtf"));
    }

}
