package com.example.madassignmentphotogallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button btnTakePhoto, btnChooseFolder;
    Uri photoUri;
    File currentPhotoFile;

    ActivityResultLauncher<Uri> cameraLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChooseFolder = findViewById(R.id.btnChooseFolder);

        // Camera result
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentPhotoFile != null) {
                        Toast.makeText(this,
                                "Photo saved: " + currentPhotoFile.getName(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Delete empty file if photo was cancelled
                        if (currentPhotoFile != null && currentPhotoFile.exists()) {
                            currentPhotoFile.delete();
                        }
                        Toast.makeText(this, "Photo cancelled",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Permission result
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission needed!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Take Photo button
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        // Choose Folder button
        btnChooseFolder.setOnClickListener(v -> showFolderPicker());
    }

    private void showFolderPicker() {
        // Get the app's pictures folder - this is where camera saves
        File appPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Build folder list
        java.util.List<String> names = new java.util.ArrayList<>();
        java.util.List<File> folders = new java.util.ArrayList<>();

        // Always show app pictures folder first
        if (appPictures != null) {
            appPictures.mkdirs(); // create if not exists
            names.add("📷 My Camera Photos (" + countImages(appPictures) + " images)");
            folders.add(appPictures);
        }

        // Add DCIM/Camera folder
        File dcimCamera = new File(Environment.getExternalStorageDirectory(),
                "DCIM/Camera");
        if (dcimCamera.exists()) {
            names.add("📁 DCIM/Camera (" + countImages(dcimCamera) + " images)");
            folders.add(dcimCamera);
        }

        // Add Downloads folder
        File downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        if (downloads != null && downloads.exists()) {
            names.add("📁 Downloads (" + countImages(downloads) + " images)");
            folders.add(downloads);
        }

        String[] nameArray = names.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Choose a Folder")
                .setItems(nameArray, (dialog, which) -> {
                    File selectedFolder = folders.get(which);
                    Intent intent = new Intent(this, GalleryActivity.class);
                    intent.putExtra("folderPath",
                            selectedFolder.getAbsolutePath());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Count images in a folder
    private int countImages(File folder) {
        if (!folder.exists()) return 0;
        File[] files = folder.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")
                    || name.endsWith(".png") || name.endsWith(".webp")) {
                count++;
            }
        }
        return count;
    }

    private void launchCamera() {
        try {
            // Create unique filename for each photo
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            String fileName = "PHOTO_" + timeStamp + ".jpg";

            // Save to app's pictures directory
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir != null) {
                storageDir.mkdirs();
            }

            currentPhotoFile = new File(storageDir, fileName);

            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    currentPhotoFile
            );

            cameraLauncher.launch(photoUri);

        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}