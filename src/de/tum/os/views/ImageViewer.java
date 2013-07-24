package de.tum.os.views;

import android.content.Context;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.showcasedemo.R;


import de.tum.os.models.GenericGestures;
import de.tum.os.models.IShowcaseViewer;

/**
 * Created by Marius on 7/24/13.
 */
public class ImageViewer extends ImageView implements IShowcaseViewer {

    int[] imgIds;
    private Handler autoPlaybackHandler = new Handler();
    private Runnable autoPlaybackRunnable;
    int currentIndex = 2;

    public ImageViewer(Context context) {
        super(context);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.setLayoutParams(lp);

        imgIds = new int[]{R.drawable.googlelogo1, R.drawable.googlelogo2,
                R.drawable.googlelogo3, R.drawable.tumlogo1,
                R.drawable.tumlogo2};

        startAutoPlayback();
    }

    private void startAutoPlayback() {
        this.autoPlaybackRunnable = getAutoPlaybackRunnable();
        this.autoPlaybackHandler.postDelayed(this.autoPlaybackRunnable, 0);
    }

    private Runnable getAutoPlaybackRunnable() {
        if (this.autoPlaybackRunnable != null) {
            return this.autoPlaybackRunnable;
        } else {
            Runnable r = new Runnable() {


                @Override
                public void run() {
                    currentIndex = (currentIndex + 1) % imgIds.length;
                    ImageViewer.this.setBackgroundResource(imgIds[currentIndex]);
                    ImageViewer.this.autoPlaybackHandler.postDelayed(this, 3000);
                }
            };
            return r;
        }
    }

    private void stopAutoPlayback() {
        this.autoPlaybackHandler.removeCallbacks(this.autoPlaybackRunnable);
    }

    @Override
    public void Consume(GenericGestures genericGesture) {

        switch (genericGesture) {
            case WaveUp: {
                stopAutoPlayback();
                currentIndex = (Math.abs(currentIndex - 1)) % imgIds.length;
                ImageViewer.this.setBackgroundResource(imgIds[currentIndex]);
                break;
            }
            case WaveDown: {
                stopAutoPlayback();
                currentIndex = (Math.abs(currentIndex + 1)) % imgIds.length;
                ImageViewer.this.setBackgroundResource(imgIds[currentIndex]);
                break;
            }
            case WaveLeft: {
                startAutoPlayback();
                break;
            }
            case WaveRight: {
                stopAutoPlayback();
                break;
            }
            case Other: {

                break;
            }
        }
    }
}
