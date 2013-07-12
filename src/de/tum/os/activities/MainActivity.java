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

import de.tum.os.activities.models.ICommandExecuter;
import de.tum.os.activities.models.PlaybackMode;
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

import de.tum.os.network.ConnectionListener;
import de.tum.os.sa.client.IShowcaseServiceAsync;
import de.tum.os.sa.shared.Command;
import de.tum.os.sa.shared.DTO.PlaybackDevice;
import de.tum.os.sa.shared.DeviceType;

public class MainActivity extends Activity implements Nanogest.GestureListener,
		Nanogest.ErrorListener, OnPreparedListener, ICommandExecuter {

	LinearLayout mainLayout;
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
	String moduleBaseURL2 ="http://131.159.193.227:8888/ShowcaseApp.html";
	String remoteServiceUrl = "http://131.159.193.227:8888/showcaseapp/showcaseService";
	String hostPageBaseUrl = "http://127.0.0.1:8888/";
	String moduleBaseForStaticFiles = "http://127.0.0.1:8888/showcaseapp/";
	String serviceEntryPointUrl = "http://127.0.0.1:8888/showcaseapp/showcaseService";
	IShowcaseServiceAsync serviceAsync;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
//		ngest = new Nanogest(this, this, this);
//		ngest.setPreventScreenTimeout(true);
		currentMode = PlaybackMode.none;
		imgIds = new int[] { R.drawable.googlelogo1, R.drawable.googlelogo2,
				R.drawable.googlelogo3, R.drawable.tumlogo1,
				R.drawable.tumlogo2 };

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

        if(s!=null && s.isConnected()){
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
            }
        });

        try {
            (new ConnectionListener(s, this)).StartListening();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void instantiateService() {

		
		AsyncTask<Activity, Void, Void> instantiateServiceAsync = new AsyncTask<Activity, Void, Void>() {

			@Override
			protected Void doInBackground(Activity... params) {
				serviceAsync = (IShowcaseServiceAsync) SyncProxy.newProxyInstance(
						IShowcaseServiceAsync.class, moduleBaseURL,"showcaseService");
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

		case R.id.action_register: {
				if(serviceAsync!=null){
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
                                }
                            });
                        }

                        @Override
						public void onFailure(Throwable arg0) {
                            message = "Couldn't register on server. Maybe network issues";
                            Log.i("showcase",message);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
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

	@Override
	public void onGesture(Gesture arg0, double arg1) {
		Log.i("SA", "Gesture from nanogest: " + arg0.toString());
		switch (arg0) {
		case SWIPE_DOWN: {
			switch (currentMode) {
			case text:
				if (mainLayout.getChildAt(0) instanceof ScrollView) {
					ScrollView scroller = (ScrollView) mainLayout.getChildAt(0);
					scroller.smoothScrollBy(0, 300);
				}
				break;

			case video:
				if (mainLayout.getChildAt(0) instanceof VideoView) {
					VideoView vv = (VideoView) mainLayout.getChildAt(0);
					if (videoPlayer != null)
						videoPlayer.setVolume(0f, 0f);

				}
				break;

			case image:
				if (mainLayout.getChildAt(0) instanceof ImageView) {
					ImageView iv = (ImageView) mainLayout.getChildAt(0);
					currentPictureIndex--;
					if (currentPictureIndex < 0 || currentPictureIndex > 4)
						currentPictureIndex = 0;
					iv.setBackgroundResource(imgIds[currentPictureIndex]);
				}
				break;

			default:
				break;
			}
			break;
		}
		case SWIPE_LEFT: {
			switch (currentMode) {
			case text:
				if (mainLayout.getChildAt(0) instanceof ScrollView) {
					ScrollView scroller = (ScrollView) mainLayout.getChildAt(0);
					TextView tv = (TextView) scroller.getChildAt(0);
					float textSize = tv.getTextSize() - 3f < 5f ? 5f : tv
							.getTextSize() - 3f;
					// tv.setTextSize(textSize);
				}
				break;

			case video:
				if (mainLayout.getChildAt(0) instanceof VideoView) {
					VideoView vv = (VideoView) mainLayout.getChildAt(0);
					vv.start();

				}
				break;

			case image:
				if (mainLayout.getChildAt(0) instanceof ImageView) {
					ImageView iv = (ImageView) mainLayout.getChildAt(0);
					currentPictureIndex--;
					if (currentPictureIndex < 0 || currentPictureIndex > 4)
						currentPictureIndex = 0;
					iv.setBackgroundResource(imgIds[currentPictureIndex]);
				}
				break;

			default:
				break;
			}
			break;
		}
		case SWIPE_RIGHT: {
			switch (currentMode) {
			case text:
				if (mainLayout.getChildAt(0) instanceof ScrollView) {
					ScrollView scroller = (ScrollView) mainLayout.getChildAt(0);
					TextView tv = (TextView) scroller.getChildAt(0);
					float textSize = tv.getTextSize() + 5f > 32f ? 32f : tv
							.getTextSize() + 3f;
					// tv.setTextSize(textSize);
				}
				break;

			case video:
				if (mainLayout.getChildAt(0) instanceof VideoView) {
					VideoView vv = (VideoView) mainLayout.getChildAt(0);
					vv.pause();

				}
				break;

			case image:
				if (mainLayout.getChildAt(0) instanceof ImageView) {
					ImageView iv = (ImageView) mainLayout.getChildAt(0);
					currentPictureIndex++;
					if (currentPictureIndex < 0 || currentPictureIndex > 4)
						currentPictureIndex = 0;
					iv.setBackgroundResource(imgIds[currentPictureIndex]);
				}
				break;

			default:
				break;
			}
			break;
		}
		case SWIPE_UP: {

			switch (currentMode) {
			case text:
				if (mainLayout.getChildAt(0) instanceof ScrollView) {
					ScrollView scroller = (ScrollView) mainLayout.getChildAt(0);
					scroller.smoothScrollBy(0, -300);
				}
				break;

			case video:
				if (mainLayout.getChildAt(0) instanceof VideoView) {
					VideoView vv = (VideoView) mainLayout.getChildAt(0);
					if (videoPlayer != null)
						videoPlayer.setVolume(100f, 100f);

				}
				break;

			case image:
				if (mainLayout.getChildAt(0) instanceof ImageView) {
					ImageView iv = (ImageView) mainLayout.getChildAt(0);
					currentPictureIndex--;
					if (currentPictureIndex < 0 || currentPictureIndex > 4)
						currentPictureIndex = 0;
					iv.setBackgroundResource(imgIds[currentPictureIndex]);
				}
				break;

			default:
				break;
			}
			break;
		}

		default:
			break;
		}

	}

	private void displayText(String filename) {
		// The InputStream opens the resourceId and sends it to the buffer
		int resourceId = R.raw.lorem;
		InputStream is = this.getResources().openRawResource(resourceId);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String readLine = null;
		StringBuilder sb = new StringBuilder();
		try {
			// While the BufferedReader readLine is not null
			while ((readLine = br.readLine()) != null) {
				sb.append(readLine);
				Log.d("TEXT", readLine);
			}

			// Close the InputStream and BufferedReader
			is.close();
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		mainLayout.removeAllViews();
		// Scrollable
		ScrollView scrollView = new ScrollView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		scrollView.setLayoutParams(lp);
		TextView tv = new TextView(this);
		tv.setText(sb.toString());
		tv.setTextSize(22f);
		scrollView.addView(tv);
		mainLayout.addView(scrollView);

	}

	private void displayPictures(String pictureFolderName) {

		mainLayout.removeAllViews();
		ImageView iv = new ImageView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		iv.setLayoutParams(lp);
		if (currentPictureIndex < 0 || currentPictureIndex > 4)
			currentPictureIndex = 0;
		iv.setBackgroundResource(imgIds[currentPictureIndex]);
		mainLayout.addView(iv);
	}

	private void displayVideo(String filename) {

		mainLayout.removeAllViews();
		VideoView vv = new VideoView(this);
		vv.setMediaController(new MediaController(this));
		Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
				+ R.raw.test1);
		vv.setVideoURI(video);
		vv.setOnPreparedListener(this);
		mainLayout.addView(vv);
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
                        }
                        break;
                    }
                    case pause: {
                        if (mainLayout.getChildAt(0) instanceof VideoView) {
                            VideoView vv = (VideoView) mainLayout.getChildAt(0);
                            vv.pause();
                        }
                        break;
                    }
                }
            }
        });
    }
}
