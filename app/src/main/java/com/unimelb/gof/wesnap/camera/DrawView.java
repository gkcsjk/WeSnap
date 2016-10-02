package com.unimelb.gof.wesnap.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Karl on 1/10/2016.
 */

public class DrawView extends ImageView {

    private int drawType;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private Paint mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;



    public DrawView(Context c){
        super(c);
        init(c);
    }

    public DrawView(Context c, AttributeSet attrs){
        super(c, attrs);
        init(c);
    }

    public DrawView(Context c, AttributeSet attrs, int defStyle){
        super(c, attrs, defStyle);
        init(c);
    }

    public DrawView(Context c, Canvas mCanvas, Bitmap mBitmap, Paint mPaint){
        super(c);
        init(c);
        init_custom(mCanvas, mBitmap, mPaint);

    }

    private void init( Context c){
        context = c;
        drawType = 0;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    private void init_custom(Canvas mCanvas, Bitmap mBitmap, Paint mPaint){
        this.mCanvas = mCanvas;
        this.mBitmap = mBitmap;
        this.mPaint = mPaint;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
    }

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
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (drawType == 0){
            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawType == 0){
            float x = event.getX();
            float y = event.getY();

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
        return true;
    }

}
