
package de.tum.os.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.showcasedemo.R;

import de.tum.os.models.GenericGestures;
import de.tum.os.models.IShowcaseViewer;

/**
 * Created by Marius on 7/24/13.
 */
public class VideoViewer extends VideoView implements IShowcaseViewer, MediaPlayer.OnPreparedListener {

    Context context;
    MediaController mController;
    MediaPlayer mPlayer;

    public VideoViewer(Context context) {
        super(context);
        this.context = context;
        this.mController = new MediaController(this.context);
        this.setMediaController(this.mController);

        this.setOnPreparedListener(this);
    }

    private void startVideoPlayback() {
        if (this.mPlayer != null) {
            this.mPlayer.start();
        } else {
            Uri videoUri = Uri.parse("android.resource://" + this.context.getPackageName() + "/"
                    + R.raw.test1);
            this.setVideoURI(videoUri);
            this.start();
        }
    }

    private void stopVideoPlayback() {
        if (this.mPlayer != null)
            this.mPlayer.pause();
    }

    @Override
    public void Consume(GenericGestures genericGesture) {
        switch (genericGesture) {
            case WaveUp: {

                break;
            }
            case WaveDown: {

                break;
            }
            case WaveLeft: {
                this.startVideoPlayback();
                break;
            }
            case WaveRight: {
                this.stopVideoPlayback();
                break;
            }
            case Other: {

                break;
            }

        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mPlayer = mediaPlayer;
    }
}
