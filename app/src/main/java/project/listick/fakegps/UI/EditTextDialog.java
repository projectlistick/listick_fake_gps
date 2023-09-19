package project.listick.fakegps.UI;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import project.listick.fakegps.Interfaces.EditTextDialogImpl;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.R;

/*
 * Created by LittleAngry on 02.01.19 (macOS 10.12)
 * */
public class EditTextDialog  {

    private final Context context;
    private final EditTextDialogImpl editTextDialogImpl;

    public EditTextDialog(Context context, EditTextDialogImpl editTextDialog){
        this.context = context;
        this.editTextDialogImpl = editTextDialog;
    }

    public void show(String dialogTitle, String dialogDescription) {
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.write_text_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = dialog.findViewById(R.id.title);
        TextView description = dialog.findViewById(R.id.text_dialog);
        final EditText editText = dialog.findViewById(R.id.text_info);
        Button okButton = dialog.findViewById(R.id.btn_action);
        Button cancelButton = dialog.findViewById(R.id.cancel_action);

        title.setText(dialogTitle);
        description.setText(dialogDescription);

        cancelButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dialog.cancel();
            }
        });

        okButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (editText.getText() != null || editText.toString().equals("")) {
                    editTextDialogImpl.getDialogResult(editText.getText().toString());
                    dialog.cancel();
                }
            }
        });

        dialog.show();
    }

}
