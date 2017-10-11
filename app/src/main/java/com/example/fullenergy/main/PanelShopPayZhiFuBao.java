package com.example.fullenergy.main;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alipay.sdk.app.PayTask;
import com.example.fullenergy.pub.ProgressDialog;
import com.example.fullenergy.pub.zhifubao.H5PayDemoActivity;
import com.example.fullenergy.pub.zhifubao.PayResult;
import com.example.fullenergy.pub.zhifubao.SignUtils;
import com.example.fullenergy.wxapi.WXEntryActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;
import com.example.fullenergy.extend_plug.StatusBar.StatusBar;
import com.example.fullenergy.pub.PubFunction;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class PanelShopPayZhiFuBao extends FragmentActivity implements OnClickListener{

	private TextView product_subject, product_price,user_name,user_address,user_phone,agreement;
	private LinearLayout panelShopPayZhiFuBaoReturn,zhifubao_edit_userinfo,userPanel,pay_type_zhifubao,pay_type_weixin;
	private ImageView pay_type_zhifubao_img,pay_type_weixin_img;
	public static Handler panelShopPayZhiFuBaoSuccessHandler, panelShopPayZhiFuBaoErrorHandler,
			panelShopPayZhiFuBaoUnknownHandler, panelShopPayZhiFuBaoGetAddressSuccessHandler,
			panelShopPayZhiFuBaoGetAddressErrorHandler,turnToLogin,mHandler,panelShopPayWeiXinSuccessHandler,panelShopPayWeiXinReturnHandler;
	private HttpPanelShopPayZhiFuBao th;
	private HttpPanelShopPayZhiFuBaoGetAddress th2;
	private HttpPanelShopPayWeixin th3;
	private SharedPreferences preferences;
	// 商户PID
	public static String PARTNER = "";
	// 商户收款账号
	public static String SELLER = "";
	// 商户私钥，pkcs8格式
	public static String RSA_PRIVATE = "";
	// 支付宝公钥
	public static String RSA_PUBLIC = "";
	private static final int SDK_PAY_FLAG = 1;
	private static final int SDK_CHECK_FLAG = 2;

	private Button button;
	private TextView payButton;
	private String name,price,content,not_address,notify_url,id,trade_sn;
	private int not_address_int;
	private String id_2 = "-1";
	private Activity mActivity;
	private ProgressDialog progressDialog;

	private int pay_type = 0;
	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_main);
		mActivity = this;

		init();
		handler();
		main();
	}

	private void main() {
		th2 = new HttpPanelShopPayZhiFuBaoGetAddress(preferences, PanelShopPayZhiFuBao.this);
		th2.start();
		progressDialog.show();
	}

	private void init() {

		//注册简易数据库
		preferences = getApplication().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);

		new StatusBar(this);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0x00000000);
		progressDialog = new ProgressDialog(this);

		Intent intent = getIntent();
		name = intent.getStringExtra("name");
		price = intent.getStringExtra("price");
		content = intent.getStringExtra("content");
		not_address = intent.getStringExtra("not_address");
		id = intent.getStringExtra("id");

		button = (Button) this.findViewById(R.id.h5pay);
		button.setOnClickListener(this);
		payButton = (TextView) this.findViewById(R.id.pay);
		payButton.setOnClickListener(this);
		panelShopPayZhiFuBaoReturn = (LinearLayout) this.findViewById(R.id.panelShopPayZhiFuBaoReturn);
		panelShopPayZhiFuBaoReturn.setOnClickListener(this);

		product_subject = (TextView) this.findViewById(R.id.product_subject);
		product_subject.setText(name.toString());
		product_price = (TextView) this.findViewById(R.id.product_price);
		product_price.setText(price.toString());
		user_name = (TextView) this.findViewById(R.id.user_name);
		user_address = (TextView) this.findViewById(R.id.user_address);
		user_phone = (TextView) this.findViewById(R.id.user_phone);

		zhifubao_edit_userinfo = (LinearLayout) this.findViewById(R.id.zhifubao_edit_userinfo);
		zhifubao_edit_userinfo.setOnClickListener(this);
		userPanel = (LinearLayout) this.findViewById(R.id.user_panel);
		not_address_int = Integer.parseInt(not_address);
		if (not_address_int == 1) {
			userPanel.setVisibility(View.GONE);
		}else{
			userPanel.setVisibility(View.VISIBLE);
		}

		pay_type_zhifubao = (LinearLayout) this.findViewById(R.id.pay_type_zhifubao);
		pay_type_zhifubao.setOnClickListener(this);
		pay_type_weixin = (LinearLayout) this.findViewById(R.id.pay_type_weixin);
		pay_type_weixin.setOnClickListener(this);
		pay_type_zhifubao_img = (ImageView) this.findViewById(R.id.pay_type_zhifubao_img);
		pay_type_weixin_img = (ImageView) this.findViewById(R.id.pay_type_weixin_img);

		agreement = (TextView) this.findViewById(R.id.agreement);
		agreement.setOnClickListener(this);
	}



	@Override
	public void onClick(View v) {
		if(v.getId() == panelShopPayZhiFuBaoReturn.getId()){
			PanelShopPayZhiFuBao.this.finish();
		}else if(v.getId() == button.getId()){
			button_h5Pay();
		}else if(payButton.getId() == v.getId()){
			if(pay_type == 0){
				if (PubFunction.isNetworkAvailable(PanelShopPayZhiFuBao.this)) {
					if (not_address_int == 1) {
						th = new HttpPanelShopPayZhiFuBao(preferences, not_address_int, id);
					}else{
						th = new HttpPanelShopPayZhiFuBao(preferences, not_address_int, id,id_2);
					}
					th.start();
				} else {
					Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}else{
				if (PubFunction.isNetworkAvailable(PanelShopPayZhiFuBao.this)) {
					if (not_address_int == 1) {
						th3 = new HttpPanelShopPayWeixin(preferences, not_address_int, id);
						progressDialog.show();
					}else{
						th3 = new HttpPanelShopPayWeixin(preferences, not_address_int, id,id_2);
						progressDialog.show();
					}
					th3.start();
				} else {
					Toast.makeText(getApplicationContext(), "没有网络服务，请先检查网络是否开启！", Toast.LENGTH_SHORT).show();
				}
			}
//				Intent intent = new Intent(this,WXEntryActivity.class);
//				startActivity(intent);
//				overridePendingTransition(R.anim.in_right, R.anim.out_left);
		}else if(zhifubao_edit_userinfo.getId() == v.getId()){
			Intent intent = new Intent(this,PanelShopPayZhiFuBaoEditAddress.class);
			startActivity(intent);
		}else if(pay_type_zhifubao.getId() == v.getId()){
			pay_type_zhifubao_img.setImageResource(R.drawable.zf3);
			pay_type_weixin_img.setImageResource(R.drawable.zf4);
			pay_type = 0;
		}else if(pay_type_weixin.getId() == v.getId()){
			pay_type_zhifubao_img.setImageResource(R.drawable.zf4);
			pay_type_weixin_img.setImageResource(R.drawable.zf3);
			pay_type = 1;
		}else if(agreement.getId() == v.getId()){
			Intent intent = new Intent(this,RentBarAgreement_.class);
			intent.putExtra("type","other");
			this.startActivity(intent);
		}
	}

	private void handler() {
		panelShopPayZhiFuBaoSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th.getResult();
				try {
					price = jsonObject.getString("price");
					trade_sn = jsonObject.getString("trade_sn");
					PARTNER = jsonObject.getString("partner");
					SELLER = jsonObject.getString("username");
					RSA_PRIVATE = jsonObject.getString("private_key_values");
					notify_url = jsonObject.getString("notify_url");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// System.out.println(jsonObject);
				pay();
				//
			}
		};

		panelShopPayWeiXinSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				progressDialog.dismiss();

				//微信注册
				api = WXAPIFactory.createWXAPI(getApplication(), "wx1786435e083180b4");

				JSONObject json = th3.getResult();
				try {
					if(null != json && !json.has("retcode") ){
						PayReq req = new PayReq();
						req.appId			= json.getString("appid");
						req.partnerId		= json.getString("partnerid");
						req.prepayId		= json.getString("prepayid");
						req.packageValue	=  json.getString("package");
						req.nonceStr		= json.getString("noncestr");
						req.timeStamp		=  json.getString("timestamp");
						req.sign			= json.getString("sign");
						req.extData = "app data"; // optional
//						Toast.makeText(getApplication(), "正常调起支付", Toast.LENGTH_SHORT).show();
						// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
						api.sendReq(req);

					}else{
						Log.d("PAY_GET", "返回错误" + json.getString("retmsg"));
						Toast.makeText(getApplication(),"返回错误"+json.getString("retmsg"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		panelShopPayZhiFuBaoErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		};
		panelShopPayZhiFuBaoUnknownHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};
		panelShopPayZhiFuBaoGetAddressErrorHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String message = msg.getData().getString("message");
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
			}
		};

		turnToLogin = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SharedPreferences preferences = mActivity.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("usrename", null);
				editor.putString("password", null);
				editor.putString("jy_password", null);
				editor.putString("PHPSESSID", null);
				editor.putString("api_userid", null);
				editor.putString("api_username", null);
				editor.commit();
				Intent intent = new Intent(mActivity, Login_.class);
				intent.putExtra("type", "1");
				mActivity.startActivity(intent);
				mActivity.finish();
			}
		};

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case SDK_PAY_FLAG: {
						PayResult payResult = new PayResult((String) msg.obj);
						/**
						 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
						 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
						 * docType=1) 建议商户依赖异步通知
						 */
						String resultInfo = payResult.getResult();// 同步返回需要验证的信息

						String resultStatus = payResult.getResultStatus();
						// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
						if (TextUtils.equals(resultStatus, "9000")) {
							Toast.makeText(PanelShopPayZhiFuBao.this, "支付成功", Toast.LENGTH_SHORT).show();
							PanelShopPayZhiFuBao.this.finish();
						} else {
							// 判断resultStatus 为非"9000"则代表可能支付失败
							// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
							if (TextUtils.equals(resultStatus, "8000")) {
								Toast.makeText(PanelShopPayZhiFuBao.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

							} else {
								// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误

								Toast.makeText(PanelShopPayZhiFuBao.this,"支付失败", Toast.LENGTH_SHORT).show();
							}
						}
						break;
					}
					case SDK_CHECK_FLAG: {
						Toast.makeText(PanelShopPayZhiFuBao.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
						break;
					}
					default:
						break;
				}
			};
		};

		panelShopPayZhiFuBaoGetAddressSuccessHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				JSONObject jsonObject = th2.getResult();
				System.out.println(jsonObject.toString());
				try {
					id_2 = jsonObject.getString("id");
					user_name.setText(jsonObject.getString("name"));
					user_address.setText(jsonObject.getString("address"));
					user_phone.setText(jsonObject.getString("mobile"));
					progressDialog.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
		};

		panelShopPayWeiXinReturnHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				progressDialog.dismiss();
			}
		};
	}

	/**
	 * call alipay sdk pay. 调用SDK支付
	 *
	 */
	public void pay() {
		if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE) || TextUtils.isEmpty(SELLER)) {
			new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i) {
							//
							finish();
						}
					}).show();
			return;
		}
		String orderInfo = getOrderInfo(name, price, price);

		/**
		 * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
		 */
		String sign = sign(orderInfo);
		try {
			/**
			 * 仅需对sign 做URL编码
			 */
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		/**
		 * 完整的符合支付宝参数规范的订单信息
		 */
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(PanelShopPayZhiFuBao.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo, true);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * check whether the device has authentication alipay account.
	 * 查询终端设备是否存在支付宝认证账户
	 *
	 */
	public void check(View v) {
		Runnable checkRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask payTask = new PayTask(PanelShopPayZhiFuBao.this);
				// 调用查询接口，获取查询结果
				boolean isExist = payTask.checkAccountIfExist();

				Message msg = new Message();
				msg.what = SDK_CHECK_FLAG;
				msg.obj = isExist;
				mHandler.sendMessage(msg);
			}
		};

		Thread checkThread = new Thread(checkRunnable);
		checkThread.start();

	}

	/**
	 * get the sdk version. 获取SDK版本号
	 *
	 */
	public void getSDKVersion() {
		PayTask payTask = new PayTask(this);
		String version = payTask.getVersion();
		Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 原生的H5（手机网页版支付切natvie支付） 【对应页面网页支付按钮】
	 *
	 * @param
	 */
	public void button_h5Pay() {
		Intent intent = new Intent(this, H5PayDemoActivity.class);
		Bundle extras = new Bundle();
		/**
		 * url是测试的网站，在app内部打开页面是基于webview打开的，demo中的webview是H5PayDemoActivity，
		 * demo中拦截url进行支付的逻辑是在H5PayDemoActivity中shouldOverrideUrlLoading方法实现，
		 * 商户可以根据自己的需求来实现
		 */
		String url = "https://mapi.alipay.com";
		// url可以是一号店或者美团等第三方的购物wap站点，在该网站的支付过程中，支付宝sdk完成拦截支付
		extras.putString("url", url);
		intent.putExtras(extras);
		startActivity(intent);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * create the order info. 创建订单信息
	 *
	 */
	private String getOrderInfo(String subject, String body, String price) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + trade_sn + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" +  notify_url + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}

	/**
	 * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 *
	 */
	private String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 *
	 * @param content
	 *            待签名订单信息
	 */
	private String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 *
	 */
	private String getSignType() {
		return "sign_type=\"RSA\"";
	}

}

class HttpPanelShopPayZhiFuBao extends Thread {

	private SharedPreferences preferences;
	private int type;
	private String goods_id, address_id;
	private JSONObject jsonObject;

	public HttpPanelShopPayZhiFuBao(SharedPreferences preferences, int type, String goods_id, String address_id) {
		this.preferences = preferences;
		this.type = type;
		this.goods_id = goods_id;
		this.address_id = address_id;
	}

	public HttpPanelShopPayZhiFuBao(SharedPreferences preferences, int type, String goods_id) {
		this.preferences = preferences;
		this.type = type;
		this.goods_id = goods_id;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);

		HttpPost httpPost;
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		if (type == 1) {
			String path = PubFunction.www + "api.php/shop/go_pay";
			httpPost = new HttpPost(path);
			list.add(new BasicNameValuePair("id", goods_id));
		} else {
			String path = PubFunction.www + "api.php/shop/go_pay_address";
			httpPost = new HttpPost(path);
			list.add(new BasicNameValuePair("address_id", address_id));
			list.add(new BasicNameValuePair("goods_id", goods_id));
		}
		httpPost.setHeader("Cookie","PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());

				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				this.jsonObject = jsonObject.getJSONObject("data");
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageStr);
				message.setData(bundle);
				if (code.equals("200")) {
					if (messageStr.equals("秘钥不正确,请重新登录")) {
						PanelShopPayZhiFuBao.turnToLogin.sendMessage(new Message());
					} else {
						PanelShopPayZhiFuBao.panelShopPayZhiFuBaoErrorHandler.sendMessage(message);
					}
				} else if (code.equals("100")) {
					PanelShopPayZhiFuBao.panelShopPayZhiFuBaoSuccessHandler.sendMessage(message);
				} else {
					PanelShopPayZhiFuBao.panelShopPayZhiFuBaoUnknownHandler.sendMessage(new Message());
				}
			} else {
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			PanelShopPayZhiFuBao.panelShopPayZhiFuBaoUnknownHandler.sendMessage(new Message());
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}

class HttpPanelShopPayZhiFuBaoGetAddress extends Thread {

	private SharedPreferences preferences;
	private JSONObject jsonObject;
	private Activity activity;

	public HttpPanelShopPayZhiFuBaoGetAddress(SharedPreferences preferences, Activity activity) {
		this.preferences = preferences;
		this.activity = activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);
		String path = PubFunction.www + "api.php/member/my_address";
		HttpPost httpPost = new HttpPost(path);
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());

				System.out.println(result.toString());

				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");
				this.jsonObject = jsonObject.getJSONObject("data");
				if (code.equals("200")) {
					if (messageStr.equals("秘钥不正确,请重新登录")) {
						PanelShopPayZhiFuBao.turnToLogin.sendMessage(new Message());
					} else {
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("message", messageStr);
						message.setData(bundle);
						PanelShopPayZhiFuBao.panelShopPayZhiFuBaoGetAddressErrorHandler.sendMessage(message);
					}
				} else if (code.equals("100")) {
					PanelShopPayZhiFuBao.panelShopPayZhiFuBaoGetAddressSuccessHandler.sendMessage(new Message());
				} else {
					PanelShopPayZhiFuBao.panelShopPayZhiFuBaoUnknownHandler.sendMessage(new Message());
				}
			}
		} catch (Exception e) {
			PanelShopPayZhiFuBao.panelShopPayZhiFuBaoUnknownHandler.sendMessage(new Message());
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}

