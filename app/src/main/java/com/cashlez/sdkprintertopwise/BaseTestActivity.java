package com.cashlez.sdkprintertopwise;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BaseTestActivity extends Activity {
	public static final int SHOW_MSG = 0;
    private static final String TAG = "TPW-BaseTestActivity";

	private int showLineNum = 0;

	private LinearLayout linearLayout;
	public ScrollView scrollView;
	private TextView textView1;
	private TextView textView2;
	private long oldTime = -1;
	public static final long DELAY_TIME = 200;
	public LinearLayout rightButArea = null;
	public ProgressDialog progressDialog;

	public EditText et_money;
	public LinearLayout ll_input_edits;
	public EditText et_order;
	public EditText et_psw;
	public EditText et_name;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String msg1 = bundle.getString("msg1");
			String msg2 = bundle.getString("msg2");
			int color = bundle.getInt("color");
			updateView(msg1, msg2, color);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// super.setContentView(R.layout.base_activity);
		linearLayout = this.findViewById(R.id.tipLinearLayout);
		scrollView = this.findViewById(R.id.tipScrollView);
		//rightButArea = (LinearLayout) this.findViewById(R.id.main_linearlayout);
	}

	public void showProgressDialog(Context context) {
		Log.d(TAG, "showProgressDialog");
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(context);
		}
		progressDialog.setMessage(getString(R.string.boot_progress_message));
		progressDialog.setTitle(getString(R.string.boot_progress_title));
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	/**
	 * 显示信息
	 *
	 * @param msg1
	 * @param msg2
	 * @param color
	 * @createtor：Administrator
	 * @date:2014-9-15 下午9:45:18
	 */
	public void updateView(final String msg1, final String msg2, final int color) {
		if ((showLineNum % 300 == 0) && (showLineNum > 0)) { // 显示够20行的时候重新开始
			if(linearLayout != null) {
				linearLayout.removeAllViews();
			} else {
				linearLayout = findViewById(R.id.tipLinearLayout);
			}
			showLineNum = 0;
		}
		showLineNum++;
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.show_item, null);
		textView1 = v.findViewById(R.id.tip1);
		textView2 = v.findViewById(R.id.tip2);
		textView1.setText(msg1);
		textView2.setText(msg2);
		textView1.setTextColor(Color.BLACK);
		textView2.setTextColor(color);
		textView1.setTextSize(20);
		textView2.setTextSize(20);
		linearLayout.addView(v);
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});

	}

	/**
	 * 更新UI
	 *
	 * @param msg1
	 * @param msg2
	 * @param color
	 * @createtor：Administrator
	 * @date:2014-11-29 下午7:01:16
	 */
	public void showMessage(final String msg1, final String msg2,
			final int color) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("msg1", msg1);
		bundle.putString("msg2", msg2);
		bundle.putInt("color", color);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	// 显示单条信息
	public void showMessage(final String msg1, final int color) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("msg1", msg1);
		bundle.putString("msg2", "");
		bundle.putInt("color", color);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	public void showMessage(String str) {
//		this.showMessage(str, Color.BLACK);
		this.showMessage(str, Color.BLUE);
	}

	public void dropProgressbar(String str) {
		Log.d(TAG, "dropProgressbar");
		if (progressDialog != null) {
			progressDialog.dismiss();
			this.showMessage(str, Color.BLUE);
		}
	}

	public void dropProgressbar() {
		Log.d(TAG, "dropProgressbar");
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		oldTime = -1;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

	/**
	 * 初始化输入弹框（一个输入框）
	 * @param listener
	 * @return
	 */
	public AlertDialog.Builder initDialog(String title, String hint, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyDialog);
		View view = getLayoutInflater().inflate(
				R.layout.dialog_input_money_layout, null, false);

		et_money = view.findViewById(R.id.money);
		et_money.setHint(hint);
		ll_input_edits = findViewById(R.id.ll_input_edits);

		builder.setTitle(title).setView(view)
				.setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.ok), listener);

		return builder;
	}

	/**
	 * 初始化输入弹框（2个输入框）
	 * @param listener
	 * @return
	 */
	public AlertDialog.Builder initDialog2(String title,String hint1,String hint2,DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyDialog);

		View view = getLayoutInflater().inflate(
				R.layout.dialog_input_money_layout2, null, false);

		et_money = view.findViewById(R.id.money);
		et_money.setHint(hint1);

		if (getString(R.string.cpu_pwd_group).equals(hint1)){
			et_money.setInputType(InputType.TYPE_CLASS_NUMBER);
			et_money.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
		}

		if ( "offset".equals(hint1)){
			et_money.setInputType(InputType.TYPE_CLASS_NUMBER);
			et_money.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
		}
		et_order = view.findViewById(R.id.order);
		et_order.setHint(hint2);
		if ( getString(R.string.cpu_read_len).equals(hint2)){
			et_order.setInputType(InputType.TYPE_CLASS_NUMBER);
			et_order.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
		}
		builder.setTitle(title).setView(view).setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.ok), listener);

		return builder;
	}

	public AlertDialog.Builder initDialog2(String title,String hint1,String hint2,int inputType,DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = initDialog2(title, hint1, hint2, listener);
		et_money.setInputType(inputType);
		return builder;
	}

	/**
	 * 初始化输入弹框（3个输入框）
	 * @param listener
	 * @return
	 */
	public AlertDialog.Builder initDialog3(String title,String hint1,String hint2,String hint3,DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyDialog);

		View view = getLayoutInflater().inflate(
				R.layout.dialog_input_money_layout3, null, false);

		et_money = view.findViewById(R.id.money);
		et_money.setHint(hint1);

		et_order = view.findViewById(R.id.order);
		et_order.setHint(hint2);

		et_psw = view.findViewById(R.id.psw);
		et_psw.setHint(hint3);

		builder.setTitle(title).setView(view).setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.ok), listener);

		return builder;
	}

	/**
	 * 初始化输入弹框（4个输入框）
	 * @param listener
	 * @return
	 */
	public AlertDialog.Builder initDialog4(String title,String hint1,String hint2,String hint3,String hint4,DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyDialog);

		View view = getLayoutInflater().inflate(
				R.layout.dialog_input_money_layout4, null, false);

		et_money = view.findViewById(R.id.money);
		et_money.setHint(hint1);

		et_order = view.findViewById(R.id.order);
		et_order.setHint(hint2);

		et_psw = view.findViewById(R.id.psw);
		et_psw.setHint(hint3);

		et_name = view.findViewById(R.id.name);
		et_name.setHint(hint4);

		builder.setTitle(title).setView(view).setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.ok), listener);

		return builder;
	}

	synchronized boolean isNormalVelocityClick(long time) {
		long newTime = System.currentTimeMillis();
		if (oldTime == -1) {
			oldTime = newTime;
			return true;
		} else {
			if ((newTime - oldTime) <= time) {
				oldTime = newTime;
				return false;
			}
			oldTime = newTime;
		}
		return true;
	}

	public String getResString(int id) {
		return getResources().getString(id);
	}

	public Bitmap getBmpFromAssets(String filename) {
		Bitmap mBitmap = null;
		AssetManager mAssetManager = getResources().getAssets();
		try {
			InputStream mInputStream = mAssetManager.open(filename);
			mBitmap = BitmapFactory.decodeStream(mInputStream);
			mInputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			mBitmap = null;
		}
		return mBitmap;
	}

	public void saveBitmap2Storage(Bitmap bitmap) {
		if (bitmap == null) {
			Log.d(TAG, "bitmap == null");
			return;
		}
		String time = String.valueOf(System.currentTimeMillis());
		String testFolder = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/USDKTestDemoTmp";
		File folderFile = new File(testFolder);
		if (!folderFile.exists()) {
			boolean ret = folderFile.mkdirs();
			Log.d(TAG, "!folderFile.exists() ret: " + ret);
		}
		String path = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/USDKTestDemoTmp/image"+ time + ".jpg";
		File file = new File(path);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String saveFile2StoragePub(Context context, String fileName) {
		String path = "";
		boolean isCopied = false;
		try {
			InputStream inputStream = context.getAssets().open(fileName);
//			String storagePath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/ResProvider/video";
			File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/USDKTestDemoTmp");
			if (!file.exists()) {
				boolean mkdir = file.mkdirs();
				if (!mkdir) {
					Log.d(TAG, " MyThread call() file.mkdirs(): " + mkdir);
				}
			}
			//file + File.separator = "/"
			FileOutputStream fileOutputStream = new FileOutputStream(file + File.separator + fileName);
			path = file + File.separator + fileName;

			int len = -1;
			byte[] buffer = new byte[1024];
			while ((len = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, len);
			}
			fileOutputStream.close();
			inputStream.close();
			return path;
		}catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
