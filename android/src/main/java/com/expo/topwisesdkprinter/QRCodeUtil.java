package com.expo.topwisesdkprinter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * QR code utility class for generating QR code bitmaps
 */
public class QRCodeUtil {
    private static final String TAG = "QRCodeUtil";

    /**
     * Create a QR code image
     * 
     * @param content QR code content
     * @param width Width of the QR code
     * @param height Height of the QR code
     * @param logo Logo to be overlaid on the QR code (can be null)
     * @return Bitmap containing the QR code
     */
    public static Bitmap createQRImage(String content, int width, int height, Bitmap logo) {
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "QR code content cannot be empty");
            return null;
        }
        
        try {
            // Set QR code parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // Higher error correction level allows the QR code to be readable even when partially obscured
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1); // Smaller margin to keep QR code compact
            
            // Generate QR code bit matrix
            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE, width, height, hints);
            
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            
            // Create bitmap from pixels
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            
            // Add logo if provided
            if (logo != null) {
                return addLogoToQRCode(bitmap, logo);
            }
            
            return bitmap;
        } catch (WriterException e) {
            Log.e(TAG, "QR code generation error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Add a logo to the center of a QR code
     * 
     * @param qrBitmap QR code bitmap
     * @param logo Logo bitmap
     * @return QR code with logo
     */
    private static Bitmap addLogoToQRCode(Bitmap qrBitmap, Bitmap logo) {
        if (qrBitmap == null || logo == null) {
            return qrBitmap;
        }
        
        int qrWidth = qrBitmap.getWidth();
        int qrHeight = qrBitmap.getHeight();
        
        // Size of logo should be about 20% of QR code size
        int logoWidth = qrWidth / 5;
        int logoHeight = qrHeight / 5;
        
        // Scale logo to the right size
        float scaleWidth = ((float) logoWidth) / logo.getWidth();
        float scaleHeight = ((float) logoHeight) / logo.getHeight();
        
        // Create a matrix for the manipulation
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        
        // Resize the logo bitmap
        Bitmap scaledLogo = Bitmap.createBitmap(
                logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix, true);
        
        // Create a bitmap to hold both QR code and logo
        Bitmap combined = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.ARGB_8888);
        
        // Paint both bitmaps
        Canvas canvas = new Canvas(combined);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        
        // Calculate position for logo (center)
        float logoX = (qrWidth - logoWidth) / 2;
        float logoY = (qrHeight - logoHeight) / 2;
        
        // Draw rounded rectangle background for logo
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(logoX - 2, logoY - 2, logoX + logoWidth + 2, logoY + logoHeight + 2, paint);
        
        // Draw the logo
        canvas.drawBitmap(scaledLogo, logoX, logoY, null);
        
        return combined;
    }
}