class HttpPanelShopPayWeixin extends Thread {

	private SharedPreferences preferences;
	private int type;
	private String goods_id, address_id;
	private JSONObject jsonObject;

	public HttpPanelShopPayWeixin(SharedPreferences preferences, int type, String goods_id, String address_id) {
		this.preferences = preferences;
		this.type = type;
		this.goods_id = goods_id;
		this.address_id = address_id;
	}

	public HttpPanelShopPayWeixin(SharedPreferences preferences, int type, String goods_id) {
		this.preferences = preferences;
		this.type = type;
		this.goods_id = goods_id;
	}

	@Override
	public void run() {
		super.run();

		String PHPSESSID = preferences.getString("PHPSESSID", null);
		String api_userid = preferences.getString("api_userid", null);
		String api_username = preferences.getString("api_username", null);

		HttpPost httpPost;
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		if (type == 1) {
			String path = PubFunction.www + "api.php/shop/go_pay_weixin_yi";
			httpPost = new HttpPost(path);
			list.add(new BasicNameValuePair("id", goods_id));
		} else {
			String path = PubFunction.www + "api.php/shop/go_pay_address_weixin";
			httpPost = new HttpPost(path);
			list.add(new BasicNameValuePair("address_id", address_id));
			list.add(new BasicNameValuePair("goods_id", goods_id));
		}
		httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);


		try {
			HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());

				JSONTokener jsonTokener = new JSONTokener(result);
				JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

				System.out.println(jsonObject.toString());
				String messageStr = jsonObject.getString("message");
				String code = jsonObject.getString("code");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("message", messageStr);
				message.setData(bundle);

				if (code.equals("200")) {
					if (messageStr.equals("秘钥不正确,请重新登录")) {
						PanelShopPayZhiFuBao.turnToLogin.sendMessage(new Message());
					} else {
						PanelShopPayZhiFuBao.panelShopPayZhiFuBaoErrorHandler.sendMessage(message);
					}
				} else if (code.equals("100")) {
					this.jsonObject = jsonObject.getJSONObject("data");
					PanelShopPayZhiFuBao.panelShopPayWeiXinSuccessHandler.sendMessage(message);
				} else {
					PanelShopPayZhiFuBao.panelShopPayZhiFuBaoUnknownHandler.sendMessage(new Message());
				}
			} else {
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			PanelShopPayZhiFuBao.panelShopPayZhiFuBaoUnknownHandler.sendMessage(new Message());
		}
	}

	public JSONObject getResult() {
		return this.jsonObject;
	}
}

