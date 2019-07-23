package com.devessentials.videoplayer;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String VIDEO_TITLE = "Favorite Home Movie Clips";
    private static final String VIDEO_INFO_ACTION = "com.devessentials.videoplayer.VIDEO_INFO";

    private static final int VIDEO_INFO_REQUEST_CODE = 101;

    private VideoView mVideoView;
    private MediaController mMediaController;
    private Button mPipButton;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, VIDEO_TITLE, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureVideoView();

        mPipButton = findViewById(R.id.button);
    }

    private void configureVideoView() {
        mVideoView = findViewById(R.id.videoView);
        mVideoView.setVideoPath("https://www.ebookfrenzy.com/android_book/movie.mp4");

        mMediaController = new MediaController(this);
        mMediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mMediaController);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                Log.i(LOG_TAG, "Duration = " + mVideoView.getDuration());
            }
        });

        mVideoView.start();
    }

    public void enterPipMode(View view) {
        Rational rational = new Rational(mVideoView.getWidth(), mVideoView.getHeight());

        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(rational)
                .build();

        mPipButton.setVisibility(View.INVISIBLE);
        mVideoView.setMediaController(null);
        enterPictureInPictureMode(params);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);

        if (isInPictureInPictureMode) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(VIDEO_INFO_ACTION);

            registerReceiver(mReceiver, filter);

            createPipAction();
        } else {
            mPipButton.setVisibility(View.VISIBLE);
            mVideoView.setMediaController(mMediaController);

            unregisterReceiver(mReceiver);
        }
    }

    private void createPipAction() {
        Intent intent = new Intent(VIDEO_INFO_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                VIDEO_INFO_REQUEST_CODE, intent, 0);

        Icon icon = Icon.createWithResource(this, android.R.drawable.ic_dialog_info);

        RemoteAction remoteAction = new RemoteAction(icon, "Info",
                "Video Info", pendingIntent);

        ArrayList<RemoteAction> actions = new ArrayList<>();
        actions.add(remoteAction);

        // SOS: these params are union'd w the params I've already set
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setActions(actions)
                .build();
        setPictureInPictureParams(params);
    }
}
