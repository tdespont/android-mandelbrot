package ch.mandelbrot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.logging.Logger;

import ch.julia.rs.ScriptC_julia;

public class JuliaActivity extends Activity {

    private Logger log = Logger.getLogger("JuliaActivity");

    private Bitmap mBitmap;

    private ImageView mDisplayView;

    private RenderScript mRS;
    private Allocation mInPixelsAllocation;
    private Allocation mOutPixelsAllocation;
    private ScriptC_julia mScript;

    private int mWidth;
    private int mHeight;

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
        setContentView(R.layout.activity_showjulia);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = (int) (size.x);
        mHeight = (int) (size.y);
        Log.d("mWidth, mHeight","{"+mWidth+","+mHeight+"},");

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, conf);

        mDisplayView = (ImageView) findViewById(R.id.JuliaView);
        mDisplayView.setImageBitmap(mBitmap);

        mRS = RenderScript.create(this);
        /*mInPixelsAllocation = Allocation.createFromBitmap(mRS, mBitmap,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);*/
        mOutPixelsAllocation = Allocation.createFromBitmap(mRS, mBitmap,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mScript = new ScriptC_julia(mRS);

        mScript.set_height(mHeight);
        mScript.set_width(mWidth);

        mScript.set_precision(255);

        mDisplayView.getLayoutParams().width = mWidth;
        mDisplayView.getLayoutParams().height = mHeight;

        renderJulia(-0.9259259f, 0.30855855f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {

            case (MotionEvent.ACTION_MOVE):
                float cx = 0f,
                        cy = 0f;
                float x = event.getAxisValue(MotionEvent.AXIS_X);
                float y = event.getAxisValue(MotionEvent.AXIS_Y);
                cx = ((x / mWidth) * 4f) - 2f;
                cy = ((y / mHeight) * 4f) - 2f;
                renderJulia(cx, cy);
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void renderJulia(float cx, float cy) {
        Log.d("tag","{"+cx+","+cy+"},");
        mScript.set_cx(cx);
        mScript.set_cy(cy);
        mScript.forEach_root(mOutPixelsAllocation, mOutPixelsAllocation);
        mOutPixelsAllocation.copyTo(mBitmap);

        mDisplayView.invalidate();
    }
}
