package de.tum.os.activities;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import de.tum.os.models.GenericGestures;
import de.tum.os.models.ICommandExecuter;
import de.tum.os.models.PlaybackMode;

import com.example.showcasedemo.R;
import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.nanocritical.nanogest.Nanogest;
import com.nanocritical.nanogest.Nanogest.ErrorCode;
import com.nanocritical.nanogest.Nanogest.Gesture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

import de.tum.os.network.ConnectionListener;
import de.tum.os.sa.client.IShowcaseServiceAsync;
import de.tum.os.sa.shared.Command;
import de.tum.os.sa.shared.DTO.PlaybackDevice;
import de.tum.os.sa.shared.DeviceType;
import de.tum.os.views.FeedViewer;
import de.tum.os.views.ImageViewer;
import de.tum.os.views.TextViewer;
import de.tum.os.views.VideoViewer;

public class MainActivity extends Activity implements Nanogest.GestureListener,
        Nanogest.ErrorListener, OnPreparedListener, ICommandExecuter {

    LinearLayout mainLayout;
    TextView tvConsole;
    Nanogest ngest;
    PlaybackMode currentMode;
    int currentPictureIndex = 2;
    int[] imgIds;
    MediaPlayer videoPlayer;
    PlaybackDevice me;

    Socket s;

    /**
     * RPC Stuff
     */
    String serverIP = "131.159.193.227";
    int serverPort = 3535;
    String moduleBaseURL = "http://131.159.193.227:8888/showcaseapp/";
    String moduleBaseURL2 = "http://131.159.193.227:8888/ShowcaseApp.html";
    String remoteServiceUrl = "http://131.159.193.227:8888/showcaseapp/showcaseService";
    String hostPageBaseUrl = "http://127.0.0.1:8888/";
    String moduleBaseForStaticFiles = "http://127.0.0.1:8888/showcaseapp/";
    String serviceEntryPointUrl = "http://127.0.0.1:8888/showcaseapp/showcaseService";
    IShowcaseServiceAsync serviceAsync;

    FeedViewer feedViewer = null;
    TextViewer textViewer = null;
    VideoViewer videoViewer = null;
    ImageViewer imageViewer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        tvConsole = (TextView) findViewById(R.id.txtViewConsole);
//		ngest = new Nanogest(this, this, this);
//		ngest.setPreventScreenTimeout(true);
        currentMode = PlaybackMode.none;
        imgIds = new int[]{R.drawable.googlelogo1, R.drawable.googlelogo2,
                R.drawable.googlelogo3, R.drawable.tumlogo1,
                R.drawable.tumlogo2};

        instantiateService();
        connectToServer();
    }

    private void connectToServer() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                connectToServlet();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void connectToServlet() {

        if (s != null && s.isConnected()) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            s = new Socket(serverIP, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        BufferedReader in = null;
        String message = "";
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            message = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        me = new PlaybackDevice(message, "I R Baboon", DeviceType.Smartphone, 4.7f);

        final String finalMessage = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MainActivity.this, finalMessage, Toast.LENGTH_LONG);
                toast.show();
                tvConsole.append("\n" + finalMessage);
            }
        });

        try {
            (new ConnectionListener(s, this)).StartListening();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvConsole.append("\n" + "ConnectionListener started");
                }
            });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void instantiateService() {


        AsyncTask<Activity, Void, Void> instantiateServiceAsync = new AsyncTask<Activity, Void, Void>() {

            @Override
            protected Void doInBackground(Activity... params) {
                serviceAsync = (IShowcaseServiceAsync) SyncProxy.newProxyInstance(
                        IShowcaseServiceAsync.class, moduleBaseURL, "showcaseService");
                return null;
            }

        };
        instantiateServiceAsync.execute(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        ngest = new Nanogest(this, this, this);
        ngest.setPreventScreenTimeout(true);
        ngest.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        ngest.stop();
        tvConsole.append("\n" + "onPause()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_showText: {
                displayText("");
                currentMode = PlaybackMode.text;
                return true;
            }
            case R.id.action_showImages: {
                displayPictures("");
                currentMode = PlaybackMode.image;
                return true;
            }
            case R.id.action_showVideo: {
                displayVideo("");
                currentMode = PlaybackMode.video;
                return true;
            }

            case R.id.action_connect: {
                instantiateService();
                connectToServer();
                return true;
            }

            case R.id.action_showFeed: {
                displayFeed("http://www.os.in.tum.de/?type=100");
//                displayFeed("http://pingeb.org/feed");
                currentMode = PlaybackMode.feed;
                return true;
            }

            case R.id.action_register: {
                if (serviceAsync != null) {
                    AsyncCallback<Boolean> registerResultCallback = new AsyncCallback<Boolean>() {
                        Toast toast;
                        String message;

                        @Override
                        public void onSuccess(Boolean arg0) {

                            if (arg0) {
                                message = "Registered on server as " + me.toString() + " just fine";
                                Log.i("showcase", message);
                            } else {
                                message = "Couldn't register on server as " + me.toString();
                                Log.i("showcase", message);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
                                    toast.show();
                                    tvConsole.append("\n" + message);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable arg0) {
                            message = "Couldn't register on server. Maybe network issues";
                            Log.i("showcase", message);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
                                    tvConsole.append("\n" + message);
                                }
                            });
                        }
                    };
                    serviceAsync.registerDevice(me, registerResultCallback);
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onError(ErrorCode arg0, String arg1, String arg2) {
        Log.w("SA", "Error from nanogest: " + arg2);
        if (arg0 != ErrorCode.OK) {
            ngest.stop();
            ngest = new Nanogest(this, this, this);
            ngest.setPreventScreenTimeout(true);
            ngest.start();
        }

    }

    private HashMap<Gesture, GenericGestures> nanogestToGenericgesture = new HashMap<Gesture, GenericGestures>() {
        {
            put(Gesture.SWIPE_UP, GenericGestures.WaveUp);
            put(Gesture.SWIPE_DOWN, GenericGestures.WaveDown);
            put(Gesture.SWIPE_LEFT, GenericGestures.WaveLeft);
            put(Gesture.SWIPE_RIGHT, GenericGestures.WaveRight);
        }
    };

    @Override
    public void onGesture(Gesture arg0, double arg1) {
        Log.i("SA", "Gesture from nanogest: " + arg0.toString());

        switch (currentMode) {
            case audio: {
                break;
            }
            case video: {
                this.videoViewer.Consume(nanogestToGenericgesture.get(arg0));
                break;
            }
            case text: {
                this.textViewer.Consume(nanogestToGenericgesture.get(arg0));
                break;
            }
            case feed: {
                feedViewer.Consume(nanogestToGenericgesture.get(arg0));
                break;
            }
            case image: {
                imageViewer.Consume(nanogestToGenericgesture.get(arg0));
                break;
            }
        }
    }

    private void displayText(String filename) {

        mainLayout.removeAllViews();
        this.textViewer = new TextViewer(this);

        if (textViewer == null) {
            return;
        }

        mainLayout.addView(textViewer);
        tvConsole.append("\n" + "textViewer added");

    }

    private void displayPictures(String pictureFolderName) {

        mainLayout.removeAllViews();
        this.imageViewer = new ImageViewer(this);
        mainLayout.addView(this.imageViewer);
        tvConsole.append("\n" + "imageViewer added");
    }

    private void displayFeed(String feedUrl) {
        mainLayout.removeAllViews();
        try {
            feedViewer = new FeedViewer(this, feedUrl, tvConsole);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (feedViewer == null)
            return;

        mainLayout.addView(feedViewer);
        tvConsole.append("\n" + "feedViewer added");

    }

    private void displayVideo(String filename) {

        mainLayout.removeAllViews();
        this.videoViewer = new VideoViewer(this);
        mainLayout.addView(this.videoViewer);
        tvConsole.append("\n" + "videoViewer added");
    }

    @Override
    public void onPrepared(MediaPlayer arg0) {
        this.videoPlayer = arg0;

    }

    @Override
    public void ExecuteCommand(final Command command) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayVideo("");
                currentMode = PlaybackMode.video;
                switch (command.commandType) {
                    case play: {
                        if (mainLayout.getChildAt(0) instanceof VideoView) {
                            VideoView vv = (VideoView) mainLayout.getChildAt(0);
                            vv.start();
                            tvConsole.append("\n" + "Received Play command");
                        }
                        break;
                    }
                    case stop: {
                        if (mainLayout.getChildAt(0) instanceof VideoView) {
                            VideoView vv = (VideoView) mainLayout.getChildAt(0);
                            vv.stopPlayback();
                            tvConsole.append("\n" + "Received Stop command");
                        }
                        break;
                    }
                }
            }
        });
    }
}
