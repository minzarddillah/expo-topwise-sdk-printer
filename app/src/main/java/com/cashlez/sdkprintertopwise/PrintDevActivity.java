package com.cashlez.sdkprintertopwise;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener;
import com.topwise.cloudpos.aidl.printer.Align;
import com.topwise.cloudpos.aidl.printer.ImageUnit;
import com.topwise.cloudpos.aidl.printer.PrintCuttingMode;
import com.topwise.cloudpos.aidl.printer.PrintItemObj;
import com.topwise.cloudpos.aidl.printer.PrintTemplate;
import com.topwise.cloudpos.aidl.printer.PrinterMessage;
import com.topwise.cloudpos.aidl.printer.TextUnit;
import com.topwise.cloudpos.aidl.printer.TextUnit.TextSize;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *  print test
 *
 * @author Tianxiaobo
 */
public class PrintDevActivity extends BaseTestActivity {
    private AidlPrinter printerDev = null;
    private final String  TAG ="TPW-PrintDevActivity";
    private boolean biasRunning =false;
    private boolean printRunning =  false;
    private  final  static  int BUFF_LEN = 48*2*5;
    private Button printBiasBtn ;
    private Switch autoCuttingSwitch;
    private int cuttingMode = 0;
    private boolean printGrayInfo = true;
    private boolean printVoltageInfo = true;
    private boolean printMileageInfo = true;
    private boolean printTemperatureInfo = true;
    private boolean printCountInfo = true;
    private int printTemperatureTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setContentView(R.layout.printdev);
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate");
        printerDev = DeviceServiceManager.getInstance().getPrintManager();
        Typeface typeface = Typeface.createFromAsset(getAssets(),"topwise.ttf");
        PrintTemplate.getInstance().init(this,typeface);
        printBiasBtn = findViewById(R.id.print_bias);
        autoCuttingSwitch = findViewById(R.id.sw_auto_cutting);
        Log.d(TAG, "model : " + Build.DISPLAY);
        if (!Build.DISPLAY.contains("1018")) {
            autoCuttingSwitch.setChecked(false);
        }
        autoCuttingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v(TAG,"autoCuttingSwitch isChecked: " + isChecked);
                if (isChecked){
                    String isSupport = MainActivity.getProp(PrintDevActivity.this, "ro.device.support.cutting_paper");
                    if (!"true".equals(isSupport)) {
                        showMessage(getResources().getString(R.string.print_system_no_support));
                    }
                }
            }
        });
        printBiasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biasRunning = !biasRunning;
                if(biasRunning){
                    printBiasBtn.setText(getResString(R.string.stop_print_bias));
                    printBias();
                }else{
                    printBiasBtn.setText(getResString(R.string.print_bias));
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG,"onRestart");
        if(printerDev == null) {
            printerDev = DeviceServiceManager.getInstance().getPrintManager();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG,"onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy");
        biasRunning = false;
    }

    /**
     *  get print status
     *
     * @param v
     * @createtor：Administrator
     * @date:2015-8-4 下午2:18:47
     */
    public void getPrintState(View v) {
        Log.v(TAG,"getPrintState");
        try {
            int printState = printerDev.getPrinterState();
            showMessage(getResources().getString(R.string.print_status) + printState);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void printText(View view){
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printText");
        try {
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template =PrintTemplate.getInstance();
            template.clear();
            int fontSize = 0;
            String language = Locale.getDefault().getLanguage();
            Log.d(TAG, "language: " + language);
            if (!"".equals(language)) {
                if (!"zh".equals(language)) {
                    fontSize = 23;
                }else {
                    fontSize = 24;
                }
            }
//            template.add(new TextUnit("fontSize: " + fontSize));
            template.add(new TextUnit(getString(R.string.print_text_default)));
            template.add(new TextUnit(getString(R.string.print_text_default)));
            template.add(new TextUnit(getString(R.string.print_text_default)));
            template.add(new TextUnit(getString(R.string.print_text_large),48));
            template.add(new TextUnit(getString(R.string.print_text_large),48));
            template.add(new TextUnit(getString(R.string.print_text_large),48));
            template.add(new TextUnit(getString(R.string.print_text_bold),fontSize).setBold(true));
            template.add(new TextUnit(getString(R.string.print_text_bold),fontSize).setBold(true));
            template.add(new TextUnit(getString(R.string.print_text_bold),fontSize).setBold(true));
            template.add(new TextUnit(getString(R.string.print_text_align_left),fontSize,Align.LEFT).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_left),fontSize,Align.LEFT).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_left),fontSize,Align.LEFT).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_center),fontSize,Align.CENTER).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_center),fontSize,Align.CENTER).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_center),fontSize,Align.CENTER).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_right),fontSize,Align.RIGHT).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_right),fontSize,Align.RIGHT).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_align_right),fontSize,Align.RIGHT).setBold(false));
            template.add(new TextUnit(getString(R.string.print_text_underline),fontSize,Align.LEFT).setUnderline(true));
            template.add(new TextUnit(getString(R.string.print_text_underline),fontSize,Align.LEFT).setUnderline(true));
            template.add(new TextUnit(getString(R.string.print_text_underline),fontSize,Align.LEFT).setUnderline(true));
            template.add(new TextUnit(getString(R.string.print_text_total),fontSize,Align.LEFT).setWordWrap(true));
            template.add(new TextUnit(getString(R.string.print_text_total),fontSize,Align.LEFT).setWordWrap(true));
            template.add(new TextUnit(getString(R.string.print_text_no_wrap),fontSize,Align.LEFT));
            template.add(new TextUnit(getString(R.string.print_text_line_space),fontSize,Align.LEFT).setWordWrap(true).setLineSpacing(20));
            template.add(new TextUnit(getString(R.string.print_text_line_space),fontSize,Align.LEFT).setWordWrap(true).setLineSpacing(41));
            template.add(new TextUnit(getString(R.string.print_text_line_space),fontSize,Align.LEFT).setWordWrap(true).setLineSpacing(20));
            template.add(new TextUnit(getString(R.string.print_text_letter_space),fontSize,Align.LEFT).setWordWrap(true).setLineSpacing(15).setLetterSpacing(25));
            template.add(new TextUnit(getString(R.string.print_text_letter_space),fontSize,Align.LEFT).setWordWrap(true).setLineSpacing(15).setLetterSpacing(25));
            template.add(new TextUnit(getString(R.string.print_text_letter_space),fontSize,Align.LEFT).setWordWrap(true).setLineSpacing(15).setLetterSpacing(25));
            template.add(new TextUnit(getString(R.string.print_text_padding_left),fontSize,Align.LEFT).setWordWrap(true));
            template.add(new TextUnit(getString(R.string.print_text_padding_left),fontSize,Align.LEFT).setWordWrap(true));
            template.add(new TextUnit(getString(R.string.print_text_padding_left),fontSize,Align.LEFT).setWordWrap(true));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    /**
     * Template print
     *
     * @param v
     * @createtor：Administrator
     * @date:2019-9-7
     */
    public void printTemplate(View v) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printTemplate");
        try {
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template =PrintTemplate.getInstance();
            template.clear();
            int textSize = 23;
            int fontSize = 15;
//            template.add(new TextUnit("textsize: " + textSize + ",fontSize: " + fontSize));
            template.add(new TextUnit(getString(R.string.print_text_union_sales),textSize,Align.CENTER));
            template.add(new TextUnit("--------------------------------------------------------------------").setWordWrap(false));
            template.add(new TextUnit(getString(R.string.print_text_merchant_name),textSize));
            template.add(new TextUnit(getString(R.string.print_text_merchant_name),textSize));
            template.add(new TextUnit(getString(R.string.print_text_merchant_number),textSize).setBold(true));
            template.add(new TextUnit(getString(R.string.print_text_terminal_number),textSize));
            template.add(new TextUnit(getString(R.string.print_text_operater_number),textSize));
            template.add(new TextUnit(getString(R.string.print_text_issuer),textSize));
            template.add(new TextUnit(getString(R.string.print_text_card_number),textSize));
            template.add(new TextUnit("6214 61** **** 6526",textSize,Align.CENTER).setBold(true));
            template.add(1,new TextUnit(getString(R.string.print_text_transaction_type),textSize),
                    2,new TextUnit(getString(R.string.print_text_transaction_detail_type),textSize,Align.CENTER).setBold(true));
            template.add(new TextUnit(getString(R.string.print_text_batch_number),textSize));
            template.add(new TextUnit(getString(R.string.print_text_voucher_number),textSize));
            template.add(new TextUnit(getString(R.string.print_text_authorization_code),textSize));
            template.add(new TextUnit(getString(R.string.print_text_reference_number),textSize));
            template.add(new TextUnit(getString(R.string.print_text_expire_time),textSize));
            template.add(new TextUnit(getString(R.string.print_text_transaction_time),textSize));
            template.add(new TextUnit("2019/08/30 16:16:18",textSize));
            template.add(1,new TextUnit(getString(R.string.print_text_transaction_amount),textSize),
                    1,new TextUnit("RMB 8.88",textSize).setBold(true));
            template.add(new TextUnit("--------------------------------------------------------------------").setWordWrap(false));
            template.add(new TextUnit(getString(R.string.print_text_remarks),textSize));
            template.add(new TextUnit(getString(R.string.print_text_transaction_number),textSize));
            template.add(new TextUnit("------------------------------------------").setWordWrap(false));
            template.add(new TextUnit(getString(R.string.print_text_transaction_promise) + "\n",textSize));
            template.add(new TextUnit(getString(R.string.print_text_large), textSize));
            template.add(new TextUnit(getString(R.string.print_text_align_left), fontSize));
            template.add(new TextUnit(getString(R.string.print_text_align_center), fontSize, Align.CENTER));
            template.add(new TextUnit(getString(R.string.print_text_align_right), fontSize, Align.RIGHT));
            template.add(1,new TextUnit(getString(R.string.print_text_both_side), fontSize, Align.RIGHT),
                    1,new TextUnit(getString(R.string.print_text_both_side), fontSize, Align.RIGHT));

            template.add(1,new TextUnit(getString(R.string.print_text_transaction_amount), fontSize, Align.LEFT),
                    1,new TextUnit(getString(R.string.print_text_detail_amount), fontSize, Align.RIGHT));//.setBold(true)
            template.add(1,new TextUnit(getString(R.string.print_text_transaction_amount), fontSize, Align.LEFT),//.setBold(true)
                    1,new TextUnit(getString(R.string.print_text_transaction_price), fontSize, Align.CENTER),//.setBold(true)
                    1,new TextUnit(getString(R.string.print_text_transaction_quantity), fontSize, Align.RIGHT));//.setBold(true)
            template.add(1,new TextUnit("1.00", fontSize, Align.LEFT),
                    1,new TextUnit("2.00", fontSize, Align.CENTER),
                    1,new TextUnit("121", fontSize, Align.RIGHT));
            template.add(1,new TextUnit("2.00", fontSize, Align.LEFT),
                    1,new TextUnit("3.00", fontSize, Align.CENTER),
                    1,new TextUnit("111", fontSize, Align.RIGHT));
            template.add(new TextUnit("\n"));
            Bitmap bitmap = QRCodeUtil.createQRImage("asdfggfffffsshhheeed", 190,  190,null);
            List<TextUnit> list = new ArrayList<TextUnit>();
            list.add(new TextUnit(getString(R.string.print_text_default_value1)));
            list.add(new TextUnit(getString(R.string.print_text_default_value2), fontSize));
            template.add(new ImageUnit(bitmap,190,190),list);
            template.add(list,new ImageUnit(bitmap,190,190));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }
    /**
     * printBitmap
     *
     * @param v
     * @createtor：Administrator
     * @date:2015-8-4 下午2:39:33
     */
    public void printBitmap(View v) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printBitmap");
        try {
            printRunning = true;
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.print_bitmap);
            Bitmap bitmap = getBmpFromAssets("bmp/print_bitmap.bmp");
            printerDev.addRuiImage(bitmap,0);
            showPrintStartInfo();
            PrintTemplate template =PrintTemplate.getInstance();
            template.clear();
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    public void printGrid(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
//        try {
//            printerDev.printRollback(1000);
//            DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","false");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        Log.v(TAG,"printGrid");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    printRunning = true;
                    InputStream is = getClass().getClassLoader().getResourceAsStream("assets/print_grid_data.txt");
                    byte[] buffer = new byte[BUFF_LEN];
                    int byteCount = 0;
                    int len =0;
                    int ret  =0;
                    showPrintStartInfo();
                    while (true) {
                        byteCount = is.read(buffer);
                        if(byteCount ==-1) {
                            break;
                        }
                        len +=byteCount;
                        if(byteCount<BUFF_LEN){
                            byte[]   lastBuf = new byte[byteCount];
                            LogUtil.d(TAG, " byteCount ==== "  + byteCount);
                            System.arraycopy(buffer,0,lastBuf,0,byteCount);
                            buffer = lastBuf;
                        }
                        String str =  new String(buffer).toUpperCase();
                        LogUtil.d(TAG, " ==== "  +str);
                        byte[] printBuf  = HexUtil.hexStringToByte(str);
                        ret =   printerDev.printBuf(printBuf);
                        if(ret != 0 ){
                            break;
                        }
                    }
                    is.close();
                    if(ret ==0) {
                        printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                            {
                                add(new PrintItemObj("\n"));
                                if(Build.DISPLAY.contains("Z3909")) {
                                    add(new PrintItemObj(""));
                                }
                            }
                        });
                        printerDev.printRuiQueue(mListen);
