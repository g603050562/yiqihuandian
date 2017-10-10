package com.example.fullenergy.wxapi;


import com.example.fullenergy.R;
import com.example.fullenergy.main.PanelShopPayZhiFuBao;
import com.example.fullenergy.pub.weixin.Constants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		if(resp.errCode == 0){
			Toast.makeText(getApplicationContext(),"支付成功！",Toast.LENGTH_LONG).show();
			this.finish();
		}else if(resp.errCode == -1){
			System.out.println(resp.errStr);
			Toast.makeText(getApplicationContext(),"订单创建失败，请重试！",Toast.LENGTH_LONG).show();
			this.finish();
		}else if(resp.errCode == -2){
			Toast.makeText(getApplicationContext(),"取消支付！",Toast.LENGTH_LONG).show();
			this.finish();
		}
//		PanelShopPayZhiFuBao.panelShopPayWeiXinReturnHandler.sendMessage(new Message());
	}
}