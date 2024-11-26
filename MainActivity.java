package com.example.audioplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    private SeekBar seekBar;
    private Button playPauseButton, nextButton, previousButton;
    private TextView currentTime, totalTime;
    private Handler handler;
    private Runnable updateSeekBar;

    private int currentTrackIndex = 0;

    // Array of audio URLs
    private String[] audioUrls = {
            "https://example.com/audio1.mp3", // Replace with your audio URLs
            "https://example.com/audio2.mp3",
            "https://example.com/audio3.mp3"
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.prevButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        handler = new Handler();

        // Initialize ExoPlayer
        setupExoPlayer();

        // Button listeners
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNextTrack());
        previousButton.setOnClickListener(v -> playPreviousTrack());

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    exoPlayer.seekTo(progress * 1000L);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        startSeekBarUpdater();
    }

    private void setupExoPlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playTrack(currentTrackIndex);

        exoPlayer.addListener(new com.google.android.exoplayer2.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    playNextTrack();
                }
            }
        });
    }

    private void playTrack(int index) {
        MediaItem mediaItem = MediaItem.fromUri(audioUrls[index]);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        updateUIForCurrentTrack();
    }

    private void togglePlayPause() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
            playPauseButton.setText("Play");
        } else {
            exoPlayer.play();
            playPauseButton.setText("Pause");
        }
    }

    private void playNextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % audioUrls.length;
        playTrack(currentTrackIndex);
    }

    private void playPreviousTrack() {
        currentTrackIndex = (currentTrackIndex - 1 + audioUrls.length) % audioUrls.length;
        playTrack(currentTrackIndex);
    }

    private void updateUIForCurrentTrack() {
        totalTime.setText(formatTime((int) (exoPlayer.getDuration() / 1000)));
    }

    private void startSeekBarUpdater() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null) {
                    int current = (int) (exoPlayer.getCurrentPosition() / 1000);
                    int duration = (int) (exoPlayer.getDuration() / 1000);

                    seekBar.setMax(duration);
                    seekBar.setProgress(current);

                    currentTime.setText(formatTime(current));
                    totalTime.setText(formatTime(duration));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateSeekBar);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @Override
    protected void onDestroy() {
        if (exoPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            exoPlayer.release();
        }
        super.onDestroy();
    }
}
