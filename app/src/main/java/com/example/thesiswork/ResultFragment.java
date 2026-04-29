package com.example.thesiswork;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStream;

public class ResultFragment extends Fragment {

    private ImageView originalImageView, compressedImageView;
    private TextView originalSizeText, compressedSizeText, compressionRatioText;
    private Button downloadButton, shareButton, newImageButton;

    private String originalUri, compressedPath, presetName;
    private long originalSize, compressedSize;
    private int compressionRatio;
    private boolean isSingleFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        if (getArguments() != null) {
            originalUri = getArguments().getString("originalUri");
            compressedPath = getArguments().getString("compressedPath");
            originalSize = getArguments().getLong("originalSize");
            compressedSize = getArguments().getLong("compressedSize");
            compressionRatio = getArguments().getInt("compressionRatio");
            isSingleFile = getArguments().getBoolean("isSingleFile", true);
            presetName = getArguments().getString("presetName", "Unknown");
        }

        originalImageView = view.findViewById(R.id.originalImageView);
        compressedImageView = view.findViewById(R.id.compressedImageView);
        originalSizeText = view.findViewById(R.id.originalSizeText);
        compressedSizeText = view.findViewById(R.id.compressedSizeText);
        compressionRatioText = view.findViewById(R.id.compressionRatioText);
        downloadButton = view.findViewById(R.id.downloadButton);
        shareButton = view.findViewById(R.id.shareButton);
        newImageButton = view.findViewById(R.id.newImageButton);

        setupViews();

        downloadButton.setOnClickListener(v -> downloadFile());
        shareButton.setOnClickListener(v -> shareFile());
        newImageButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_result_to_upload));

        return view;
    }

    private void setupViews() {
        if (originalUri != null) {
            Glide.with(this).load(Uri.parse(originalUri)).into(originalImageView);
        }
        if (compressedPath != null) {
            Glide.with(this).load(new File(compressedPath)).into(compressedImageView);
        }

        originalSizeText.setText("Size: " + ImageCompressor.formatBytes(originalSize));
        compressedSizeText.setText("Size: " + ImageCompressor.formatBytes(compressedSize));
        compressionRatioText.setText(compressionRatio + "% smaller");
    }

    private void downloadFile() {
        try {
            if (isSingleFile) {
                saveImageToGallery();
            } else {
                saveZipToDownloads();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Download failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile() {
        try {
            File fileToShare = new File(compressedPath);

            if (!isSingleFile) {
                fileToShare = createZipWithMetadata(fileToShare);
            }

            Uri fileUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getApplicationContext().getPackageName() + ".provider",
                    fileToShare);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(isSingleFile ? "image/webp" : "application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share image via"));

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Share failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToGallery() throws Exception {
        File sourceFile = new File(compressedPath);
        String fileName = "ByteSized_" + System.currentTimeMillis() + ".webp";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/webp");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ByteSized");

        Uri imageUri = requireContext().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri == null) throw new Exception("Failed to create file in gallery");

        try (OutputStream os = requireContext().getContentResolver().openOutputStream(imageUri);
             FileInputStream fis = new FileInputStream(sourceFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        Toast.makeText(requireContext(), "Saved to Photos > ByteSized!", Toast.LENGTH_SHORT).show();
    }

    private void saveZipToDownloads() throws Exception {
        File zipFile = createZipWithMetadata(new File(compressedPath));
        String fileName = "ByteSized_" + System.currentTimeMillis() + ".zip";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/zip");
        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/ByteSized");

        Uri downloadUri = requireContext().getContentResolver()
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (downloadUri == null) throw new Exception("Failed to create file in Downloads");

        try (OutputStream os = requireContext().getContentResolver().openOutputStream(downloadUri);
             FileInputStream fis = new FileInputStream(zipFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        Toast.makeText(requireContext(), "Saved to Downloads > ByteSized!", Toast.LENGTH_SHORT).show();
    }

    private File createZipWithMetadata(File imageFile) throws Exception {
        File zipFile = new File(requireContext().getExternalFilesDir(null), "compressed_image.zip");
        File metadataFile = new File(requireContext().getExternalFilesDir(null), "metadata.json");

        FileWriter writer = new FileWriter(metadataFile);
        writer.write("{\n");
        writer.write("  \"originalSize\": " + originalSize + ",\n");
        writer.write("  \"compressedSize\": " + compressedSize + ",\n");
        writer.write("  \"compressionRatio\": \"" + compressionRatio + "%\",\n");
        writer.write("  \"preset\": \"" + presetName + "\"\n");
        writer.write("}");
        writer.close();

        ZipFile zip = new ZipFile(zipFile);
        zip.addFile(imageFile);
        zip.addFile(metadataFile);

        metadataFile.delete();

        return zipFile;
    }
}