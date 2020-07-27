package com.example.croller;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class Croller extends View {

    private Bitmap image1;

    private float midx, midy;
    private Paint circlePaint, circlePaint2, linePaint, handlePaint, centerPaint;
    private float currdeg = 0, deg = 3, downdeg = 0;

    private int backCircleColor = Color.parseColor("#222222");
    private int mainCircleColor = Color.parseColor("#000000");
    private int indicatorColor = Color.parseColor("#FFA036");
    private int progressPrimaryColor = Color.parseColor("#FFA036");
    private int progressSecondaryColor = Color.parseColor("#111111");
    private int handleColor = Color.parseColor("#FFA036");
    private int centerCircleColor = Color.parseColor("#ffffff");

    private int backCircleDisabledColor = Color.parseColor("#82222222");
    private int mainCircleDisabledColor = Color.parseColor("#82000000");
    private int indicatorDisabledColor = Color.parseColor("#82FFA036");
    private int progressPrimaryDisabledColor = Color.parseColor("#82FFA036");
    private int progressSecondaryDisabledColor = Color.parseColor("#82111111");

    private float progressPrimaryCircleSize = -1;
    private float progressSecondaryCircleSize = -1;

    private float progressPrimaryStrokeWidth = 25;
    private float progressSecondaryStrokeWidth = 10;

    private float mainCircleRadius = -1;
    private float backCircleRadius = -1;
    private float progressRadius = -1;
    private float handleCircleRadius = -1;
    private float centerCircleRadius = -1;

    // progress bar의 점 최대 개수
    private int max = 60;
    private int min = 1;

    private float indicatorWidth = 30;

//    private int startOffset = 30;
    private int startOffset = 0;
    private int startOffset2 = 0;
    private int sweepAngle = -1;

    private boolean isEnabled = true;

    private boolean startEventSent = false;

    RectF oval;

    public Croller(Context context) {
        super(context);
        init();
    }

    public Croller(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initXMLAttrs(context, attrs);
        init();
    }

    public Croller(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initXMLAttrs(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true); // for smooth edge
        circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
        circlePaint.setStyle(Paint.Style.FILL);

        circlePaint2 = new Paint();
        circlePaint2.setAntiAlias(true);
        circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
        circlePaint2.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(indicatorWidth);

        handlePaint = new Paint();
        handlePaint.setAntiAlias(true);
        handlePaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Paint.Style.FILL);

        oval = new RectF();
    }

    // 맞춤 속성 적용
    private void initXMLAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Croller);

        setEnabled(ta.getBoolean(R.styleable.Croller_enabled, true));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // widthMeasureSpec, heightMeasureSpec : 부모 컨테이너에서 정한 가로, 세로 크기

        int minWidth = (int) Utils.convertDpToPixel(160, getContext());
        int minHeight = (int) Utils.convertDpToPixel(160, getContext());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        /**
         * MeasureSpec.EXACTLY : match_parent (외부에서 이미 크기가 정하여져 있음)
         * MeasureSpec.AT_MOST : wrap_content (뷰 내부의 크기에 따라서 달라짐)
         * MeasureSpec.UNSPECIFIED : MODE가 셋팅되지 않은 크기가 넘어올때 (거의 없음.)
         */
        int width, height;

        // 현재 width 크기 구하기
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }
        else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(minWidth, widthSize);
        }
        else {
            // UNSPECIFIED --> ScrollView의 경우에만 작동하고 이외의 경우에는 걸리지 않는다.
            // 만약 width가 wrap_content 인 경우 height와 똑같이 만든다.
            width = heightSize;
        }

        // 현재 height 크기 구하기
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        }
        else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(minHeight, heightSize);
        }
        else {
            // UNSPECIFIED --> ScrollView의 경우에만 작동하고 이외의 경우에는 걸리지 않는다.
            // 만약 height가 wrap_content 인 경우 width와 똑같이 만든다.
            height = widthSize;
        }

        if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
            width = minWidth;
            height = minHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 중앙 지점 구하기
        midx = getWidth() / 2;
        midy = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        circlePaint2.setColor(progressPrimaryColor);
        circlePaint.setColor(progressSecondaryColor);
        linePaint.setColor(indicatorColor);
        handlePaint.setColor(handleColor);
        centerPaint.setColor(centerCircleColor);

        //--------------------------------//
        int radius = (int) (Math.min(midx, midy) * ((float) 14.5 / 16));

        if (sweepAngle == -1) {
            sweepAngle = 360 - (2 * startOffset);
        }

        if (mainCircleColor == -1) {
            mainCircleRadius = radius * ((float) 11/15);
        }
        if (backCircleRadius == -1) {
            backCircleRadius = radius * ((float) 13/15);
        }
        if (progressRadius == -1) {
            progressRadius = radius;
        }
        if (handleCircleRadius == -1) {
            handleCircleRadius = radius * ((float) 1/10);
        }
        if (centerCircleColor == -1) {
            centerCircleRadius = radius * ((float) 3/5);
        }

        circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
        circlePaint2.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(indicatorWidth);

        float deg3 = Math.min(deg, max + 2);

        oval.set(midx - progressRadius, midy - progressRadius, midx + progressRadius, midy + progressRadius);

        // canvas.drawArc(oval, (float) 90+startOffset, (float)sweepAngle, false, circlePaint);
        // 시작점 수정 --> 90도에서 시작에서 270도로
        canvas.drawArc(oval, (float) 270+startOffset, (float)sweepAngle, false, circlePaint);

        // 시계 방향만 필요하므로
        // progress bar의 회전 액션을 그릴 때 필요한 code
        canvas.drawArc(oval, (float) 270+startOffset, ((deg3-2)*(sweepAngle / max)), false, circlePaint2);

        float tmp2 = ((float) startOffset / 360) + (((float) sweepAngle / 360) * ((deg-2)/(max))) * 2;

        // 막대 크기 설정하기 위한 x1, x2, y1, y2
