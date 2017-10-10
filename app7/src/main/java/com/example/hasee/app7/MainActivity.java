package com.example.hasee.app7;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener{

    private Activity activity;
    private GridView mGridView;
    private List<ResolveInfo> mApps;
    private List<Drawable> drawables = new ArrayList<Drawable>();
    private List<Map<String,String>> maps;

    private LinearLayout set_panel;
    private int setup_count = 0;
    private String set_pwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        main();
    }

    private void init() {
        activity = this;
        loadApps();
        mGridView = (GridView) findViewById(R.id.gridView1);
        set_panel = (LinearLayout) this.findViewById(R.id.set_panel);
        set_panel.setOnClickListener(this);
        maps = new ArrayList<Map<String, String>>();
    }

    private void main() {
        for (int i = 0; i < mApps.size(); i++) {
            Map<String, String> map = new HashMap<String, String>();
            PackageManager pm = this.getPackageManager();
            String str = mApps.get(i).activityInfo.applicationInfo.packageName;
            if (str.indexOf("hasee.app") != -1 || str.equals("android.providers.downloads.ui")) {
                System.out.println(str.toString());
                drawables.add(mApps.get(i).activityInfo.applicationInfo.loadIcon(pm));
                map.put("title", mApps.get(i).activityInfo.applicationInfo.loadLabel(pm).toString());
                map.put("pkg", mApps.get(i).activityInfo.packageName);
                map.put("cls", mApps.get(i).activityInfo.name);
                maps.add(map);
            }
        }
        IndexRecommendAdapterOld indexRecommendAdapterOld = new IndexRecommendAdapterOld(getApplicationContext(), maps, new int[]{R.layout.item}, new String[]{"title"}, new int[]{R.id.title}, this, drawables);
        mGridView.setAdapter(indexRecommendAdapterOld);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 应用的包名
                String pkg = maps.get(position).get("pkg");
                System.out.println(pkg);
                // 应用的主Activity
                String cls = maps.get(position).get("cls");
                ComponentName componentName = new ComponentName(pkg, cls);
                Intent intent = new Intent();
                intent.setComponent(componentName);
                startActivity(intent);
            }
        });

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 应用的主Activity
                String pkg = maps.get(i).get("pkg");
                System.out.println(pkg);
                if (pkg.equals("com.example.hasee.app6")) {
                    new MyToast().showTheToast(activity, "请勿删除！！！！！");
                } else {
                    dialog(pkg);
                }
                return true;
            }
        });
    }

    private void dialog(String str) {
        final String a = str;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认删除这个程序吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent uninstall_intent = new Intent();
                uninstall_intent.setAction(Intent.ACTION_DELETE);
                uninstall_intent.setData(Uri.parse("package:"+ a));
                startActivity(uninstall_intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.create().dismiss();
            }
        });
        builder.create().show();
    }


    /**
     * 加载app
     */
    private void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = getPackageManager().queryIntentActivities(intent, 0);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mApps.clear();
        drawables.clear();
        maps.clear();
    }

    @Override
    public void onClick(View v) {
        if(setup_count < 5){
            setup_count = setup_count + 1;
        }else{
            setup_count = 0;
            showThePanel();
        }
    }

    private void showThePanel() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = inflater.inflate(R.layout.show_the_dialog, null);
        TextView suerText = (TextView) view.findViewById(R.id.date_picker_sure);
        final EditText editText = (EditText) view.findViewById(R.id.setup_pwd);
        suerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_pwd = editText.getText().toString();
                if (set_pwd.equals("zyp0111051001732")) {
                    for(int i = 0 ; i < mApps.size() ; i++){
                        Map<String,String> map = new HashMap<String, String>();
                        PackageManager pm = activity.getPackageManager();
                        String str = mApps.get(i).activityInfo.applicationInfo.packageName;
                        if(str.equals("com.android.settings")){
                            String pkg = mApps.get(i).activityInfo.packageName;
                            String cls = mApps.get(i).activityInfo.name;
                            ComponentName componentName = new ComponentName(pkg, cls);
                            Intent intent = new Intent();
                            intent.setComponent(componentName);
                            startActivity(intent);
                        }
                    }
                }else{
                    editText.setText("");
                    dialog.dismiss();
                    Toast.makeText(getApplication(),"密码输入错误！",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }
}

class IndexRecommendAdapterOld extends BaseAdapter implements Filterable {
    private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;
    private ViewHolder mHolder;

    protected List<? extends Map<String, ?>> mData;

    private int[] mResources;
    private LayoutInflater mInflater;
    private Activity activity;
    private int imagePanel;

    private SimpleFilter mFilter;
    private ArrayList<Map<String, ?>> mUnfilteredData;
    private JSONArray top;
    List<Drawable> drawables;

    public IndexRecommendAdapterOld(Context context, List<? extends Map<String, ?>> data, int[] resources, String[] from, int[] to, Activity mactivity,List<Drawable> drawables) {
        mData = data;
        mResources  = resources;
        mFrom = from;
        mTo = to;
        if (context != null) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        this.activity = mactivity;
        this.drawables = drawables;
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

        if(convertView == null){
            convertView = mInflater.inflate(mResources[0], null);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.myImg);
            imageView.setImageDrawable(drawables.get(position));
        }else{

        }

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
