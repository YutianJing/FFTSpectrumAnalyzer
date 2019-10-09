package fftpack.aig.uol.ca.fftspectrumanalyzer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import FFTLibrary.RealDoubleFFT;

import static android.graphics.Paint.Style.FILL_AND_STROKE;

public class SoundRecordAndAnalysisActivity extends AppCompatActivity {

    private GraphView graphView; // frequency spectrum

    private TextView bicep_txtview; // bicep text view
    private EditText bicep_frq_et; // bicep frequency edit text
    private TextView triceps_txtview; // triceps text view
    private EditText triceps_frq_et; // triceps frequency edit text
    private TextView forearm_txtview; // forearm text view
    private EditText forearm_frq_et; // forearm frequency edit text
    private ImageView bicep_iv , tricep_iv , forearm_iv; // bitmap image
    private Bitmap bitmap_bicep , bitmap_tricep , bitmap_forearm;
    private Canvas canvas_bicep , canvas_tricep , canvas_forearm;

    private TextView extension_txtview; // extension text view
    private EditText extension_frq_et; // extension frequency edit text
    private TextView flexion_txtview; // flexion text view
    private EditText flexion_frq_et; // flexion frequency edit text
    private TextView pronation_txtview; // pronation text view
    private EditText pronation_frq_et; // peonation frequency edit text
    private TextView supination_txtview; // supination text view
    private EditText supination_frq_et; // supination frequency edit text
    private ImageView extension_iv , flexion_iv , pronation_iv , supination_iv; // bitmap image
    private Bitmap bitmap_extension , bitmap_flexion , bitmap_pronation , bitmap_supination;
    private Canvas canvas_extension , canvas_flexion , canvas_pronation , canvas_supination;

    private Display display; // display of device
    private Button update_frq_button;
    private Button replay_recording_button;
    private Button start_recording_button;

//    private TextView progress_tv , magnitude_tv , frequency_tv;
    private int width, height;
    private float xmax , xmin , ymax , ymin;

    private LineGraphSeries<DataPoint> min_series , max_series , sound_series; // data for the graph
    private BarGraphSeries<DataPoint> bicep_series , triceps_series , forearm_series;
    private BarGraphSeries<DataPoint> extension_series , flexion_series , pronation_series , supination_series;
    private double x_min , y_min , x_max , y_max; // x_frq and y_frq coordinates

    private int BICEP_FRQ = 1000 , TRICEPS_FRQ = 7000 , FOREARM_FRQ = 14000;
    private int EXTENSION_FRQ = 3000 , FLEXION_FRQ = 3500 , PRONATION_FRQ = 4000 , SUPINATION_FRQ = 4500;
    private int MAX_MAGNITUDE = 30 , MIN_MAGNITUDE = 10 , MID_MAGNITUDE = 20 , margin = 50; // MAX_MAGNITUDE = 90 MID_MAGITUDE = 50

    RecordAudio recordTask;

