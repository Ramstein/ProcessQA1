//package net.gotev.speechdemo;
//
//import android.annotation.SuppressLint;
//import android.app.Dialog;
//import android.app.TimePickerDialog;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.text.format.DateFormat;
//import android.widget.TextView;
//import android.widget.TimePicker;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.DialogFragment;
//
//import java.util.Calendar;
//
//
//public class TimePickerFragment extends DialogFragment {
//    TextView textViewCountDown;
//    TextView textView;
//    MainActivity mainActivity;
//    long beginTimerMillis;
//    long timeLeftInMillis;
//    long endTimeInMillis;
//    boolean timerRunning;
//    CountDownTimer countDownTimer;
//    long beginMilliSec;
//    int idx_ms = 0;
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Calendar c = Calendar.getInstance();
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        int minute = c.get(Calendar.MINUTE);
//        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
//            @Override
//            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                mainActivity = new MainActivity();
//
//                Calendar calender = Calendar.getInstance();
//                calender.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                calender.set(Calendar.MINUTE, minute);
//                calender.set(Calendar.SECOND, 0);
//                if (calender.before(Calendar.getInstance())) {
//                    calender.add(Calendar.DATE, 1);
//                }
//
//                updateTimeText(calender);
//                beginTimerMillis = calender.getTimeInMillis() - System.currentTimeMillis();
//                mainActivity.startTimer(beginTimerMillis);
//                updateCountDownText(beginTimerMillis);
//            }
//        }, hour, minute, DateFormat.is24HourFormat(getActivity()));
//    }
//
//    @SuppressLint("SetTextI18n")
//    private synchronized void updateTimeText(Calendar calender) {
//        textView.setText("Lambda set for: " + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(calender.getTime()));
//    }
//
//    @SuppressLint("DefaultLocale")
//    public synchronized void updateCountDownText(long timeLeftInMillis) {
//        textViewCountDown.setText(String.format("%02d:%02d:%02d", (timeLeftInMillis / 3600000) % 24, (timeLeftInMillis / 60000) % 60, (timeLeftInMillis / 1000) % 60));
//    }
//
//    @SuppressLint("SetTextI18n")
//    public synchronized void resetTimer() {
//        if (countDownTimer != null) {  // stopping timer and rest processes
//            beginMilliSec = 0;
//            countDownTimer.cancel();
//            updateCountDownText(0);
//        }
//        idx_ms = 0;
//        textView.setText("Quanification cancelled.");
//    }
//}
