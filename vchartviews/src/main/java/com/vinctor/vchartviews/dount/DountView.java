package com.vinctor.vchartviews.dount;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.vinctor.vchartviews.AutoView;
import com.vinctor.vchartviews.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Vinctor on 2017/6/30.
 */

public class DountView extends AutoView {

    private float height;
    private float width;
    private float centerX;
    private float centerY;
    private float drawDountLeft;
    private float drawDountRight;
    private float drawDountTop;
    private float drawDountBottom;
    private float radius;
    private boolean noneData;
    private float tagMargin = 20;
    private float tagLineTextMargin = 8;
    protected float[] centerAngles;


    private Paint dountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint tagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    List<DountData> datas = new ArrayList<>();
    private float spaceAngle = 2;
    private float dountWidth = 80;
    private int textSize = 20;
    private int tagTextColor = 0xff00ff00;
    private int tagLineColor = 0xff00ff00;
    private float tagLineWidth = 3;
    private onShowTagCallBack onShowTagCallBack;


    //无数据状态
    Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    boolean isEmptyStyle = false;
    int emptyDountColor = 0xffe0e0e0;
    String emptyText = "无";
    int emptyTextColor = 0xff666666;
    float emptyTextSize = 34;
    float emptyRadius = 0f;

    public DountView(Context context) {
        super(context);
        init(context, null);
    }

