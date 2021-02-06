package net.gotev.speechdemo;

//import android.app.AlertDialog;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Utils extends AppCompatActivity {
    static final int RECORD_AUDIO_PERMISSIONS_REQUEST = 1;
    public Context context;


    public AlertDialog.Builder alertDialogBuilder() {
        return new AlertDialog.Builder(this);
    }

    public boolean requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSIONS_REQUEST);
        }
        return false;
    }

    public RequestQueue Queue() {
        return Volley.newRequestQueue(context);
    }

}
