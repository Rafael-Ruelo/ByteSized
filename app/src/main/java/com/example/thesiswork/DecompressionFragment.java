package com.example.thesiswork;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DecompressionFragment extends Fragment {

    private ImageView imageView;
    private Button decompressButton;
    private ProgressBar progressBar;
    private TextView statusText;

    private String imageUriString;
    private boolean isSingleFile;
    private ExecutorService executor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decompression, container, false);

        if (getArguments() != null) {
            imageUriString = getArguments().getString("imageUri");
            isSingleFile = getArguments().getBoolean("isSingleFile", true);
        }

        imageView = view.findViewById(R.id.imageView);
        decompressButton = view.findViewById(R.id.decompressButton);
        progressBar = view.findViewById(R.id.progressBar);
        statusText = view.findViewById(R.id.statusText);

        Glide.with(this).load(Uri.parse(imageUriString)).into(imageView);

        decompressButton.setOnClickListener(v -> performDecompression());

        return view;
    }

    private void performDecompression() {
        decompressButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Decompressing...");

        executor.execute(() -> {
            try {
                String imagePath = getRealPathFromURI(Uri.parse(imageUriString));
                File outputDir = requireContext().getExternalFilesDir(null);

                ImageCompressor.CompressionResult result =
                        ImageCompressor.decompressImage(imagePath, outputDir);

                requireActivity().runOnUiThread(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("originalUri", imageUriString);
                    bundle.putString("compressedPath", result.compressedFile.getAbsolutePath());
                    bundle.putLong("originalSize", result.originalSize);
                    bundle.putLong("compressedSize", result.compressedSize);
                    bundle.putInt("compressionRatio", result.compressionRatio);
                    bundle.putBoolean("isSingleFile", isSingleFile);

                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_decompression_to_result, bundle);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Decompression failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    decompressButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("");
                });
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return contentUri.getPath();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}