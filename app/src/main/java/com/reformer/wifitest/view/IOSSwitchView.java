package com.reformer.wifitest.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.reformer.wifitest.R;

/**
 * Created by Administrator on 2015-07-20.
 */
public class IOSSwitchView extends View implements ValueAnimator.AnimatorUpdateListener {
    private float lastMotionX;
    private float lastMotionY;
    private int viewWidth = 1;
    private int viewHeight = 1;
    private int touchSlop;
    private String onText = "";
    private String offText = "";
    private boolean textEnable = true;
    private Drawable closeDrawable;
    private Drawable openDrawable;
    private float downX = 0;
    private final static int RADIUS = 56;
    private final static int LOCATION = 2;
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING_X = 1;
    private final static int TOUCH_STATE_SCROLLING_Y = 2;
    public final static int SWITCH_LEFT = 1;
    public final static int SWITCH_RIGHT = 2;

    private int touchState = TOUCH_STATE_REST;
    public int currentState = SWITCH_LEFT;
    private OnSwitchListener switchListener;
    private ShapeHolder ball = null;
    private ValueAnimator bounceAnim = null;
    private Context context;

    public IOSSwitchView(Context context) {
        super(context);
        this.context = context;
        init(context);

    }

    public IOSSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public IOSSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    public void setState(boolean currentState){
        this.currentState = (currentState==true)?SWITCH_RIGHT:SWITCH_LEFT;
        init(this.context);
    }
    public boolean getState(){
        return (currentState == SWITCH_RIGHT)?true:false;
    }


    private void init(Context context) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = ViewConfigurationCompat
                .getScaledPagingTouchSlop(configuration);
        closeDrawable = context.getResources().getDrawable(
                R.drawable.switch_close_background);
        openDrawable = context.getResources().getDrawable(
                R.drawable.switch_open_background);
        if (currentState == SWITCH_LEFT)
            ball = addBall(LOCATION+1, LOCATION);
        else{
            ball = addBall(45, LOCATION);
        }
        drawBack();
        startAnimation();
        invalidate();
    }

    private ShapeHolder addBall(float x, float y) {
        OvalShape circle = new OvalShape();
        circle.resize(RADIUS, RADIUS);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        ShapeHolder shapeHolder = new ShapeHolder(drawable);
        shapeHolder.setX(x);
        shapeHolder.setY(y);
        Paint paint = drawable.getPaint();
        paint.setColor(Color.WHITE);
        shapeHolder.setPaint(paint);
        return shapeHolder;
    }

    public void setSwitchListener(OnSwitchListener switchListener) {
        this.switchListener = switchListener;
    }

    @SuppressWarnings("deprecation")
    private void drawBack() {
        if (currentState == SWITCH_LEFT) {
            this.setBackgroundDrawable(closeDrawable);
        } else if (currentState == SWITCH_RIGHT) {
            this.setBackgroundDrawable(openDrawable);
        }

    }

    private void createLeftAnimation() {

        bounceAnim = ObjectAnimator.ofFloat(ball, "x", ball.getX(), 1)
                .setDuration(200);
        bounceAnim.addUpdateListener(this);

    }

    private void createRightAnimation() {

        bounceAnim = ObjectAnimator.ofFloat(ball, "x", ball.getX(), 41)
                .setDuration(200);
        bounceAnim.addUpdateListener(this);

    }

    public void startAnimation() {

//        if (currentState == SWITCH_LEFT) {
//            createLeftAnimation();
//        } else if (currentState == SWITCH_RIGHT) {
//            createRightAnimation();
//        }
//        bounceAnim.start();
    }

    private void drawText(Canvas canvas) {
        if (textEnable) {
            String text = "";
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            paint.setAntiAlias(true);
            int height = 40;
            if (currentState == SWITCH_LEFT) {
                if (offText != null && !"".equals(offText)) {
                    text = offText;
                    paint.setTextSize(50);
                    height = 55;
                }
            } else if (currentState == SWITCH_RIGHT) {
                if (onText != null && !"".equals(onText)) {
                    text = onText;
                }
            }
            canvas.save();
            canvas.drawText(text, viewHeight * 3 / 5, height, paint);
            canvas.restore();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (viewWidth < 2) {
            viewWidth = this.getMeasuredWidth();
        }
        if (viewHeight < 2) {
            viewHeight = this.getMeasuredHeight();
        }
        int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                if (downX < viewWidth / 2) {
                    if (currentState == SWITCH_RIGHT) {
                        currentState = SWITCH_LEFT;
                        setSwitchStateListener(this,false);
                        ball.setX(3);
                        invalidate();
                        return false;
                    }

                } else if (downX > viewWidth / 2) {
                    if (currentState == SWITCH_LEFT) {
                        currentState = SWITCH_RIGHT;
                        setSwitchStateListener(this,true);
                        ball.setX(45);
                        invalidate();
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                boolean isRight = x - downX > 0;
                int location = 0;

                if (isRight) {
                    currentState = SWITCH_RIGHT;
                    setSwitchStateListener(this,true);
                    location = 45;
                } else {
                    currentState = SWITCH_LEFT;
                    setSwitchStateListener(this,false);
                    location = 3;
                }
                ball.setX(location);
                invalidate();
                downX = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                int detalx = (int) (x - downX);
                checkInMoving(x, y);
                if (touchState == TOUCH_STATE_SCROLLING_X) {
                    if (currentState == SWITCH_RIGHT) {
                        if (detalx > 0) {
                            return true;
                        }
                        detalx += 45;
                        if (detalx < 3) {
                            detalx = 3;
                        }
                    } else if (currentState == SWITCH_LEFT) {
                        if (detalx < 0) {
                            return true;
                        }
                        detalx += 1;
                        if (detalx > 45) {
                            detalx = 45;
                        }
                    }
                }
                ball.setX(detalx);
                invalidate();
                break;
        }
        return true;
    }

    private void setSwitchStateListener(View view,boolean switchState) {
        if (switchListener != null) {
            switchListener.onSwitch(view,switchState);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widhtSize = MeasureSpec.getSize(widthMeasureSpec);
        int widhtMode = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getSize(heightMeasureSpec);
        int widht;
        int height;
        if (widhtMode == MeasureSpec.EXACTLY) {
            widht = Math.min(104, widhtSize);
        } else {
            widht = 104;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = Math.min(60, heightSize);
        } else {
            height = 60;
        }
        setMeasuredDimension(widht, height);
    }

    private void checkInMoving(float x, float y) {
        final int xDiff = (int) Math.abs(x - lastMotionX);
        final int yDiff = (int) Math.abs(y - lastMotionY);

        final int touchSlop = this.touchSlop;
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;

        if (xMoved) {
            touchState = TOUCH_STATE_SCROLLING_X;
            lastMotionX = x;
            lastMotionY = y;
        }

        if (yMoved) {
            touchState = TOUCH_STATE_SCROLLING_Y;
            lastMotionX = x;
            lastMotionY = y;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(ball.getX(), ball.getY());
        ball.getShape().draw(canvas);
        canvas.restore();
        final float location = ball.getX();
        if (location == 2.0) {
            currentState = SWITCH_LEFT;
        } else if (location == 45.0) {
            currentState = SWITCH_RIGHT;
        }
        drawBack();
        drawText(canvas);
    }

    public interface OnSwitchListener {
        public void onSwitch(View view,boolean isSwitchOn);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {

    }
}

