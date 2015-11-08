//contains sound measurement algo (which has noise) and post request button.
package testapps.shoutfight.shoutfightappv1;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    RequestQueue queue;
    // Tag used to log messages
    private static final String TAG = MainActivity.class.getSimpleName();
    TextView textView;
    MediaRecorder mRecorder;
    int delay = 200; //ms interval of decibel measuring

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        textView = (TextView) findViewById(R.id.ayy);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile("/dev/null");
        try{
            mRecorder.prepare();
        } catch (IOException e){
            Log.d(TAG, e.toString(), e);
        }
        mRecorder.start();
        textView.setText("ayy");

        final Handler h = new Handler();
        h.postDelayed(new Runnable(){
            public void run(){
                getSoundReading();
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
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
                params.put("id","1");
                params.put("dummy","1000");

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

    public void getSoundReading() {
//        double powerDb = 20 * Math.log10(getAmplitude() / 30000.0);
//        textView.setText(String.valueOf(powerDb));
        textView.setText(String.valueOf(getAmplitude()));
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;

    }
}
