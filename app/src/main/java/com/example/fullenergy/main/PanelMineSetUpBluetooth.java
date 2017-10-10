package com.example.fullenergy.main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fullenergy.R;

public class PanelMineSetUpBluetooth extends Fragment implements OnClickListener {

    private String TAG = "BluetoothChat";
    private View view;
    private ImageView panelMineSetUpReturn;
    private ListView listview;
    private List<Map<String, String>> list;
    private List<BluetoothDevice> bluetoothDeviceList;
    private SimpleAdapter simpleadapter;
    private BluetoothAdapter adapter;
    private Handler goneHanderHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.panel_mine_setup_bluetooth, container, false);

        init();
        handler();
        main();

        return view;
    }

    private void init() {
        list = new ArrayList<Map<String, String>>();
        bluetoothDeviceList = new ArrayList<BluetoothDevice>();

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(intent);
            }

            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            for (int i = 0; i < devices.size(); i++) {
                BluetoothDevice device = (BluetoothDevice) devices.iterator().next();
                Map<String, String> map = new HashMap<String, String>();
                map.put("title", device.getName());
                map.put("content", device.getAddress());
                bluetoothDeviceList.add(device);
                list.add(map);
            }

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mReceiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getActivity().registerReceiver(mReceiver, filter);
            adapter.startDiscovery();

        } else {
            Toast.makeText(getActivity(), "没有蓝牙设备", Toast.LENGTH_SHORT).show();
        }

        panelMineSetUpReturn = (ImageView) view.findViewById(R.id.panelMineSetUpBluetoothReturn);
        panelMineSetUpReturn.setOnClickListener(this);
        View headerView = getActivity().getLayoutInflater().inflate(R.layout.panel_mine_setup_bluetooth_header, null);
        listview = (ListView) view.findViewById(R.id.bluetooth_list);
        listview.addHeaderView(headerView, null, false);

        simpleadapter = new SimpleAdapter(getActivity(), getdata(), R.layout.panel_mine_setup_bluetooth_item,
                new String[]{"title", "content"}, new int[]{R.id.title, R.id.content});
        listview.setAdapter(simpleadapter);
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(getActivity(), position + "aa", Toast.LENGTH_SHORT).show();
                try {
                    connect(bluetoothDeviceList.get(position - 1));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }

    private List<? extends Map<String, ?>> getdata() {
        return list;
    }

    private void handler() {
        goneHanderHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                View view = (View) listview.getChildAt(0);
                view.setVisibility(View.GONE);
                view.setPadding(0, -view.getHeight(), 0, 0);
            }
        };
    }

    private void main() {

    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (arg0.getId() == panelMineSetUpReturn.getId()) {
            PanelMineSetUp index = new PanelMineSetUp();
            fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
            fragmentTransaction.replace(R.id.panelMinePanel, index);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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
                    PanelMineSetUp index = new PanelMineSetUp();
                    fragmentTransaction.setCustomAnimations(R.anim.in_left, R.anim.out_right);
                    fragmentTransaction.replace(R.id.panelMinePanel, index);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println(action.toString() + "aaaaaaaaaaaaaaaaaaa");
            // 找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    int same = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).get("content").toString().trim().equals(device.getAddress().toString().trim())) {
                            same = 1;
                            break;
                        } else {
                            same = 0;
                        }
                    }

                    if (same == 0) {
                        if (list != null && simpleadapter != null) {
                            Map<String, String> map = new HashMap<String, String>();
                            if (device.getName() == null) {
                                map.put("title", "未知蓝牙设备!");
                            } else {
                                map.put("title", device.getName());
                            }
                            map.put("content", device.getAddress());
                            bluetoothDeviceList.add(device);
                            list.add(map);
                            simpleadapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            // 搜索完成
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getActivity(), "搜索完成", Toast.LENGTH_SHORT).show();
                if (goneHanderHandler != null) {
                    goneHanderHandler.sendMessage(new Message());
                }
            } else {
                return;
            }
        }
    };


    private void connect(BluetoothDevice device) throws IOException {
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
    }
}
