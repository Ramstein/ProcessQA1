package net.gotev.speechdemo;

//import android.app.AlertDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
    public static List<String> SpeechRecognizerInput = new ArrayList<>();
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


    public static synchronized boolean SpeakPromptly(String text) {
        while (true) {
            if (!Speech.getInstance().isSpeaking()) {
                Speech instance = Speech.getInstance();
                instance.setTextToSpeechRate((float) 0.8);
                instance.say(text, new TextToSpeechCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public boolean onCompleted() {
                        return true;
                    }

                    @Override
                    public void onError() {
                    }
                });
            } else {
                Utils.sleep(1);
            }
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

        if (Utils.ResolveRecognizerIntent(recognizerIntent) != null) {
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
}
