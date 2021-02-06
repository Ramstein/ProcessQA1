package net.gotev.speechdemo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.SupportedLanguagesListener;
import net.gotev.speech.TextToSpeechCallback;
import net.gotev.speech.UnsupportedReason;
import net.gotev.speech.ui.SpeechProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static java.lang.System.gc;

public class MainActivity extends AppCompatActivity implements SpeechDelegate {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String get_url = "https://0yh5imhg3m.execute-api.ap-south-1.amazonaws.com/prod";
    private static final int RECORD_AUDIO_PERMISSIONS_REQUEST = 1;

    static TextView textViewCountDown;
    static TextView textView;
    static int n_que_index = 0;
    static boolean[] isSpeaking = {false};
    private static TimePickerFragment timePickerFragment;
    private static EditText editText;
    private static EditText n_que_layout;
    private static String sub_code = "";
    private static ImageButton button;
    private static SpeechProgressView progress;
    private static LinearLayout linearLayout;
    private static Integer n_que = 20; //10 + 3 + 5 + more 2 extra // you can set the number
    private static List<String> SpeechRecognizerInput = new ArrayList<>();
    private static Integer n_que_answered = 0;
    private static Integer n_que_answer_spoken = 0;
    private static boolean test;
    private static boolean mIslistening = false;
    private static PreferencesHandler preferencesHandler;
    private static Utils utils;
    private Button sub_code_btn;
    private Button speak;
    private TextView text;
    private EditText textToSpeech;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private TextToSpeech.OnInitListener mTttsInitListener = status -> {
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

    public static void onSpeechToTextExec() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            if (utils.requestAudioPermission()) {
                Log.e("STT", "doing STT for ProcessQA.");
                onRecordAudioPermissionGranted();
            }
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//                Log.e("STT", "doing STT for ProcessQA.");
//                onRecordAudioPermissionGranted();
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSIONS_REQUEST);
//            }
        }
    }

    private static void onRecordAudioPermissionGranted() {
        button.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

        try {
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(progress, (SpeechDelegate) utils.context);

//        } catch (SpeechRecognitionNotAvailable exc) {
//            showSpeechNotSupportedDialog();
//
//        } catch (GoogleVoiceTypingDisabledException exc) {
//            showEnableGoogleVoiceTyping();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    SpeechUtil.redirectUserToGoogleAppOnPlayStore(utils.context);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = utils.alertDialogBuilder();
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private static void showEnableGoogleVoiceTyping() {
        AlertDialog.Builder builder = utils.alertDialogBuilder();
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
                onSpeechToTextQuestion();
                processQuestionToAnswer();

                Speech.getInstance().say("Answering the questions now", new TextToSpeechCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onCompleted() {
                        startAnsweringTheQuestions();
                    }

                    @Override
                    public void onError() {
                    }
                });
            }
        }.start();
        timePickerFragment.timerRunning = true;
    }

    private static void onSpeechToTextQuestion() {
        sub_code = editText.getText().toString().toUpperCase();
        if (sub_code.length() > 3) {
            for (n_que_index = 0; n_que_index < n_que; ) {
                if (!mIslistening) {
                    Log.e("STT", "");
                    test = false;
                    onSpeechToTextExec();
                }
//                ThreadSleep.sleep(10);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                SpeakPromptly("Not a valid subject code " + sub_code);
            }
        }
    }

    private static void processQuestionToAnswer() {
        for (int i = 0; i < SpeechRecognizerInput.size(); i++) {
            String speech = SpeechRecognizerInput.get(i);

            if (speech.equals("")) {
                SpeakPromptly("Not a question.");
            } else {
                String url = createUrl(get_url, 0, sub_code, speech);
                sendGetRequest(url);
                String[] que_ans = preferencesHandler.getQueAnsFromPreferences("question" + (n_que_answered + 1), "answer" + (n_que_answered + 1));
                if (que_ans[1].equals("")) {
                    SpeakPromptly((n_que_answered + 1) + " Answer not found.");
                    n_que_answered += 1;
                }
            }
        }
    }

    private static void startAnsweringTheQuestions() {
        for (int i = 0; i < n_que_answered; i++) {
            String[] que_ans = preferencesHandler.getQueAnsFromPreferences("question" + (i + 1), "answer" + (i + 1));
            if (!que_ans[0].equals("") & !que_ans[1].equals("")) {
                SpeakAnswer(que_ans[0] + " Answering now " + que_ans[1]);
                preferencesHandler.removeQueAnsFromPreferences("question" + (i + 1), "answer" + (i + 1));
            } else {
                SpeakPromptly("Negative " + (i + 1));
            }
            n_que_answer_spoken += 1;
        }
    }

    private static synchronized void sendGetRequest(String url) {
        RequestQueue queue = utils.Queue();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.e("sendGetRequest", "Volley Request succeed:" + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                preferencesHandler.putQueAnsInPreferences((n_que_answered + 1), jsonObject.getString("question"), jsonObject.getString("answer"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("sendGetRequest", "answer Request failed:" + error.getMessage());
            ThreadSleep.sleep(1);
            textView.setText(("answer Request failed: " + error.getMessage()));
        });
        queue.add(stringRequest);
    }

    ///////////////////////////  api request
    @SuppressLint({"WrongConstant", "ShowToast"})
    private static synchronized String createUrl(String url, Integer test, String sub_code, String question) {
        url = url + "?test" + "=" + test +
                "&sub_code" + "=" + sub_code +
                "&question" + "=" + question;
        Log.e("createURL", url);
        Logger.error(LOG_TAG, url);
        return url;
    }

    //#########################  TTS speaking the en language
    private static synchronized void SpeakPromptly(String text) {
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

    private static synchronized void SpeakAnswer(String text) {
        String[] s_arr = text.split(" ");
        String words = "";
        for (int idx_space = 1; idx_space <= s_arr.length; idx_space++) {
            words += s_arr[idx_space - 1] + " ";
            if (idx_space % 5 == 0 || idx_space == s_arr.length) {
                for (int i = 0; i < 3; ) {
                    if (!isSpeaking[0]) {
                        i += 1;
                        Speech.getInstance().setTextToSpeechRate((float) 0.8).say(words, new TextToSpeechCallback() {
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
                }
                ThreadSleep.sleep(2);
                words = "";
            }
        }
        gc();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils();
        utils.context = MainActivity.this;
        timePickerFragment = new TimePickerFragment();
        preferencesHandler = new PreferencesHandler();
        Speech.init(this, getPackageName(), mTttsInitListener);


        linearLayout = findViewById(R.id.linearLayout);

        button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            test = true;
            onSpeechToTextExec();
        });

        speak = findViewById(R.id.speak);
        speak.setOnClickListener(view -> onSpeakClick());

        text = findViewById(R.id.text);
        textToSpeech = findViewById(R.id.textToSpeech);
        progress = findViewById(R.id.progress);

        int[] colors = {
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        };
        progress.setColors(colors);

        n_que_layout = findViewById(R.id.n_que);
        editText = findViewById(R.id.editText);
        sub_code_btn = findViewById(R.id.sub_code_btn);
        textView = findViewById(R.id.textView);
        textViewCountDown = findViewById(R.id.textViewCountDown);


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


        sub_code_btn.setOnClickListener(view -> {
            String url = createUrl(get_url, 1, "KCE051", "As people have said, calling the Toast initiation within onResponse() works.");
            sendGetRequest(url);
            String[] que_ans = preferencesHandler.getQueAnsFromPreferences("question" + 1, "answer" + 1);
            editText.setText(que_ans[1]);
            startAnsweringTheQuestions();
        });
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

    private void onSetSpeechToTextLanguage() {
        Speech.getInstance().getSupportedSpeechToTextLanguages(new SupportedLanguagesListener() {
            @Override
            public void onSupportedLanguages(List<String> supportedLanguages) {
                CharSequence[] items = new CharSequence[supportedLanguages.size()];
                supportedLanguages.toArray(items);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Current language: " + Speech.getInstance().getSpeechToTextLanguage())
                        .setItems(items, (dialogInterface, i) -> {
                            Locale locale;

                            locale = Locale.forLanguageTag(supportedLanguages.get(i));

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
        if (requestCode != RECORD_AUDIO_PERMISSIONS_REQUEST) {
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
        button.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);

        if (result.isEmpty()) {
            Speech.getInstance().say(getString(R.string.repeat));
        } else {
            if (test) {
                text.setText(result);
            } else if (!test) {
                SpeechRecognizerInput.add(result);
                n_que_index += 1;
            }
        }
        mIslistening = false;
    }

    @Override
    public void onSpeechPartialResults(List<String> results) {
        text.setText("");
        for (String partial : results) {
            text.append(partial + " ");
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
            return new TimePickerDialog(getActivity(), (view, hourOfDay, minute1) -> {
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
            textView.setText("Lambda set for: " + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(calender.getTime()));
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
    }

}
