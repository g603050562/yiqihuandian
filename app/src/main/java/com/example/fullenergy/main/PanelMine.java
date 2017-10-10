package com.example.fullenergy.main;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fullenergy.R;
import com.example.fullenergy.pub.PubFunction;

public class PanelMine extends Fragment{

	private View view;
	public static Handler panelMineIndexInformationHeadimgHandler,panaelMineToIndexHandler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.panel_mine,container,false);
		
		PanelMineIndex panelMineIndex = new PanelMineIndex(1);
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.panelMinePanel, panelMineIndex);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		
		handler();
		
		return view;
	}
	
	private void handler(){
		panelMineIndexInformationHeadimgHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, 1);
			}
		};
		panaelMineToIndexHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				PanelMineIndex panelMineIndex = new PanelMineIndex(1);
				FragmentManager fragmentManager = getChildFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.panelMinePanel, panelMineIndex);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
			}
		};
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && null != data) {
        	Uri selectedImage = data.getData();
        	String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            PubFunction.bitmap = BitmapFactory.decodeFile(picturePath);
            PanelMineInformation.panelMineIndexInformationHeadimgHandler.sendMessage(new Message());
        }
  
    }
}
