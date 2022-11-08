package com.example.mc_project_2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

public class CustomDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        String dialog_msg = getArguments().getString("dialog_msg");
        String dialog_title = getArguments().getString("dialog_title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(dialog_msg)
                .setTitle(dialog_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("check1", "working properly");
                    }
             })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //close the app
                        getActivity().finishAndRemoveTask();
                    }
                });
    return builder.create();
    }

}
