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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.PubFunction;

public class PanelMinePassword extends Fragment implements OnClickListener{

	private View view;
	private LinearLayout panelMinePasswordReturn;
	private EditText panelMinePasswordOld,panelMinePasswordNew,panelMinePasswordNewRe;
	private TextView panelMinePasswordSubmit;
	private SharedPreferences preferences;
	private HttpPanelMinePassword th;
	public static Handler panelMinePasswordSuccessHandler,panelMinePasswordErrorHandler,panelMinePasswordUnknownHandler,turnToLogin;
	private InputMethodManager manager = null;
	private ProgressDialog progressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_mine_password,container,false);
		
		init();
		handler();
		main();
		
		return view;
	}
	
	private void handler() {
		panelMinePasswordSuccessHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "密码设置成功！", Toast.LENGTH_SHORT).show();
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
		panelMinePasswordErrorHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
				panelMinePasswordOld.setText("");
				panelMinePasswordNew.setText("");
				panelMinePasswordNewRe.setText("");
				progressDialog.dismiss();
			}
		};
		panelMinePasswordUnknownHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getActivity(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				panelMinePasswordOld.setText("");
				panelMinePasswordNew.setText("");
				panelMinePasswordNewRe.setText("");
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
			}
		};
	}

	private void init(){
		preferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
		progressDialog = Panel.progressDialog;
		
		panelMinePasswordReturn = (LinearLayout) view.findViewById(R.id.panelMinePasswordReturn);
		panelMinePasswordReturn.setOnClickListener(this);
		panelMinePasswordOld = (EditText) view.findViewById(R.id.panelMinePasswordOld);
		panelMinePasswordNew = (EditText) view.findViewById(R.id.panelMinePasswordNew);
		panelMinePasswordNewRe = (EditText) view.findViewById(R.id.panelMinePasswordNewRe);
		panelMinePasswordSubmit = (TextView) view.findViewById(R.id.panelMinePasswordSubmit);
		panelMinePasswordSubmit.setOnClickListener(this);
		
		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);  
	}
	
	private void main(){
		
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == panelMinePasswordReturn.getId()){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			PanelMineSetUp setup = new PanelMineSetUp();
			fragmentTransaction.setCustomAnimations(R.anim.in_left,R.anim.out_right);
			fragmentTransaction.replace(R.id.panelMinePanel, setup);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			PubFunction.hideInput(getActivity(), view);
		}else if(panelMinePasswordSubmit.getId() == arg0.getId()){
			String oldPwd = "";
			String newPwd = "";
			String newPwdRe = "";
			oldPwd = panelMinePasswordOld.getText().toString().trim();
			newPwd = panelMinePasswordNew.getText().toString().trim();
			newPwdRe = panelMinePasswordNewRe.getText().toString().trim();
			if(oldPwd.equals("")||newPwd.equals("")||newPwdRe.equals("")){
				Toast.makeText(getActivity(), "信息不能为空！", Toast.LENGTH_SHORT).show();
			}else{
				if(newPwd.equals(newPwdRe)){
					if(PubFunction.isNetworkAvailable(getActivity())){
						th = new HttpPanelMinePassword(preferences, oldPwd,newPwd,this,getActivity());
						th.start();
						progressDialog.show();
					}else{
						Toast.makeText(getActivity(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(getActivity(), "新密码两次填写不相同！", Toast.LENGTH_SHORT).show();
					panelMinePasswordOld.setText("");
					panelMinePasswordNew.setText("");
					panelMinePasswordNewRe.setText("");
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
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}

class HttpPanelMinePassword extends Thread{
	
	private SharedPreferences preferences;
	private String oldPwd,newPwd;
	private Fragment fragment;
	private Activity activity;
	
	public HttpPanelMinePassword(SharedPreferences preferences,String oldPsw,String newPsw,Fragment fragment, Activity activity) {
		this.preferences = preferences;
		this.newPwd = newPsw;
		this.oldPwd = oldPsw;
		this.fragment = fragment;
		this.activity = activity;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		
		
		String path = PubFunction.www+"api.php/home/update_password";
		HttpPost httpPost = new HttpPost(path);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("old_password", oldPwd));
		list.add(new BasicNameValuePair("new_password", newPwd));
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(httpResponse.getEntity());
				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				
				if(fragment.getView() !=null){
					if(code.equals("200")){
						if (messageStr.equals("秘钥不正确,请重新登录")) {
							PanelMinePassword.turnToLogin.sendMessage(new Message());
						} else {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("message", messageStr);
							message.setData(bundle);
							PanelMinePassword.panelMinePasswordErrorHandler.sendMessage(message);
						}
					}else if(code.equals("100")){
						PanelMinePassword.panelMinePasswordSuccessHandler.sendMessage(new Message());
					}else{
						PanelMinePassword.panelMinePasswordUnknownHandler.sendMessage(new Message());
					}
				}
			}
		} catch (Exception e) {
			if(fragment.getView()!=null){
				PanelMinePassword.panelMinePasswordUnknownHandler.sendMessage(new Message());
			}
		}
	}
	
}
