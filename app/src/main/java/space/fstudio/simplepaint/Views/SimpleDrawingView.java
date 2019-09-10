package space.fstudio.simplepaint.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import space.fstudio.simplepaint.Objects.FileOperations;
import top.defaults.colorpicker.ColorPickerPopup;

public class SimpleDrawingView extends View {

    private final FileOperations fO = new FileOperations();
    private Paint mPaint;
    private int sColor;
    private int width = 1;
    private Bitmap bMap;
    private Path mPath;
    private Paint mBitmapPaint;
    private Canvas mCanvas;
    private int cHeight;
    private int cWidth;


    public SimpleDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupPaint();
    }

    private void setupPaint() {
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void clearCanvas() {
        bMap = Bitmap.createBitmap(cWidth, cHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bMap);
        invalidate();
    }

    public void saveImage() {
        setDrawingCacheEnabled(true);
        Bitmap bitmap = getDrawingCache();
        fO.saveBitmap(bitmap, getContext());
        setDrawingCacheEnabled(false);
    }

    public void loadImage() {
        Bitmap testMap = fO.loadBitmap(getContext());
        if (testMap != null) {
            bMap = testMap;
            mCanvas = new Canvas(bMap);
            invalidate();
        } else Toast.makeText(getContext(), "Saved file doesn't exist", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        this.cWidth = w;
        this.cHeight = h;

        bMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bMap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStrokeWidth(this.width);
        if (bMap != null) {
            canvas.drawBitmap(bMap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        if (sColor == 0)
            Toast.makeText(getContext(), "Please select a color", Toast.LENGTH_SHORT).show();
        else
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }

        return true;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    public void colorPickerDialog() {
        new ColorPickerPopup.Builder(getContext())
                .initialColor(sColor) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose")
                .cancelTitle("Cancel")
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(getRootView(), new ColorPickerPopup.ColorPickerObserver() {
                    @Override
                    public void onColorPicked(int color) {
                        sColor = color;
                        mPaint.setColor(color);
                    }
                });
    }
}