    // when app in launched
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_record_and_analysis); // call the UI
        setUpVariables();
        setUpMuscles();
        requestRecordAudioPermission();
    }

    // initializing all views with their respective IDs
    private void setUpVariables() {
        bicep_txtview = (TextView) findViewById(R.id.bicep_tv);
        bicep_frq_et = (EditText) findViewById(R.id.bicep_frq_et);
        triceps_txtview = (TextView) findViewById(R.id.triceps_tv);
        triceps_frq_et = (EditText) findViewById(R.id.triceps_frq_et);
        forearm_txtview = (TextView) findViewById(R.id.forearm_tv);
        forearm_frq_et = (EditText) findViewById(R.id.forearm_frq_et);
        extension_txtview = (TextView) findViewById(R.id.extension_tv);
        extension_frq_et = (EditText) findViewById(R.id.extension_frq_et);
        flexion_txtview = (TextView) findViewById(R.id.flexion_tv);
        flexion_frq_et = (EditText) findViewById(R.id.flexion_frq_et);
        pronation_txtview = (TextView) findViewById(R.id.pronation_tv);
        pronation_frq_et = (EditText) findViewById(R.id.pronation_frq_et);
        supination_txtview = (TextView) findViewById(R.id.supination_tv);
        supination_frq_et = (EditText) findViewById(R.id.supination_frq_et);

        update_frq_button = (Button) findViewById(R.id.update_frq_btn);
        replay_recording_button = (Button) findViewById(R.id.replay_recording_btn);
        start_recording_button = (Button) findViewById(R.id.start_recording_btn);
        start_recording_button.setTag(1);

        bicep_frq_et.setHint(String.valueOf(BICEP_FRQ));
        triceps_frq_et.setHint(String.valueOf(TRICEPS_FRQ));
        forearm_frq_et.setHint(String.valueOf(FOREARM_FRQ));
        bicep_iv = (ImageView) findViewById(R.id.bicep_iv);
        tricep_iv = (ImageView) findViewById(R.id.tricep_iv);
        forearm_iv = (ImageView) findViewById(R.id.forearm_iv);
        extension_frq_et.setHint(String.valueOf(EXTENSION_FRQ));
        flexion_frq_et.setHint(String.valueOf(FLEXION_FRQ));
        pronation_frq_et.setHint(String.valueOf(PRONATION_FRQ));
        supination_frq_et.setHint(String.valueOf(SUPINATION_FRQ));
        extension_iv = (ImageView) findViewById(R.id.extension_iv);
        flexion_iv = (ImageView) findViewById(R.id.flexion_iv);
        pronation_iv = (ImageView) findViewById(R.id.pronation_iv);
        supination_iv = (ImageView) findViewById(R.id.supination_iv);

//        progress_tv = (TextView) findViewById(R.id.progress);
//        magnitude_tv = (TextView) findViewById(R.id.magnitude);
//        frequency_tv = (TextView) findViewById(R.id.frequency);
        display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight() / 4;
    }

    private void setUpMuscles() {

        bitmap_bicep = Bitmap.createBitmap(4 , 130 , Bitmap.Config.ARGB_8888);
        bitmap_tricep = Bitmap.createBitmap(4 , 130 , Bitmap.Config.ARGB_8888);
        bitmap_forearm = Bitmap.createBitmap(5 , 125 , Bitmap.Config.ARGB_8888);
        canvas_bicep = new Canvas(bitmap_bicep);
        canvas_bicep.drawColor(Color.GREEN);
        canvas_tricep = new Canvas(bitmap_tricep);
        canvas_tricep.drawColor(Color.GREEN);
        canvas_forearm = new Canvas(bitmap_forearm);
        canvas_forearm.drawColor(Color.GREEN);
        bicep_iv.setImageBitmap(bitmap_bicep);
        tricep_iv.setImageBitmap(bitmap_tricep);
        forearm_iv.setImageBitmap(bitmap_forearm);
        bicep_iv.invalidate();
        tricep_iv.invalidate();
        forearm_iv.invalidate();

/////////////////////////////////////////////////////////////////
        // arrow for extension
        Drawable drawable_extension = extension_iv.getDrawable();
        bitmap_extension =Bitmap.createBitmap(drawable_extension.getIntrinsicWidth(), drawable_extension.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas_extension = new Canvas(bitmap_extension);
        Paint paint_extension = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_extension.setColor(Color.GREEN);

        int drawable_extension_width = drawable_extension.getIntrinsicWidth();
        // drawable_extension.getIntrinsicWidth() = drawable_extension.getIntrinsicHeight() = 63
        // arrow itself
        Path path_extension = new Path();
        path_extension.moveTo((float) drawable_extension_width / 2,0);
        path_extension.lineTo(0,(float) drawable_extension_width / 2);
        path_extension.lineTo(0,(float) drawable_extension_width / 2 + 10);
        path_extension.lineTo((float) drawable_extension_width / 2,10);
        path_extension.lineTo(drawable_extension_width,(float) drawable_extension_width / 2 + 10);
        path_extension.lineTo(drawable_extension_width,(float) drawable_extension_width / 2);
        path_extension.close();
        // top left triangle
        Path path_extension_1 = new Path();
        path_extension_1.moveTo(0,0);
        path_extension_1.lineTo((float) drawable_extension_width / 2,0);
        path_extension_1.lineTo(0,(float) drawable_extension_width / 2);
        path_extension_1.close();
        // top right triangle
        Path path_extension_2 = new Path();
        path_extension_2.moveTo(drawable_extension_width,0);
        path_extension_2.lineTo(drawable_extension_width,(float) drawable_extension_width / 2);
        path_extension_2.lineTo((float) drawable_extension_width / 2,0);
        path_extension_2.close();
        // bottom cutout
        Path path_extension_3 = new Path();
        path_extension_3.moveTo(0,drawable_extension_width);
        path_extension_3.lineTo(drawable_extension_width,drawable_extension_width);
        path_extension_3.lineTo(drawable_extension_width,(float) drawable_extension_width / 2 + 10);
        path_extension_3.lineTo((float) drawable_extension_width / 2,10);
        path_extension_3.lineTo(0,(float) drawable_extension_width / 2 + 10);
        path_extension_3.close();

        canvas_extension.drawPath(path_extension, paint_extension);
        canvas_extension.clipOutPath(path_extension_1);
        canvas_extension.clipOutPath(path_extension_2);
        canvas_extension.clipOutPath(path_extension_3);
        extension_iv.setImageBitmap(bitmap_extension);
        extension_iv.invalidate();

/////////////////////////////////////////////////////////////
        // arrow for flexion
        Drawable drawable_flexion = flexion_iv.getDrawable();
        bitmap_flexion =Bitmap.createBitmap(drawable_flexion.getIntrinsicWidth(), drawable_flexion.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas_flexion = new Canvas(bitmap_flexion);
        Paint paint_flexion = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_flexion.setColor(Color.GREEN);

        int drawable_flexion_width = drawable_flexion.getIntrinsicWidth();
        // drawable_flexion.getIntrinsicWidth() = drawable_flexion.getIntrinsicHeight() = 63
        // arrow itself
        Path path_flexion = new Path();
        path_flexion.moveTo((float) drawable_flexion_width / 2,drawable_flexion_width);
        path_flexion.lineTo(0,(float) drawable_flexion_width / 2);
        path_flexion.lineTo(0,(float) drawable_flexion_width / 2 - 10);
        path_flexion.lineTo((float) drawable_flexion_width / 2,drawable_flexion_width - 10);
        path_flexion.lineTo(drawable_flexion_width,(float) drawable_flexion_width / 2 - 10);
        path_flexion.lineTo(drawable_flexion_width,(float) drawable_flexion_width / 2);
        path_flexion.close();
        // bottom left triangle
        Path path_flexion_1 = new Path();
        path_flexion_1.moveTo(0,drawable_flexion_width);
        path_flexion_1.lineTo(0,(float) drawable_flexion_width / 2);
        path_flexion_1.lineTo((float) drawable_flexion_width / 2,drawable_flexion_width);
        path_flexion_1.close();
        // bottom right triangle
        Path path_flexion_2 = new Path();
        path_flexion_2.moveTo(drawable_flexion_width,drawable_flexion_width);
        path_flexion_2.lineTo(drawable_flexion_width,(float) drawable_flexion_width / 2);
        path_flexion_2.lineTo((float) drawable_flexion_width / 2,drawable_flexion_width);
        path_flexion_2.close();
        // top cutout
        Path path_flexion_3 = new Path();
        path_flexion_3.moveTo(0,0);
        path_flexion_3.lineTo(drawable_flexion_width,0);
        path_flexion_3.lineTo(drawable_flexion_width,(float) drawable_flexion_width / 2 - 10);
        path_flexion_3.lineTo((float) drawable_flexion_width / 2,drawable_flexion_width - 10);
        path_flexion_3.lineTo(0,(float) drawable_flexion_width / 2 - 10);
        path_flexion_3.close();

        canvas_flexion.drawPath(path_flexion, paint_flexion);
        canvas_flexion.clipOutPath(path_flexion_1);
        canvas_flexion.clipOutPath(path_flexion_2);
        canvas_flexion.clipOutPath(path_flexion_3);
        flexion_iv.setImageBitmap(bitmap_flexion);
        flexion_iv.invalidate();

/////////////////////////////////////////////////////////////////
        // arrow for pronation
        Drawable drawable_pronation = pronation_iv.getDrawable();
        bitmap_pronation =Bitmap.createBitmap(drawable_pronation.getIntrinsicWidth(), drawable_pronation.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas_pronation = new Canvas(bitmap_pronation);
        Paint paint_pronation = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_pronation.setColor(Color.GREEN);

        int drawable_pronation_width = drawable_pronation.getIntrinsicWidth();
        // drawable_pronation.getIntrinsicWidth() = drawable_pronation.getIntrinsicHeight() = 63
        // arrow itself
        Path path_pronation = new Path();
        path_pronation.moveTo(0,(float) drawable_pronation_width / 2);
        path_pronation.lineTo((float) drawable_pronation_width / 2,0);
        path_pronation.lineTo((float) drawable_pronation_width / 2 + 10,0);
        path_pronation.lineTo(10,(float) drawable_pronation_width / 2);
        path_pronation.lineTo((float) drawable_pronation_width / 2 + 10,drawable_pronation_width);
        path_pronation.lineTo((float) drawable_pronation_width / 2,drawable_pronation_width);
        path_pronation.close();
        // top left triangle
        Path path_pronation_1 = new Path();
        path_pronation_1.moveTo(0,0);
        path_pronation_1.lineTo(0,(float) drawable_pronation_width / 2);
        path_pronation_1.lineTo((float) drawable_pronation_width / 2,0);
        path_pronation_1.close();
        // bottom left triangle
        Path path_pronation_2 = new Path();
        path_pronation_2.moveTo(0,drawable_pronation_width);
        path_pronation_2.lineTo((float) drawable_pronation_width / 2,drawable_pronation_width);
        path_pronation_2.lineTo(0,(float) drawable_pronation_width / 2);
        path_pronation_2.close();
        // right cutout
        Path path_pronation_3 = new Path();
        path_pronation_3.moveTo(drawable_pronation_width,0);
        path_pronation_3.lineTo(drawable_pronation_width,drawable_pronation_width);
        path_pronation_3.lineTo((float) drawable_pronation_width / 2 + 10,drawable_pronation_width);
        path_pronation_3.lineTo(10,(float) drawable_pronation_width / 2);
        path_pronation_3.lineTo((float) drawable_pronation_width / 2 + 10,0);
        path_pronation_3.close();

        canvas_pronation.drawPath(path_pronation, paint_pronation);
        canvas_pronation.clipOutPath(path_pronation_1);
        canvas_pronation.clipOutPath(path_pronation_2);
        canvas_pronation.clipOutPath(path_pronation_3);
        pronation_iv.setImageBitmap(bitmap_pronation);
        pronation_iv.invalidate();

/////////////////////////////////////////////////////////////////
        // arrow for supination
        Drawable drawable_supination = supination_iv.getDrawable();
        bitmap_supination =Bitmap.createBitmap(drawable_supination.getIntrinsicWidth(), drawable_supination.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas_supination = new Canvas(bitmap_supination);
        Paint paint_supination = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_supination.setColor(Color.GREEN);

        int drawable_supination_width = drawable_supination.getIntrinsicWidth();
        // drawable_supination.getIntrinsicWidth() = drawable_supination.getIntrinsicHeight() = 63
        // arrow itself
        Path path_supination = new Path();
        path_supination.moveTo(drawable_supination_width,(float) drawable_supination_width / 2);
        path_supination.lineTo((float) drawable_supination_width / 2,0);
        path_supination.lineTo((float) drawable_supination_width / 2 - 10,0);
        path_supination.lineTo(drawable_supination_width - 10,(float) drawable_supination_width / 2);
        path_supination.lineTo((float) drawable_supination_width / 2 - 10,drawable_supination_width);
        path_supination.lineTo((float) drawable_supination_width / 2,drawable_supination_width);
        path_supination.close();
        // top right triangle
        Path path_supination_1 = new Path();
        path_supination_1.moveTo(drawable_supination_width,0);
        path_supination_1.lineTo((float) drawable_supination_width / 2,0);
        path_supination_1.lineTo(drawable_supination_width,(float) drawable_supination_width / 2);
        path_supination_1.close();
        // bottom right triangle
        Path path_supination_2 = new Path();
        path_supination_2.moveTo(drawable_supination_width,drawable_supination_width);
        path_supination_2.lineTo((float) drawable_supination_width / 2,drawable_supination_width);
        path_supination_2.lineTo(drawable_supination_width,(float) drawable_supination_width / 2);
        path_supination_2.close();
        // left cutout
        Path path_supination_3 = new Path();
        path_supination_3.moveTo(0,0);
        path_supination_3.lineTo(0,drawable_supination_width);
        path_supination_3.lineTo((float) drawable_supination_width / 2 - 10,drawable_supination_width);
        path_supination_3.lineTo(drawable_supination_width - 10,(float) drawable_supination_width / 2);
        path_supination_3.lineTo((float) drawable_supination_width / 2 - 10,0);
        path_supination_3.close();

        canvas_supination.drawPath(path_supination, paint_supination);
        canvas_supination.clipOutPath(path_supination_1);
        canvas_supination.clipOutPath(path_supination_2);
        canvas_supination.clipOutPath(path_supination_3);
        supination_iv.setImageBitmap(bitmap_supination);
        supination_iv.invalidate();
    }

/*
    // Convert transparentColor to be transparent in a Bitmap.
    public static Bitmap makeTransparent(Bitmap bit, Color transparentColor) {
        int width =  bit.getWidth();
        int height = bit.getHeight();
        Bitmap myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int [] allpixels = new int [ myBitmap.getHeight()*myBitmap.getWidth()];
        bit.getPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(),myBitmap.getHeight());
        myBitmap.setPixels(allpixels, 0, width, 0, 0, width, height);

        for(int i =0; i<myBitmap.getHeight()*myBitmap.getWidth();i++){
            if( allpixels[i] == transparentColor.toArgb()) {
                allpixels[i] = Color.alpha(Color.TRANSPARENT);
            }
        }

        myBitmap.setPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        return myBitmap;
    }
*/

    // update frequency button
    public void updateFrequency(View view) {
        if(!TextUtils.isEmpty(bicep_frq_et.getText().toString())) {
            this.BICEP_FRQ = Integer.parseInt(bicep_frq_et.getText().toString());
            bicep_frq_et.setHint(String.valueOf(BICEP_FRQ));
            bicep_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(BICEP_FRQ , 1000)
            };
            bicep_series.resetData(dataPoint);
            graphView.addSeries(bicep_series);
        } else {}
        if(!TextUtils.isEmpty(triceps_frq_et.getText().toString())) {
            this.TRICEPS_FRQ = Integer.parseInt(triceps_frq_et.getText().toString());
            triceps_frq_et.setHint(String.valueOf(TRICEPS_FRQ));
            triceps_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(TRICEPS_FRQ , 1000)
            };
            triceps_series.resetData(dataPoint);
            graphView.addSeries(triceps_series);
        } else {}
        if(!TextUtils.isEmpty(forearm_frq_et.getText().toString())) {
            this.FOREARM_FRQ = Integer.parseInt(forearm_frq_et.getText().toString());
            forearm_frq_et.setHint(String.valueOf(FOREARM_FRQ));
            forearm_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(FOREARM_FRQ , 1000)
            };
            forearm_series.resetData(dataPoint);
            graphView.addSeries(forearm_series);
        } else {}
        if(!TextUtils.isEmpty(extension_frq_et.getText().toString())) {
            this.EXTENSION_FRQ = Integer.parseInt(extension_frq_et.getText().toString());
            extension_frq_et.setHint(String.valueOf(EXTENSION_FRQ));
            extension_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(EXTENSION_FRQ , 1000)
            };
            extension_series.resetData(dataPoint);
            graphView.addSeries(extension_series);
        } else {}
        if(!TextUtils.isEmpty(flexion_frq_et.getText().toString())) {
            this.FLEXION_FRQ = Integer.parseInt(flexion_frq_et.getText().toString());
            flexion_frq_et.setHint(String.valueOf(FLEXION_FRQ));
            flexion_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(FLEXION_FRQ , 1000)
            };
            flexion_series.resetData(dataPoint);
            graphView.addSeries(flexion_series);
        } else {}
        if(!TextUtils.isEmpty(pronation_frq_et.getText().toString())) {
            this.PRONATION_FRQ = Integer.parseInt(pronation_frq_et.getText().toString());
            pronation_frq_et.setHint(String.valueOf(PRONATION_FRQ));
            pronation_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(PRONATION_FRQ , 1000)
            };
            pronation_series.resetData(dataPoint);
            graphView.addSeries(pronation_series);
        } else {}
        if(!TextUtils.isEmpty(supination_frq_et.getText().toString())) {
            this.SUPINATION_FRQ = Integer.parseInt(supination_frq_et.getText().toString());
            supination_frq_et.setHint(String.valueOf(SUPINATION_FRQ));
            supination_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(SUPINATION_FRQ , 1000)
            };
            supination_series.resetData(dataPoint);
            graphView.addSeries(supination_series);
        } else {}
    }

    // start recording button
    public void startRecording(View view) {
        int status = (Integer) start_recording_button.getTag();
        if(status == 1) {
            start_recording_button.setText("Stop Recording"); // change text when clicked
            start_recording_button.setTag(0);
        }
        else {
            start_recording_button.setText("Start Recording"); // change text when clicked
            start_recording_button.setTag(1);
        }
    }

    // replay recording button
    public void replayRecording(View view) { // TODO: to be implemented
        Snackbar.make(view, "Feature still not implemented", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void requestRecordAudioPermission() {
        //check API version, do nothing if API version < 23!
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Activity", "Granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Activity", "Denied!");
                    finish();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class RecordAudio extends AsyncTask<Void , double[] , Boolean> {

        private Paint seriesPaint;
        private RealDoubleFFT transformer;
        private AudioRecord audioRecord;
        private float xmax , xmin , ymax , ymin;
        int blockSize = 256;
        boolean started = false;
        boolean CANCELLED_FLAG = false;
        double[][] cancelledResult = {{100}};
        private double[] real , imaginary , magnitude , frequency;
        int sampleRate = 42000;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        DataPoint[] dataPoints;
        private boolean bicepActive , tricepActive , forearmActive , extensionActive , flexionActive , pronationActive , supinationActive;

        public final static String IO_FILENAME= "KISDataREC";
        public FileOutputStream fOut;
        public FileInputStream fIn;
        public File file;
        public InputStreamReader myInReader;
        public OutputStreamWriter myOutWriter;
        public boolean isRecording = false , wasRecording = false , wasRepaying = false , isReplaying = false;

        public RecordAudio(float xmax, float xmin, float ymax, float ymin) {
            this.xmax = xmax;
            this.xmin = xmin;
            this.ymax = ymax;
            this.ymin = ymin;

            bicepActive = false;
            tricepActive = false;
            forearmActive = false;
            extensionActive = false;
            flexionActive = false;
            pronationActive = false;
            supinationActive = false;

        }

        private DataPoint[] generateData(double[][] progress) {
            int count = 128;
            DataPoint[] values = new DataPoint[count];
            for(int i = 0 ; i < count ; i++) {
                double x = (21000 / 128) * i;
                double y = progress[0][i];
                DataPoint v = new DataPoint(x , y);
                values[i] = v;
            }
            return values;
        }

        private void setUpGraph() {
            graphView = (GraphView) findViewById(R.id.frequency_spectrum);
            min_series = new LineGraphSeries<DataPoint>();
            max_series = new LineGraphSeries<DataPoint>();
            bicep_series = new BarGraphSeries<DataPoint>();
            triceps_series = new BarGraphSeries<DataPoint>();
            forearm_series = new BarGraphSeries<DataPoint>();
            extension_series = new BarGraphSeries<DataPoint>();
            flexion_series = new BarGraphSeries<DataPoint>();
            pronation_series = new BarGraphSeries<DataPoint>();
            supination_series = new BarGraphSeries<DataPoint>();
            sound_series = new LineGraphSeries<DataPoint>();
            display = getWindowManager().getDefaultDisplay(); // get device screen info
            int height = display.getHeight(); // get height of screen
            int graphViewHeight = height / 4; // graph is 1/4 of the screen's height
            graphView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , graphViewHeight)); // set graph's width and height
            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() { // set labels for x_frq and y_frq axis
                @Override
                public String formatLabel(double value , boolean isValueX) {
                    if (isValueX) {
                        // show normal x_frq values
                        return super.formatLabel(value / 1000, isValueX) + "kHz";
                    } else {
                        // show currency for y_frq values
                        return super.formatLabel(value, isValueX) + "dB";
                    }
                }
            });
            graphView.getViewport().setXAxisBoundsManual(true); // set range of x-axis
            graphView.getViewport().setMinX(0.0);
            graphView.getViewport().setMaxX(21000);
            graphView.getViewport().setYAxisBoundsManual(true); // set range of y-axis
            graphView.getViewport().setMinY(0.0);
            graphView.getViewport().setMaxY(100);
            graphView.getViewport().setScalable(true); // make zooming and scrolling active x-axis
            graphView.getViewport().setScalableY(true); // make zooming and scrolling active y-axis

            // setting up min line
            x_min = 0;
            Paint min_paint = new Paint();
            setDashPaint(min_series , min_paint , Color.GREEN);
            for(int j = 0 ; j < 128 ; j++) {
                x_min = (21000 / 128) * j;
                y_min = MIN_MAGNITUDE;
                min_series.appendData(new DataPoint(x_min , y_min) , true , 128);
            }
            graphView.addSeries(min_series);

            // setting up max line
            x_max = 0;
            Paint max_paint = new Paint();
            setDashPaint(max_series , max_paint , Color.RED);
            for(int j = 0 ; j < 128 ; j++) {
                x_max = (21000 / 128) * j;
                y_max = MAX_MAGNITUDE;
                max_series.appendData(new DataPoint(x_max , y_max) , true , 128);
            }
            graphView.addSeries(max_series);

            setTargetFrequencyLines(bicep_series , Color.CYAN , BICEP_FRQ);
            setTargetFrequencyLines(triceps_series , Color.YELLOW , TRICEPS_FRQ);
            setTargetFrequencyLines(forearm_series , Color.MAGENTA , FOREARM_FRQ);
            setTargetFrequencyLines(extension_series , 0xff99cc00 , EXTENSION_FRQ); // android.R.color.holo_green_light
            setTargetFrequencyLines(flexion_series , 0xff33b5e5 , FLEXION_FRQ); // android.R.color.holo_blue_light
            setTargetFrequencyLines(pronation_series , 0xffffbb33 , PRONATION_FRQ); // android.R.color.holo_orange_light
            setTargetFrequencyLines(supination_series , 0xffff4444 , SUPINATION_FRQ); // android.R.color.holo_red_light

            seriesPaint = new Paint();
            seriesPaint.setColor(Color.BLUE);
            seriesPaint.setStyle(FILL_AND_STROKE);
            seriesPaint.setStrokeWidth(6);
        }

        // set max and min magnitude dashed lines
        public void setDashPaint(LineGraphSeries series , Paint paint , int color) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(color);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{2000, 2000}, 0);
            paint.setPathEffect(dashPathEffect);
            paint.setAntiAlias(true);
            series.setCustomPaint(paint);
        }

        // set target frequencies
        public void setTargetFrequencyLines(BarGraphSeries series , int color , int frq) {
            series.appendData(new DataPoint(frq , 1000) , true , 1);
            series.setDataWidth(700f);
            series.setColor(color);
            graphView.addSeries(series);
        }

        private double[] freqMagnitude(double [] toTransform) {
            real = new double[blockSize];
            imaginary = new double[blockSize];
            magnitude = new double[blockSize / 2];
            frequency = new double[blockSize / 2];

            for (int i = 0 ; i < blockSize / 2 ; i++) {
                real[i] = toTransform[i * 2];
                imaginary[i] = toTransform[(i * 2) + 1];
            }

            for (int i = 0 ; i < blockSize / 2 ; i++) {
                magnitude[i] = (Math.sqrt((real[i] * real[i]) + (imaginary[i] * imaginary[i]))); // magnitude is calculated by the square root of (imaginary^2 + real^2)
                frequency[i] = i * (sampleRate) / (blockSize); // calculated the frequency
            }

            return magnitude;
        }

        public File init_writeFile() {
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/KIS/"); // Get the directory for the user's public pictures directory.
            if(!path.exists()) { // Make sure the path directory exists.
                path.mkdirs(); // Make it, if it doesn't exit
            }
            File file = new File(path, RecordAudio.IO_FILENAME);
            return file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            transformer = new RealDoubleFFT(blockSize);
            dataPoints = new DataPoint[]{new DataPoint(1 , 1)};
//            setUpMuscles();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d("Recording doBackground", voids.toString());
            file = init_writeFile(); // initiation of file writing
            try {
                if(!file.exists()) // check if file doesn't exist
                    file.createNewFile(); // create a new file

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut = new FileOutputStream(file); // output file
                myOutWriter = new OutputStreamWriter(fOut); // output writer?

                fIn = new FileInputStream(file); // input file
                myInReader = new InputStreamReader(fIn); // input reader?
            } catch (IOException e) {
                e.printStackTrace();
            }
            int  bufferSize = AudioRecord.getMinBufferSize(sampleRate , channelConfiguration , audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT , sampleRate , channelConfiguration , audioEncoding , bufferSize);
            int state = audioRecord.getState();
            int bufferReadResult = 0;
            int counter = 0;
            long total = 0;
            boolean run = true;
            short[] buffer = new short[blockSize];
            byte[] buff = new byte[2 * blockSize];
            double[] toTransform = new double[blockSize];
            try {
                audioRecord.startRecording();
                started = true;
            } catch (IllegalStateException e) {
                Log.e("Recording failed" , e.toString());
            }
            while (started) {
                if(isCancelled() || (CANCELLED_FLAG == true)) {
                    started = false;
                    publishProgress(cancelledResult);
                    Log.d("doInBackground" , "Cancelling the RecordTask");
                    break;
                }
                else {
                    if(!isReplaying) {
                        bufferReadResult = audioRecord.read(buffer , 0 , blockSize);
                        if (isRecording) { // if is recording
                            ByteBuffer.wrap(buff).asShortBuffer().put(buffer); // write to buffer?
                            wasRecording = true;
                            try {
                                if (total + bufferReadResult > 4294967295L) { // Write as many bytes as we can before hitting the max size
                                    for (int i = 0; i < bufferReadResult && total <= 4294967295L; i++, total++) {
                                        fOut.write(buff[i]);
                                    }
                                    isRecording = false; // is recording is false because file limit is reached
                                    Log.v("File ", "hit file limit");
                                } else {
                                    fOut.write(buff, 0, bufferReadResult); // Write out the entire read buffer
                                }
                                total += bufferReadResult;

                            } catch (IOException ex) {
                            } finally { //return new Object[]{ex};
                                if (!isRecording && wasRecording && fOut != null)
                                    try {
                                        fOut.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        }
                    }
                    else if(isReplaying) {
                    SystemClock.sleep(75);
                    try {
                        fIn = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (total <= buff.length) { //if short record
                            fIn.read(buff, 0, (int) total);
                            bufferReadResult = (int) total;
                            isReplaying = false;
                        } else { //if long record
                            if (counter < 10) {
                                fIn.read(buff, counter * buff.length, buff.length);
                                counter++;
                            }else
                            if (counter == blockSize-1) {
                                fIn.read(buff, counter * buff.length, (int) (total - buff.length * counter));
                                counter = 0;
                                isReplaying = false;
                            }
                            bufferReadResult = blockSize;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if (!isReplaying && wasRepaying && fIn != null)
                            try {
                                fIn.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                    ByteBuffer.wrap(buff).asShortBuffer().get(buffer);
                }
                    for (int i = 0 ; i < blockSize && i < bufferReadResult ; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                    }

                    transformer.ft(toTransform);
                    publishProgress(freqMagnitude(toTransform));
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(double[]...progress) {
            Log.e("RecordingProgress", "Displaying in progress_tv");
            graphView.removeSeries(sound_series);
            sound_series.resetData(generateData(progress));
            sound_series = new LineGraphSeries<>(generateData(progress));
            sound_series.setCustomPaint(seriesPaint);
            //sound_series.setBackgroundColor(Color.rgb(0 , 157 , 255));
            sound_series.setBackgroundColor(Color.rgb(0 , 0 , 0));
            sound_series.setDrawBackground(true);

            graphView.addSeries(sound_series);
            for(int i = 0 ; i < progress[0].length ; i++) {

//                progress_tv.setText("Progress = " + Double.toString(progress[0][i]));
//                magnitude_tv.setText("Magnitude = " + Double.toString(magnitude[i]));
//                frequency_tv.setText("Frequency = " + Double.toString(frequency[i]));

                if(progress[0][i] >= MIN_MAGNITUDE && progress[0][i] < MID_MAGNITUDE) {
                    if(frequency[i] > (BICEP_FRQ - margin) && frequency[i] < (BICEP_FRQ + margin)) {
                        bicepActive = true;
                        int red_value = (int) (Math.min((progress[0][i] ) , 1) * 255);
                        int green_value = 220;
                        canvas_bicep.drawColor(Color.rgb(red_value , green_value , 0));
                    }
                    if(frequency[i] > (TRICEPS_FRQ - margin) && frequency[i] < (TRICEPS_FRQ + margin)) {
                        tricepActive = true;
                        int red_value = (int) (Math.min((progress[0][i] / MAX_MAGNITUDE) , 1) * 255);
                        canvas_tricep.drawColor(Color.rgb(red_value , 255 , 0));
                    }
                    if(frequency[i] > (FOREARM_FRQ - margin) && frequency[i] < (FOREARM_FRQ + margin)) {
                        forearmActive = true;
                        int red_value = (int) (Math.min((progress[0][i] / MAX_MAGNITUDE) , 1) * 255);
                        canvas_forearm.drawColor(Color.rgb(red_value , 255 , 0));
                    }
                    if(frequency[i] > (EXTENSION_FRQ - margin) && frequency[i] < (EXTENSION_FRQ + margin)) {
                        extensionActive = true;
                        int red_value = (int) (Math.min((progress[0][i] / MAX_MAGNITUDE) , 1) * 255);
                        canvas_extension.drawColor(Color.rgb(red_value , 255 , 0));
                    }
                    if(frequency[i] > (FLEXION_FRQ - margin) && frequency[i] < (FLEXION_FRQ + margin)) {
                        flexionActive = true;
                        int red_value = (int) (Math.min((progress[0][i] / MAX_MAGNITUDE) , 1) * 255);
                        canvas_flexion.drawColor(Color.rgb(red_value , 255 , 0));
                    }
                    if(frequency[i] > (PRONATION_FRQ - margin) && frequency[i] < (PRONATION_FRQ + margin)) {
                        pronationActive = true;
                        int red_value = (int) (Math.min((progress[0][i] / MAX_MAGNITUDE) , 1) * 255);
                        canvas_pronation.drawColor(Color.rgb(red_value , 255 , 0));
                    }
                    if(frequency[i] > (SUPINATION_FRQ - margin) && frequency[i] < (SUPINATION_FRQ + margin)) {
                        supinationActive = true;
                        int red_value = (int) (Math.min((progress[0][i] / MAX_MAGNITUDE) , 1) * 255);
                        canvas_supination.drawColor(Color.rgb(red_value , 255 , 0));
                    }
                }


                if (progress[0][i] >= MID_MAGNITUDE) {
                    if(frequency[i] > (BICEP_FRQ - margin) && frequency[i] < (BICEP_FRQ + margin)) {
                        bicepActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i]) , 0)  * 255);
                        canvas_bicep.drawColor(Color.rgb(255 , 0 , 0));
                    }
                    if(frequency[i] > (TRICEPS_FRQ - margin) && frequency[i] < (TRICEPS_FRQ + margin)) {
                        tricepActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i])/ MAX_MAGNITUDE , 0)  * 255);
                        canvas_tricep.drawColor(Color.rgb(255 , green_value , 0));
                    }
                    if(frequency[i] > (FOREARM_FRQ - margin) && frequency[i] < (FOREARM_FRQ + margin)) {
                        forearmActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i])/ MAX_MAGNITUDE , 0)  * 255);
                        canvas_forearm.drawColor(Color.rgb(255 , green_value , 0));
                    }
                    if(frequency[i] > (EXTENSION_FRQ - margin) && frequency[i] < (EXTENSION_FRQ + margin)) {
                        extensionActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i])/ MAX_MAGNITUDE , 0)  * 255);
                        canvas_extension.drawColor(Color.rgb(255 , green_value , 0));
                    }
                    if(frequency[i] > (FLEXION_FRQ - margin) && frequency[i] < (FLEXION_FRQ + margin)) {
                        flexionActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i])/ MAX_MAGNITUDE , 0)  * 255);
                        canvas_flexion.drawColor(Color.rgb(255 , green_value , 0));
                    }
                    if(frequency[i] > (PRONATION_FRQ - margin) && frequency[i] < (PRONATION_FRQ + margin)) {
                        pronationActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i])/ MAX_MAGNITUDE , 0)  * 255);
                        canvas_pronation.drawColor(Color.rgb(255 , green_value , 0));
                    }
                    if(frequency[i] > (SUPINATION_FRQ - margin) && frequency[i] < (SUPINATION_FRQ + margin)) {
                        supinationActive = true;
                        int green_value = (int) (Math.max((MAX_MAGNITUDE - progress[0][i])/ MAX_MAGNITUDE , 0)  * 255);
                        canvas_supination.drawColor(Color.rgb(255 , green_value , 0));
                    }
                }


                if(progress[0][i] < MIN_MAGNITUDE) {
                    if(frequency[i] > (BICEP_FRQ - margin) && frequency[i] < (BICEP_FRQ + margin)) {
                        bicepActive = false;
                        canvas_bicep.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (TRICEPS_FRQ - margin) && frequency[i] < (TRICEPS_FRQ + margin)) {
                        tricepActive = false;
                        canvas_tricep.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (FOREARM_FRQ - margin) && frequency[i] < (FOREARM_FRQ + margin)) {
                        forearmActive = false;
                        canvas_forearm.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (EXTENSION_FRQ - margin) && frequency[i] < (EXTENSION_FRQ + margin)) {
                        extensionActive = false;
                        canvas_extension.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (FLEXION_FRQ - margin) && frequency[i] < (FLEXION_FRQ + margin)) {
                        forearmActive = false;
                        canvas_flexion.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (PRONATION_FRQ - margin) && frequency[i] < (PRONATION_FRQ + margin)) {
                        pronationActive = false;
                        canvas_pronation.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (SUPINATION_FRQ - margin) && frequency[i] < (SUPINATION_FRQ + margin)) {
                        supinationActive = false;
                        canvas_supination.drawColor(Color.GREEN);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                Log.e("Stop failed", e.toString());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        recordTask = new RecordAudio(xmax , xmin , ymax , ymin);
        recordTask.setUpGraph();
        recordTask.execute();
    }
}