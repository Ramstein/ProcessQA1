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

import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.TextToSpeechCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.System.gc;

public class Utils extends AppCompatActivity {
    public static final int RECORD_AUDIO_PERMISSIONS_REQUEST = 1;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final Integer RECOGNIZERINTENTREQUESTCODE = 10;
    public static List<String> SpeechRecognizerInput = new ArrayList<>();
    public static boolean test;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    static boolean[] isSpeaking = {false};

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

    public static void StartRecognizerActivityForResult(Activity activity, Intent intent, Integer RECOGNIZERINTENTREQUESTCODE) {
        activity.startActivityForResult(intent, RECOGNIZERINTENTREQUESTCODE);
    }


    @SuppressLint({"QueryPermissionsNeeded", "SetTextI18n"})
    public static boolean getSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (Utils.ResolveRecognizerIntent(intent) != null) {
            Log.e("getSpeechInput", "Utils.StartRecognizerActivityForResult");
            Utils.StartRecognizerActivityForResult((Activity) Utils.context, intent, RECOGNIZERINTENTREQUESTCODE); //RECOGNIZERINTENTREQUESTCODE
            return true;
        } else {
            Log.e("getSpeechInput", "Your device don't support speech input");
            return false;
        }
    }

    //#########################  TTS speaking the en language
    public static synchronized void SpeakPromptly(String text) {
        if (!isSpeaking[0]) {
            Speech.getInstance().setTextToSpeechRate((float) 0.8).say(text, new TextToSpeechCallback() {
                @Override
                public void onStart() {
                    Logger.error(LOG_TAG, "TTS onStart");
                    isSpeaking[0] = true;
                }

                @Override
                public void onCompleted() {
                    Logger.error(LOG_TAG, "TTS onCompleted");
                    isSpeaking[0] = false;
                }

                @Override
                public void onError() {
                    Logger.error(LOG_TAG, "TTS onError");
                    isSpeaking[0] = false;
                }
            });
        }
        gc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOGNIZERINTENTREQUESTCODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                SpeechRecognizerInput.add(result.get(0).toUpperCase());
                Log.e("onActivityResult", String.valueOf(result));

                if (test) {
                    if (SpeechRecognizerInput.get((SpeechRecognizerInput.size() - 1)).equals("")) {
                        SpeakPromptly("Not able to hear you.");
                    }
                    SpeechRecognizerInput.set((SpeechRecognizerInput.size() - 1), "");
                }
            }
        }
    }

}
