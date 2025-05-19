package com.cashlez.sdkprintertopwise;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Method;

public class MainActivity extends BaseActivity {

    private static final String T6M = "6520";
    private ListView operationList;
    private ArrayAdapter<String> adapter;
    private static final String PRO_DEVICE_PRODUCT_ID = "ro.boot.product";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] cls = null;
        String[] listItem = null;
        boolean is2606 = "MP35S".equals(getProp(this,PRO_DEVICE_PRODUCT_ID));
        cls = getResources().getStringArray(R.array.activity_list_default);
        listItem = getResources().getStringArray(R.array.activity_list_default_title);

        operationList = findViewById(R.id.main_list_view);
        adapter = new ArrayAdapter<String>(this, R.layout.show_item2, listItem);
        operationList.setAdapter(adapter);
        final String[] finalCls = cls;
        operationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                Intent intent = new Intent();
                intent.setClassName(MainActivity.this, finalCls[position]);
                startActivity(intent);
            }
        });

    }

    public static boolean isNonFinancialProducts(Context context){
        return getProp(context,"ro.sp.support").equals("false");
    }

    public static String getProp(Context context, String key) {
        String ret;
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class systemProperties = cl.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;
            Method get = systemProperties.getMethod("get", paramTypes);
            //参数
            Object[] params = new Object[1];
            params[0] = key;
            ret = (String) get.invoke(systemProperties, params);
        } catch (IllegalArgumentException iAE) {
            ret = "";
        } catch (Exception e) {
            ret = "";
        }
        return ret;
    }
}