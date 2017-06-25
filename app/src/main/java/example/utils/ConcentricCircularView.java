package example.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import example.com.stepcounter.R;

/**
 * Created by Ayush on 10/06/17.
 */

public class ConcentricCircularView extends View {
    private static final int COUNT = 15;
    private Paint paint;
    private DashPathEffect[] dashPaths = new DashPathEffect[COUNT];
    private int halfWidth, halfHeight, radius;
    private int radiusIncrement;
    boolean shouldDraw=true;
    float distanceBtwDots;
    private static final String TAG = "ConcentricCircularView";
    int strokeWidth;
    public ConcentricCircularView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Get the dimensions.
        strokeWidth = dipToPixels(getResources().getDimension(R.dimen.d1));
       // int distanceBtwDots = dipToPixels(getResources().getDimension(R.dimen.d5));
        radiusIncrement = dipToPixels(getResources().getDimension(R.dimen.d10));
        // Setup the path effects;
        radius= getScreenWidth()/5;

        for (int i=0; i<COUNT; i++) {
//            int radius=
            distanceBtwDots=getDistanceBetweenDots(radius + radiusIncrement * i,i);
            Log.e(TAG, "ConcentricCircularView: "+distanceBtwDots );
            dashPaths[i] = new DashPathEffect(
                    new float[]{ 1, distanceBtwDots}, 0);
        }
        radius= getScreenWidth();
        Log.e(TAG, "Radius from constructor"+radius );

        // Setup the paint.
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

    }

    private float getDistanceBetweenDots(int radius,int i) {
    int numberOfZeros=20+i*10;
    double circumference=2*3.14*radius-(1*numberOfZeros);
    return (float) circumference/numberOfZeros;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfWidth = w / 2;
        halfHeight = h / 2;
        radius = w / 4;
        Log.e(TAG, "Radius from on size changed"+radius );

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
     //   Log.e(TAG, "onDraw called" );
     //   Log.e(TAG, String.valueOf(shouldDraw));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            for (int i = 0; i < COUNT; i++) {
//                paint.setPathEffect(dashPaths[i]);
//                canvas.drawCircle(halfWidth, halfHeight, radius + radiusIncrement * i, paint);
//            }
       // paint.setPathEffect(dashPaths[1]);
        canvas.drawCircle(halfWidth, halfHeight, radius + radiusIncrement * 1, paint);

        shouldDraw=false;
        // draw your view here
    }

    private int dipToPixels(float value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    @Override
    public void invalidate() {
        shouldDraw = true;
        super.invalidate();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

}

