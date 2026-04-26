package com.example.thesiswork;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.FileWriter;

public class DecompressionResultFragment extends Fragment {

    private ImageView originalImageView, decompressedImageView;
    private TextView originalSizeText, decompressedSizeText;
    private Button downloadButton, newImageButton;

    private String originalUri, decompressedPath;
    private long originalSize, decompressedSize;
    private boolean isSingleFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_decompression, container, false);

        if (getArguments() != null) {
            originalUri = getArguments().getString("originalUri");
            decompressedPath = getArguments().getString("compressedPath"); // Reusing key for simplicity
            originalSize = getArguments().getLong("originalSize");
            decompressedSize = getArguments().getLong("compressedSize");
            isSingleFile = getArguments().getBoolean("isSingleFile", true);
        }

        originalImageView = view.findViewById(R.id.originalImageView);
        decompressedImageView = view.findViewById(R.id.decompressedImageView);
        originalSizeText = view.findViewById(R.id.originalSizeText);
        decompressedSizeText = view.findViewById(R.id.decompressedSizeText);
        downloadButton = view.findViewById(R.id.downloadButton);
        newImageButton = view.findViewById(R.id.newImageButton);

        setupViews();

        downloadButton.setOnClickListener(v -> downloadFile());
        newImageButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_result_to_upload));

        return view;
    }

    private void setupViews() {
        Glide.with(this).load(Uri.parse(originalUri)).into(originalImageView);
        Glide.with(this).load(new File(decompressedPath)).into(decompressedImageView);

        originalSizeText.setText("Size: " + ImageCompressor.formatBytes(originalSize));
        decompressedSizeText.setText("Size: " + ImageCompressor.formatBytes(decompressedSize));
    }

    private void downloadFile() {
        try {
            File fileToShare = new File(decompressedPath);

            if (!isSingleFile) {
                fileToShare = createZipWithMetadata(fileToShare);
            }

            Uri fileUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getApplicationContext().getPackageName() + ".provider",
                    fileToShare);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(isSingleFile ? "image/*" : "application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Save restored file"));

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Save failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private File createZipWithMetadata(File imageFile) throws Exception {
        File zipFile = new File(requireContext().getExternalFilesDir(null), "restored_image.zip");
        File metadataFile = new File(requireContext().getExternalFilesDir(null), "restoration_metadata.json");

        FileWriter writer = new FileWriter(metadataFile);
        writer.write("{\n");
        writer.write("  \"originalSize\": " + originalSize + ",\n");
        writer.write("  \"restoredSize\": " + decompressedSize + ",\n");
        writer.write("  \"status\": \"Restored\"\n");
        writer.write("}");
        writer.close();

        ZipFile zip = new ZipFile(zipFile);
        zip.addFile(imageFile);
        zip.addFile(metadataFile);

        metadataFile.delete();

        return zipFile;
    }
}