//                autoCuttingPaper();
                    }else{
                        printRunning = false;
                        showMessage(getResources().getString(R.string.print_error_code) + ret);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    try {
//                        DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","true");
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();

    }

    /**
     * print barcode
     *
     * @param v
     * @createtor：Administrator
     * @date:2015-8-4 下午3:02:21
     */
    public void printBarCode(View v) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printBarCode");
        try {
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template =PrintTemplate.getInstance();
            template.clear();
            template.add(new ImageUnit(CodeUtil.createBarcode("23418753401333", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("03400471", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("2341875340111", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("23411875", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("*23418*", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("234187534011", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("23418", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("{A23418333", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("123456765432123412", 350,  160),350,160));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    private String getCurTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String time = format.format(date);
        return time;
    }

    public void getPrintMessage(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResString(R.string.printer_message_title));
        final String printGray = getString(R.string.printer_gray_message);
        final String[] messages = new String[] {getString(R.string.printer_gray_message), getString(R.string.printer_voltage_message), getString(R.string.printer_mileage_message), getString(R.string.printer_temperature_message), getString(R.string.printer_count_message)};
        boolean[] messagesChoice = new boolean[] {printGrayInfo, printVoltageInfo, printMileageInfo, printTemperatureInfo, printCountInfo};
        builder.setMultiChoiceItems(messages, messagesChoice, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch (which) {
                    case 0:
                        printGrayInfo = isChecked;
                        break;
                    case 1:
                        printVoltageInfo = isChecked;
                        break;
                    case 2:
                        printMileageInfo = isChecked;
                        break;
                    case 3:
                        printTemperatureInfo = isChecked;
                        break;
                    case 4:
                        printCountInfo = isChecked;
                        break;
                    default:
                        break;
                }
            };
        });
        builder.setPositiveButton(getResString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String printMessage = getPrintMessageInfo();
                showMessage(printMessage);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
//        try {
//            PrinterMessage message = printerDev.getPrinterMessage();
//            showMessage(getString(R.string.print_gray_level) + message.getPrinterGray());
//            showMessage(getString(R.string.print_voltage) + message.getPrinterVoltage());
//            showMessage(getString(R.string.print_mileage) + message.getPrinterMileage());
//            showMessage(getString(R.string.print_temperature) + message.getPrinterTemperature());
//            showMessage(getString(R.string.print_counter) + message.getPrinterCount());
//        } catch (Exception e) {
//            e.printStackTrace();
//            showMessage(getString(R.string.print_system_no_support));
//        }
    }

    public String getPrintMessageInfo() {
        StringBuilder printMessage = new StringBuilder("");
        try {
            PrinterMessage message = printerDev.getPrinterMessage();
            if (printGrayInfo) {
                printMessage.append(getString(R.string.print_gray_level) + message.getPrinterGray() + "\n");
            }
            if (printVoltageInfo) {
                printMessage.append(getString(R.string.print_voltage) + message.getPrinterVoltage() + "\n");
            }
            if (printMileageInfo) {
                printMessage.append(getString(R.string.print_mileage) + message.getPrinterMileage() + "\n");
            }
            if (printTemperatureInfo) {
                printMessage.append(getString(R.string.print_temperature) + message.getPrinterTemperature() + "\n");
            }
            if (printCountInfo) {
                printMessage.append(getString(R.string.print_counter) + message.getPrinterCount());
            }
        }catch (Exception e) {
            e.printStackTrace();
            return printMessage.append(getString(R.string.print_system_no_support)).toString();
        }
        return printMessage.toString();
    }

    public void setPrintGray(View v){
        AlertDialog.Builder mBuilder = initDialog(getString(R.string.input_gray_level),getString(R.string.hint_gray_range),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String gray = et_money.getText().toString().isEmpty()?"3":et_money.getText().toString();
                List<PrintItemObj> list =new ArrayList<>();
                list.add(new PrintItemObj(getString(R.string.set_print_gray_level) + gray));
                list.add(new PrintItemObj(getString(R.string.set_print_gray_level) + gray));
                list.add(new PrintItemObj(getString(R.string.set_print_gray_level) + gray));
                list.add(new PrintItemObj(getString(R.string.set_print_gray_level) + gray));
                list.add(new PrintItemObj(getString(R.string.set_print_gray_level) + gray));
                list.add(new PrintItemObj("\n\n"));
                try {
                    printerDev.setPrinterGray(Integer.parseInt(gray));

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        et_money.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        et_money.setKeyListener(new NumberKeyListener(){
            @Override
            public int getInputType() {
                // TODO Auto-generated method stub
                return InputType.TYPE_CLASS_NUMBER;
            }
            @Override
            protected char[] getAcceptedChars() {
                // TODO Auto-generated method stub
                char[] mychar={'1','2','3','4'};
                return mychar;
            }

        });
        mBuilder.show();
    }

    int cuttingPaperIndex = 0;
    public void cuttingPaper(View v){
        String isSupport = MainActivity.getProp(this, "ro.device.support.cutting_paper");
        if (!"true".equals(isSupport)) {
            showMessage(getResources().getString(R.string.print_system_no_support));
            return;
        }
        final String[] mode = new String[]{"HALT", "FULL"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialog)
                .setTitle(getResources().getString(R.string.pinpad_dukpt_aes_work_key_type))
                .setSingleChoiceItems(mode, cuttingPaperIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int result = -1;
                        switch (mode[which]) {
                            case "HALT":
                                try {
                                    result = printerDev.cuttingPaper(PrintCuttingMode.CUTTING_MODE_HALT);
                                    cuttingMode = 0;
                                    cuttingPaperIndex = which;
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "FULL":
                                try {
                                    result = printerDev.cuttingPaper(PrintCuttingMode.CUTTING_MODE_FULL);
                                    cuttingMode = 1;
                                    cuttingPaperIndex = which;
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                        if(result == 0) {
                            showMessage(getString(R.string.print_cut_paper_success));
                        } else if(result == -1) {
                            showMessage(getString(R.string.print_system_no_support));
                        } else {
                            showMessage(getString(R.string.print_cut_error_code) + result);
                        }
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void printRollback(View v){
        Log.v(TAG,"printRollback");
        try {
            String isSupport = MainActivity.getProp(this, "ro.device.support.print_rollback");
            if(!"true".equals(isSupport)) {
                showMessage(getString(R.string.print_system_no_support));
                return;
            }
            int result = printerDev.printRollback(8);
            if(result == 0) {
                showMessage(getString(R.string.print_rollback_success));
            } else {
                showMessage(getString(R.string.print_rollback_code) + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int printLedStateIndex = 3;
    public void setPrintLedState(View v){
        String isSupport = MainActivity.getProp(this, "ro.device.support.print_led");
        if (!"true".equals(isSupport)) {
            showMessage(getResources().getString(R.string.print_system_no_support));
            return;
        }
        Log.v(TAG,"setPrintLedState");
        final String[] mode = new String[]{"FAST", "DEFAULT","SLOW","OFF"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialog)
                .setTitle(getResources().getString(R.string.pinpad_dukpt_aes_work_key_type))
                .setSingleChoiceItems(mode, printLedStateIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (mode[which]) {
                            case "FAST":
                                try {
                                    printerDev.setPrintLedState(true,250);
                                    printLedStateIndex = which;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "DEFAULT":
                                try {
                                    printerDev.setPrintLedState(true,375);
                                    printLedStateIndex = which;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "SLOW":
                                try {
                                    printerDev.setPrintLedState(true,500);
                                    printLedStateIndex = which;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "OFF":
                                try {
                                    printerDev.setPrintLedState(false,0);
                                    printLedStateIndex = which;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }


    private  void printGray(String s){
        PrintTemplate template =PrintTemplate.getInstance();
        template.clear();
        int textSize = TextSize.NORMAL;
        template.add(new TextUnit(s,textSize,Align.LEFT));
        template.add(new TextUnit("\n\n"));
        printAddLineFree(template);
        try {
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void printTickertape(View view) {
        if (printerDev == null) {
            showMessage(getString(R.string.fail_get_print_service));
            return;
        }
        if(printRunning) {
            showMessage(getString(R.string.print_in_progress));
            return;
        }
//        try {
//            printerDev.printRollback(1000);
//            DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","false");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        Log.v(TAG,"printTickertape");
        printRunning = true;
        showPrintStartInfo();
        showProgressDialog(this);
        final String orderNo = "1234567890123456541";
//        Bitmap bitmap = getBmpFromAssets("bmp/pic_3.bmp");
        Bitmap bitmap = getBmpFromAssets("bmp/VISA_card.bmp");
        try {
            PrintTemplate template = PrintTemplate.getInstance();
            template.init(this,null);
            template.clear();
            template.add(new ImageUnit(bitmap,bitmap.getWidth(),bitmap.getHeight()));
            template.add(new TextUnit(getResString(R.string.print_title),TextSize.LARGE,Align.CENTER).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantname),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantno)+"00000000000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_terminalno)+"100000000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_operator)+"01",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_cardno)+"6214444******0095  1",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_issno)+"01021000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_acqno)+"01031000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_txntype)+getResString(R.string.consume),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_expdate)+"20/12",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_batchno)+"000001",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_voucherno)+"000033",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_authno)+"000000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_refno)+"1009000000033",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_datetime)+"2017/10/10 11:11:11",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_amount)+"  100.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_tips)+"  1.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_total)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            Bitmap bitmap1 =CodeUtil.createBarcode(orderNo,350,90);
            template.add(new ImageUnit(bitmap1,bitmap1.getWidth(),bitmap1.getHeight()));
            template.add(new TextUnit(getResString(R.string.print_reference)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("AID:A000000333010101 TVR:008004600:",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("ARQC:ABCDEFDGJHHHGA ATC:0020:",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_signature),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_acknowledge),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("-----------------------------------------------------------",TextSize.NORMAL,Align.CENTER).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantcopy),TextSize.NORMAL,Align.CENTER).setBold(false));
            bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",300,300,null);
            template.add(new ImageUnit(Align.CENTER,bitmap,300,300));
            bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",200,200,null);
            template.add(new ImageUnit(Align.CENTER,bitmap,200,200));
            bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",180,180,null);
            template.add(new ImageUnit(Align.CENTER,bitmap,180,180));
            printAddLineFree(template);
//            Bitmap printGertec = getBmpFromAssets("print_data_gertec.jpg");
//            template.add(new ImageUnit(Align.CENTER,printGertec,printGertec.getWidth(),printGertec.getHeight()));
//            template.add(new TextUnit(""));
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(new AidlPrinterListener.Stub() {
                @Override
                public void onError(int i) throws RemoteException {
                    showMessage(getResources().getString(R.string.print_error_code) + i);
                    String printMessage = getPrintMessageInfo();
                    showMessage(printMessage);
                    printRunning = false;
                    dropProgressbar();
                }
                @Override
                public void onPrintFinish() throws RemoteException {
                    printGertec();
//                    dropProgressbar();
                }

            });
        } catch (RemoteException e) {
            printRunning = false;
            dropProgressbar();
            e.printStackTrace();
        }
    }

    public void printTickertapeReverse(View v) {
        if (printerDev == null) {
            showMessage(getString(R.string.fail_get_print_service));
            return;
        }
        if(printRunning) {
            showMessage(getString(R.string.print_in_progress));
            return;
        }
//        try {
//            printerDev.printRollback(1000);
//            DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","false");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        Log.v(TAG,"printTickertapeReverse");
        printRunning = true;
        showPrintStartInfo();
        showProgressDialog(this);
        final String orderNo = "1234567890123456541";
        Bitmap bitmap = getBmpFromAssets("bmp/pic_3.bmp");
        try {
            PrintTemplate template = PrintTemplate.getInstance();
            template.init(this,null);
            template.clear();
            template.add(new ImageUnit(bitmap,bitmap.getWidth(),bitmap.getHeight()));
            template.add(new TextUnit(getResString(R.string.print_title),TextSize.LARGE,Align.CENTER).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_merchantname),TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_merchantno)+"00000000000",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_terminalno)+"100000000",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_operator)+"01",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_cardno)+"6214444******0095  1",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_issno)+"01021000",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_acqno)+"01031000",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_txntype)+getResString(R.string.consume),TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_expdate)+"20/12",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_batchno)+"000001",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_voucherno)+"000033",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_authno)+"000000",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_refno)+"1009000000033",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_datetime)+"2017/10/10 11:11:11",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_amount)+"  100.00",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_tips)+"  1.00",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_total)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            Bitmap bitmap1 =CodeUtil.createBarcode(orderNo,350,90);
            template.add(new ImageUnit(bitmap1,bitmap1.getWidth(),bitmap1.getHeight()));
            template.add(new TextUnit(getResString(R.string.print_reference)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit("AID:A000000333010101 TVR:008004600:",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit("ARQC:ABCDEFDGJHHHGA ATC:0020:",TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_signature),TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_acknowledge),TextSize.NORMAL,Align.LEFT).setBold(false).setReverse(true));
            template.add(new TextUnit("-----------------------------------------------------------",TextSize.NORMAL,Align.CENTER).setBold(false).setReverse(true));
            template.add(new TextUnit(getResString(R.string.print_merchantcopy),TextSize.NORMAL,Align.CENTER).setBold(false).setReverse(true));
            bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",300,300,null);
            template.add(new ImageUnit(Align.CENTER,bitmap,300,300));
            bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",200,200,null);
            template.add(new ImageUnit(Align.CENTER,bitmap,200,200));
            bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",180,180,null);
            template.add(new ImageUnit(Align.CENTER,bitmap,180,180));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(new AidlPrinterListener.Stub() {
                @Override
                public void onError(int i) throws RemoteException {
                    showMessage(getResources().getString(R.string.print_error_code) + i);
                    String printMessage = getPrintMessageInfo();
                    showMessage(printMessage);
                    printRunning = false;
                    dropProgressbar();
                }
                @Override
                public void onPrintFinish() throws RemoteException {
                    printGertec();
                }

            });
        } catch (RemoteException e) {
            printRunning = false;
            dropProgressbar();
            e.printStackTrace();
        }
    }
    public static void generateBMPFromByteArray(byte[] monoData, int width, int height, String filePath) {
        // 创建一个Bitmap，这里我们假设是ARGB_8888，因为BMP通常不支持索引颜色
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 遍历byte数组
        for (int y = 0; y < height; y++) {
            int byteIndex = y * (width / 8); // 假设每个byte包含8个像素
            for (int x = 0; x < width; x++) {
                // 计算当前像素在byte中的位置（0-7）
                int bitIndex = 7 - (x % 8);
                // 使用位运算获取当前像素的值
                boolean isBlack = ((monoData[byteIndex] >> bitIndex) & 1) == 1;
                // 设置像素颜色
                bitmap.setPixel(x, y, isBlack ? Color.BLACK : Color.WHITE);

                // 如果当前byte已经处理完8个像素，则移动到下一个byte
                if (x % 8 == 7) {
                    byteIndex++;
                }
            }
        }

        // 保存Bitmap为BMP文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            System.out.println("BMP file saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 释放Bitmap资源（可选，但推荐）
        bitmap.recycle();
    }
    public static byte[] readFully(InputStream input) throws IOException {
        // 使用ByteArrayOutputStream来收集读取的数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 创建一个缓冲区来读取文件
        byte[] buffer = new byte[4096]; // 4KB的缓冲区
        int bytesRead;

        // 读取数据直到文件结束
        while ((bytesRead = input.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        // 关闭输入流（重要，但要确保在操作完成后进行）
        // 注意：在try-with-resources语句中可以自动关闭

        // 将收集到的数据转换为byte数组
        return baos.toByteArray();
    }
    public void writeByteArrayToFileInternal(Context context, String fileName, byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void printGertec(){
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("assets/print_data.txt");
//            byte[] fileContent = readFully(is);
//            byte[] data = BytesUtil.hexString2Bytes(new String(fileContent));
//            Log.d(TAG, "data: "+ data );
//            Log.d(TAG, "data.length: "+ data.length );
//            Log.d(TAG, "data.length/48: "+ data.length/48 );
//            writeByteArrayToFileInternal(this, "print_data_bitmap.txt", data);
//            generateBMPFromByteArray(data, 384, data.length/48, "/sdcard/print_data_bitmap2.jpg");
            byte[] buffer = new byte[BUFF_LEN];
            int byteCount = 0;
            int len =0;
            int ret  =0;
            while (true) {// 循环从输入流读取 buffer字节
                byteCount = is.read(buffer);
                if(byteCount ==-1) {
                    break;
                }
                len +=byteCount;
                if(byteCount<BUFF_LEN){
                    byte[]   lastBuf = new byte[byteCount];
                    LogUtil.d(TAG, " byteCount ==== "  + byteCount);
                    System.arraycopy(buffer,0,lastBuf,0,byteCount);
                    buffer = lastBuf;
                }
                String str =  new String(buffer).toUpperCase();
                LogUtil.d(TAG, " ==== "  +str);
                byte[] printBuf  = HexUtil.hexStringToByte(str);
//                byte[]   lastBuf = new byte[48 * 5];
//                byteCount=data.length-len>=48 * 5?48 * 5:data.length-len;
//                System.arraycopy(data,len,lastBuf,0,byteCount);
//                len +=byteCount;
//                ret =   printerDev.printBuf(lastBuf);
                ret =   printerDev.printBuf(printBuf);
                if(ret != 0 ){
                    break;
                }
                LogUtil.d(TAG, "ret  ==== "  + ret);
            }
            LogUtil.d(TAG, "len  ==== "  + len);
            is.close();
            if(ret ==0) {
                printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                    {
                        add(new PrintItemObj(""));
//                        if(Build.DISPLAY.contains("Z3909")) {
//                            add(new PrintItemObj(""));
//                        }
                    }
                });

                printerDev.printRuiQueue(mListen);
//                autoCuttingPaper();
            }else{
                printRunning = false;
                showMessage(getResources().getString(R.string.print_error_code) + ret);
            }
//            if(ret ==0) {
//                String endTime = getCurTime();
//                showMessage(getResources().getString(R.string.print_end_time) + endTime);
//            }else{
//                showMessage(getResources().getString(R.string.print_error_code) + ret);
//            }
//            autoCuttingPaper();
            printerDev.close();
        } catch (Exception e) {
            printRunning = false;
            e.printStackTrace();
        } finally {
            printRunning = false;
            dropProgressbar();
//            try {
//                DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","true");
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }
    }


    private void printBias(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[]  printBuf = getBitmpBuff();
                    int     ret = 0;
                    while (biasRunning) {
                        Log.v(TAG,"printBias");
                        ret = printerDev.printBuf(printBuf);
                        if(ret !=0){
                            break;
                        }
                    }
                    printerDev.close();
                    if(ret ==0) {
                        showMessage(getResources().getString(R.string.print_success) );
                    }else{
                        showMessage(getResources().getString(R.string.print_error_code) + ret);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
    public byte[] getBitmpBuff() {
        int time = 1;
        byte[] buffer = new byte[48*8*time];
        byte data = (byte) 0x80;
        for(int j =0;j<time;j++) {
            int i =0;
            for (; i < 8; i++) {
                byte[] temp = fillAndGetBuf((byte) ((data & 0xff) >> i));
                System.arraycopy(temp, 0, buffer, 384*j+i * 48, 48);
                LogUtil.d(TAG,"temp len===  "+ HexUtil.bcd2str(temp));
            }
        }
        return buffer;
    }
    private byte[] fillAndGetBuf(byte data){
        byte[] buffer= new byte[48];
        for(int i = 0;i<48;i++){
            buffer[i] = data;
        }
        return buffer;
    }
    AidlPrinterListener mListen = new AidlPrinterListener.Stub() {
        @Override
        public void onError(int i) throws RemoteException {
            showMessage(getResources().getString(R.string.print_error_code) + i);
            String printMessage = getPrintMessageInfo();
            showMessage(printMessage);
            printRunning = false;
        }

        @Override
        public void onPrintFinish() throws RemoteException {
            String endTime = getCurTime();
            String printMessage = getPrintMessageInfo();
            showMessage(getResources().getString(R.string.print_end_time) + endTime);
            Log.d("TPW-PrinterBinderTest",getResources().getString(R.string.print_end_time) + endTime);
            showMessage(printMessage);
            autoCuttingPaper();
            printRunning = false;
        }
    };

    public void showPrintStartInfo() {
        String startTime = getCurTime();
        String printMessage = getPrintMessageInfo();
        showMessage(getResources().getString(R.string.print_start_time) + startTime);
        Log.d("TPW-PrinterBinderTest", getResources().getString(R.string.print_start_time) + startTime);
        showMessage(printMessage);
    }

    public void printBlackBlock(View v) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printBlackBlock");
        try {
            printRunning = true;
            Bitmap bitmap1 = getBmpFromAssets("bmp/black.bmp");
            printerDev.addRuiImage(bitmap1, 0);
            printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                {
                    add(new PrintItemObj("\n"));
                    if(Build.DISPLAY.contains("Z3909")) {
                        add(new PrintItemObj(""));
                    }
                }
            });
            showPrintStartInfo();
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    public void printSmBuy(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printSmBuy");
        try {
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template = PrintTemplate.getInstance();
            template.init(this,null);
            template.clear();
            template.add(new TextUnit(getResString(R.string.print_title),TextSize.NORMAL,Align.CENTER).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantname),TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantno)+"00000000000",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_terminalno)+"100000000",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_operator)+"01",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_cardno)+"6214444******0095  1",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_issno)+"01021000",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_acqno)+"01031000",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_txntype)+getResString(R.string.consume),TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_expdate)+"20/12",TextSize.SMALL,Align.LEFT).setBold(false));

            template.add(new TextUnit(getResString(R.string.print_batchno)+"000001",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_voucherno)+"000033",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_authno)+"000000",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_refno)+"1009000000033",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_datetime)+"2017/10/10 11:11:11",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_amount)+"  100.00",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_tips)+"  1.00",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_total)+"101.00",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_reference),TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit("AID:A000000333010101 TVR:008004600:",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit("ARQC:ABCDEFDGJHHHGA ATC:0020:",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_signature),TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_acknowledge),TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit("----------------------------------------------------------------",TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantcopy),TextSize.SMALL,Align.LEFT).setBold(false));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e1) {
            e1.printStackTrace();
            printRunning = false;
        }
    }

    public void printBigBuy(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printBigBuy");
        try {
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template = PrintTemplate.getInstance();
            template.init(this,null);
            template.clear();
            template.add(new TextUnit(getResString(R.string.print_title),TextSize.LARGE,Align.CENTER).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantname),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantno)+"00000000000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_terminalno)+"100000000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_operator)+"01",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_cardno)+"6214444******0095  1",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_issno)+"01021000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_acqno)+"01031000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_txntype)+getResString(R.string.consume),TextSize.LARGE,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_expdate)+"20/12",TextSize.NORMAL,Align.LEFT).setBold(false));

            template.add(new TextUnit(getResString(R.string.print_batchno)+"000001",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_voucherno)+"000033",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_authno)+"000000",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_refno)+"1009000000033",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_datetime)+"2017/10/10 11:11:11",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_amount)+"  100.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_tips)+"  1.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_total)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_reference),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("AID:A000000333010101 TVR:008004600:",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("ARQC:ABCDEFDGJHHHGA ATC:0020:",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_signature),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_acknowledge),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("----------------------------------------------------------------",TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit(getResString(R.string.print_merchantcopy),TextSize.NORMAL,Align.LEFT).setBold(false));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e1) {
            e1.printStackTrace();
            printRunning = false;
        }
    }

    public void printBitmaps(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printBitmaps");
        try {
            printRunning = true;
            Bitmap bitmap1 = getBmpFromAssets("bmp/pic_1.bmp");
            Bitmap bitmap2 = getBmpFromAssets("bmp/pic_2.bmp");
            Bitmap bitmap3 = getBmpFromAssets("bmp/pic_3.bmp");
            Bitmap bitmap4 = getBmpFromAssets("bmp/pic_4.bmp");
            Bitmap bitmap5 = getBmpFromAssets("bmp/pic_5.bmp");
            Bitmap bitmap6 = getBmpFromAssets("bmp/pic_6.bmp");
            Bitmap bitmap7 = getBmpFromAssets("bmp/pic_7.bmp");

            printerDev.addRuiImage(bitmap1, 0);
            printerDev.addRuiImage(bitmap2, 0);
            printerDev.addRuiImage(bitmap3, 0);
            printerDev.addRuiImage(bitmap4, 0);
            printerDev.addRuiImage(bitmap5, 0);
            printerDev.addRuiImage(bitmap6, 0);
            printerDev.addRuiImage(bitmap7, 0);
            printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                {
                    add(new PrintItemObj("\n"));
                    if(Build.DISPLAY.contains("Z3909")) {
                        add(new PrintItemObj(""));
                    }
                }
            });

            showPrintStartInfo();
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    private void printAddLineFree(PrintTemplate template) {
        if(Build.DISPLAY.contains("Z3909")) {
            template.add(new TextUnit(""));
        }
    }

    public void printDiamond(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printDiamond");
        try {
            printRunning = true;
            Bitmap bitmap1 = getBmpFromAssets("bmp/diamond.bmp");
            printerDev.addRuiImage(bitmap1, 0);
            printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                {
                    add(new PrintItemObj("\n"));
                    if(Build.DISPLAY.contains("Z3909")) {
                        add(new PrintItemObj(""));
                    }
                }
            });
            showPrintStartInfo();
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    public void printBiasForMeter(View view)  {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printBiasForMeter");
        try {
            printRunning = true;
            Bitmap bitmap = getBmpFromAssets("bmp/print_speed.bmp");
            for(int i = 0;i < 20; i++) {
                printerDev.addRuiImage(bitmap, 0);
            }
            printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                {
                    add(new PrintItemObj("\n"));
                    if(Build.DISPLAY.contains("Z3909")) {
                        add(new PrintItemObj(""));
                    }
                }
            });
            showPrintStartInfo();
            printerDev.printRuiQueue(mListen);
        } catch (Exception e) {
            printRunning = false;
        }
    }

    public void print_ten_times_receipts(View view) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                showMessage(getString(R.string.in_progress));
                return;
            }
        }
        if (printerDev == null) {
            showMessage(getString(R.string.fail_get_print_service));
            return;
        }
        if(printRunning) {
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"print_ten_times_receipts");
        printRunning = true;
        showProgressDialog(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String orderNo = "1234567890123456541";
                    Bitmap bitmap = getBmpFromAssets("bmp/pic_3.bmp");
                    PrintTemplate template = PrintTemplate.getInstance();
                    template.init(PrintDevActivity.this,null);
                    template.clear();
                    template.add(new ImageUnit(bitmap,bitmap.getWidth(),bitmap.getHeight()));
                    template.add(new TextUnit(getResString(R.string.print_title),TextSize.LARGE,Align.CENTER).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_merchantname),TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_merchantno)+"00000000000",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_terminalno)+"100000000",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_operator)+"01",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_cardno)+"6214444******0095  1",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_issno)+"01021000",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_acqno)+"01031000",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_txntype)+getResString(R.string.consume),TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_expdate)+"20/12",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_batchno)+"000001",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_voucherno)+"000033",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_authno)+"000000",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_refno)+"1009000000033",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_datetime)+"2017/10/10 11:11:11",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_amount)+"  100.00",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_tips)+"  1.00",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_total)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false));
                    Bitmap bitmap1 =CodeUtil.createBarcode(orderNo,350,90);
                    template.add(new ImageUnit(bitmap1,bitmap1.getWidth(),bitmap1.getHeight()));
                    template.add(new TextUnit(getResString(R.string.print_reference)+"101.00",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit("AID:A000000333010101 TVR:008004600:",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit("ARQC:ABCDEFDGJHHHGA ATC:0020:",TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_signature),TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_acknowledge),TextSize.NORMAL,Align.LEFT).setBold(false));
                    template.add(new TextUnit("-----------------------------------------------------------",TextSize.NORMAL,Align.CENTER).setBold(false));
                    template.add(new TextUnit(getResString(R.string.print_merchantcopy),TextSize.NORMAL,Align.CENTER).setBold(false));
                    bitmap = CodeUtil.createQRImage("sghgjhkkcddrrddddd123456789",300,300,null);
                    template.add(new ImageUnit(Align.CENTER,bitmap,300,300));
                    Bitmap bitmap2 = template.getPrintBitmap().copy(Bitmap.Config.ARGB_8888, false);
                    ImageUnit imageUnit = new ImageUnit(bitmap2, bitmap2.getWidth(), bitmap2.getHeight());
                    for (int i = 0; i < 9; i++) {
                        template.add(imageUnit);
                    }
