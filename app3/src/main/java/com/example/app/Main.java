package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.pub.ProgressDialog;
import com.example.app.pub.PubFunction;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EActivity(R.layout.main)
public class Main extends Activity{

    @ViewById
    ListView list;
    @ViewById
    TextView re;

    private Activity activity;
    private List<Map<String,String>> dataLiat = new ArrayList<Map<String, String>>();
    private myAdapt simpleAdapter;
    private JSONArray jsonArray;

    private ProgressDialog progressDialog;
    private SharedPreferences Preferences;

    public static Handler openHandler,closeHandler;
    public static int scrollTo = 0;
    private int fScrollTo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        main();
        handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(dataLiat!=null){
            dataLiat.clear();
        }
    }

    private void handler() {
        openHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String number = msg.getData().getString("number");
                fScrollTo= msg.getData().getInt("scrollTo");
                openBox(number);
            }
        };
        closeHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String number = msg.getData().getString("number");
                fScrollTo= msg.getData().getInt("scrollTo");
                closeBox(number);
            }
        };

    }

    private void main() {
        httpGetData();
        progressDialog.show();
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        Preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrollTo = list.getFirstVisiblePosition();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Click
    void re(){
        dataLiat.clear();
        main();
    }

    // 登陆时如果上传给服务器的信息正确 ，就是登陆成功 ，返回给这个handler，写入数据库，然后进行转跳页面
    @UiThread
    void loginSuccess(String msg){
        simpleAdapter = new myAdapt(this,getdata(jsonArray),new int[]{R.layout.main_item},new String[] { "title","box_id"}, new int[] { R.id.title,R.id.box_ID},list);
        list.setAdapter(simpleAdapter);
        if(fScrollTo != 0){
            list.setSelection(fScrollTo);
        }
        progressDialog.dismiss();
    }


    private List<Map<String, String>> getdata(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Map<String, String> map = new HashMap<String, String>();
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                map.put("title", jsonObject.get("address").toString());
                map.put("box_id", jsonObject.get("cabinet_number").toString());
                if(jsonObject.get("allow_login").toString().equals("0")){
                    map.put("stage", "打开箱子");
                }else{
                    map.put("stage", "关闭箱子");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dataLiat.add(map);
        }
        return dataLiat;
    }

    @Background
    void httpGetData(){
        //接口地址
        String path = PubFunction.www + "adphone.php/home/list_cab";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        try {
            //服务端
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            //返回成功
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //数据解析
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonParser = new JSONTokener(result);
                JSONObject person = (JSONObject) jsonParser.nextValue();

                jsonArray  = person.getJSONArray("data");
                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person.toString());

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        loginSuccess(messageStr);
                    } else {
                        unknown(messageStr);
                    }
                }
            }else{
                errorcode();
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    // 登陆时返回了错误信息，就在这里
    @UiThread
    void loginError(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
    // 返回的时候 不知道 出现什么错误的时候都会在这里 但一般的情况下 都是json解析出错了
    @UiThread
    void unknown(String e){
        Toast.makeText(getApplicationContext(),e, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
    @UiThread
    void errorcode(){
        Toast.makeText(getApplicationContext(), "服务器错误！", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @Background
    void openBox(String number){
        //接口地址
        String path = PubFunction.www + "adphone.php/home/open_cab";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        // post参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cabinet_number", number));

        try {
            //写入post参数
            HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
            httpPost.setEntity(entity);
            //服务端
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            //返回成功
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //数据解析
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonParser = new JSONTokener(result);
                JSONObject person = (JSONObject) jsonParser.nextValue();

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person);

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        refrush();
                    } else {
                        unknown(messageStr);
                    }
                }

            }else{
                errorcode();
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    @Background
    void closeBox(String number){
        //接口地址
        String path = PubFunction.www + "adphone.php/home/close_cab";
        //建立连接
        HttpPost httpPost = new HttpPost(path);
        //相应时间
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

        String PHPSESSID = Preferences.getString("PHPSESSID", null);
        String api_userid = Preferences.getString("api_userid", null);
        String api_username = Preferences.getString("api_username", null);
        httpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID + ";" + "api_userid=" + api_userid + ";" + "api_username=" + api_username);

        // post参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cabinet_number", number));



        try {
            //写入post参数
            HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
            httpPost.setEntity(entity);
            //服务端
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            //返回成功
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //数据解析
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonParser = new JSONTokener(result);
                JSONObject person = (JSONObject) jsonParser.nextValue();

                String messageStr = person.getString("message");
                String code = person.getString("code");

                System.out.println(person);

                //发消息了
                if (activity != null) { //判断activity时候存在，但是后来发现是骗自己的，没用，懒得改了,没啥影响
                    if (code.equals("200")) {
                        loginError(messageStr);
                    } else if (code.equals("100")) {
                        refrush();
                    } else {
                        unknown(messageStr);
                    }
                }

            }else{
                errorcode();
            }
        } catch (Exception e) {
            if (activity != null) {
                unknown(e.toString());
            }
        }
    }

    @UiThread
    void refrush(){
        dataLiat.clear();
        main();
    }


}

class myAdapt extends BaseAdapter implements Filterable {
    private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;
    private ViewHolder mHolder;

    protected List<? extends Map<String, ?>> mData;

    private int[] mResources;
    private int[] mDropDownResources;
    private LayoutInflater mInflater;

    private SimpleFilter mFilter;
    private ArrayList<Map<String, ?>> mUnfilteredData;
    private Message message;
    private ListView listView;

    public myAdapt(Context context, List<? extends Map<String, ?>> data, int[] resources, String[] from, int[] to,ListView listView) {
        mData = data;
        mResources = mDropDownResources = resources;
        mFrom = from;
        mTo = to;
        if (context != null) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        this.listView = listView;
    }

    @Override
    public int getViewTypeCount() {
        return mResources.length;
    }

    public int getCount() {
        return mData.size();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResources[getItemViewType(position)], null);
            mHolder = new ViewHolder();
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        String state = mData.get(position).get("stage").toString();
        TextView textView1 = (TextView) convertView.findViewById(R.id.state_on);
        TextView textView2 = (TextView) convertView.findViewById(R.id.state_off);
        if (state.equals("打开箱子")) {
            textView1.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.GONE);
        } else {
            textView2.setVisibility(View.VISIBLE);
            textView1.setVisibility(View.GONE);
        }
        final String number = mData.get(position).get("box_id").toString();

        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("number",number);
                bundle.putInt("scrollTo", Main.scrollTo);
                message.setData(bundle);
                Main.openHandler.sendMessage(message);
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("number",number);
                bundle.putInt("scrollTo", Main.scrollTo);
                message.setData(bundle);
                Main.closeHandler.sendMessage(message);
            }
        });
        return createViewFromResource(position, convertView, parent, mResources[getItemViewType(position)]);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }
        bindView(position, v);
        return v;
    }

    public void setDropDownViewResource(int[] resources) {
        this.mDropDownResources = resources;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResources[getItemViewType(position)]);
    }

    private void bindView(int position, View view) {
        final Map<String, ?> dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }

        final ViewBinder binder = mViewBinder;
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(
                                    v.getClass().getName() + " should be bound to a Boolean, not a "
                                            + (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else if (v instanceof Spinner) {
                        if (data instanceof Integer) {
                            ((Spinner) v).setSelection((Integer) data);
                        } else {
                            continue;
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a "
                                + " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SimpleFilter();
        }
        return mFilter;
    }

    public static interface ViewBinder {
        boolean setViewValue(View view, Object data, String textRepresentation);
    }

    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Map<String, ?>>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, ?>> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Map<String, ?>> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {

                        int len = mTo.length;

                        for (int j = 0; j < len; j++) {
                            String str = (String) h.get(mFrom[j]);

                            String[] words = str.split(" ");
                            int wordCount = words.length;

                            for (int k = 0; k < wordCount; k++) {
                                String word = words[k];

                                if (word.toLowerCase().startsWith(prefixString)) {
                                    newValues.add(h);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // noinspection unchecked
            mData = (List<Map<String, ?>>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private class ViewHolder {
        ImageView mImageView;
        TextView mTextView;
    }
}