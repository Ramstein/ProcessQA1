package net.gotev.speechdemo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.SupportedLanguagesListener;
import net.gotev.speech.TextToSpeechCallback;
import net.gotev.speech.UnsupportedReason;
import net.gotev.speech.ui.SpeechProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.System.gc;
import static net.gotev.speechdemo.Utils.LOG_TAG;
import static net.gotev.speechdemo.Utils.SpeakPromptly;
import static net.gotev.speechdemo.Utils.getSpeechInput;

public class MainActivity extends AppCompatActivity implements SpeechDelegate {

    private static final String GET_URL = "https://0yh5imhg3m.execute-api.ap-south-1.amazonaws.com/prod";
    private static final String POST_URL = "https://89t84kai7b.execute-api.ap-south-1.amazonaws.com/prod";
    private static final boolean API_POST = false;
    private static final float SPEECHRATE = (float) 0.7;  // SpeechRate 0.0 < x < 2.0
    private static final long[] TIMER_MILLIs = {
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000,
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000};
    private static final boolean TimerWithGoogleWindow = false;
    private static final String[] params = new String[3];
    public static Integer n_que_answered = 0;
    public static Integer n_que_answer_spoken = 0;
    public static boolean mIslistening = false;

    @SuppressLint("StaticFieldLeak")
    static TextView textViewCountDown;
    @SuppressLint("StaticFieldLeak")
    static TextView textView;
    private static Integer n_que = 20; //10 + 3 + 5 + more 2 extra // you can set the number // 0 < n_que > 24
    private static TimePickerFragment timePickerFragment;
    @SuppressLint("StaticFieldLeak")
    private static EditText editText;
    @SuppressLint("StaticFieldLeak")
    private static EditText n_que_layout;
    private static String sub_code = "";
    @SuppressLint("StaticFieldLeak")
    private static ImageButton mic_btn;
    private static SpeechProgressView progress;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout linearLayout;
    private static PreferencesHandler preferencesHandler;
    private final TextToSpeech.OnInitListener mTttsInitListener = status -> {
        switch (status) {
            case TextToSpeech.SUCCESS:
                Logger.info(LOG_TAG, "TextToSpeech engine successfully started");
                break;

            case TextToSpeech.ERROR:
                Logger.error(LOG_TAG, "Error while initializing TextToSpeech engine!");
                break;

            default:
                Logger.error(LOG_TAG, "Unknown TextToSpeech status: " + status);
                break;
        }
    };
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                Utils.isBluetoothConnected = true;
                Log.e("BluetoothDevice", "Utils.isBluetoothConnected = true;  ");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Utils.isBluetoothConnected = false;
                Log.e("BluetoothDevice", "Utils.isBluetoothConnected = false;  ");
                //Device has disconnected
            }
        }
    };
    public Handler backgroundHandler;
    private TextView topTextView;
    private EditText textToSpeech;
    private HandlerThread backgroundThread;

    public static void onSpeechToTextExec() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            if (Utils.requestAudioPermission() & Utils.isBluetoothConnected) {
                onRecordAudioPermissionGranted();
            }
        }
    }

    private static void onRecordAudioPermissionGranted() {

        mic_btn.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

        try {
            Speech.getInstance().stopTextToSpeech();
            Log.e("STT", "Speech.getInstance().startListening(progress, (SpeechDelegate) Utils.context);.");
            Speech.getInstance().startListening(progress, (SpeechDelegate) Utils.context);
        } catch (SpeechRecognitionNotAvailable exc) {
            showSpeechNotSupportedDialog();
        } catch (GoogleVoiceTypingDisabledException exc) {
            showEnableGoogleVoiceTyping();
        }
    }

    private static void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    SpeechUtil.redirectUserToGoogleAppOnPlayStore(Utils.context);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = Utils.alertDialogBuilder();
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private static void showEnableGoogleVoiceTyping() {
        AlertDialog.Builder builder = Utils.alertDialogBuilder();
        builder.setMessage(R.string.enable_google_voice_typing)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    // do nothing
                })
                .show();
    }

    public static synchronized void startTimer(long ms) {
        timePickerFragment.timerRunning = true;
        timePickerFragment.countDownTimer = new CountDownTimer(ms, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                timePickerFragment.updateCountDownText(millisUntilFinished);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                timePickerFragment.timerRunning = false;
                if (n_que_answered <= n_que) {
                    String temp_txt = n_que_layout.getText().toString();
                    if (!temp_txt.equals("") & temp_txt.matches("[0-9]+")) {
                        int n = Integer.parseInt(temp_txt);
                        if (n > 1 & n < 25) {
                            n_que = n;
                        } else {
                            textView.setText("Going with 20 questions.");
                        }
                    } else {
                        textView.setText("Going with 20 questions.");
                    }
                    if (TimerWithGoogleWindow) {
                        SpeechToText();  // launches speech to text with a google interface window
                        processQuestionToAnswer();
                    } else {
                        onSpeechToTextQuestion();
                    }
                    timePickerFragment.nextExecution();
                } else { // all of the question answered from lambda, start speaking them
                    Speech.getInstance().say("Answering the questions now", new TextToSpeechCallback() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public boolean onCompleted() {
                            startAnsweringTheQuestions();
                            return false;
                        }

                        @Override
                        public void onError() {
                        }
                    });
                }

            }
        }.start();
        timePickerFragment.timerRunning = true;
    }

    private static void onSpeechToTextQuestion() {
        sub_code = editText.getText().toString().toUpperCase();
        mIslistening = false;
        if (sub_code.length() > 3) {
            SpeakPromptly("r" + (n_que_answered + 1));
            Utils.sleep(1);
            Utils.test = false;
            onSpeechToTextExec();
        } else {
            for (int i = 0; i < 3; i++) {
                SpeakPromptly("Not a valid subject code " + sub_code);
            }
        }
    }

    private static void processQuestionToAnswer() {
        String speech = "";
        try {
            Log.e("processQuestionToAnswer", String.valueOf(n_que_answered));
            speech = Utils.SpeechRecognizerInput.get(n_que_answered);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (speech.equals("")) {
            SpeakPromptly("Not a question.");
            Utils.sleep(1);
        } else {
            String url = createUrl(0, sub_code, speech);
            if (API_POST) {
                sendPostRequest(url);
            } else {
                sendGetRequest(url);
            }
        }
    }

    private static void startAnsweringTheQuestions() {
        for (int i = 0; i < n_que_answered; i++) {
            String[] que_ans = preferencesHandler.getQueAnsFromPreferences("question" + (i + 1), "answer" + (i + 1));
            Log.e("startAnswering", Arrays.toString(que_ans));
            if (!que_ans[0].equals("") & !que_ans[1].equals("")) {
                if (SpeakPromptly(que_ans[0])) {
                    Utils.sleep(10);
                    if (SpeakPromptly("Answering now")) {
                        Utils.sleep(1);
                        SpeakAnswer(que_ans[1]);
                        preferencesHandler.removeQueAnsFromPreferences("question" + (i + 1), "answer" + (i + 1));
                    }
                }
            } else {
                SpeakPromptly("Negative " + (i + 1));
            }
            n_que_answer_spoken += 1;
        }
    }

    private static synchronized void sendGetRequest(String url) {
        RequestQueue queue = Utils.Queue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            response = response.replace("\n", "");
            response = response.replace("\\", "");
            String que = response.split("\": \"")[5].split("\", \"")[0]; // "\ " and ", "
            String ans = response.split("\": \"")[6].split("\", \"")[0]; // "\ " and ", "
            Log.e("sendGetRequest", "Volley Request succeed: " + response);
            if (preferencesHandler.putQueAnsInPreferences((n_que_answered + 1), que, ans)) {
                n_que_answered += 1;
            } else {
                SpeakPromptly((n_que_answered + 1) + " negative.");
            }
            Log.e("sendGetRequest", "Volley Request succeed: n_que_answered: " + n_que_answered);
//                // JSONObject > JsonArray
//                String val = "{\"statusCode\":200,\n" +
//                        "\"headers\":{\"Content-type\":\"application\\/json\"}," +
//                        "\"body\":\"{\\\"launch_time\\\": \\\"21-02-06 22:36:46\\\", \\\"sub_code\\\": \\\"KCE051\\\", \\\"question\\\": \\\"what is magnetic confinement?\\\", \\\"answer\\\": \\\"Magnetic confinement fusion is an approach to generate thermonuclear fusion power that uses magnetic fields to confine fusion fuel in the form of a plasma. Magnetic confinement is one of two major branches of fusion energy research, along with inertial confinement fusion\\\\\\\\n'\\\", \\\"score\\\": 100}\"}";
        }, error -> {
            Log.e("sendGetRequest", "answer Request failed:" + error.getMessage());
            SpeakPromptly((n_que_answered + 1) + " negative.");
            Utils.sleep(1);
            textView.setText(("answer Request failed: " + error.getMessage()));
        });
        queue.add(stringRequest);
    }

    private static synchronized void sendPostRequest(String url) {
        Map<String, String> url_params = new HashMap<>();
        RequestQueue queue = Utils.Queue();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, POST_URL, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                Log.e("sendPostRequest", "Volley Request succeed: " + jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

//            Log.e("sendPostRequest", "Volley Request succeed: " + response);
//            response = response.replace("\n", "");
//            response = response.replace("\\", "");
//            Log.e("sendPostRequest", "Volley Request succeed: " + response);
//            String que = response.split("\": \"")[5].split("\", \"")[0]; // "\ " and ", "
//            String ans = response.split("\": \"")[6].split("\", \"")[0]; // "\ " and ", "
//            Log.e("sendPostRequest", "Volley Request succeed: " + response);
//            if (preferencesHandler.putQueAnsInPreferences((n_que_answered + 1), que, ans)) {
//                n_que_answered += 1;
//            } else {
//                SpeakPromptly((n_que_answered + 1) + " negative.");
//            }
//            Log.e("sendPostRequest", "Volley Request succeed: n_que_answered: " + n_que_answered);
        }, error -> {
            Log.e("sendPostRequest", "answer Request failed:" + error.getMessage());
            SpeakPromptly((n_que_answered + 1) + " negative.");
            Utils.sleep(1);
            textView.setText(("answer Request failed: " + error.getMessage()));


        }) {
            @Override
            protected Map<String, String> getParams() {
                url_params.put("t", MainActivity.params[0]);
                url_params.put("s", MainActivity.params[1]);
                url_params.put("q", MainActivity.params[2]);
                return url_params;
            }

//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json; charset=utf-8");
//                return headers;
//            }
//
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            public byte[] getBody() {
//                return url_params == null ? null : url_params.toString().getBytes(StandardCharsets.UTF_8);
//            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }

        };
        // Add the reliability on the connection.
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 2, 1.0f));
        queue.add(stringRequest);
    }

    ///////////////////////////  api request
    @SuppressLint({"WrongConstant", "ShowToast"})
    private static synchronized String createUrl(Integer test, String sub_code, String question) {
        params[0] = test.toString();
        params[1] = sub_code;
        params[2] = question;
        String url;
        if (API_POST) {
            url = POST_URL + "?t" + "=" + test +
                    "&s" + "=" + sub_code +
                    "&q" + "=" + question;
            Log.e("params: ", "POST_URL: " + url);
        } else {
            url = GET_URL + "?t" + "=" + test +
                    "&s" + "=" + sub_code +
                    "&q" + "=" + question;
            Log.e("params: ", "GET_URL: " + url);
        }
        return url;
    }

    private static synchronized void SpeakAnswer(String text) {
        String[] s_arr = text.split(" ");
        String words = "";
        for (int idx_space = 1; idx_space <= s_arr.length; idx_space++) {
            words += s_arr[idx_space - 1] + " ";
            if (idx_space % 5 == 0 || idx_space == s_arr.length) {
                for (int i = 0; i < 3; ) {
                    if (!Speech.getInstance().isSpeaking() & Utils.isBluetoothConnected) {
                        Speech.getInstance().setTextToSpeechRate(SPEECHRATE).say(words, new TextToSpeechCallback() {
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public boolean onCompleted() {
                                Utils.sleep(1);

                                return false;
                            }

                            @Override
                            public void onError() {
                            }
                        });
                        i += 1;
                    }
                }
                Utils.sleep(2);
                words = "";
            }
        }
        gc();
    }

    private static void SpeechToText() {
        sub_code = editText.getText().toString().toUpperCase();
        mIslistening = false;
        if (sub_code.length() > 3) {
            SpeakPromptly("r" + (n_que_answered + 1));
            Utils.sleep(1);
            Utils.test = false;
            getSpeechInput();
        } else {
            for (int i = 0; i < 3; i++) {
                SpeakPromptly("Not a valid subject code " + sub_code);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.context = MainActivity.this;
        timePickerFragment = new TimePickerFragment();
        preferencesHandler = new PreferencesHandler();
        Speech.init(this, getPackageName(), mTttsInitListener);

        n_que_layout = findViewById(R.id.n_que_layout);
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        textViewCountDown = findViewById(R.id.textViewCountDown);
        linearLayout = findViewById(R.id.linearLayout);
        topTextView = findViewById(R.id.topTextView);
        textToSpeech = findViewById(R.id.textToSpeech);
        progress = findViewById(R.id.progress);

        mic_btn = findViewById(R.id.mic_btn);
        mic_btn.setOnClickListener(view -> {
            Utils.test = true;
            Utils.getSpeechInput();
        });

        Button speak = findViewById(R.id.speak);
        speak.setOnClickListener(view -> onSpeakClick());

        int[] colors = {
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        };
        progress.setColors(colors);

        Button btn_schedule = findViewById(R.id.btn_schedule);
        btn_schedule.setOnClickListener(v -> {
            if (timePickerFragment.timerRunning) {
                timePickerFragment.resetTimer();
            }
            DialogFragment timePicker = new TimePickerFragment();  // calling a different package TimePickerFragment.java
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        ////////////////////// Timer
        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> timePickerFragment.resetTimer());
        timePickerFragment.updateCountDownText(timePickerFragment.beginTimerMillis);

        Button sub_code_btn = findViewById(R.id.sub_code_btn);
        sub_code_btn.setOnClickListener(view -> {
            String url = createUrl(1, "KCE051", "As people have said, calling the Toast initiation within onResponse() works.");
            if (API_POST) {
                sendPostRequest(url);
            } else {
                sendGetRequest(url);
            }
            String[] que_ans = preferencesHandler.getQueAnsFromPreferences("question" + 1, "answer" + 1);
            textView.setText(que_ans[1]);
            startAnsweringTheQuestions();
        });

        //// Bluetooth controls
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.RECOGNIZERINTENTREQUESTCODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Utils.SpeechRecognizerInput.add(result.get(0).toUpperCase());
                mIslistening = false;
                Log.e("onActivityResult", String.valueOf(result));
                if (Utils.test) {
                    if (Utils.SpeechRecognizerInput.get((Utils.SpeechRecognizerInput.size() - 1)).equals("")) {
                        SpeakPromptly("Not able to hear you.");
                    }
                    Utils.SpeechRecognizerInput.set((Utils.SpeechRecognizerInput.size() - 1), "");
                }
            }
        }
    }


    private void onSetSpeechToTextLanguage() {
        Speech.getInstance().getSupportedSpeechToTextLanguages(new SupportedLanguagesListener() {
            @Override
            public void onSupportedLanguages(List<String> supportedLanguages) {
                CharSequence[] items = new CharSequence[supportedLanguages.size()];
                supportedLanguages.toArray(items);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Current language: " + Speech.getInstance().getSpeechToTextLanguage())
                        .setItems(items, (dialogInterface, i) -> {
                            Locale locale = Locale.forLanguageTag(supportedLanguages.get(i));
                            Speech.getInstance().setLocale(locale);
                            Toast.makeText(MainActivity.this, "Selected: " + items[i], Toast.LENGTH_LONG).show();
                        })
                        .setPositiveButton("Cancel", null)
                        .create()
                        .show();
            }

            @Override
            public void onNotSupported(UnsupportedReason reason) {
                switch (reason) {
                    case GOOGLE_APP_NOT_FOUND:
                        showSpeechNotSupportedDialog();
                        break;

                    case EMPTY_SUPPORTED_LANGUAGES:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.set_stt_langs)
                                .setMessage(R.string.no_langs)
                                .setPositiveButton("OK", null)
                                .show();
                        break;
                }
            }
        });
    }

    private void onSetTextToSpeechVoice() {
        List<Voice> supportedVoices = Speech.getInstance().getSupportedTextToSpeechVoices();

        if (supportedVoices.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.set_tts_voices)
                    .setMessage(R.string.no_tts_voices)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        CharSequence[] items = new CharSequence[supportedVoices.size()];
        Iterator<Voice> iterator = supportedVoices.iterator();
        int i = 0;

        while (iterator.hasNext()) {
            Voice voice = iterator.next();

            items[i] = voice.toString();
            i++;
        }

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Current: " + Speech.getInstance().getTextToSpeechVoice())
                .setItems(items, (dialogInterface, i1) -> {
                    Speech.getInstance().setVoice(supportedVoices.get(i1));
                    Toast.makeText(MainActivity.this, "Selected: " + items[i1], Toast.LENGTH_LONG).show();
                })
                .setPositiveButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != Utils.RECORD_AUDIO_PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecordAudioPermissionGranted();
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onSpeakClick() {
        String txt = textToSpeech.getText().toString().trim();
        if (txt.isEmpty()) {
            Toast.makeText(this, R.string.input_something, Toast.LENGTH_LONG).show();
            SpeakAnswer("One way of doing this without changing Volley's source code is to check for the response data in the VolleyError and parse it your self..");
            return;
        }
        SpeakAnswer(txt);
    }

    @Override
    public void onStartOfSpeech() {
        mIslistening = true;
    }

    @Override
    public void onSpeechRmsChanged(float value) {
        //Log.d(getClass().getSimpleName(), "Speech recognition rms is now " + value +  "dB");
    }

    @Override
    public void onSpeechResult(String result) {
        mic_btn.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
        if (!result.isEmpty()) {
            Utils.SpeechRecognizerInput.add(result);
            mIslistening = false;
            Log.e("onSpeechResult", result);
            if (Utils.test) {
                if (Utils.SpeechRecognizerInput.get((Utils.SpeechRecognizerInput.size() - 1)).equals("")) {
                    SpeakPromptly("Not able to hear you.");
                }
                Utils.SpeechRecognizerInput.set((Utils.SpeechRecognizerInput.size() - 1), "");
            }
            String url = createUrl(0, sub_code, result);
            if (API_POST) {
                sendPostRequest(url);
            } else {
                sendGetRequest(url);
            }
        }
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        topTextView.setText("");
        for (String partial : results) {
            topTextView.append(partial + " ");
        }
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", timePickerFragment.timeLeftInMillis);
        outState.putBoolean("timerRunning", timePickerFragment.timerRunning);
        outState.putLong("endTime", timePickerFragment.endTimeInMillis);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timePickerFragment.timeLeftInMillis = savedInstanceState.getLong("millisLeft");
        timePickerFragment.timerRunning = savedInstanceState.getBoolean("timerRunning");
        timePickerFragment.updateCountDownText(timePickerFragment.timeLeftInMillis);
        if (timePickerFragment.timerRunning) {
            timePickerFragment.endTimeInMillis = savedInstanceState.getLong("endTime");
            timePickerFragment.timeLeftInMillis = timePickerFragment.endTimeInMillis - System.currentTimeMillis();
            startTimer(timePickerFragment.timeLeftInMillis);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Quanification Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.supportedSTTLanguages:
                onSetSpeechToTextLanguage();
                return true;

            case R.id.supportedTTSLanguages:
                onSetTextToSpeechVoice();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class TimePickerFragment extends DialogFragment {
        long beginTimerMillis;
        long timeLeftInMillis;
        long endTimeInMillis;
        boolean timerRunning;
        CountDownTimer countDownTimer;
        long beginMilliSec;
        int idx_ms = 0;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), (view, hourOfDay, minute1) -> { //onTimeSet
                Calendar calender = Calendar.getInstance();
                calender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calender.set(Calendar.MINUTE, minute1);
                calender.set(Calendar.SECOND, 0);
                if (calender.before(Calendar.getInstance())) {
                    calender.add(Calendar.DATE, 1);
                }

                updateTimeText(calender);
                beginTimerMillis = calender.getTimeInMillis() - System.currentTimeMillis();
                startTimer(beginTimerMillis);
                updateCountDownText(beginTimerMillis);
            }, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @SuppressLint("SetTextI18n")
        private synchronized void updateTimeText(Calendar calender) {
            textView.setText("Quanification set for: " + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(calender.getTime()));
        }

        @SuppressLint("DefaultLocale")
        public synchronized void updateCountDownText(long timeLeftInMillis) {
            textViewCountDown.setText(String.format("%02d:%02d:%02d", (timeLeftInMillis / 3600000) % 24, (timeLeftInMillis / 60000) % 60, (timeLeftInMillis / 1000) % 60));
        }

        @SuppressLint("SetTextI18n")
        public synchronized void resetTimer() {
            if (countDownTimer != null) {  // stopping timer and rest processes
                beginMilliSec = 0;
                countDownTimer.cancel();
                updateCountDownText(0);
            }
            idx_ms = 0;
            textView.setText("Quanification cancelled.");
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        private synchronized void nextExecution() {
            if (countDownTimer != null) {
                countDownTimer.cancel(); // first cancel previously running countdown timer
            }
            if (idx_ms <= TIMER_MILLIs.length) {
                Utils.sleep(1);
                startTimer(TIMER_MILLIs[idx_ms]);
                updateCountDownText(TIMER_MILLIs[idx_ms]);
                idx_ms += 1;
            }
        }
    }
}
