package com.example.fullenergystore.extend_plug.RecyclableImageView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RecyclableImageView extends ImageView {

	public RecyclableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RecyclableImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public RecyclableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		setImageDrawable(null);
	}
}