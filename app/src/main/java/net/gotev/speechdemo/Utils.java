package net.gotev.speechdemo;

//import android.app.AlertDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.gotev.speech.Speech;
import net.gotev.speech.TextToSpeechCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Utils extends AppCompatActivity {
    public static final int RECORD_AUDIO_PERMISSIONS_REQUEST = 1;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final Integer RECOGNIZERINTENTREQUESTCODE = 10;
    public static final long SPEECHRATE = (long) 0.7;  // SpeechRate 0.0 < x < 2.0
    public static List<String> SpeechRecognizerInput = new ArrayList<>();
    public static boolean isBluetoothConnected = false;
    public static boolean test;
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static AlertDialog.Builder alertDialogBuilder() {
        return new AlertDialog.Builder(context);
    }

    public static boolean requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSIONS_REQUEST);
        }
        return false;
    }

    public static RequestQueue Queue() {
        return Volley.newRequestQueue(context);
    }

    @SuppressLint("QueryPermissionsNeeded")
    public static ComponentName ResolveRecognizerIntent(Intent intent) {
        return intent.resolveActivity(context.getPackageManager());
    }


    public static synchronized void SpeakPromptly(String text) {
        if (isBluetoothConnected) {
            if (!Speech.getInstance().isSpeaking()) {
                Speech.getInstance().setTextToSpeechRate(SPEECHRATE).say(text, new TextToSpeechCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError() {
                    }
                });
                Utils.sleep(1);
            } else {
                Log.e("SpeakPromptly", "!Speech.getInstance().isSpeaking(): " + !Speech.getInstance().isSpeaking());
            }
        } else {
            Log.e("SpeakPromptly", "isBluetoothConnected: " + isBluetoothConnected);
        }
    }


    @SuppressLint({"QueryPermissionsNeeded", "SetTextI18n"})
    public static boolean getSpeechInput() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        if (Utils.ResolveRecognizerIntent(recognizerIntent) != null & isBluetoothConnected) {
            ((Activity) Utils.context).startActivityForResult(recognizerIntent, RECOGNIZERINTENTREQUESTCODE);
            Log.e("getSpeechInput", "Utils.StartRecognizerActivityForResult");
            return true;
        } else {
            Log.e("getSpeechInput", "Your device don't support speech input");
            return false;
        }
    }

    static void sleep(long second) {
        try {
            SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBluetoothHeadsetConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Utils.isBluetoothConnected = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
        if (Utils.isBluetoothConnected) {
            Log.e("BluetoothHeadset", "isBluetoothHeadsetConnected: " + Utils.isBluetoothConnected);
            return true;
        } else {
            Log.e("BluetoothHeadset", "isBluetoothHeadsetConnected: " + Utils.isBluetoothConnected);
            return false;
        }
    }
}