    public DountView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DountView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DountView);

            spaceAngle = ta.getInt(R.styleable.DountView_spaceAngle, 3);
            setDountWidth(ta.getDimension(R.styleable.DountView_dountWidth, dountWidth));
            setTextSize(ta.getDimensionPixelSize(R.styleable.DountView_tagTextSize, textSize));
            tagTextColor = ta.getColor(R.styleable.DountView_tagTextColor, tagTextColor);
            tagLineColor = ta.getColor(R.styleable.DountView_tagLineColor, tagLineColor);
            setTagLineWidth(ta.getDimension(R.styleable.DountView_tagLineWidth, tagLineWidth));

            ta.recycle();
        }
        setPaint();
    }

    public void commit() {
        setPaint();
        compute();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
        compute();
        setPaint();
    }

    private void compute() {
        //check
        check();
        if (width == 0 || height == 0 || (noneData && !isEmptyStyle)) {
            return;
        }
        centerX = width / 2;
        centerY = height / 2;
        if (isEmptyStyle) {
            if (emptyRadius == 0) {
                radius = Math.min(height, width) / 2 - 10;
            } else {
                radius = Math.min(emptyRadius, Math.min(height, width) / 2 - 10);
            }
            drawDountLeft = centerX - radius;
            drawDountRight = centerX + radius;
            drawDountTop = centerY - radius;
            drawDountBottom = centerY + radius;

        } else {
            centerAngles = new float[datas.size()];
            //avaiable
            float maxTagWidth = getMaxTagWidthWithMargin();
            float left = maxTagWidth;
            float right = width - maxTagWidth;
            float top = 0;
            float bottom = height;
            radius = Math.min(radius, Math.min(right - left, bottom - top) / 2);
            drawDountLeft = centerX - radius;
            drawDountRight = centerX + radius;
            drawDountTop = centerY - radius;
            drawDountBottom = centerY + radius;


            float spaceTotalAngle = spaceAngle * getSpaceCount();
            float dataAvaiableAngle = 360 - spaceTotalAngle;
            float peerNumAngle = dataAvaiableAngle / getDataNumsTotal();
            for (DountData data : datas) {
                data.setAngle(peerNumAngle * data.getNum());
            }
        }
    }

    private void setPaint() {
        dountPaint.setStyle(Paint.Style.STROKE);
        dountPaint.setStrokeWidth(dountWidth);

        tagPaint.setColor(tagLineColor);
        tagPaint.setStrokeWidth(tagLineWidth);
        tagPaint.setTextSize(textSize);

        emptyPaint.setTextSize(emptyTextSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width == 0 || height == 0 || (noneData && !isEmptyStyle)) {
            return;
        }
        if (isEmptyStyle) {
            drawEmptyStyle(canvas);
        } else {
            drawDount(canvas);
            if (onShowTagCallBack != null) {
                drawTag(canvas);
            }
        }
    }

    //空数据
    private void drawEmptyStyle(Canvas canvas) {
        emptyPaint.setColor(emptyDountColor);
        emptyPaint.setAlpha(0xff);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setStrokeWidth(dountWidth);
        canvas.drawCircle(centerX, centerY, radius - dountWidth / 2, emptyPaint);

        emptyPaint.setStyle(Paint.Style.FILL);
        emptyPaint.setColor(emptyTextColor);
        emptyPaint.setAlpha(0xff);
        float textX = centerX - emptyPaint.measureText(emptyText) / 2;
        float textY = centerY + emptyTextSize / 2;
        canvas.drawText(emptyText, textX, textY, emptyPaint);
    }

    private void drawDount(Canvas canvas) {
        int size = datas.size();
        if (size == 1) {
            DountData data = datas.get(0);
            dountPaint.setColor(data.getDountColor());
            dountPaint.setAlpha(0xff);
            RectF recf = new RectF(
                    drawDountLeft + dountWidth / 2,
                    drawDountTop + dountWidth / 2,
                    drawDountRight - dountWidth / 2,
                    drawDountBottom - dountWidth / 2);
            canvas.drawArc(recf, 0, 360, false, dountPaint);
            centerAngles[0] = 0f;
            return;
        }
        float startAngle = spaceAngle / 2 - 90;
        for (int i = 0; i < size; i++) {
            DountData data = datas.get(i);
            dountPaint.setColor(data.getDountColor());
            dountPaint.setAlpha(0xff);
            RectF recf = new RectF(
                    drawDountLeft + dountWidth / 2,
                    drawDountTop + dountWidth / 2,
                    drawDountRight - dountWidth / 2,
                    drawDountBottom - dountWidth / 2);
            canvas.drawArc(recf, startAngle, data.getAngle(), false, dountPaint);
            centerAngles[i] = startAngle + data.getAngle() / 2;
            startAngle += data.getAngle() + spaceAngle;
        }
    }

    float tagLastY = 0;
    int lastGravity = Gravity.RIGHT;

    private void drawTag(Canvas canvas) {

        int size = centerAngles.length;
        int rightTopIndex = 0;
        int rightBottomIndex = 0;
        int leftBottomIndex = 0;
        for (int j = 0; j < size; j++) {
            float angle = centerAngles[j];
            if (angle <= 0) {
                rightTopIndex = j;
            }
            if (angle > 0 && angle < 90) {
                rightBottomIndex = j;
            }
            if (angle >= 90 && angle <= 180) {
                leftBottomIndex = j;
            }
        }
        if (rightBottomIndex == 0) {
            rightBottomIndex = rightTopIndex;
        }
        if (leftBottomIndex == 0) {
            leftBottomIndex = rightBottomIndex;
        }
        tagLastY = 0;
        lastGravity = Gravity.RIGHT;
        //右上
        for (int i = rightTopIndex; i >= 0; i--) {
            DountData data = datas.get(i);
            float centerAngle = centerAngles[i];
            drawRightTopTag(canvas, centerAngle, data);
        }
        tagLastY = 0;
        lastGravity = Gravity.LEFT;
        //左下
        for (int i = leftBottomIndex; i > rightBottomIndex; i--) {
            DountData data = datas.get(i);
            float centerAngle = centerAngles[i];
            drawLeftBottomTag(canvas, centerAngle, data);
        }

        tagLastY = 0;
        lastGravity = Gravity.RIGHT;
        // 右下 左上
        for (int i = 0; i < size; i++) {
            DountData data = datas.get(i);
            float centerAngle = centerAngles[i];
            if (centerAngle <= 0 || (centerAngle >= 90 && centerAngle <= 180)) {
                continue;
            }

            drawDefaultTag(canvas, centerAngle, data);
        }
    }

    //左下
    private void drawLeftBottomTag(Canvas canvas, float centerAngle, DountData data) {
        centerAngle += 90;
        float innerX = getPointX(centerAngle, radius - tagMargin);
        float innerY = getPointY(centerAngle, radius - tagMargin);

        float outX = getPointX(centerAngle, radius + tagMargin);
        float outY = getPointY(centerAngle, radius + tagMargin);

        //避免重叠 遮盖
        if (tagLastY != 0) {

            if ((tagLastY + tagLineTextMargin + textSize + tagLineWidth > outY)) {
                outY = tagLastY + +textSize + tagLineTextMargin + tagLineWidth + 5;
            }
            lastGravity = Gravity.RIGHT;
        }
        tagLastY = outY;


        Path path = new Path();
        path.moveTo(innerX, innerY);
        path.lineTo(outX, outY);

        float tagTextWidth = tagPaint.measureText(data.getTagText());
        float endX = 0f;
        float endY = outY;
        float textX = 0f;
        if (outX > centerX)

        {
            endX = outX + tagTextWidth;
            textX = outX;
        } else

        {
            endX = outX - tagTextWidth;
            textX = endX;
        }
        path.lineTo(endX, endY);

        tagPaint.setStyle(Paint.Style.STROKE);
        tagPaint.setColor(tagLineColor);
        tagPaint.setAlpha(0xff);
        canvas.drawPath(path, tagPaint);

        tagPaint.setStyle(Paint.Style.FILL);
        tagPaint.setColor(tagTextColor);
        tagPaint.setAlpha(0xff);
        canvas.drawText(data.getTagText(), textX, endY - tagLineTextMargin, tagPaint);
    }

    //右上
    private void drawRightTopTag(Canvas canvas, float centerAngle, DountData data) {
        centerAngle += 90;
        float innerX = getPointX(centerAngle, radius - tagMargin);
        float innerY = getPointY(centerAngle, radius - tagMargin);

        float outX = getPointX(centerAngle, radius + tagMargin);
        float outY = getPointY(centerAngle, radius + tagMargin);

        //避免重叠 遮盖
        if (tagLastY != 0) {
            if ((tagLastY - tagLineTextMargin - textSize - tagLineWidth < outY)) {
                outY = tagLastY - +textSize - tagLineTextMargin - tagLineWidth - 5;
            }
            lastGravity = Gravity.RIGHT;
        }
        tagLastY = outY;


        Path path = new Path();
        path.moveTo(innerX, innerY);
        path.lineTo(outX, outY);

        float tagTextWidth = tagPaint.measureText(data.getTagText());
        float endX = 0f;
        float endY = outY;
        float textX = 0f;
        if (outX > centerX)

        {
            endX = outX + tagTextWidth;
            textX = outX;
        } else

        {
            endX = outX - tagTextWidth;
            textX = endX;
        }
        path.lineTo(endX, endY);

        tagPaint.setStyle(Paint.Style.STROKE);
        tagPaint.setColor(tagLineColor);
        tagPaint.setAlpha(0xff);
        canvas.drawPath(path, tagPaint);

        tagPaint.setStyle(Paint.Style.FILL);
        tagPaint.setColor(tagTextColor);
        tagPaint.setAlpha(0xff);
        canvas.drawText(data.getTagText(), textX, endY - tagLineTextMargin, tagPaint);
    }

    //左上 右下
    private void drawDefaultTag(Canvas canvas, float centerAngle, DountData data) {
        centerAngle += 90;
        float innerX = getPointX(centerAngle, radius - tagMargin);
        float innerY = getPointY(centerAngle, radius - tagMargin);

        float outX = getPointX(centerAngle, radius + tagMargin);
        float outY = getPointY(centerAngle, radius + tagMargin);

        //避免重叠 遮盖
        if (outX > centerX) {//右侧
            if (lastGravity == Gravity.RIGHT) {
                if ((tagLastY + tagLineTextMargin + textSize + tagLineWidth > outY)) {
                    outY = tagLastY + +textSize + tagLineTextMargin + tagLineWidth + 5;
                    tagLastY = outY;
                }
                tagLastY = outY;
            } else
                tagLastY = 0;
            lastGravity = Gravity.RIGHT;
        } else {//左侧
            if (lastGravity == Gravity.LEFT) {
                if (tagLastY - textSize - tagLineTextMargin - tagLineWidth < outY) {
                    outY = tagLastY - textSize - tagLineTextMargin - tagLineWidth - 5;
                }
                tagLastY = outY;
            } else
                tagLastY = outY;
            lastGravity = Gravity.LEFT;
        }


        Path path = new Path();
        path.moveTo(innerX, innerY);
        path.lineTo(outX, outY);

        float tagTextWidth = tagPaint.measureText(data.getTagText());
        float endX = 0f;
        float endY = outY;
        float textX = 0f;
        if (outX > centerX)

        {
            endX = outX + tagTextWidth;
            textX = outX;
        } else

        {
            endX = outX - tagTextWidth;
            textX = endX;
        }
        path.lineTo(endX, endY);

        tagPaint.setStyle(Paint.Style.STROKE);
        tagPaint.setColor(tagLineColor);
        tagPaint.setAlpha(0xff);
        canvas.drawPath(path, tagPaint);

        tagPaint.setStyle(Paint.Style.FILL);
        tagPaint.setColor(tagTextColor);
        tagPaint.setAlpha(0xff);
        canvas.drawText(data.getTagText(), textX, endY - tagLineTextMargin, tagPaint);
    }

    /**
     * 得到需要计算的角度
     *
     * @param angle 角度，例：30.60.90
     * @return res
     */

    private float getNewAngle(float angle) {
        float res = angle;
        if (angle >= 0 && angle <= 90) {
            res = 90 - angle;
        } else if (angle > 90 && angle <= 180) {
            res = angle - 90;
        } else if (angle > 180 && angle <= 270) {
            res = 270 - angle;
        } else if (angle > 270 && angle <= 360) {
            res = angle - 270;
        }
        return res;
    }

    /**
     * 若以圆心为原点，返回该角度顶点的所在象限
     *
     * @param angle
     * @return
     */
    private int getQr(float angle) {
        int res = 0;
        if (angle >= 0 && angle <= 90) {
            res = 1;
        } else if (angle > 90 && angle <= 180) {
            res = 2;
        } else if (angle > 180 && angle <= 270) {
            res = 3;
        } else if (angle > 270 && angle <= 360) {
            res = 4;
        }
        return res;
    }

    /**
     * 返回多边形顶点X坐标
     *
     * @param angle
     * @return
     */
    private float getPointX(float angle, float radius) {
        float newAngle = getNewAngle(angle);
        float res = 0;
        float width = (float) (radius * Math.cos(newAngle / 180 * Math.PI));
        int qr = getQr(angle);
        switch (qr) {
            case 1:
            case 2:
                res = centerX + width;
                break;
            case 3:
            case 4:
                res = centerX - width;
                break;
            default:
                break;
        }
        return res;
    }

    /**
     * 返回多边形顶点Y坐标
     */
    private float getPointY(float angle, float radius) {
        float newAngle = getNewAngle(angle);
        float height = (float) (radius * Math.sin(newAngle / 180 * Math.PI));
        float res = 0;
        int qr = getQr(angle);
        switch (qr) {
            case 1:
            case 4:
                res = centerY - height;
                break;
            case 2:
            case 3:
                res = centerY + height;
                break;
            default:
                break;
        }
        return res;
    }


    private float getMaxTagWidthWithMargin() {
        if (onShowTagCallBack == null) {
            return 0;
        }
        int maxNum = 0;
        int minNum = datas.get(0).getNum();
        for (DountData data : datas) {
            int num = data.getNum();
            //max
            if (maxNum < num) {
                maxNum = num;
            }
            //min
            if (minNum > num) {
                minNum = num;
            }
        }

        float max = 0;
        for (int i = 0; i < datas.size(); i++) {
            DountData data = datas.get(i);
            int num = data.getNum();
            int type = NUM_TYPE_DEFAULT;
            if (num == maxNum) {
                type = NUM_TYPE_MAX;
            }
            if (num == minNum) {
                type = NUM_TYPE_MIN;
            }
            String tag = onShowTagCallBack.onShowTag(num, i, data.getTagText(), type);
            if (tag == null) {
                tag = num + "";
            }
            data.setTagText(tag);
            float temp = tagPaint.measureText(data.getTagText());
            if (temp > max) {
                max = temp;
            }
        }

        max += tagMargin;
        return max;
    }

    private void check() {
        if (isEmptyStyle) {
            datas.clear();
            return;
        }
        Iterator<DountData> it = datas.iterator();
        while (it.hasNext()) {
            DountData data = it.next();
            if (data.getNum() == 0) {
                it.remove();
            }
        }
        if (datas.size() == 0)
            noneData = true;
        else noneData = false;
    }

    private int getSpaceCount() {
        int dataSize = datas.size();
        if (dataSize == 1) {
            return 0;
        } else return dataSize;
    }

    private int getDataNumsTotal() {
        int result = 0;
        for (DountData data : datas) {
            result += data.getNum();
        }
        return result;
    }

    public interface onShowTagCallBack {
        String onShowTag(int num, int position, String oldTagText, @DountNumType int tag);

    }

    public final static int NUM_TYPE_DEFAULT = -1;
    public final static int NUM_TYPE_MAX = 0;
    public final static int NUM_TYPE_MIN = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NUM_TYPE_MAX, NUM_TYPE_MIN, NUM_TYPE_DEFAULT})
    public @interface DountNumType {

    }

    public DountView clearList() {
        datas.clear();
        return this;
    }

    public DountView setList(List<DountData> datas) {
        this.datas = datas;
        return this;
    }

    public DountView setData(DountData data) {
        datas.clear();
        datas.add(data);
        return this;
    }

    public DountView addData(DountData data) {
        datas.add(data);
        return this;
    }

    public DountView setOnShowTagCallBack(DountView.onShowTagCallBack onShowTagCallBack) {
        this.onShowTagCallBack = onShowTagCallBack;
        return this;
    }

    public DountView setSpaceAngle(float spaceAngle) {
        this.spaceAngle = spaceAngle;
        return this;
    }

    public DountView setDountWidth(float dountWidth) {
        this.dountWidth = getAutoWidthSize(dountWidth);
        return this;
    }

    public DountView setTextSize(int textSize) {
        this.textSize = getAutoHeightSize(textSize);
        return this;
    }

    public DountView setTagLineColor(int tagLineColor) {
        this.tagLineColor = tagLineColor;
        return this;
    }

    public DountView setTagTextColor(int tagTextColor) {
        this.tagTextColor = tagTextColor;
        return this;
    }

    public DountView setTagLineWidth(float tagLineWidth) {
        this.tagLineWidth = getAutoHeightSize(tagLineWidth);
        return this;
    }

    public DountView setEmptyDountColor(int emptyDountColor) {
        this.emptyDountColor = emptyDountColor;
        return this;
    }

    public DountView setEmptyText(String emptyText) {
        this.emptyText = emptyText;
        return this;
    }

    public DountView setEmptyTextColor(int emptyTextColor) {
        this.emptyTextColor = emptyTextColor;
        return this;
    }

    public DountView setEmptyTextSize(float emptyTextSize) {
        this.emptyTextSize = getAutoHeightSize(emptyTextSize);
        return this;
    }

    public boolean isEmptyStyle() {
        return isEmptyStyle;
    }

    public DountView setEmptyStyle(boolean emptyStyle) {
        isEmptyStyle = emptyStyle;
        return this;
    }

    public DountView setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public DountView setEmptyRadius(float emptyRadius) {
        this.emptyRadius = getAutoWidthSize(emptyRadius);
        return this;
    }
}
