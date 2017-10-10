package com.example.fullenergystore.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.example.fullenergystore.R;
import com.example.fullenergystore.extend_plug.StatusBar.statusBar;
import com.readystatesoftware.systembartint.SystemBarTintManager;


public class LoginInfo extends Activity implements View.OnClickListener{

	LinearLayout page_return;
	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_info);
		init();
	}

	private void init() {

		page_return = (LinearLayout) this.findViewById(R.id.page_return);
		page_return.setOnClickListener(this);
		webview = (WebView) this.findViewById(R.id.webview);

		new statusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0x00000000);

		webview.loadUrl("http://www.huandianwang.com/page/xieyi_sj.html");
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				//返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});

	}

	@Override
	public void onClick(View v) {
		if(page_return.getId() == v.getId()){
			this.finish();
		}
	}
}
