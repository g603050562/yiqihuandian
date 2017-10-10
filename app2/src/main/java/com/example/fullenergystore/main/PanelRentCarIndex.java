package com.example.fullenergystore.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.fullenergystore.R;

public class PanelRentCarIndex extends Fragment implements View.OnClickListener{

    private View view;
    private LinearLayout setup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.panel_rentcar_index, container, false);
        init();

        return view;
    }

    private void init() {
        setup = (LinearLayout) view.findViewById(R.id.setup);
        setup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == setup.getId()){
            Intent intent = new Intent(getActivity(),SetUp.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
    }
}
