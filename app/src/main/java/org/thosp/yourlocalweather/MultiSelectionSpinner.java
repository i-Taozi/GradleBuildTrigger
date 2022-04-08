package org.thosp.yourlocalweather;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import org.thosp.yourlocalweather.model.VoiceSettingParametersDbHelper;
import org.thosp.yourlocalweather.utils.VoiceSettingParamType;

import java.util.ArrayList;
import java.util.Arrays;

import static org.thosp.yourlocalweather.utils.LogToFile.appendLog;


public class MultiSelectionSpinner extends AppCompatSpinner implements DialogInterface.OnMultiChoiceClickListener {

    private static final String TAG = "MultiSelectionSpinner";

    ArrayList<MultiselectionItem> items = null;

    boolean[] selection = null;

    ArrayAdapter adapter;
    Long voiceSettingId;

    public MultiSelectionSpinner(Context context) {
        super(context);
        adapter = new ArrayAdapter(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        adapter = new ArrayAdapter(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (selection != null && which < selection.length) {
            selection[which] = isChecked;
            adapter.clear();
            adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
        writeCurrentSetting();
    }

    private void writeCurrentSetting() {
        StringBuilder selectedBtDevices = new StringBuilder();
        for (int i = 0; i < selection.length; i++) {
            if (selection[i]) {
                selectedBtDevices.append(items.get(i).getAddress());
                selectedBtDevices.append(",");
            }
        }
        String selectedBtDevicesString = selectedBtDevices.toString();
        appendLog(getContext(), TAG, "writeCurrentSetting: voiceSettingId=", voiceSettingId, ", selectedBtDevicesString=", selectedBtDevicesString);
        VoiceSettingParametersDbHelper voiceSettingParametersDbHelper = VoiceSettingParametersDbHelper.getInstance(getContext());
        voiceSettingParametersDbHelper.saveStringParam(
                voiceSettingId,
                VoiceSettingParamType.VOICE_SETTING_ENABLED_WHEN_BT_DEVICES.getVoiceSettingParamTypeId(),
                selectedBtDevicesString);
        appendLog(getContext(), TAG, "writeCurrentSetting saved");
    }

    @Override
    public boolean performClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String[] itemNames = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemNames[i] = items.get(i).getName();
        }
        builder.setMultiChoiceItems(itemNames, selection, this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // Do nothing
            }
        });

        builder.show();

        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {

        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(ArrayList<MultiselectionItem> items) {
        this.items = items;
        selection = new boolean[this.items.size()];
        adapter.clear();
        adapter.add("");
        Arrays.fill(selection, false);
    }

    public void setSelection(ArrayList<MultiselectionItem> selection) {
        for (int i = 0; i < this.selection.length; i++) {
            this.selection[i] = false;
        }

        for (MultiselectionItem sel : selection) {
            for (int j = 0; j < items.size(); ++j) {
                if (items.get(j).getValue().equals(sel.getValue())) {
                    this.selection[j] = true;
                }
            }
        }

        adapter.clear();
        adapter.add(buildSelectedItemString());
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(items.get(i).getName());
            }
        }
        return sb.toString();
    }

    public ArrayList<MultiselectionItem> getSelectedItems() {
        ArrayList<MultiselectionItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                selectedItems.add(items.get(i));
            }
        }
        return selectedItems;
    }

    public void setVoiceSettingId(Long voiceSettingId) {
        this.voiceSettingId = voiceSettingId;
    }
}
