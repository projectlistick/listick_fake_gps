package project.listick.fakegps.UI;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import project.listick.fakegps.FakeGPSApplication;
import project.listick.fakegps.Interfaces.EditTextDialogImpl;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.R;

/*
 * Created by LittleAngry on 02.01.19 (macOS 10.12)
 * */
public class TimePickerDialog {

    public interface TimePickerImpl {
        void onTimePicked(int minutes, int seconds);
    }

    private Dialog mDialog;
    private Context mContext;
    private TimePickerImpl mListener;

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

        title.setText(dialogTitle);
        description.setText(dialogDescription);


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

                int minutes = 0;
                int seconds = 0;
                if (!(s_minutes.isEmpty() || s_seconds.isEmpty())) {
                    minutes = Integer.parseInt(s_minutes);
                    seconds = Integer.parseInt(s_seconds);
                }

                if (seconds > 60 || minutes > 60) {
                    PrettyToast.error(mContext, R.string.enter_valid_time, Toast.LENGTH_LONG);
                    return;
                }

                hideKeyboard();
                mListener.onTimePicked(minutes, seconds);
                mDialog.cancel();
            }
        });

        cancelAction.setOnClickListener(view -> {
            hideKeyboard();
            mDialog.cancel();
        });

        mDialog.show();
    }

    private void hideKeyboard() {
        View view = mDialog.getCurrentFocus();
        if (view != null) {
            FakeGPSApplication.getInputMethodManager().hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
