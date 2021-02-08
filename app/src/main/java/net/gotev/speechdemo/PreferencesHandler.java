package net.gotev.speechdemo;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class PreferencesHandler extends AppCompatActivity {
    private static final String app_name = "processqa";
    SharedPreferences q_pref;

    public boolean putQueAnsInPreferences(int q_no, String question, String answer) {
        if (q_pref == null) {
            q_pref = Utils.context.getSharedPreferences(app_name, 0);
        }
        SharedPreferences.Editor q_pref_editor = q_pref.edit();
        q_pref_editor.putString("question" + q_no, q_no + " " + question);
        q_pref_editor.putString("answer" + q_no, answer);
        q_pref_editor.apply();
        return true;
    }

    public String[] getQueAnsFromPreferences(String question_id, String answer_id) {
        if (q_pref == null) {
            q_pref = Utils.context.getSharedPreferences(app_name, 0);
        }
        String question = q_pref.getString(question_id, "");
        String answer = q_pref.getString(answer_id, "");
        return new String[]{question, answer};
    }

    public void removeQueAnsFromPreferences(String question_id, String answer_id) {
        if (q_pref == null) {
            q_pref = Utils.context.getSharedPreferences(app_name, 0);
        }
        SharedPreferences.Editor q_pref_editor = q_pref.edit();
        q_pref_editor.remove(question_id);
        q_pref_editor.remove(answer_id);
        q_pref_editor.apply();
    }


}
