package aps.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jintong on 17-4-3.
 */

public class circleImageView extends ImageView {
    public circleImageView(Context context) {
        super(context);
    }
    public circleImageView(Context context, AttributeSet attrs)
    {
        super(context,attrs);
    }

    public circleImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context,attrs,defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Drawable drawable=getDrawable();

        if(drawable==null)
        {
            return ;
        }

        if(getWidth()==0||getHeight()==0)
        {
            return;
        }

        if(!(drawable instanceof BitmapDrawable))
        {
            return;
        }

        Bitmap b=((BitmapDrawable)drawable).getBitmap();
        if(null==b)
        {
            return;
        }

        Bitmap bitmap=b.copy(Bitmap.Config.ARGB_8888,true);
        int w=getWidth();

        Bitmap roundBItmap=getCroppedBitmap(bitmap,w);
        canvas.drawBitmap(roundBItmap,0,0,null);
    }

    /**
     * 初始Bitmap对象的缩放裁剪过程
     * @param bmp        初始Bitmap对象
     * @param radius    圆形图片直径大小
     * @return 返回一个圆形的缩放裁剪过后的Bitmap对象
     */
    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        //比较初始Bitmap宽高和给定的圆形直径，判断是否需要缩放裁剪Bitmap对象
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f,
                sbmp.getHeight() / 2 + 0.7f, sbmp.getWidth() / 2 + 0.3f, paint);
        //核心部分，设置两张图片的相交模式，在这里就是上面绘制的Circle和下面绘制的Bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }
}
