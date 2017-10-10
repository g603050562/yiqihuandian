package com.example.fullenergy.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alipay.sdk.data.a;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMineFeedBack extends Fragment implements OnClickListener{

	private View view;
	private LinearLayout panelMineFeedbackReturn;
	private SharedPreferences preferences;
	private HttpPanelMineFeedBack th;
	private TextView panelMineFeedBackSubmit;
	private EditText panelMineFeedBackContent;
	public static Handler panelMineFeedBackSuccessHandler,panelMineFeedBackErrorHandler,panelMineFeedBackUnknownHandler,turnToLogin;

	private ProgressDialog progressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_mine_feedback,container,false);
		
		init();
		handler();
		main();

		return view;
	}

	private void handler() {
		panelMineFeedBackSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				PanelMineSetUp setup = new PanelMineSetUp();
				fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
				fragmentTransaction.replace(R.id.panelMinePanel, setup);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				PubFunction.hideInput(getActivity(), view);
				progressDialog.dismiss();
			}
		};
		panelMineFeedBackErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelMineFeedBackUnknownHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "出现未知错误!", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		turnToLogin = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = getActivity().getSharedPreferences("userInfo",Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(getActivity(), Login_.class);
				intent.putExtra("type", "1");
				getActivity().startActivity(intent);
				getActivity().finish();
				getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
				progressDialog.dismiss();
			}
		};
	}

	private void init(){
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		progressDialog = Panel.progressDialog;
		
		panelMineFeedbackReturn = (LinearLayout) view.findViewById(R.id.panelMineFeedbackReturn);
		panelMineFeedbackReturn.setOnClickListener(this);
		panelMineFeedBackSubmit = (TextView) view.findViewById(R.id.panelMineFeedBackSubmit);
		panelMineFeedBackSubmit.setOnClickListener(this);
		panelMineFeedBackContent = (EditText) view.findViewById(R.id.panelMineFeedBackContent);
	}
	
	private void main(){
		
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(panelMineFeedbackReturn.getId() == arg0.getId()){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineSetUp setup = new PanelMineSetUp();
			fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, setup);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			PubFunction.hideInput(getActivity(), view);
		}else if(panelMineFeedBackSubmit.getId() == arg0.getId()){
			String content = "";
			content = panelMineFeedBackContent.getText().toString().trim();
			if(content.equals("")){
				Toast.makeText(getActivity(), "内容不能为空!", Toast.LENGTH_SHORT).show();
			}else{
				if(PubFunction.isNetworkAvailable(getActivity())){
					th = new HttpPanelMineFeedBack(content, preferences,this,getActivity());
					th.start();
					progressDialog.show();
				}else{
					Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					PanelMineSetUp setup = new PanelMineSetUp();
					fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
					fragmentTransaction.replace(R.id.panelMinePanel, setup);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
					PubFunction.hideInput(getActivity(), view);
					return true;
				}
				return false;
			}
		});
	}
}

class HttpPanelMineFeedBack extends Thread{
	
	private String content;
	private SharedPreferences Preferences;
	private Fragment fragment;
	private Activity activity;
	
	public HttpPanelMineFeedBack(String content,SharedPreferences Preferences,Fragment fragment,Activity activity) {
		this.content = content;
		this.Preferences = Preferences;
		this.fragment = fragment;
		this.activity = activity;
	}
	
	@Override
	public void run() {
		super.run();
		
		String PHPSESSID = Preferences.getString("PHPSESSID", null);
		String api_userid = Preferences.getString("api_userid", null);
		String api_username = Preferences.getString("api_username", null);
		String path = PubFunction.www+"api.php/member/tousu";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("content", content));
		
		try {
			httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
			HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageString = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageString);
				message.setData(bundle);
				
				if(fragment.getView()!=null){
					if(code.equals("200")){
						if (messageString.equals("秘钥不正确,请重新登录")) {
							PanelMineFeedBack.turnToLogin.sendMessage(new Message());
						} else {
							PanelMineFeedBack.panelMineFeedBackErrorHandler.sendMessage(message);
						}
					}else if(code.equals("100")){
						PanelMineFeedBack.panelMineFeedBackSuccessHandler.sendMessage(message);
					}else{
						PanelMineFeedBack.panelMineFeedBackUnknownHandler.sendMessage(new Message());
					}
				}
			}
		}catch (Exception e) {
			if(fragment.getView()!=null){
				PanelMineFeedBack.panelMineFeedBackUnknownHandler.sendMessage(new Message());		
			}
		}
	}
}
