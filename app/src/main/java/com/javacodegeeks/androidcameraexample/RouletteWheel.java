package com.javacodegeeks.androidcameraexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RouletteWheel extends View {

    Paint paintB, paintR, paintG;
    Path path;
    float left, right, top, bottom;



    public RouletteWheel(Context context) {
        super(context);
        init();
    }

    public RouletteWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouletteWheel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RouletteWheel(Context context, float left, float top, float right, float bottom){
        super(context);
        init(left,top,right,bottom);
    }

    private void init(){
        paintB = new Paint();
        paintB.setColor(Color.BLACK);
        paintB.setStrokeWidth(1);
        paintB.setStyle(Paint.Style.FILL);

        paintR = new Paint();
        paintR.setColor(Color.RED);
        paintR.setStrokeWidth(1);
        paintR.setStyle(Paint.Style.FILL);

        paintG = new Paint();
        paintG.setColor(Color.GREEN);
        paintG.setStrokeWidth(1);
        paintG.setStyle(Paint.Style.FILL);

    }

    private void init(float x, float y, float z, float w){
        paintB = new Paint();
        paintB.setColor(Color.BLACK);
        paintB.setStrokeWidth(1);
        paintB.setStyle(Paint.Style.FILL);

        paintR = new Paint();
        paintR.setColor(Color.RED);
        paintR.setStrokeWidth(1);
        paintR.setStyle(Paint.Style.FILL);

        paintG = new Paint();
        paintG.setColor(Color.GREEN);
        paintG.setStrokeWidth(1);
        paintG.setStyle(Paint.Style.FILL);

        left = x;
        right = y;
        top = z;
        bottom = w;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        final RectF oval = new RectF();



  /*
   * drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)
   *
   * oval - The bounds of oval used to define the shape and size of the arc
   * startAngle - Starting angle (in degrees) where the arc begins
   * sweepAngle - Sweep angle (in degrees) measured clockwise
   * useCenter - If true, include the center of the oval in the arc, and close it if it is being stroked. This will draw a wedge
   * paint - The paint used to draw the arc
   */
        oval.set(left, right, top, bottom);
        canvas.drawArc(oval, 0, 40, true, paintG);
        canvas.drawArc(oval, 40, 40, true, paintR);
        canvas.drawArc(oval, 80, 40, true, paintB);
        canvas.drawArc(oval, 120, 40, true, paintR);
        canvas.drawArc(oval, 160, 40, true, paintB);
        canvas.drawArc(oval, 200, 40, true, paintR);
        canvas.drawArc(oval, 240, 40, true, paintB);
        canvas.drawArc(oval, 280, 40, true, paintR);
        canvas.drawArc(oval, 320, 40, true, paintB);


    }
}
