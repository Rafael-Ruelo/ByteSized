package com.example.thesiswork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class UploadFragment extends Fragment {

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        showModeSelectionDialog(imageUri);
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        Button selectImageButton = view.findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(v -> checkPermissionAndOpenPicker());

        return view;
    }

    private void checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showModeSelectionDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mode_selection, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.compressButton).setOnClickListener(v -> {
            dialog.dismiss();
            showDownloadFormatDialog(imageUri, true);
        });

        dialogView.findViewById(R.id.decompressButton).setOnClickListener(v -> {
            dialog.dismiss();
            showDownloadFormatDialog(imageUri, false);
        });

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDownloadFormatDialog(Uri imageUri, boolean isCompressMode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_download_format, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.singleFileButton).setOnClickListener(v -> {
            dialog.dismiss();
            navigateToNextScreen(imageUri, isCompressMode, true);
        });

        dialogView.findViewById(R.id.folderButton).setOnClickListener(v -> {
            dialog.dismiss();
            navigateToNextScreen(imageUri, isCompressMode, false);
        });

        dialogView.findViewById(R.id.skipButton).setOnClickListener(v -> {
            dialog.dismiss();
            navigateToNextScreen(imageUri, isCompressMode, true);
        });

        dialog.show();
    }

    private void navigateToNextScreen(Uri imageUri, boolean isCompressMode, boolean isSingleFile) {
        Bundle bundle = new Bundle();
        bundle.putString("imageUri", imageUri.toString());
        bundle.putBoolean("isCompressMode", isCompressMode);
        bundle.putBoolean("isSingleFile", isSingleFile);

        if (isCompressMode) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_upload_to_compression, bundle);
        } else {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_upload_to_result, bundle);
        }
    }
}