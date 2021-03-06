package com.fiuba.tallerii.lincedin.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.DatePicker;

import com.fiuba.tallerii.lincedin.R;
import com.fiuba.tallerii.lincedin.events.DatePickedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerDialogFragment extends DialogFragment {

    private static final String TAG = DatePickerDialogFragment.class.getName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_date_picker_dialog, null))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        DatePicker datePicker = (DatePicker) d.findViewById(R.id.dialog_date_picker);
                        EventBus.getDefault().post(
                                new DatePickedEvent(
                                        datePicker.getDayOfMonth(),
                                        datePicker.getMonth() + 1,  // January = 0
                                        datePicker.getYear()
                                )
                        );
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            }
        });
        return dialog;
    }


}
