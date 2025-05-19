package com.cashlez.sdkprintertopwise;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.topwise.cloudpos.aidl.printer.Align;
import com.topwise.cloudpos.aidl.printer.TextUnit;

enum PrintStyle {
    QRCODE(0),
    TEXT(1),
    BARCODE(2);

    private int value;

    PrintStyle (int value){
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

public class PrintDialog extends Dialog {
    public PrintDialog(@NonNull Context context) {
        super(context);
    }

    public PrintDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PrintDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder{
        private Context context;
        private EditText codeEditText;
        private View contentView;
        private String title;
        private String message;
        private float textSize;
        private String positiveButtonText;
        private String negativeButtonText;
        private OnClickListener positiveButtonOnClickListener;
        private OnClickListener negativeButtonOnClickListener;
        TextView tvTitle;
        RadioButton smallRb;
        RadioButton normalRb;
        RadioButton largeRb;
        RadioButton leftRb;
        RadioButton midRb;
        RadioButton rightRb;
        RadioButton qrcodeRb;
        RadioButton textRb;
        RadioButton barcodeRb;

        public Builder(Context context) {
            this.context = context;
        }
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(String message, float textSize) {
            this.message = message;
            this.textSize = textSize;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        public Builder setTitle(int title){
            this.title = (String) context.getText(title);
            return this;
        }

        public Builder setTitle(String title){
            this.title = title;
            return this;
        }

        public Builder setContentView(View v){
            this.contentView = v;
            return this;
        }

        public String getText() {
            return codeEditText.getText().toString();
        }
        public Builder setPositiveButton(int positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonOnClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonOnClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText, OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeButtonOnClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonOnClickListener = listener;
            return this;
        }
        public Align getAlign (){
            if (leftRb.isChecked()) {
                return Align.LEFT;
            }else if (midRb.isChecked()) {
                return Align.CENTER;
            }else {
                return Align.RIGHT;
            }
        }

        public int getPrintTextSize() {
            if (smallRb.isChecked()) {
                return TextUnit.TextSize.SMALL;
            }else if (normalRb.isChecked()) {
                return TextUnit.TextSize.NORMAL;
            }else {
                return TextUnit.TextSize.LARGE;
            }
        }

        public PrintStyle getPrintStyle() {
            if (qrcodeRb.isChecked()) {
                return PrintStyle.QRCODE;
            }else if (textRb.isChecked()) {
                return PrintStyle.TEXT;
            }else {
                return PrintStyle.BARCODE;
            }
        }


        public PrintDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final PrintDialog printDialog = new PrintDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_input_print_text, null);
            qrcodeRb = layout.findViewById(R.id.qr_code_rb);
            textRb = layout.findViewById(R.id.text_rb);
            barcodeRb = layout.findViewById(R.id.barcode_rb);
            smallRb = layout.findViewById(R.id.small_rb);
            normalRb = layout.findViewById(R.id.normal_rb);
            largeRb = layout.findViewById(R.id.large_rb);
            leftRb = layout.findViewById(R.id.left_rb);
            midRb = layout.findViewById(R.id.middle_rb);
            rightRb = layout.findViewById(R.id.right_rb);
            codeEditText = layout.findViewById(R.id.code_et);
            tvTitle = layout.findViewById(R.id.title_print);
            tvTitle.setText(title);
            if (positiveButtonText != null) {
                ((Button)layout.findViewById(R.id.print_dialog_pb))
                        .setText(positiveButtonText);
                if (positiveButtonOnClickListener != null) {
                    ((Button)layout.findViewById(R.id.print_dialog_pb))
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    positiveButtonOnClickListener.onClick(printDialog, DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                layout.findViewById(R.id.print_dialog_pb).setVisibility(View.GONE);
            }
            if (negativeButtonText != null) {
                ((Button)layout.findViewById(R.id.print_dialog_nb))
                        .setText(negativeButtonText);
                if (negativeButtonOnClickListener != null) {
                    ((Button)layout.findViewById(R.id.print_dialog_nb))
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    negativeButtonOnClickListener.onClick(printDialog, DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                layout.findViewById(R.id.print_dialog_nb).setVisibility(View.GONE);
            }
            printDialog.setContentView(layout);
            return printDialog;
        }
    }
}
