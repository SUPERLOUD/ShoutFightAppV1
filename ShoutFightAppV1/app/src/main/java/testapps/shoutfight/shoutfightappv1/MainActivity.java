package testapps.shoutfight.shoutfightappv1;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    String id;
    final Random r = new Random();

    RequestQueue queue;
    // Tag used to log messages
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int sampleRate = 11025;
    private static final int bufferSizeFactor = 10;

    private AudioRecord audio;
    private int bufferSize;

    private ProgressBar level;

    private TextView textView, textView2;

    private Handler handler = new Handler();

    private int lastLevel = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        level = (ProgressBar) findViewById(R.id.progressbar_level);

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);

//        level.setMax(32676);
        level.setMax(70000);

        ToggleButton record = (ToggleButton) findViewById(R.id.toggleButton);

        record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT) * bufferSizeFactor;

                    audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    audio.startRecording();

                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            readAudioBuffer();
                        }
                    });

                    thread.setPriority(Thread.currentThread().getThreadGroup().getMaxPriority());

                    thread.start();

                    handler.removeCallbacks(update);
                    handler.postDelayed(update, 25);

                } else if (audio != null) {
                    audio.stop();
                    audio.release();
                    audio = null;
                    handler.removeCallbacks(update);
                }

            }
        });
    }

    public void onClick(View view) {
        Button b = (Button) view;
        String text = b.getText().toString();

        if (text.equals("Up")) {
            id = "17";
        } else if (text.equals("Left")) {
            id = "18";
        } else if (text.equals("Down")) {
            id = "19";
        } else if (text.equals("Right")) {
            id = "20";
        } else if (text.equals("A")) {
            id = "21";
        } else if (text.equals("B")) {
            id = "22";
        }

//        if (text == "Up") {
//            id = "24";
//        } else if (text == "Left") {
//            id = "25";
//        } else if (text == "Down") {
//            id = "26";
//        } else if (text == "Right") {
//            id = "27";
//        } else if (text == "A") {
//            id = "28";
//        } else if (text == "B") {
//            id = "29";
//        }

        postRequest(id, String.valueOf(r.nextInt(1000000) + 1));
    }

    public void postRequest(final String id, final String value) {
        StringRequest sr = new StringRequest(Request.Method.POST,"https://api.syncano.io/v1/instances/dark-snowflake-7198/webhooks/action/run/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(
                                getApplicationContext(),
                                response,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString(), error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("id",id);
                params.put("dummy",value);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("X-API-KEY","7376106916d5ae8ab578040ae303e860f76c0b50");
                return params;
            }
        };
        queue.add(sr);
    }

    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult;

            do {

                bufferReadResult = audio.read(buffer, 0, bufferSize);

                for (int i = 0; i < bufferReadResult; i++){

                    if (buffer[i] > lastLevel) {
                        lastLevel = buffer[i];
                    }

                }

            } while (bufferReadResult > 0 && audio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);

            if (audio != null) {
                audio.release();
                audio = null;
                handler.removeCallbacks(update);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Runnable update = new Runnable() {

        public void run() {

            MainActivity.this.level.setProgress(lastLevel);
            textView.setText(String.valueOf(lastLevel));
            if (lastLevel > 28000) { //28000
                int pls = Integer.parseInt(textView2.getText().toString()) + 1;
                String plString = String.valueOf(pls);

                if (pls % 5 == 0 && !plString.equals(textView2.getText().toString())) {
                    postRequest("23", plString);
//                postRequest("30", pls);
                }
                textView2.setText(plString);
            }
            lastLevel *= .5;

            handler.postAtTime(this, SystemClock.uptimeMillis() + 500);

        }

    };
}