package com.sevensec.extra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

public class CircularImageView extends androidx.appcompat.widget.AppCompatImageView {
    final Path clipPath = new Path();

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = getWidth() / 2f;
        clipPath.reset();
        clipPath.addCircle(radius, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}

