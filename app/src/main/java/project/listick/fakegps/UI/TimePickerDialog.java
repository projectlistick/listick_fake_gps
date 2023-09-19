package project.listick.fakegps.UI;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import project.listick.fakegps.FakeGPSApplication;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.R;

/*
 * Created by LittleAngry on 02.01.19 (macOS 10.12)
 * */
public class TimePickerDialog {

    public interface TimePickerImpl {
        void onTimePicked(int minutes, int seconds, String format);
    }

    private Dialog mDialog;
    private final Context mContext;
    private final TimePickerImpl mListener;

    public TimePickerDialog(Context context, TimePickerImpl listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void show(String dialogTitle, String dialogDescription) {
        mDialog = new Dialog(mContext);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.timepicker_dialog);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = mDialog.findViewById(R.id.title);
        TextView description = mDialog.findViewById(R.id.text_dialog);

        EditText et_minutes = mDialog.findViewById(R.id.minutes);
        EditText et_seconds = mDialog.findViewById(R.id.seconds);

        Button okButton = mDialog.findViewById(R.id.btn_action);
        Button cancelAction = mDialog.findViewById(R.id.cancel);

        title.setText(dialogTitle != null ? dialogTitle : "");
        description.setText(dialogDescription != null ? dialogDescription : "");


        et_minutes.setOnFocusChangeListener((v, hasFocus) -> {
            v.setOnFocusChangeListener(null);
            ((EditText) v).setText("");
        });
        et_seconds.setOnFocusChangeListener((v, hasFocus) -> {
            v.setOnFocusChangeListener(null);
            ((EditText) v).setText("");
        });
        okButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                String s_minutes = et_minutes.getText().toString();
                String s_seconds = et_seconds.getText().toString();

                if (s_minutes.isEmpty())
                    s_minutes = "0";
                else if (s_seconds.isEmpty())
                    s_seconds = "0";

                int minutes = 0;
                int seconds = 0;
                if (!(s_minutes.isEmpty() || s_seconds.isEmpty())) {
                    minutes = Integer.parseInt(s_minutes);
                    seconds = Integer.parseInt(s_seconds);
                }

                if (seconds > 60 || minutes > 60) {
                    //PrettyToast.error(mContext, R.string.enter_valid_time);
                    return;
                }

                hideKeyboard();


                String format = String.format(Locale.ENGLISH, "%d:%d", minutes, seconds);
                if (minutes < 9)
                    format = String.format(Locale.ENGLISH, "0%d:%d", minutes, seconds);

                if (seconds < 9)
                    format = String.format(Locale.ENGLISH, "%d:0%d", minutes, seconds);

                if (minutes < 9 && seconds < 9)
                    format = String.format(Locale.ENGLISH, "0%d:0%d", minutes, seconds);


                mListener.onTimePicked(minutes, seconds, format);
                mDialog.cancel();
            }
        });

        cancelAction.setOnClickListener(view -> {
            hideKeyboard();
            mDialog.cancel();
        });

        mDialog.show();
    }
    public void show() {
        show(null, null);
    }

    private void hideKeyboard() {
        View view = mDialog.getCurrentFocus();
        if (view != null) {
            FakeGPSApplication.getInputMethodManager().hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
