package ch.mandelbrot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MandelbrotView extends View {

    private Logger log = Logger.getLogger("MandelbrotView");

    private Paint paint;
    private int width;
    private int height;
    private int maxIteration = 700;
    private double x0;
    private double x1;
    private double y0;
    private double y1;
    private double new_x0;
    private double new_y0;
    private double new_x1;
    private double new_y1;

    private ArrayList<Color> gradient = new ArrayList();
    private int gradientSize = 40;

    private Bitmap imageData;
    private Bitmap toRestoreData;
    private Canvas canvas;

    private final static int SCALE = 2;

    private class Color {
        int r;
        int g;
        int b;

        Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public MandelbrotView(Context context) {
        super(context);
    }

    public MandelbrotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MandelbrotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (imageData != null) {
            //canvas.drawBitmap(imageData, 0, 0, paint);
            canvas.drawBitmap(imageData, null, new Rect(0,0,width*SCALE, height*SCALE), paint);
        }
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        width = xNew;
        height = yNew;

        init();
    }

    private void init() {

        width = width / SCALE;
        height = height / SCALE;

        paint = new Paint();
        imageData = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(imageData);

        this.x0 = -2.8;
        this.x1 = 1.2;
        this.y0 = (this.x0 - this.x1) * this.height / (2 * this.width); //-1.2;
        this.y1 = -this.y0; //1.2;

        this.initGradient(new Color(255,0,0), new Color(0,255,0), new Color(0,0,255));

        this.drawMandelbrot();
    }

    private void initGradient(Color... colors) {
        int size = colors.length;
        int chunk = (int)Math.ceil(this.gradientSize/(size-1));
        for (int i = 0; i < size-1; i++) {
            Color diffColor = this.createDiffColor(colors[i], colors[i+1]);
            this.writeGradient(diffColor,colors[i],i*chunk,(i+1)*chunk);
        }
    }

    private Color createDiffColor(Color startColor, Color endColor) {
        return new Color(endColor.r - startColor.r, endColor.g - startColor.g,
                endColor.b - startColor.b);
    }

    private void writeGradient(Color diffColor,Color startColor,int start,int size) {
        for (int i=start; i<=size; i++) {
            double percent = ((double)i) / ((double)size);
            Color c = new Color((int)(diffColor.r * percent) + startColor.r,
                    (int)(diffColor.g * percent) + startColor.g,
                    (int)(diffColor.b * percent) + startColor.b);
            this.gradient.add(i, c);
        }
    }

    private void drawPoint(int x, int y, int iteration) {
        if (iteration != 0) {
            int ratio = iteration % this.gradientSize;
            paint.setARGB(255,this.gradient.get(ratio).r,
                    this.gradient.get(ratio).g,
                    this.gradient.get(ratio).b);
        } else {
            paint.setARGB(255,0,0,0);
        }

        canvas.drawPoint(x, y, paint);
    }

    private void computeMandelbrot() {
        double rx = (this.x1 - this.x0) / this.width;
        double ry = (this.y1 - this.y0) / this.height;
        for (int x = 0; x < this.width; x++) {
            double a0 = this.x0 + x * rx;
            for (int y = 0; y < this.height; y++) {
                double b0 = this.y0 + y * ry;
                double a = 0.0;
                double b = 0.0;
                int iteration = 0;
                while (iteration < this.maxIteration) {
                    double atemp = a * a - b * b + a0;
                    double btemp = 2 * a * b + b0;
                    if (a == atemp && b == btemp) {
                        iteration = this.maxIteration;
                        break;
                    }
                    a = atemp;
                    b = btemp;
                    iteration = iteration + 1;
                    if (Math.abs(a + b) > 16) {
                        break;
                    }
                }
                this.drawPoint(x, y, this.maxIteration - iteration);
            }
        }
    }

    private void refresh() {
        this.invalidate();
    }

    private void drawMandelbrot() {
        this.computeMandelbrot();
        //toRestoreData = Bitmap.createBitmap(imageData, 0, 0, width, height);
        toRestoreData = Bitmap.createBitmap(imageData);
        this.refresh();
    }

    private double map(double val, double origRangeStart, double origRangeEnd, double destRangeStart, double destRangeEnd) {
        return destRangeStart + (destRangeEnd - destRangeStart) * ((val - origRangeStart) / (origRangeEnd - origRangeStart));
    }

    private void computeYfromX(double pageY) {
        if (pageY < this.new_y0) {
            this.new_y1 = this.new_y0 - this.height * (Math.abs(this.new_x1 - this.new_x0) / this.width);
        } else {
            this.new_y1 = this.new_y0 + this.height * (Math.abs(this.new_x1 - this.new_x0) / this.width);
        }
    }

    private void drawSelection() {
        paint.setARGB(255,255,255,255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        double x0 = this.new_x0;
        double y0 = this.new_y0;
        double x1 =this.new_x1;
        double y1 = this.new_y1;
        if (x0 < x1 && y0 < y1) {
            canvas.drawRect((float)x0, (float)y0, (float)(x1), (float)(y1), paint);
        } else if (x0 > x1 && y0 < y1) {
            canvas.drawRect((float)x1, (float)y0, (float)(x0), (float)(y1), paint);
        } else if (x0 < x1 && y0 > y1) {
            canvas.drawRect((float)x0, (float)y1, (float)(x1), (float)(y0), paint);
        } else if (x0 > x1 && y0 > y1) {
            canvas.drawRect((float)x1, (float)y1, (float)(x0), (float)(y0), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX()/SCALE;
        float pointY = event.getY()/SCALE;
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                new_x0 = pointX;
                new_y0 = pointY;
                break;
            case MotionEvent.ACTION_MOVE:
                new_x1 = pointX;
                computeYfromX(pointY);
                if (toRestoreData != null) {
                    this.canvas.drawBitmap(toRestoreData, 0, 0, paint);
                }
                drawSelection();
                refresh();
                break;
            case MotionEvent.ACTION_UP:
                double tempX0 = map(new_x0, 0, width, x0, x1);
                double tempX1 = map(new_x1, 0, width, x0, x1);
                double tempY0 = map(new_y0, 0, height, y0, y1);
                double tempY1 = map(new_y1, 0, height, y0, y1);
                if (tempX0 < tempX1) {
                    x0 = tempX0;
                    x1 = tempX1;
                } else {
                    x1 = tempX0;
                    x0 = tempX1;
                }
                if (tempY0 < tempY1) {
                    y0 = tempY0;
                    y1 = tempY1;
                } else {
                    y1 = tempY0;
                    y0 = tempY1;
                }
                drawMandelbrot();
                break;
            default:
                return false;
        }
        return true;
    }
}
