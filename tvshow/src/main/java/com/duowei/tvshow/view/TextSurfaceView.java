package com.duowei.tvshow.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Administrator on 2017-03-02.
 */

public class TextSurfaceView extends SurfaceView implements Callback, Runnable{
    /**
     *是否滚动
     */
    private boolean         isMove = true;
    /**
     * 移动方向(默认向左)
     */
    private int             orientation = 1;
    /**
     * 向左移动
     */
    public final static int MOVE_LEFT = 1;
    /**
     * 向右移动
     */
    public final static int MOVE_RIGHT = 0;
    /**
     * 向上移动
     */
    public final static int MOVE_TOP = 2;
    /**
     * 向下移动
     */
    public final static int MOVE_BOTTOM = 3;
    /**
     * 移动速度　1.5s　移动一次
     */
    private long             speed = 100;
    /**
     *字幕内容
     */
    private String             content = "";

    /**
     * 字幕背景色
     * */
    private String             bgColor = "#e0018B7E";

    /**
     * 字幕透明度　默认：60
     */
    private int             bgalpha = 60;

    /**
     * 字体颜色 　默认：白色 (#FFFFFF)
     */
    private String             fontColor = "#dde9f0";

    /**
     * 字体透明度　默认：不透明(255)
     */
    private int             fontAlpha = 255;

    /**
     * 字体大小 　默认：40
     */
    private float             fontSize = 80f;
    /**
     * 容器
     */
    private SurfaceHolder mSurfaceHolder;
    /**
     * 线程控制
     */
    private boolean         loop = true;
    /**
     * 内容滚动位置起始坐标
     */
    private float             x=0;
    /**
     * 内容滚动位置起始坐标
     */
    private float             y=0;

    /**
     * 文字内容宽度
     */
    private float textContentWith = 0;
    private float textHeigth = 0;
    private int repeatCount = 0;
    /**
     * 内容
     */
    private List<String> mList;
    private int currentNews = 0;


    /**
     * @param context
     * <see>默认滚动</see>
     */
    public TextSurfaceView(Context context) {
        super(context);
        init();
    }
    public TextSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init(){
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        //设置画布背景不为黑色　继承Sureface时这样处理才能透明
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        //背景色
        setBackgroundColor(Color.parseColor(bgColor));
        //设置透明
        getBackground().setAlpha(bgalpha);
    }
    public void setData(List<String> mList){
        if(mList == null || mList.size()==0){
            return;
        }
        this.mList = mList;
        currentNews = 0;
        content = mList.get(currentNews);

    }
    /**
     * @param context
     * @param move
     * <see>是否滚动</see>
     */
    public TextSurfaceView(Context context,boolean move) {
        this(context);
        this.isMove = move;
        setLoop(isMove());
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}

    public void surfaceCreated(SurfaceHolder holder) {

        y = getHeight()/2-getFontHeight(this.fontSize)/2;
        Paint paint = new Paint();
        if (!TextUtils.isEmpty(content))
            textContentWith = paint.measureText(content);
            textHeigth = getFontHeight(this.fontSize);

        Log.e("WIDTH:",""+getWidth());
        if(isMove){//滚动效果
            Log.i("surfaceCreated:",textContentWith+"");
            if(orientation == MOVE_LEFT){
                x = getWidth();
            }else if(orientation == MOVE_RIGHT){
                x = -(content.length()*10);
            }else if(orientation == MOVE_TOP){
                x = getWidth()/2-(textContentWith)/2;
                y = getHeight()/2-textHeigth/2;
            }else{
                x = getWidth()/2-(textContentWith)/2;
                y = getHeight()/2-textHeigth/2;
            }
            new Thread(this).start();
        }else{//不滚动只画一次
            draw();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        loop = false;
    }
    /**
     * 画图
     */
    private void draw(){
        //锁定画布
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if(mSurfaceHolder == null || canvas == null){
            return;
        }
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        //清屏
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //锯齿
        paint.setAntiAlias(true);
        //设置阴影
//        paint.setShadowLayer(10, 10, 10, Color.GREEN);
        //字体
        paint.setTypeface(Typeface.SANS_SERIF);
        //字体大小
        paint.setTextSize(fontSize);
        //字体颜色
        paint.setColor(Color.parseColor(fontColor));
        //字体透明度
        paint.setAlpha(fontAlpha);
        /**画文字*/
        canvas.drawText(content,x*2,y, paint);
        //解锁显示
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        //滚动效果
        if(isMove){
            /**内容所占像素*/
            float conlen = paint.measureText(content)/4;
            //组件宽度
            int w = getWidth();
            //方向
            if(orientation == MOVE_LEFT){//向左
                if(x< -conlen){
                    x = w - 5;
                    nextNews();
                }else{
                    x -= 2;
                }
            }else if(orientation == MOVE_RIGHT){//向右
                if(x >= w - 5){
                    x = -conlen;
                    nextNews();
                }else{
                    x+=2;
                }
            }else if(orientation == MOVE_TOP){//向上
                x = getWidth()/2-(conlen)/2;
                if(y< -getFontHeight(fontSize)){
                    y = getHeight() - 5;
                    nextNews();
                }else{
                    y -= 2;
                }
            }else if(orientation == MOVE_BOTTOM){//向下
                if(y >= getFontHeight(fontSize)){
                    y = -getHeight() - 5;
                    nextNews();
                }else{
                    y+=2;
                }
            }
        }
    }
    public void run(){
        while(loop){
            synchronized (mSurfaceHolder) {
                draw();
            }
            try{
                Thread.sleep(1);
            }catch(InterruptedException ex){
                Log.e("TextSurfaceView",ex.getMessage()+"\n"+ex);
            }
        }
        content = null;
    }
    private void nextNews(){
        if (mList==null||mList.size()==0){
            return;
        }
        content = mList.get(currentNews);
        currentNews++;
        if (currentNews>=mList.size()){
            currentNews = 0;
        }

    }
    /******************************set get method***********************************/

    private int getOrientation() {
        return orientation;
    }

    /**
     * @param orientation
     *  <li>可以选择类静态变量</li>
     *  <li>1.MOVE_RIGHT 向右 (默认)</li>
     *  <li>2.MOVE_LEFT  向左</li>
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    private long getSpeed() {
        return speed;
    }

    /**
     * @param speed
     * <li>速度以毫秒计算两次移动之间的时间间隔</li>
     * <li>默认为 1500 毫秒</li>
     */
    public void setSpeed(long speed) {
        this.speed = speed;
    }
    public boolean isMove() {
        return isMove;
    }
    /**
     * @param isMove
     * <see>默认滚动</see>
     */
    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
    public void setBgalpha(int bgalpha) {
        this.bgalpha = bgalpha;
    }
    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }
    public void setFontAlpha(int fontAlpha) {
        this.fontAlpha = fontAlpha;
    }
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public void setmList(List<String> mList) {
        this.mList = mList;
    }

    public int getFontHeight(float fontSize)
    {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
//        return (int) Math.ceil(fm.descent - fm.top)-2;
        return (int)((fm.descent - fm.ascent) / 2 - 100);
    }
}
