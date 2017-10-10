package com.example.fullenergy.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.fullenergy.R;
import com.example.fullenergy.pub.PubFunction;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigDecimal;

/**
 * Created by apple on 2017/8/15.
 */

@EFragment(R.layout.green_main_panel)
public class GreenMainPanel extends Fragment {

    private View view;

    @ViewById
    FrameLayout main_image_green;
    @ViewById
    ImageView setlocal , service;
    @ViewById
    TextView text1,text2,text3,text4,text5;
    @ViewById
    LinearLayout navigation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.green_main_panel,container,false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @AfterViews
    void afterViews(){
        init();
    }

    private void init() {
        main_image_green.setVisibility(View.INVISIBLE);
    }

    @Click
    void setlocal(){
        GreenMainMap_.setLocalHandler.sendMessage(new Message());
    }

    @Click
    void service(){
        getActivity().startActivity(new Intent(getActivity(),GreenService_.class));
    }

    @Click
    void navigation(){
        getActivity().startActivity(new Intent(getActivity(),GreenNavigation.class));
    }


    @UiThread
    void show(String data){

        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            String address = jsonObject.getString("address");
            String count = jsonObject.getString("A_surplus");
            String companyname = jsonObject.getString("companyname");
            String mobile = jsonObject.getString("mobile");

            LatLng start = new LatLng(PubFunction.local[0], PubFunction.local[1]);
            LatLng end = new LatLng(PubFunction.marker[0], PubFunction.marker[1]);
            float distance = AMapUtils.calculateLineDistance(start, end);
            distance = distance / 1000;
            BigDecimal b = new BigDecimal(distance);
            double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            text2.setText(f1 + "");
            text1.setText(count);
            text3.setText(companyname);
            text4.setText(mobile);
            text5.setText(address);

            System.out.println(jsonObject.toString());

        }catch (Exception e){
            System.out.println(e.toString());
        }

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(main_image_green, "alpha", 0f, 1.0f);
        alphaAnim.setRepeatMode(ObjectAnimator.REVERSE);
        alphaAnim.setDuration(500);
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                main_image_green.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        alphaAnim.start();
    }

    @UiThread
    void reShow(final String data){

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(main_image_green, "alpha", 1.0f, 0f);
        alphaAnim.setRepeatMode(ObjectAnimator.REVERSE);
        alphaAnim.setDuration(500);
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                try {
                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                    String address = jsonObject.getString("address");
                    String count = jsonObject.getString("A_surplus");
                    String companyname = jsonObject.getString("companyname");
                    String mobile = jsonObject.getString("mobile");

                    LatLng start = new LatLng(PubFunction.local[0], PubFunction.local[1]);
                    LatLng end = new LatLng(PubFunction.marker[0], PubFunction.marker[1]);
                    float distance = AMapUtils.calculateLineDistance(start, end);
                    distance = distance / 1000;
                    BigDecimal b = new BigDecimal(distance);
                    double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    text2.setText(f1 + "");
                    text1.setText(count);
                    text3.setText(companyname);
                    text4.setText(mobile);
                    text5.setText(address);

                    System.out.println(jsonObject.toString());

                }catch (Exception e){
                    System.out.println(e.toString());
                }

                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(main_image_green, "alpha", 0f, 1.0f);
                alphaAnim.setRepeatMode(ObjectAnimator.REVERSE);
                alphaAnim.setDuration(500);
                alphaAnim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        main_image_green.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                alphaAnim.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        alphaAnim.start();
    }

    @UiThread
    void dismiss(){
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(main_image_green, "alpha", 1.0f, 0f);
        alphaAnim.setRepeatMode(ObjectAnimator.REVERSE);
        alphaAnim.setDuration(500);
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                main_image_green.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        alphaAnim.start();
    }


}

