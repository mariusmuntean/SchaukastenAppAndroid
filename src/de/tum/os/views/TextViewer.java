package de.tum.os.views;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.showcasedemo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.tum.os.models.GenericGestures;
import de.tum.os.models.IShowcaseViewer;

/**
 * A custom class for displaying text.
 * <p/>
 * Created by Marius on 7/24/13.
 */
public class TextViewer extends ScrollView implements IShowcaseViewer {

    Context context;
    TextView txtView;

    public TextViewer(Context context) {
        super(context);

        this.context = context;

        initUi();

        displayTextFile("");
    }

    private void initUi() {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.setLayoutParams(lp);
        this.txtView = new TextView(this.context);
        txtView.setTextSize(22f);
        this.addView(txtView);
    }

    private void displayTextFile(String filename) {
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

        if (this.txtView != null) {
            this.txtView.setText(sb.toString());
        }

    }


    @Override
    public void Consume(GenericGestures genericGesture) {
        switch (genericGesture) {
            case WaveUp: {
                this.smoothScrollBy(0, -300);
                break;
            }

            case WaveDown: {
                this.smoothScrollBy(0, 300);
                break;
            }

            case WaveLeft: {
                this.txtView.setTextSize(this.txtView.getTextSize() - 1);
                break;
            }

            case WaveRight: {
                this.txtView.setTextSize(this.txtView.getTextSize() + 1);
                break;
            }
            case Other: {

                break;
            }
        }
    }
}
