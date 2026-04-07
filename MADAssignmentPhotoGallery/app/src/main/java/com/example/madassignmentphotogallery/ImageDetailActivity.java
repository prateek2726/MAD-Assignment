package com.example.madassignmentphotogallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    ImageView ivDetailImage;
    TextView tvName, tvPath, tvSize, tvDate;
    Button btnDelete;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        ivDetailImage = findViewById(R.id.ivDetailImage);
        tvName = findViewById(R.id.tvName);
        tvPath = findViewById(R.id.tvPath);
        tvSize = findViewById(R.id.tvSize);
        tvDate = findViewById(R.id.tvDate);
        btnDelete = findViewById(R.id.btnDelete);

        imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath == null) {
            finish();
            return;
        }

        loadImage();
        loadDetails();

        btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this image?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    private void loadImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        if (bitmap != null) {
            ivDetailImage.setImageBitmap(bitmap);
        }
    }

    private void loadDetails() {
        File file = new File(imagePath);
        tvName.setText(file.getName());
        tvPath.setText(file.getAbsolutePath());

        long bytes = file.length();
        String size = bytes > 1048576
                ? String.format(Locale.getDefault(), "%.2f MB", bytes / 1048576.0)
                : (bytes / 1024) + " KB";
        tvSize.setText(size);

        tvDate.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                Locale.getDefault()).format(new Date(file.lastModified())));
    }

    private void deleteImage() {
        File file = new File(imagePath);

        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Make sure file is writable
        file.setWritable(true, false);

        if (file.delete()) {
            // Success
            Toast.makeText(this, "Image deleted!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent()
                    .putExtra("deletedPath", imagePath));
            finish();
        } else {
            // Show file details to debug
            Toast.makeText(this,
                    "Delete failed.\nPath: " + imagePath +
                            "\nWritable: " + file.canWrite() +
                            "\nExists: " + file.exists(),
                    Toast.LENGTH_LONG).show();
        }
    }
}