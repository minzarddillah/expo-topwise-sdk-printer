package view;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cashlez.sdkprintertopwise.R;

import java.util.ArrayList;

public class DialogFactory {
	private final Context context;
	private Dialog dialog;
	public static DialogFactory factory = null;

	public DialogFactory(Context context) {
		this.context = context;
	}

	public static DialogFactory getInstance(Context context) {
		// 没单例是因为要弹窗 context需要实时更新
		factory = new DialogFactory(context);
		return factory;
	}

	/**
	 * 弹出对话框
	 */
	public void showDialog(ArrayList<DialogItemObj> list, String title,
						   OnItemListener listener) {
		dialog = createDialog(list, title, listener);
		Window win = dialog.getWindow();
		win.setGravity(Gravity.CENTER);
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.x = 0;
		lp.y = 0;
		// lp.height = (int) (display.getHeight() / 2);
		win.setAttributes(lp);
		if (dialog != null)
			dialog.show();
	}

	/**
	 * 创建对话框
	 * 
	 * @return
	 */
	private Dialog createDialog(ArrayList<DialogItemObj> list, String title,
								OnItemListener listener) {
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		View layout = LayoutInflater.from(context).inflate(
				R.layout.dialog_main, null);
		initViewLayout(layout, list, listener);
		float density = context.getResources().getDisplayMetrics().density;
		layout.setMinimumWidth((int) (display.getWidth() - density * 80));
		Dialog dialog = new Dialog(context, R.style.AlertDialogCustom);
		dialog.setTitle(title);
		dialog.setContentView(layout);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	/**
	 * 初始化对话框
	 * 
	 * @param layout
	 */
	private void initViewLayout(View layout, ArrayList<DialogItemObj> list,
								final OnItemListener listener) {
		LinearLayout ll = layout.findViewById(R.id.main_ll);
		final ArrayList<View> viewList = new ArrayList<View>();
		for (DialogItemObj obj : list) {
			View view = LayoutInflater.from(context).inflate(
					R.layout.dialog_item, null);
			EditText et = view.findViewById(R.id.dialog_et);
			et.setHint(obj.getHintText());
			InputFilter[] filters = { new InputFilter.LengthFilter(
					obj.getMaxLength()) };
			et.setFilters(filters);
			if (!TextUtils.isEmpty(obj.getDefaultText())) {
				et.setText(obj.getDefaultText());
			}
			view.setTag(obj);
			ll.addView(view);
			viewList.add(view);
		}
		final String[] strList = new String[viewList.size()];
		Button btn = layout.findViewById(R.id.dialog_btn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (int i = 0; i < viewList.size(); i++) {
					EditText et1 = viewList.get(i).findViewById(
							R.id.dialog_et);
					String inputText = et1.getText().toString();
					System.out.println(inputText);
					DialogItemObj obj1 = (DialogItemObj) viewList.get(i)
							.getTag();
					if (!obj1.isCanEmpty() && TextUtils.isEmpty(inputText)) {
						Toast.makeText(context, "第" + (i + 1) + "项不允许为空，请重新输入",
								Toast.LENGTH_LONG).show();
						return;
					}
					if (!obj1.isCanEmpty() && obj1.getLimitLength() != -1
							&& inputText.length() != obj1.getLimitLength()) {
						Toast.makeText(
								context,
								"第" + (i + 1) + "项输入长度应为:"
										+ obj1.getLimitLength() + "，请重新输入",
								Toast.LENGTH_LONG).show();
						return;
					}
					strList[i] = inputText;

				}
				listener.onConfirm(strList);
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});

	}

	public interface OnItemListener {
		void onConfirm(String[] finalInputs);
	}

}
