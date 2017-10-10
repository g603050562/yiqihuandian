package com.example.hasee.app6;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hasee.app6.RecyclableImageView.RecyclableImageView;


public class ProgressDialog {
	private Dialog progressDialog;
	private AnimationDrawable animationDrawable;
	private Context context;

	public ProgressDialog(Context context) {
		this.context = context;
		init();

	}

	private void init(){
		progressDialog = new Dialog(context, R.style.progress_dialog);
		progressDialog.setContentView(R.layout.dialog);
		progressDialog.setCancelable(true);
		progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
		msg.setText("请稍候...");
		RecyclableImageView bar = (RecyclableImageView) progressDialog.findViewById(R.id.loadingPrigressBar);
		bar.setImageResource(R.drawable.progress_drawable_white);
		animationDrawable = (AnimationDrawable)bar.getDrawable();
	}

	public void show(){
		if(progressDialog==null){

			init();
		}
		animationDrawable.start();
		progressDialog.show();
	}

	public void dismiss(){
		animationDrawable.stop();
		progressDialog.dismiss();
	}

	public void destory(){
		progressDialog = null;
		animationDrawable = null;
	}
}
