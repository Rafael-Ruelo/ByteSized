package com.example.thesiswork;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCompressor {

    public static class CompressionResult {
        public File compressedFile;
        public long originalSize;
        public long compressedSize;
        public int compressionRatio;

        public CompressionResult(File compressedFile, long originalSize, long compressedSize) {
            this.compressedFile = compressedFile;
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
            this.compressionRatio = (int) ((1 - ((double) compressedSize / originalSize)) * 100);
        }
    }

    private static Bitmap.CompressFormat getWebPFormat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Bitmap.CompressFormat.WEBP_LOSSY;
        } else {
            return Bitmap.CompressFormat.WEBP;
        }
    }

    public static CompressionResult compressImage(String sourcePath, File outputDir, AppPreset preset) throws IOException {
        File sourceFile = new File(sourcePath);
        long originalSize = sourceFile.length();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(sourcePath, options);

        if (bitmap == null) {
            throw new IOException("Failed to decode image");
        }

        int quality = preset.getQuality();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = getWebPFormat();

        do {
            baos.reset();
            bitmap.compress(format, quality, baos);

            if (baos.size() <= preset.getMaxSize() || quality <= 10) {
                break;
            }
            quality -= 5;
        } while (true);

        File compressedFile = new File(outputDir, "compressed_" + System.currentTimeMillis() + ".webp");
        FileOutputStream fos = new FileOutputStream(compressedFile);
        fos.write(baos.toByteArray());
        fos.close();

        bitmap.recycle();

        return new CompressionResult(compressedFile, originalSize, compressedFile.length());
    }

    public static CompressionResult decompressImage(String sourcePath, File outputDir) throws IOException {
        File sourceFile = new File(sourcePath);
        long originalSize = sourceFile.length();

        Bitmap bitmap = BitmapFactory.decodeFile(sourcePath);
        if (bitmap == null) {
            throw new IOException("Failed to decode image for decompression");
        }

        File decompressedFile = new File(outputDir, "decompressed_" + System.currentTimeMillis() + ".webp");
        FileOutputStream fos = new FileOutputStream(decompressedFile);
        bitmap.compress(getWebPFormat(), 100, fos);
        fos.close();

        bitmap.recycle();

        return new CompressionResult(decompressedFile, originalSize, decompressedFile.length());
    }

    public static String formatBytes(long bytes) {
        if (bytes == 0) return "0 Bytes";
        int k = 1024;
        String[] sizes = {"Bytes", "KB", "MB"};
        int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round((bytes / Math.pow(k, i)) * 100.0) / 100.0 + " " + sizes[i];
    }
}