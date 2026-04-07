package com.example.madassignmentphotogallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    GridView gridView;
    TextView tvFolderName;
    List<File> imageFiles = new ArrayList<>();
    ImageAdapter adapter;
    String folderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gridView = findViewById(R.id.gridView);
        tvFolderName = findViewById(R.id.tvFolderName);

        folderPath = getIntent().getStringExtra("folderPath");
        if (folderPath == null) {
            Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Convert File list to Uri list for adapter
        List<Uri> imageUris = new ArrayList<>();
        adapter = new ImageAdapter(this, imageUris);
        gridView.setAdapter(adapter);

        // On image click
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra("imagePath", imageFiles.get(position).getAbsolutePath());
            startActivityForResult(intent, 100);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh every time we come back to this screen
        refreshImages();
    }

    private void refreshImages() {
        imageFiles.clear();

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            Toast.makeText(this, "Folder not accessible", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            // Sort by date - newest first
            Arrays.sort(files, (f1, f2) ->
                    Long.compare(f2.lastModified(), f1.lastModified()));

            for (File file : files) {
                if (isImageFile(file)) {
                    imageFiles.add(file);
                }
            }
        }

        // Update adapter with new Uri list
        List<Uri> uriList = new ArrayList<>();
        for (File f : imageFiles) {
            uriList.add(Uri.fromFile(f));
        }

        // Update adapter data
        ((ImageAdapter) gridView.getAdapter()).updateData(uriList);

        // Update folder name with count
        tvFolderName.setText("📁 " + folder.getName() +
                " (" + imageFiles.size() + " images)");

        if (imageFiles.isEmpty()) {
            Toast.makeText(this, "No images in this folder",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isImageFile(File file) {
        if (!file.isFile()) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".gif")
                || name.endsWith(".bmp") || name.endsWith(".webp");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            refreshImages();
        }
    }
}