package com.example.fullenergystore.extend_plug.RoundImageView;




import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.example.fullenergystore.R;


@SuppressLint("Recycle")
public class RoundImageView extends ImageView {

	private Context mContext;

	private int mBorderThickness = 0;

	private int defaultColor = 0xFFFFFFFF;


	private int mBorderOutsideColor = 0;

	private int mBorderInsideColor = 0;


	private int defaultWidth = 0;

	private int defaultHeight = 0;

	public RoundImageView(Context context) {

		super(context);

		mContext = context;

	}

	public RoundImageView(Context context, AttributeSet attrs) {

		super(context, attrs);

		mContext = context;

		setCustomAttributes(attrs);

	}

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);

		mContext = context;

		setCustomAttributes(attrs);

	}

	private void setCustomAttributes(AttributeSet attrs) {

		TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Round_image_view);
		mBorderThickness = a.getDimensionPixelSize(R.styleable.Round_image_view_border_thickness, 0);
		mBorderOutsideColor = a.getColor(R.styleable.Round_image_view_border_inside_color,defaultColor);
		mBorderInsideColor = a.getColor(R.styleable.Round_image_view_border_inside_color, defaultColor);
		

	}

	@Override
	protected void onDraw(Canvas canvas) {

		Drawable drawable = getDrawable();

		if (drawable == null) {

			return;

		}

		if (getWidth() == 0 || getHeight() == 0) {

			return;

		}

		this.measure(0, 0);

		if (drawable.getClass() == NinePatchDrawable.class)

			return;

		Bitmap b = ((BitmapDrawable) drawable).getBitmap();

		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

		if (defaultWidth == 0) {

			defaultWidth = getWidth();

		}

		if (defaultHeight == 0) {

			defaultHeight = getHeight();

		}

		int radius = 0;

		if (mBorderInsideColor != defaultColor
				&& mBorderOutsideColor != defaultColor) {// 閿熸枻鎷烽敓钘夌敾閿熸枻鎷烽敓鏂ゆ嫹閿熺妗嗭紝鍒嗘唻鎷蜂负閿熸枻鎷峰渾閿熺鍖℃嫹閿熸枻鎷烽敓鐨嗚鎷峰憱閿燂拷

			radius = (defaultWidth < defaultHeight ? defaultWidth
					: defaultHeight) / 2 - 2 * mBorderThickness;

			// 閿熸枻鎷烽敓鏂ゆ嫹鍦�

			drawCircleBorder(canvas, radius + mBorderThickness / 2,
					mBorderInsideColor);

			// 閿熸枻鎷烽敓鏂ゆ嫹鍦�

			drawCircleBorder(canvas, radius + mBorderThickness
					+ mBorderThickness / 2, mBorderOutsideColor);

		} else if (mBorderInsideColor != defaultColor
				&& mBorderOutsideColor == defaultColor) {// 閿熸枻鎷烽敓钘夌敾涓�閿熸枻鎷烽敓绔尅鎷�

			radius = (defaultWidth < defaultHeight ? defaultWidth
					: defaultHeight) / 2 - mBorderThickness;

			drawCircleBorder(canvas, radius + mBorderThickness / 2,
					mBorderInsideColor);

		} else if (mBorderInsideColor == defaultColor
				&& mBorderOutsideColor != defaultColor) {// 閿熸枻鎷烽敓钘夌敾涓�閿熸枻鎷烽敓绔尅鎷�

			radius = (defaultWidth < defaultHeight ? defaultWidth
					: defaultHeight) / 2 - mBorderThickness;

			drawCircleBorder(canvas, radius + mBorderThickness / 2,
					mBorderOutsideColor);

		} else {// 娌￠敓鍙竟鍖℃嫹

			radius = (defaultWidth < defaultHeight ? defaultWidth
					: defaultHeight) / 2;

		}

		Bitmap roundBitmap = getCroppedRoundBitmap(bitmap, radius);

		canvas.drawBitmap(roundBitmap, defaultWidth / 2 - radius, defaultHeight
				/ 2 - radius, null);

	}

	/**
	 * 
	 * 閿熸枻鎷峰彇閿熺煫纭锋嫹閿熸枻鎷烽敓鐨嗚鎷烽敓閰佃锟�
	 * 
	 * @param radius閿熻寰�
	 */

	public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {

		Bitmap scaledSrcBmp;

		int diameter = radius * 2;

		// 涓洪敓鍓垮嚖鎷锋閿熸枻鎷峰崯閿熸枻鎷烽敓楗猴綇鎷烽敓鏂ゆ嫹閿熺殕璇ф嫹閿熼叺璁☆剨鎷烽敓鏂ゆ嫹鍗遍敓鏂ゆ嫹閿熷壙鏂ゆ嫹鍙栭敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熷彨杈炬嫹閿熸枻鎷烽敓鍙》鎷蜂綅閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷峰浘鐗�

		int bmpWidth = bmp.getWidth();

		int bmpHeight = bmp.getHeight();

		int squareWidth = 0, squareHeight = 0;

		int x = 0, y = 0;

		Bitmap squareBitmap;

		if (bmpHeight > bmpWidth) {// 閿熺杈炬嫹閿熻妭鍖℃嫹

			squareWidth = squareHeight = bmpWidth;

			x = 0;

			y = (bmpHeight - bmpWidth) / 2;

			// 閿熸枻鎷峰彇閿熸枻鎷烽敓鏂ゆ嫹鍥剧墖

			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
					squareHeight);

		} else if (bmpHeight < bmpWidth) {// 閿熸枻鎷烽敓鏂ゆ嫹璇熼敓锟�

			squareWidth = squareHeight = bmpHeight;

			x = (bmpWidth - bmpHeight) / 2;

			y = 0;

			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
					squareHeight);

		} else {

			squareBitmap = bmp;

		}

		if (squareBitmap.getWidth() != diameter
				|| squareBitmap.getHeight() != diameter) {

			scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,
					diameter, true);

		} else {

			scaledSrcBmp = squareBitmap;

		}

		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),

		scaledSrcBmp.getHeight(),

		Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();

		Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),
				scaledSrcBmp.getHeight());

		paint.setAntiAlias(true);

		paint.setFilterBitmap(true);

		paint.setDither(true);

		canvas.drawARGB(0, 0, 0, 0);

		canvas.drawCircle(scaledSrcBmp.getWidth() / 2,

		scaledSrcBmp.getHeight() / 2,

		scaledSrcBmp.getWidth() / 2,

		paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);

		bmp = null;

		squareBitmap = null;

		scaledSrcBmp = null;

		return output;

	}

	/**
	 * 
	 * 閿熸枻鎷风紭閿熸枻鎷峰渾
	 */

	private void drawCircleBorder(Canvas canvas, int radius, int color) {

		Paint paint = new Paint();

		/* 鍘婚敓鏂ゆ嫹閿燂拷 */

		paint.setAntiAlias(true);

		paint.setFilterBitmap(true);

		paint.setDither(true);

		paint.setColor(color);

		/* 閿熸枻鎷烽敓鏂ゆ嫹paint閿熶茎鈽呮嫹style閿熸枻鎷蜂负STROKE閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷� */

		paint.setStyle(Paint.Style.STROKE);

		/* 閿熸枻鎷烽敓鏂ゆ嫹paint閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓锟� */

		paint.setStrokeWidth(mBorderThickness);

		canvas.drawCircle(defaultWidth / 2, defaultHeight / 2, radius, paint);

	}

}