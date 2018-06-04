package com.example.yu.luckwheel.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.yu.luckwheel.R;
import com.example.yu.luckwheel.utils.Utils;

import java.util.ArrayList;

public class LuckWheelView extends View {

    private Paint mPaint;
    private int num;
    private String[] names;
    private ArrayList<Bitmap> bitmaps;
    private int angle;
    //内环与外环的间距,没有设置是默认30dp
    private float distance;
    //文字与内环的垂直偏移量，没有设置默认20dp
    private float voffset;
    //文字大小
    private float textSize;
    //指针方向，只能是上下左右4个方向、默认是朝上
    private int orientation;

    //偏移角度
    private int startAngle;
    private int totalAngle;


    public void LogYuw(String str) {
        Log.w("yujj", str);
    }

    public void LogYuE(String str) {
        Log.e("yujj", str);
    }

    public LuckWheelView(Context context) {
        this(context, null);
    }

    public LuckWheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(textSize);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LuckWheelView);
        num = typedArray.getInteger(R.styleable.LuckWheelView_num, 4);
        int nameId = typedArray.getResourceId(R.styleable.LuckWheelView_names, -1);
        int iconId = typedArray.getResourceId(R.styleable.LuckWheelView_icons, -1);
        distance = typedArray.getDimension(R.styleable.LuckWheelView_innerDistance, Utils.dip2px(context, 30));
        voffset = typedArray.getDimension(R.styleable.LuckWheelView_offset, Utils.dip2px(context, 20));
        textSize = typedArray.getDimension(R.styleable.LuckWheelView_textSize, 15);
        totalAngle = orientation = typedArray.getInteger(R.styleable.LuckWheelView_orientation, 270);

        LogYuw("方向:" + orientation);
        if (num == 1 || num == 0) {
            throw new RuntimeException("num 属性值不能为1或0");
        }
        if (360 % num != 0) {
            throw new RuntimeException(num + "不能被360整除");
        }
        if (nameId == -1 || iconId == -1) {
            throw new RuntimeException("没有找到资源id");
        }
        names = context.getResources().getStringArray(nameId);
        String[] icons = context.getResources().getStringArray(iconId);
        if (names.length != num || names.length != icons.length) {
            throw new RuntimeException("名称个数或资源个数不相等!");
        }

        bitmaps = new ArrayList<>();
        for (String name : icons) {
            //根据名称找到对应的图片id
            int mipmapId = context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
            bitmaps.add(BitmapFactory.decodeResource(context.getResources(), mipmapId));
        }
        angle = 360 / num;
        startAngle = orientation % angle;
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        int minValue = Math.min(measuredHeight, measuredWidth);
        LogYuw("measuredHeight:" + measuredHeight + "------measuredWidth:" + measuredWidth);
        setMeasuredDimension(minValue, minValue);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawPan(canvas);
        drawIconAndText(canvas);
    }


    //画背景色
    private void drawBackground(Canvas canvas) {
        mPaint.setColor(Color.rgb(255, 92, 93));
        //获取x、y坐标
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        canvas.drawCircle(x, y, getWidth() / 2, mPaint);
    }

    //根据传入的块数，画不同的块块
    private void drawPan(Canvas canvas) {
        int startAngle = this.startAngle;
        //这里要记得与外环的间距
        RectF rectF = new RectF(getPaddingLeft() + distance, getPaddingTop() + distance, getWidth() - distance, getHeight() - distance);
        for (int i = 0; i < num; i++) {
            //交替色
            mPaint.setColor((i % 2 == 0) ? Color.rgb(255, 133, 132) : Color.rgb(254, 104, 105));
            canvas.drawArc(rectF, startAngle, angle, true, mPaint);
            startAngle += angle;
        }
    }

    /**
     * 画文字和icon
     * <p>
     * 圆弧上任意一点的坐标，可根据勾股定理得到，如果在圆心O(0,0)上，则x²+y²=r²,
     * {x=cosθ
     * {y=sinθ
     * 若不在圆心O(0,0)上，则圆弧所在圆的方程可表示为(x-a)²+(x-b)²=r²
     * {x=a+rcosθ
     * {y=b+rsinθ
     * <p>
     * <p>
     * 弧长计算公式l=n°πr÷180°（l=n°x2πr/360°)
     * <p>
     * 1、Math.sin(x)      x 的正玄值。返回值在 -1.0 到 1.0 之间；
     * Math.cos(x)    x 的余弦值。返回的是 -1.0 到 1.0 之间的数；
     * <p>
     * 这两个函数中的X 都是指的“弧度”而非“角度”，弧度的计算公式为： 2*PI/360*角度；
     * 30° 角度 的弧度 = 2*PI/360*30
     * <p>
     * 2、如何得到圆上每个点的坐标？
     * 解决思路：根据三角形的正玄、余弦来得值；
     * <p>
     * 假设一个圆的圆心坐标是(a,b)，半径为r，
     * 则圆上每个点的X坐标=a + Math.sin(2*Math.PI / 360) * r ；Y坐标=b + Math.cos(2*Math.PI / 360) * r ；
     *
     * @param canvas 画布
     */
    private void drawIconAndText(Canvas canvas) {
        RectF oval = new RectF(getPaddingLeft() + distance, getPaddingTop() + distance, getWidth() - distance, getHeight() - distance);

        //单个方块角度的一般
        int half = angle / 2;
        //远点x,y坐标
        int xx = (getWidth()) / 2;
        int yy = getHeight() / 2;
        float paddingLeft = getPaddingLeft() + distance;
        //內圆半径
        float radius = (getWidth() - paddingLeft - distance - getPaddingRight()) / 2;
        int startAngle = this.startAngle;
        int imageWidth = (int) (radius / 4);
//        startAngle += half;
        for (int i = 0; i < num; i++) {
            //根据角度转换成弧度
            double v = Math.toRadians(startAngle + half);
            //这里取内环半径的2/3处的一个点
            int x = (int) (xx + Math.cos(v) * (radius * 2 / 3));
            int y = (int) (yy + Math.sin(v) * (radius * 2 / 3));
            Bitmap bitmap = bitmaps.get(i);
            RectF rectF = new RectF(x - imageWidth * 2 / 3, y - imageWidth * 2 / 3, x + imageWidth * 2 / 3, y + imageWidth * 2 / 3);
            canvas.drawBitmap(bitmap, null, rectF, mPaint);

            String name = names[i];
            Path path = new Path();
            path.addArc(oval, startAngle, angle);
            float textWidth = mPaint.measureText(name);
//            弧长计算公式l=n°πr÷180°（l=n°x2πr/360°)
            int hoffset = (int) ((angle * Math.PI * radius / 180 - textWidth) / 2);
//            int voffset = (int) (radius / 2 / 6);
            mPaint.setColor(Color.WHITE);
            canvas.drawTextOnPath(name, path, hoffset, voffset, mPaint);
            startAngle += angle;
        }
    }

    public void startRotate(int position) {
        if (position > num) {
            throw new IndexOutOfBoundsException("所选区块不能超过总块数");
        }
        LogYuw("startRotate:" + startAngle);
        int targetAngle;
        if (startAngle < orientation) {
//            startAngle += orientation;
            targetAngle = Math.abs(orientation - (position * angle - angle / 2));
        } else {
            targetAngle = position * angle;
        }
        //转的圈数
//        int v = (int) (Math.random() * 3) + 3;
        int v = 3;
        totalAngle = targetAngle + 360 * v + startAngle;
        LogYuw("开始角度:" + startAngle + "----targetAngle:" + targetAngle + "-------total:" + totalAngle + "--position:" + position + "----v" + v);
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "rotation", startAngle, totalAngle);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int rotation = (int) animation.getAnimatedValue("rotation");
//                LogYuE("角度:" + rotation);
                startAngle = rotation;
                ViewCompat.postInvalidateOnAnimation(LuckWheelView.this);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animation.removeAllListeners();
            }
        });
        animator.setDuration(3600);
        animator.start();
    }
}