//            for(int i = 0;i < 10;i++) {
                    printerDev.addRuiImage(template.getPrintBitmap(),0);
//            }
                    printAddLineFree(template);
                    printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                        {
                            add(new PrintItemObj("\n"));
                            if(Build.DISPLAY.contains("Z3909")) {
                                add(new PrintItemObj(""));
                            }
                        }
                    });
                    dropProgressbar();
                    showPrintStartInfo();
                    printerDev.printRuiQueue(mListen);
                } catch (RemoteException e) {
                    printRunning = false;
                    dropProgressbar();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void autoCuttingPaper() {
        if(autoCuttingSwitch.isChecked()) {
            try {
                printerDev.cuttingPaper(cuttingMode == 0 ? PrintCuttingMode.CUTTING_MODE_HALT : PrintCuttingMode.CUTTING_MODE_FULL);
                Log.v(TAG,"autoCuttingPaper");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printStringTest(View view) {
        if (printerDev == null) {
            showMessage(getString(R.string.fail_get_print_service));
            return;
        }
        if(printRunning) {
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printStringTest");
        try {
            printRunning = true;
            final String[] texts = new String[] {
                    "+30.0.............DARKNESS",
                    "6.0 IPS........PRINT SPEED",
                    "007...............TEAR OFF",
                    "TEAR OFF........PRINT MODE",
                    "CONTINUOUS......MEDIA TYPE",
                    "TRANSMISSIVE.IRECT-THERMAL",
                    "SENSOR SELEC..PRINT METHOD",
                    "1344 PRINT...........WIDTH",
                    "2000 LABEL..........LENGTH",
                    "P1085892/000.PRINT HEAD ID",
                    "15.IN3B0MM..MAXIMUM LENGTH",
                    "MAINT....... EARLY WARNING",
                    "CONNECTED.........USB COMM",
                    "BIDIRECT ION.PARALLEL COMM",
                    "RS232..........SERIAL COMM",
                    "9600..................BAUD",
                    "8 BITS...........DATA BITS",
                    "PARITY.............XON/XOF",
                    "HOST HANDSHA.....NONE....2",
                    "PROTOCOL.......NORMAL MODE",
                    "COMMUNICATIO.......く~>7EH",
                    "CONTROL PREF..........)5EH",
                    "FORMAT PREFI.......く，>2CH",
                    "DELIMITER CH........ZPL II",
                    "ZPL MODE..........INACTIVE",
                    "COMMAND...........OVERRIDE",
                    "FEED. MEDIA.......POWER UP",
                    "LENGTH..........HEAD CLOSE",
                    "DEFAULT...........BACKFEED",
                    "+000. LABEL............TOP",
                    "+0000 LEFT........POSITION",
                    "APPLICATOR P.......ENABLED",
                    "ERROR ON PAU....PULSE MODE",
                    "START PRINT ......DISABLED",
                    "REPRINT MODE....... SENSOR",
                    "090. MEDIA......... SENSOR",
                    "TAKE LABEL.....MARK SENSOR",
                    "MIARK MED SE....TRANS GAIN",
                    "TRANS BASE ......TRANS LED",
                    "002 100..........TRANS LED",
                    "TIARK GAIN.......MIARK LED",
                    "MODES.......MODES DISABLED",
                    "1344 8/ MM..RES RESOLUTION",
                    "4.0. LINK-0S.......VERSION",
                    "1.3 XML.............SCHEMA",
                    "VB0.20.03.........FIRMWARE",
                    "32/Bk..........HARDWARE ID",
                    "RAM................524288k",
                    "\n",
                    "\n"
            };
            PrintTemplate template = PrintTemplate.getInstance();
            template.clear();
            for (String text : texts) {
                template.add(new TextUnit(text, 25, Align.LEFT).setBold(false));
            }
//            template.add(new TextUnit("FIRMWARE IN THIS PRINTER IS COPYRIGHTED", 25, Align.LEFT).setBold(false));
//            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            showPrintStartInfo();
            printerDev.printRuiQueue(mListen);
            showMessage("Totally print " + texts.length + " lines.");
        }catch (RemoteException e) {
            e.printStackTrace();
            printRunning = false;
        }
    }

    public void printStringImageTest(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printStringImageTest");
        try {
            printRunning = true;
            Bitmap bitmap1 = getBmpFromAssets("print_strings.png");

            printerDev.addRuiImage(bitmap1, 0);
            printerDev.addRuiImage(bitmap1, 0);
            printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                {
                    add(new PrintItemObj("\n"));
                    if(Build.DISPLAY.contains("Z3909")) {
                        add(new PrintItemObj(""));
                    }
                }
            });
            showPrintStartInfo();
            showMessage("Totally print 96 lines.");
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    public void printInputTest(View view) {
        final StringBuilder printText = new StringBuilder();
        final PrintTemplate template = PrintTemplate.getInstance();
        template.clear();
        final PrintDialog.Builder builder = new PrintDialog.Builder(this);
        builder.setTitle(getString(R.string.print_input_content_title))
                .setPositiveButton(getString(R.string.determine), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(printRunning){
                            showMessage(getString(R.string.print_in_progress));
                            return;
                        }
                        try {
                            if (!"".equals(builder.getText())) {
                                template.clear();
                                if (printText != null) {
                                    int len = printText.length();
                                    Log.d(TAG, " printInputTest printText length: " + len);
                                    if (len != 0) {
                                        printText.delete(0, len);
                                    }
                                }
                                String text = "";
                                text = builder.getText();
                                printText.append(text);
                                Log.d(TAG, " printInputTest printText: " + printText);
                                PrintStyle style = builder.getPrintStyle();
                                switch (style) {
                                    case QRCODE:
                                        template.add(new ImageUnit(builder.getAlign(), QRCodeUtil.createQRImage(printText.toString(), 190,  190,null),
                                                setWidthHeight(builder.getPrintTextSize(), PrintStyle.QRCODE)[0], setWidthHeight(builder.getPrintTextSize(), PrintStyle.QRCODE)[1]));
                                        template.add(new TextUnit("\n\n"));
                                        printAddLineFree(template);
                                        printerDev.addRuiImage(template.getPrintBitmap(),0);
                                        break;
                                    case TEXT:
                                        template.add(new TextUnit(printText.toString(), builder.getPrintTextSize(), builder.getAlign()).setBold(false));
                                        template.add(new TextUnit("\n\n"));
                                        printAddLineFree(template);
                                        printerDev.addRuiImage(template.getPrintBitmap(),0);
                                        break;
                                    case BARCODE:
                                        template.add(new ImageUnit(builder.getAlign(), CodeUtil.createBarcode(printText.toString(),
                                                        setWidthHeight(builder.getPrintTextSize(), PrintStyle.BARCODE)[0], setWidthHeight(builder.getPrintTextSize(), PrintStyle.BARCODE)[1]),
                                                    setWidthHeight(builder.getPrintTextSize(), PrintStyle.BARCODE)[0], setWidthHeight(builder.getPrintTextSize(), PrintStyle.BARCODE)[1]));
                                        template.add(new TextUnit("\n\n"));
                                        printAddLineFree(template);
                                        printerDev.addRuiImage(template.getPrintBitmap(),0);
                                        break;
                                }
                                printRunning = true;
                                showPrintStartInfo();
                                printerDev.printRuiQueue(mListen);
                                if (!"".equals(printText.toString())) {
                                    showMessage(getString(R.string.print_input_text_show) + printText);
                                }
//                                dialog.dismiss();
                            }else {
//                                Toast.makeText(PrintDevActivity.this, getString(R.string.fail), Toast.LENGTH_SHORT).show();
                                showToast(getString(R.string.fail));
                            }
                        }catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }
    public void printDrag(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
//        try {
//            printerDev.printRollback(1000);
//            DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","false");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        Log.v(TAG,"printDrag");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    printRunning = true;
                    InputStream is = getClass().getClassLoader().getResourceAsStream("assets/print_drag_data.txt");
                    byte[] buffer = new byte[BUFF_LEN];
                    int byteCount = 0;
                    int len =0;
                    int ret  =0;
                    showPrintStartInfo();
                    while (true) {
                        byteCount = is.read(buffer);
                        if(byteCount ==-1) {
                            break;
                        }
                        len +=byteCount;
                        if(byteCount<BUFF_LEN){
                            byte[]   lastBuf = new byte[byteCount];
                            LogUtil.d(TAG, " byteCount ==== "  + byteCount);
                            System.arraycopy(buffer,0,lastBuf,0,byteCount);
                            buffer = lastBuf;
                        }
                        String str =  new String(buffer).toUpperCase();
                        LogUtil.d(TAG, " ==== "  +str);
                        byte[] printBuf  = HexUtil.hexStringToByte(str);
                        ret = printerDev.printBuf(printBuf);
                        if(ret != 0 ){
                            break;
                        }
                    }
                    is.close();
                    if(ret ==0) {
                        printerDev.addRuiText(new ArrayList<PrintItemObj>() {
                            {
                                add(new PrintItemObj("\n"));
                                if(Build.DISPLAY.contains("Z3909")) {
                                    add(new PrintItemObj(""));
                                }
                            }
                        });

                        printerDev.printRuiQueue(mListen);
//                autoCuttingPaper();
                    }else{
                        printRunning = false;
                        showMessage(getResources().getString(R.string.print_error_code) + ret);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    try {
//                        DeviceServiceManager.getInstance().getSystemManager().SystemPropertiesSet("persist.sys.usdk.rollback","true");
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();
    }


    public int[] setWidthHeight(int size, PrintStyle style) {
        if (style == PrintStyle.QRCODE) {
            switch (size) {
                case TextSize.SMALL:
                    return new int[] {90, 90};
                case TextSize.NORMAL:
                    return new int[] {190, 190};
                case TextSize.LARGE:
                    return new int[] {290, 290};
            }
            return new int[] {190, 190};
        } else if (style == PrintStyle.BARCODE) {
            switch (size) {
                case TextSize.SMALL:
                    return new int[]{280, 128};
                case TextSize.NORMAL:
                    return new int[]{350, 160};
                case TextSize.LARGE:
                    return new int[]{420, 192};
            }
            return new int[]{350, 120};
        }else {
            return null;
        }
    }

    private Toast toast;
    public void showToast(String msg) {
        if (toast != null) {
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast = Toast.makeText(PrintDevActivity.this, msg, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void printHighTemperatureTest(View view) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printHighTemperatureTest");
        try {
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template =PrintTemplate.getInstance();
            template.clear();
            template.add(new ImageUnit(CodeUtil.createBarcode("23418753401333", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("03400471", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("2341875340111", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("23411875", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("*23418*", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("234187534011", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("23418", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("{A23418333", 350,  160),350,160));
            template.add(new ImageUnit(CodeUtil.createBarcode("123456765432123412", 350,  160),350,160));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            Log.v(TAG,"printHighTemperatureTest : " + printTemperatureTimes);
            printerDev.printRuiQueue(new AidlPrinterListener.Stub() {
                @Override
                public void onError(int ret) throws RemoteException {
                    printRunning = false;
                    showMessage(getResources().getString(R.string.print_error_code) + ret);
                    String printMessage = getPrintMessageInfo();
                    showMessage(printMessage);
                    showMessage("print times : " + (++printTemperatureTimes));
                    printTemperatureTimes = 0;
                }

                @Override
                public void onPrintFinish() throws RemoteException {
                    printRunning = false;
                    String printMessage = getPrintMessageInfo();
                    showMessage(printMessage);
                    showMessage("print times : " + ++printTemperatureTimes);
                    printHighTemperatureTest(null);
                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }

    /**
     * Template print
     *
     * @param v
     * @createtor：Administrator
     * @date:2019-9-7
     */
    public void printGalleryBitmap(View v) {
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
    }

    public void printCompatibility(View v) {
        String binaryString = "1111111"; // 示例：ASCII码65对应的二进制（大写字母'A'）

        // 将二进制字符串转换为整数
        int decimalValue = Integer.parseInt(binaryString, 2);

        // 将整数转换为字符
        char asciiChar = (char) decimalValue;
        String[] asciiTexts = new String[16];
        int index = 0;
        StringBuilder charTable = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            charTable.append((char) i);
            System.out.println("charTable字符: " + charTable);
            if ((i + 1) % 8 == 0) {
                asciiTexts[index] = charTable.toString();
                charTable = new StringBuilder();
                index++;
            }
        }
        String commandMode = "Command mode :  EPSON(ESC/POS)\n" +
                             "Interface:  USB&\n" +
                             "\t\tSeria1 19200,None,8,1\n" +
                             "\t\tEthernet(10Base/100Base)\n" +
                             "DHCP:       Disabled\n" +
                             "Ethernet ID:20-00-20-17-00-0c\n" +
                             "Ip address: 130.33.0.32\n" +
                             "Port:       9100\n" +
                             "Netmask :   255 255.255.0\n" +
                             "Gateway:    192.168,1.1\n" +
                             "Save Paper: NO\n" +
                             "NPRePrint   YES\n" +
                             "PNEStart:   YES\n" +
                             "BMMode:     NO\n" +
                             "Cutter:     YES\n" +
                             "Beeper:     YES\n" +
                             "Chinese character mode: Yes\n" +
                             "Drawer contro1:     Yes\n" +
                             "Character per line: 48-fontA\n" +
                             "                    64-fontB\n" +
                             "                    64-fontC\n" +
                             "Print Density:      Light\n" +
                             "Default code page:  page 255\n";
        String codePage = "Code Page:\n" +
                          "0:PC437[Std.Europe]       1.Katakana\n" +
                          "2:PC850[Mu1tilingua1]     3.PC860[Portuguese]\n" +
                          "4:PC863[Canadian]         5.CP865[Nordic]\n" +
                          "6:PC1251[Cyri11ic]        7.PC866[Cyriiliec]\n" +
                          "8:MIK[Cyri11ic/Bulgarian] 9.PC755[EastEurope]\n" +
                          "10:Iran                   11.RESERVE\n" +
                          "12:RESERVE                13.RESERVE\n" +
                          "14:RESERVE                15.PC862[Hebrew]\n" +
                          "16:PC1252 Latin I         17.PC1253[Greek]\n" +
                          "18:PC852 [Latina 2]       19.PC858 Latin\n" +
                          "20:Iran II                21.Latvian\n" +
                          "22:PC864[Arabic]          23.IS0-8859-1\n" +
                          "24:CP737[Greek]           25.PC1257[Baltic]\n" +
                          "26:Thai                   27.PC720[Arabic]\n" +
                          "28:PC855                  29,PC857[Turkish]\n" +
                          "30:PC1250[Central Eurpoe] 31.PC775\n" +
                          "32:PC1254[Turkish]      \n33 RESERVE\n" +
                          "34:PC1256[Arabic]         35.PC1258[Vietnam]\n" +
                          "36:IS0-8859-2[Latin 2]    37IS0-8859-3\n" +
                          "38:IS0-8859-4[Ba1tic]     39.IS0-8859-5\n" +
                          "40:IS0-8859-6[Arabic]     41.IS0-8859-7\n" +
                          "42:IS0-8859-8[Hebrew]     43.IS0-8859-9\n" +
                          "44:IS0-8859-15 [Latin 3]  45.Thai2\n" +
                          "46:PC856                  47.PC874\n";
        String functions = "DIP-8 Function     ON OFF\n" +
                           "SW-1 Select cutter No Yes\n" +
                           "SW-2 Select beeper Yes No\n" +
                           "SW-3 Select print density\n Dark Light\n" +
                           "SW-4 Two-byte char cod  No Yes\n" +
                           "SW-5 Character per line 42 48\n" +
                           "SW-6 Select drawer      Yes No\n" +
                           "SW-7 Select baudrate\n" +
                           "SW-8 Select baudrate\n" +
                           "\n" +
                           "SW-7 SW-8          Baudrate\n" +
                           "ON   ON            38400\n" +
                           "OFF  ON            115200\n" +
                           "ON   OFF           9600\n" +
                           "OFF  OFF           19200\n" +
                           "\n" +
                           "DIP-8 Status\n" +
                           "SW-1 SW-2 SW-3 SW-4 SW-5 SW-6 SW-7 SW-8\n" +
                           "       ON                  ON\n" +
                           " OFF       OFF  OFF  OFF       OFF  OFF\n" +
                           "DEGREE:30\n" +
                           "MILAGE:65.93\n" +
                           "HEAT D0T=640 0N=600 0FF=70\n" +
                           "VERSION AT407_V1.20 2021-07-29\n" +
                           "CPUID:\n40007117-88784000-0818870c-00000400\n";
        // 打印结果
        if(printRunning){
            showMessage(getString(R.string.print_in_progress));
            return;
        }
        Log.v(TAG,"printCompatibility");
        try {
            int fontSize = 25;
            printRunning = true;
            showPrintStartInfo();
            PrintTemplate template =PrintTemplate.getInstance();
            template.clear();
            template.add(new TextUnit("CHARACTER CODE TABLE", fontSize, Align.LEFT));
            template.add(new TextUnit("01234567 89ABCDEF", fontSize, Align.CENTER));
            template.add(new TextUnit("0- " + asciiTexts[0] + "  " + asciiTexts[1],fontSize,Align.CENTER));
            template.add(new TextUnit("1- " + asciiTexts[2] + "  " + asciiTexts[3],fontSize,Align.CENTER));
            template.add(new TextUnit("2- " + asciiTexts[4] + "  " + asciiTexts[5],fontSize,Align.CENTER));
            template.add(new TextUnit("3- " + asciiTexts[6] + "  " + asciiTexts[7],fontSize,Align.CENTER));
            template.add(new TextUnit("4- " + asciiTexts[8] + "  " + asciiTexts[9],fontSize,Align.CENTER));
            template.add(new TextUnit("5- " + asciiTexts[10] + "  " + asciiTexts[11],fontSize,Align.CENTER));
            template.add(new TextUnit("6- " + asciiTexts[12] + "  " + asciiTexts[13],fontSize,Align.CENTER));
            template.add(new TextUnit("7- " + asciiTexts[14] + "  " + asciiTexts[15],fontSize,Align.CENTER));
            template.add(new TextUnit("GBK编码", fontSize, Align.LEFT));
            template.add(new TextUnit("第一区汉字(6787个):", fontSize, Align.LEFT));
            template.add(new TextUnit("啊阿埃挨哎唉哀她癌蔼矮艾碍爱隘鞍氨安俺按暗岸胺案肮昂盎凹敖熬翱袄傲奥懊澳芭捌扒叭吧爸八疤巴拔跋靶把把坝霸罢爸白柏百摆佰败拜碑斑班搬扳般颁板版扮拌伴瓣半办绊邦帮梆榜膀绑棒磅蚌镑傍谤苞胞包褒剥\n", fontSize, Align.LEFT));
            template.add(new TextUnit(commandMode, fontSize, Align.LEFT));
            template.add(new TextUnit(codePage, fontSize, Align.LEFT));
            template.add(new TextUnit(functions, fontSize, Align.LEFT));
            template.add(new ImageUnit(Align.CENTER, QRCodeUtil.createQRImage("UID:400c7117-88764000-0818870c-00000400", 200, 200, null), 200, 200));
            template.add(new TextUnit("\n\n"));
            printAddLineFree(template);
            printerDev.addRuiImage(template.getPrintBitmap(),0);
            printerDev.printRuiQueue(mListen);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            printRunning = false;
        }
    }


    public void showRet(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView imageView = new ImageView(this);
        builder.setView(imageView);
        builder.setPositiveButton("Print", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(printRunning){
                    showMessage(getString(R.string.print_in_progress));
                    return;
                }
                Log.v(TAG,"printTemplate");
                try {
                    showPrintStartInfo();
                    PrintTemplate template =PrintTemplate.getInstance();
                    template.clear();
                    int height = bitmap.getHeight() * 384 / bitmap.getWidth();
                    template.add(new ImageUnit(bitmap,384, height));
                    template.add(new TextUnit("\n\n"));
                    printAddLineFree(template);
                    printerDev.addRuiImage(template.getPrintBitmap(),0);
                    printRunning = true;
                    printerDev.printRuiQueue(mListen);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    printRunning = false;
                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        imageView.setImageBitmap(bitmap);
        Dialog dialog = builder.create();
        dialog.show();
    }
//    private Bitmap compressPic(Bitmap bitmapOrg) {
//        // 获取这个图片的宽和高
//        int width = bitmapOrg.getWidth();
//        int height = bitmapOrg.getHeight();
//        // 定义预转换成的图片的宽度和高度
//        int newWidth = 384;
//        int newHeight = newWidth * height / width;
//        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
//        Canvas targetCanvas = new Canvas(targetBmp);
//        targetCanvas.drawColor(0xffffffff);
//        targetCanvas.drawBitmap(bitmapOrg, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
//        return targetBmp;
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap printBmp = null;
        if (requestCode == 2 ) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    printBmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
//                    Log.v(TAG,"printBmp" + printBmp.getDensity());
                    showRet(printBmp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
