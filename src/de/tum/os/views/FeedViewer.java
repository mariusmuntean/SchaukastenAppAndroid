package de.tum.os.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.showcasedemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.theengine.android.simple_rss2_android.RSSItem;
import at.theengine.android.simple_rss2_android.SimpleRss2Parser;
import at.theengine.android.simple_rss2_android.SimpleRss2ParserCallback;
import de.tum.os.models.GenericGestures;
import de.tum.os.models.IShowcaseViewer;

/**
 * A custom class for displaying RSS Feeds.
 * One item at a time.
 * <p/>
 * Created by Marius on 7/19/13.
 */
public class FeedViewer extends ListView implements IShowcaseViewer {

    String feedUrl;
    private final String feedItemTitle = "feedItemTitle";
    private final String feedItemContent = "feedItemContent";

    Context context;

    Handler autoPlaybackHandler = new Handler();
    Runnable autoPlaybackRunnable;

    TextView tv;

    public FeedViewer(Context context, AttributeSet attrs, String feedUrl) throws Exception {
        super(context, attrs);
    }

    public FeedViewer(Context context, String feedUrl, TextView tv) throws Exception {
        super(context);

        this.tv = tv;
        this.feedUrl = feedUrl;
        this.context = context;

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.setLayoutParams(lp);
        this.setDivider((new ColorDrawable()));
        this.setDividerHeight(25);
        this.setVerticalFadingEdgeEnabled(true);
        this.setFadingEdgeLength(10);

        fetchFeed();

    }

    private void fetchFeed() throws Exception {
        if (this.feedUrl == null || this.feedUrl.isEmpty()) {
            throw new Exception("Feed URL cannot be null");
        }

        SimpleRss2Parser rssParser = new SimpleRss2Parser(feedUrl, new SimpleRss2ParserCallback() {
            @Override
            public void onFeedParsed(List<RSSItem> rssItems) {
                displayFeedItems(rssItems);
            }

            @Override
            public void onError(Exception e) {
                int t = 0;
            }
        });
        rssParser.parseAsync();
    }

    private void displayFeedItems(List<RSSItem> rssItems) {
        ArrayList<HashMap<String, Object>> data = getDisplayableData(rssItems);

        SimpleAdapter feedAdapter = new SimpleAdapter(context, data, R.layout.feed_item_template_layout,
                new String[]{feedItemTitle, feedItemContent}, new int[]{R.id.feedItemTitleTV, R.id.feedItemContentTV});
        this.setAdapter(feedAdapter);
        startAutoPlayback();
    }


    private ArrayList<HashMap<String, Object>> getDisplayableData(List<RSSItem> rssItems) {
        if (rssItems == null || rssItems.size() < 1) {
            return null;
        }
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (RSSItem item : rssItems) {
            HashMap<String, Object> currentItemMap = new HashMap<String, Object>();
            currentItemMap.put(feedItemTitle, item.getTitle());
            currentItemMap.put(feedItemContent, Html.fromHtml(item.getContent()));
            data.add(currentItemMap);
        }

        return data;
    }

    private void startAutoPlayback() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.append("Playback started");
            }
        });
        FeedViewer.this.autoPlaybackRunnable = getAutoPlaybackRunnable();
        this.autoPlaybackHandler.postDelayed(FeedViewer.this.autoPlaybackRunnable, 0);
    }

    private Runnable getAutoPlaybackRunnable() {
        if (FeedViewer.this.autoPlaybackRunnable != null) {
            return FeedViewer.this.autoPlaybackRunnable;
        } else {
            Runnable r = new Runnable() {
                int currentIndex = 0;

                @Override
                public void run() {
                    FeedViewer.this.setSelection(currentIndex);
//                FeedViewer.this.smoothScrollToPosition(4);
                    currentIndex = (currentIndex + 1) % FeedViewer.this.getAdapter().getCount();

                    Log.i("FeedViewer", "Scrolling to position: " + currentIndex);
                    FeedViewer.this.autoPlaybackHandler.postDelayed(this, 5000);
                }
            };
            return r;
        }
    }

    private void stopAutoPlayback() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.append("Playback stopped");
            }
        });
        FeedViewer.this.autoPlaybackHandler.removeCallbacks(FeedViewer.this.autoPlaybackRunnable);
    }

    @Override
    public void Consume(final GenericGestures genericGesture) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.append("Got gesture: "+genericGesture.toString());
            }
        });
        switch (genericGesture) {
            case WaveDown: {
                stopAutoPlayback();
                int currentFeedItemIndex = FeedViewer.this.getFirstVisiblePosition();
                int nextFeedItemIndex = (currentFeedItemIndex + 1) % FeedViewer.this.getAdapter().getCount();
                FeedViewer.this.setSelection(nextFeedItemIndex);
                break;
            }
            case WaveUp: {
                stopAutoPlayback();
                int currentFeedItemIndex = FeedViewer.this.getFirstVisiblePosition();
                int nextFeedItemIndex = (Math.abs(currentFeedItemIndex - 1)) % FeedViewer.this.getAdapter().getCount();
                FeedViewer.this.setSelection(nextFeedItemIndex);
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
