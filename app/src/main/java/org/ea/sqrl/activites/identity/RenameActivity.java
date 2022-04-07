package org.ea.sqrl.activites.identity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.ea.sqrl.R;
import org.ea.sqrl.activites.MainActivity;
import org.ea.sqrl.activites.base.BaseActivity;
import org.ea.sqrl.utils.SqrlApplication;

public class RenameActivity extends BaseActivity {
    private EditText txtIdentityName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename);

        txtIdentityName = findViewById(R.id.txtIdentityName);

        final long currentId = SqrlApplication.getCurrentId(this.getApplication());

        if(currentId != 0) {
            txtIdentityName.setText(mDbHelper.getIdentityName(currentId));
            txtIdentityName.setSelectAllOnFocus(true);
            txtIdentityName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        txtIdentityName.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    doRename();
                    return true;
                default:
                    return false;
            }
        });

        findViewById(R.id.btnRename).setOnClickListener(v -> doRename());
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();

        if (isTaskRoot()) {
            startActivity(getNextActivity());
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void doRename() {
        final long currentId = SqrlApplication.getCurrentId(this.getApplication());

        if(currentId != 0) {
            mDbHelper.updateIdentityName(RenameActivity.this, currentId,
                    txtIdentityName.getText().toString());
        }

        hideKeyboard();
        RenameActivity.this.finishAffinity();
        startActivity(getNextActivity());
    }

    private Intent getNextActivity() {
        Intent nextActivity = new Intent(this, MainActivity.class);

        if (getIntent().hasExtra(SqrlApplication.EXTRA_NEXT_ACTIVITY)) {
            try {
                String activityClassName = getIntent().getStringExtra(SqrlApplication.EXTRA_NEXT_ACTIVITY);
                nextActivity = new Intent(this, Class.forName(activityClassName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nextActivity;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtIdentityName.getWindowToken(), 0);
    }
}
