package com.lx.sweepstakes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuxi on 2017/9/3.
 */

public class SweepstakesView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

    private SurfaceHolder mHolder;
    private boolean isLottery;
    int count = 0;
    int max = 9999;
    private OnSweepstakesListener mSweepstakesListener;
    private int bgcolorId = R.color.colorPrimaryDark;
    private int margin = 20;
    private int prizeWidth;


    private android.os.Handler mHandler = new Handler();
    public SweepstakesView(Context context) {
        this(context, null);
    }

    public SweepstakesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SweepstakesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

        getHolder().addCallback(this);
        mHolder = getHolder();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        new Thread(this).run();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {

        Canvas canvas = null;

        try {
            canvas = mHolder.lockCanvas();
            if (canvas != null) {
                drawBg(canvas);
                drawPrizes(canvas);
                drawCenter(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBg(Canvas canvas) {
        canvas.drawColor(ContextCompat.getColor(getContext(), bgcolorId), PorterDuff.Mode.CLEAR);
        int x = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        Rect r = new Rect(0, 0, x, x);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);//Paint.ANTI_ALIAS_FLAG 加上此属性，使绘制出的UI具有抗锯齿效果；
        p.setColor(ContextCompat.getColor(getContext(), bgcolorId));
        canvas.drawRect(r, p);
    }

    private Rect getItemRect(int i) {
        int top = 0, left = 0, right = 0, bottom = 0;
        if (i >= 16) {
            i = i % 16;
        }
        if (i < 5) {
            left = i * (margin + prizeWidth) + margin;
            top = margin;
            right = (i + 1) * (margin + prizeWidth);
            bottom = margin + prizeWidth;
        } else if (i >= 5 && i < 8) {
            left = 4 * (margin + prizeWidth) + margin;
            top = (i - 4) * (margin + prizeWidth) + margin;
            right = 5 * (margin + prizeWidth);
            bottom = (i - 3) * (margin + prizeWidth);
        } else if (i >= 8 && i < 13) {
            left = (12 - i) * (margin + prizeWidth) + margin;
            top = 4 * (margin + prizeWidth) + margin;
            right = (13 - i) * (margin + prizeWidth);
            bottom = 5 * (margin + prizeWidth);
        } else if (i >= 13 && i < 16) {
            left = margin;
            top = (16 - i) * (margin + prizeWidth) + margin;
            right = margin + prizeWidth;
            bottom = (17 - i) * (margin + prizeWidth);
        }
        return new Rect(left, top, right, bottom);
    }

    List<Prize> prizeList = new ArrayList();

    public class Prize {

        public Bitmap bitmap;

    }

    private void drawPrizes(final Canvas canvas) {
        for (int i = 0; i < 16; i++) {
            if (prizeList == null || prizeList.isEmpty())
                return;
            Prize prize = prizeList.get(i);
            if (prize == null) return;
            Bitmap bitmap = prize.bitmap;
            if (bitmap == null) return;
            canvas.drawBitmap(bitmap, null, getItemRect(i), null);
        }
    }

    private void drawCenter(Canvas canvas) {
        int left = prizeWidth + margin * 2;
        int right = (prizeWidth + margin) * 4;
        Rect rect = new Rect(left, left, right, right);//示例，具体计算方式，详见demo
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sweepstakes_button);
        canvas.drawBitmap(bitmap, null, rect, null);
    }

    class SweepstakesRunnable implements Runnable {

        @Override
        public void run() {
            if (isLottery) {
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas();
                    if (canvas != null) {
                        drawBg(canvas);
                        drawPrizes(canvas);
                        drawCenter(canvas);
                        drawTransfer(canvas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null)
                        mHolder.unlockCanvasAndPost(canvas);
                }
            }
            //逐渐变慢的效果
            if (isLottery()) {
                int disCount = Max - count;
                int delayed;
                if (disCount < 3) {
                    delayed = 450;
                } else if (disCount < 8) {
                    delayed = 300;
                } else if (disCount < 16) {
                    delayed = 150;
                } else {
                    delayed = 50;
                }
                mHandler.postDelayed(new SweepstakesRunnable(), delayed);
            } else {
                if (mSweepstakesListener != null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mSweepstakesListener.onLotteryFinish();
                }
            }
        }
    }

    private void drawTransfer(final Canvas canvas) {
        if (count <= Max) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sweepstakes_checked_icon);
            canvas.drawBitmap(bitmap, null, getItemRect(count), null);
            if (count == Max) {
                setLottery(false);
            }
            count++;
        } else {
            setLottery(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        witchPos(event);
        return super.onTouchEvent(event);
    }

    private void witchPos(MotionEvent event) {
        // 获取点击屏幕时的点的坐标
        float x = event.getX();
        float y = event.getY();
        int left = (int) ((prizeWidth + margin * 2) + prizeWidth * 0.4);
        int right = (int) ((prizeWidth + margin) * 4 - prizeWidth * 0.4);
        int top = (prizeWidth + margin) * 3;
        int bottom = (int) ((prizeWidth + margin) * 4 - prizeWidth * 0.4);
        if (x > left && x < right && y > top && y < bottom && event.getAction() == MotionEvent.ACTION_DOWN) {
            EventAgent.onEvent(getContext(), EventId.SWEEPSTAKES_APP);
            startLottery();
        }
    }

    private void startLottery() {
        if (isLottery()) {
            ToastUtils.show(getContext(), "正在抽奖...");
            return;
        }
        setLottery(true);
        count = 0;
        new SweepstakesRunnable().run();
        if (mSweepstakesListener != null) {
            mSweepstakesListener.OnSweepstakesStart();
        }
    }

    //absPos 为抽中奖品的绝对位置
//本篇文章中，默认左上角 position == 1,顺时针旋转
    public void setEndPos(int absPos) {
        if (absPos == -1) {
            setLottery(false);
            return;
        }
        Max = absPos + count + 32 - (count % 16);//计算好结束位置后,让它在多转两圈再结束，防止结束过早，略显尴尬。
    }

    public boolean isLottery() {
        return isLottery;
    }

    public void setLottery(boolean lottery) {
        isLottery = lottery;
    }


}
