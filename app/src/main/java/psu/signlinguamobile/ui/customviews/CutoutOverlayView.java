package psu.signlinguamobile.ui.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class CutoutOverlayView extends View
{
    private Paint overlayPaint;
    private Path cutoutPath;
    Paint ringPaint;
    private float radius;

    public CutoutOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CutoutOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CutoutOverlayView(Context context) {
        super(context);
        init();
    }

    private void init() {
        overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overlayPaint.setColor(Color.parseColor("#AA000000")); // semi-transparent black
        overlayPaint.setStyle(Paint.Style.FILL);

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(Color.GRAY);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(20f);

        cutoutPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Use 90% of the smaller dimension for radius
        float marginFactor = 0.025f; // 2.5% margin on each side
        float diameter = Math.min(width, height) * (1f - 2 * marginFactor);
        radius = diameter / 2f;

        float centerX = width / 2f;
        float centerY = height / 2f;

        cutoutPath.reset();
        cutoutPath.addRect(0, 0, width, height, Path.Direction.CW);
        cutoutPath.addCircle(centerX, centerY, radius, Path.Direction.CCW);

        canvas.save();
        canvas.clipPath(cutoutPath);
        canvas.drawPaint(overlayPaint);

        // Ring
        canvas.drawCircle(centerX, centerY, radius, ringPaint);

        canvas.restore();
    }
}
