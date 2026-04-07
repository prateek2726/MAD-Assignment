package com.example.madassignmentmediaplayer;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // UI elements
    VideoView videoView;
    TextView tvStatus;
    Button btnOpenFile, btnOpenUrl, btnPlay, btnPause, btnStop, btnRestart;

    // MediaPlayer for audio
    MediaPlayer mediaPlayer;

    // Track mode and saved URI/URL
    boolean isAudioMode = false;
    boolean isVideoMode = false;
    Uri audioUri = null;      // saved audio URI for restart after stop
    String videoUrl = null;   // saved video URL for restart after stop

    // File picker launcher
    ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link UI elements
        videoView = findViewById(R.id.videoView);
        tvStatus = findViewById(R.id.tvStatus);
        btnOpenFile = findViewById(R.id.btnOpenFile);
        btnOpenUrl = findViewById(R.id.btnOpenUrl);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnRestart = findViewById(R.id.btnRestart);

        // File picker - supports audio AND video files
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        // Persist permission to access this URI
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        stopAll();
                        String mimeType = getContentResolver().getType(uri);

                        if (mimeType != null && mimeType.startsWith("video")) {
                            // It's a video file
                            isVideoMode = true;
                            isAudioMode = false;
                            videoUrl = null;
                            audioUri = uri;  // FIXED: save video file URI in audioUri for reload

                            videoView.setVideoURI(uri);
                            videoView.requestFocus();
                            tvStatus.setText("Status: Video file loaded. Press Play.");
                            Toast.makeText(this, "Video file loaded!", Toast.LENGTH_SHORT).show();


                        } else {
                            // Treat as audio file
                            isAudioMode = true;
                            isVideoMode = false;
                            audioUri = uri;
                            videoUrl = null;

                            loadAudio(uri);
                        }
                    }
                }
        );

        // Open File button
        btnOpenFile.setOnClickListener(v -> {
            // Opens picker for both audio and video files
            filePickerLauncher.launch(new String[]{"audio/*", "video/*"});
        });

        // Open URL button
        btnOpenUrl.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Enter video URL");
            input.setText("https://www.w3schools.com/html/mov_bbb.mp4");

            new AlertDialog.Builder(this)
                    .setTitle("Enter Video URL")
                    .setView(input)
                    .setPositiveButton("Load", (dialog, which) -> {
                        String url = input.getText().toString().trim();
                        if (!url.isEmpty()) {
                            stopAll();
                            isVideoMode = true;
                            isAudioMode = false;
                            videoUrl = url;  // save URL for restart
                            audioUri = null;

                            videoView.setVideoURI(Uri.parse(url));
                            videoView.requestFocus();
                            tvStatus.setText("Status: Video URL loaded. Press Play.");
                            Toast.makeText(this, "Video URL loaded!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Play button
        btnPlay.setOnClickListener(v -> {
            if (isAudioMode && mediaPlayer != null) {
                mediaPlayer.start();
                tvStatus.setText("Status: Playing Audio...");
            } else if (isVideoMode) {
                videoView.start();
                tvStatus.setText("Status: Playing Video...");
            } else {
                Toast.makeText(this, "Please open a file or URL first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Pause button
        btnPause.setOnClickListener(v -> {
            if (isAudioMode && mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                tvStatus.setText("Status: Paused");
            } else if (isVideoMode && videoView.isPlaying()) {
                videoView.pause();
                tvStatus.setText("Status: Paused");
            }
        });

        // Stop button - stops and reloads so Play works again
        btnStop.setOnClickListener(v -> {
            if (isAudioMode && mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                // Reload audio so play works again after stop
                if (audioUri != null) loadAudio(audioUri);
                tvStatus.setText("Status: Stopped");

            } else if (isVideoMode) {
                videoView.stopPlayback();
                // Reload video URI/URL so play works again after stop
                // Reload video so Play works after Stop
                if (videoUrl != null) {
                    // URL mode - reload from URL string
                    videoView.setVideoURI(Uri.parse(videoUrl));
                } else if (audioUri != null) {
                    // File mode - reload from saved file URI
                    videoView.setVideoURI(audioUri);
                }
                tvStatus.setText("Status: Stopped");
            }
        });

        // Restart button - seeks to beginning and plays
        btnRestart.setOnClickListener(v -> {
            if (isAudioMode && mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                tvStatus.setText("Status: Restarted Audio");

            } else if (isVideoMode) {
                videoView.seekTo(0);
                videoView.start();
                tvStatus.setText("Status: Restarted Video");
            }
        });
    }

    // Helper: load audio from URI into MediaPlayer
    private void loadAudio(Uri uri) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            tvStatus.setText("Status: Audio loaded. Press Play.");
            Toast.makeText(this, "Audio file loaded!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            tvStatus.setText("Status: Error loading audio");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Helper: stop everything
    private void stopAll() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        videoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}