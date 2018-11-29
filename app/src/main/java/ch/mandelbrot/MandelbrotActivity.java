package ch.mandelbrot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import ch.mandelbrot.rs.ScriptC_mandelbrot;

public class MandelbrotActivity extends Activity {

    private Logger log = Logger.getLogger("MandelbrotActivity");

    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private float mScaleFactor = 1.f;

    private Bitmap mBitmap;

    private ImageView mDisplayView;

    private RenderScript mRS;
    private Allocation mOutPixelsAllocation;
    private ScriptC_mandelbrot mScript;

    private int mWidth;
    private int mHeight;
    private float x0 = -2.8f;
    private float y0 = -1.2f;
    float rx;
    float ry;

    AtomicBoolean isDrawing = new AtomicBoolean(false);

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mScript != null) {
            mScript.destroy();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmandelbrot);

        mGestureDetector = new GestureDetectorCompat(this,new MyGestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(this,new MyScaleGestureListener());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = (int) (size.x);
        mHeight = (int) (size.y);
        Log.d("mWidth, mHeight","{"+mWidth+","+mHeight+"},");

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        mDisplayView = (ImageView) findViewById(R.id.MandelbrotView);
        mDisplayView.setImageBitmap(mBitmap);

        mRS = RenderScript.create(this);
        mOutPixelsAllocation = Allocation.createFromBitmap(mRS, mBitmap,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mScript = new ScriptC_mandelbrot(mRS);

        x0 = -2.8f;
        float x1 = 1.2f;
        y0 = (x0 - x1) * mHeight / (2 * mWidth); //-1.2;
        float y1 = -y0; //1.2;
        rx = (x1 - x0) / mWidth;
        ry = (y1 - y0) / mHeight;

        Theme theme = new Theme("", this.getBaseContext());

        mScript.set_rx(rx);
        mScript.set_ry(ry);
        mScript.set_maxIteration(63);

        mDisplayView.getLayoutParams().width = mWidth;
        mDisplayView.getLayoutParams().height = mHeight;



        byte[] d = Palette.getPalette(this.getBaseContext(), theme, 100);

        Element type = Element.U8(mRS);
        Allocation colorAllocation = Allocation.createSized(mRS, type, theme.precission * 3);
        mScript.bind_color(colorAllocation);

        colorAllocation.copy1DRangeFrom(0, theme.precission * 3, d);

        renderMandelbrot(x0, y0);
    }

    private void renderMandelbrot(float x0, float y0) {
        if (isDrawing.compareAndSet(false, true)) {
            //Log.d("tag","{"+x0+","+y0+"},");
            this.x0 = x0;
            this.y0 = y0;
            mScript.set_x0(x0);
            mScript.set_y0(y0);
            mScript.forEach_root(mOutPixelsAllocation, mOutPixelsAllocation);
            mOutPixelsAllocation.copyTo(mBitmap);

            mDisplayView.invalidate();
            isDrawing.set(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (this.mGestureDetector.onTouchEvent(event)
                || this.mScaleGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d("onScroll","{"+distanceX+","+distanceY+"},");
            renderMandelbrot(x0 + distanceX/200*mScaleFactor, y0 + distanceY/200*mScaleFactor);
            return true;
        }
    }

    class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            float x = detector.getFocusX();
            float y = detector.getFocusY();
            //Log.d("onScale","{"+x+","+y+"},");

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.0001f, Math.min(mScaleFactor, 1.0f));
            //Log.d("mScaleFactor","{"+mScaleFactor+"},");

            /*x0 = -2.8f;
            float x1 = 1.2f;
            y0 = (x0 - x1) * mHeight / (2 * mWidth); //-1.2;
            float y1 = -y0; //1.2;
            float rx = (x1 - x0) / mWidth;
            float ry = (y1 - y0) / mHeight;*/
            mScript.set_rx(rx*mScaleFactor);
            mScript.set_ry(ry*mScaleFactor);
            renderMandelbrot(x0, y0);
            return true;
        }
    }

}