//        float x1 = midx + (float) (radius * ((float) 2/5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
//        float y1 = midy + (float) (radius * ((float) 2/5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));
//        float x2 = midx + (float) (radius * ((float) 3/5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
//        float y2 = midy + (float) (radius * ((float) 3/5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));

        float x1 = midx + (float) (radius * ((float) 1/5) * Math.sin(Math.PI * (1.0 - tmp2)));
        float y1 = midy + (float) (radius * ((float) 1/5) * Math.cos(Math.PI * (1.0 - tmp2)));
        float x2 = midx + (float) (radius * Math.sin(Math.PI * (1.0 - tmp2)));
        float y2 = midy + (float) (radius * Math.cos(Math.PI * (1.0 - tmp2)));

        Log.e("temp2 : ", ""+tmp2);
        Log.e("sin : ", ""+Math.sin(Math.PI * (1.0 - tmp2)));
        Log.e("cos : ", ""+Math.cos(Math.PI * (1.0 - tmp2)));

        circlePaint.setStyle(Paint.Style.FILL);

        circlePaint.setColor(backCircleColor);
        canvas.drawCircle(midx, midy, backCircleRadius, circlePaint);

        circlePaint.setColor((mainCircleColor));
        canvas.drawCircle(midx, midy, mainCircleRadius, circlePaint);

        canvas.drawLine(x1, y1, x2, y2, linePaint);

        canvas.drawCircle(x2, y2, handleCircleRadius, handlePaint);
        canvas.drawCircle(midx, midy, centerCircleRadius,centerPaint);

        float move_size = Utils.getDistance(midx, midy, midx-centerCircleRadius, midy-centerCircleRadius);
        move_size /= 2;

        image1 = BitmapFactory.decodeResource(getResources(), R.drawable.seoul);
        int bitmap_Size = (int)(centerCircleRadius);
        Bitmap resize_bitmap = Bitmap.createScaledBitmap(image1, bitmap_Size, bitmap_Size, true);
        canvas.drawBitmap(resize_bitmap, null, new Rect((int)(midx-move_size), (int)(midy-move_size), (int)(midx+move_size), (int)(midy+move_size)), null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (Utils.getDistance(event.getX(), event.getY(), midx, midy) > Math.max(mainCircleRadius, Math.max(backCircleRadius, progressRadius))) {

        }

        float dx, dy;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dx = event.getX() - midx;
                dy = event.getY() - midy;

                downdeg = (float)((Math.atan2(dy, dx) * 180) / Math.PI);
                downdeg += 90;
                if (downdeg < 0) {
                    downdeg += 360;
                }
                // 넣었을 때와 안 넣었을 때의 터치 감도가 달라진다.
                downdeg = (float) Math.floor((downdeg / 360) * (max+5));

                return true;
            case MotionEvent.ACTION_MOVE:
                dx = event.getX() - midx;
                dy = event.getY() - midy;
                currdeg = (float) ((Math.atan2(dy, dx) * 180) / Math.PI);
                currdeg += 90;
                if (currdeg < 0) {
                    currdeg += 360;
                }
                currdeg = (float) Math.floor((currdeg/360) * (max+5));

                if ((currdeg / (max + 4)) > 0.75f && ((downdeg) / (max + 4)) < 0.25f) {
                    deg++;
                    if (deg > max+2) {
                        deg = max + 2;
                    }
                }
                else {
                    deg += (currdeg - downdeg);
                    if (deg > max + 2) {
                        deg = max + 2;
                    }
                    if (deg < min + 2) {
                        deg = (min + 2);
                    }
                }

                downdeg = currdeg;

                invalidate();

                return true;
            case MotionEvent.ACTION_UP:

                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getParent() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            // 부모로 touch event 전달하지 않음
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(event);
    }

    public void setCenterImage() {

    }
}
