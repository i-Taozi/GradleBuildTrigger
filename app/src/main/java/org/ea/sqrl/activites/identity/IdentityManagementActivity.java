package org.ea.sqrl.activites.identity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.ea.sqrl.R;
import org.ea.sqrl.activites.base.BaseActivity;
import org.ea.sqrl.activites.create.CreateIdentityActivity;

import org.ea.sqrl.utils.IdentitySelector;
import org.ea.sqrl.utils.SqrlApplication;
import org.ea.sqrl.utils.Utils;

import java.util.Objects;

/**
 * This activity is the central hub for all identity management functionality.
 *
 * @author Daniel Persson
 */
public class IdentityManagementActivity extends BaseActivity {
    private static final String TAG = "IdentityManagementActivity";

    private IdentitySelector mIdentitySelector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_management);

        setupErrorPopupWindow(getLayoutInflater());

        final Button btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(v -> startActivity(new Intent(this, CreateIdentityActivity.class)));

        final Button btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(
                v -> startActivity(new Intent(this, ImportOptionsActivity.class))
        );

        mIdentitySelector = new IdentitySelector(this, true, true, false);
        mIdentitySelector.registerLayout(findViewById(R.id.identitySelector));
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean runningTest = getIntent().getBooleanExtra("RUNNING_TEST", false);
        if(runningTest) return;

        final ImageView imgIdentityManagement = findViewById(R.id.imgIdentityManagement);
        Utils.hideViewIfDisplayHeightSmallerThan(this, imgIdentityManagement, 600);

        if(!mDbHelper.hasIdentities()) {
            IdentityManagementActivity.this.finish();
        } else {
            // Here we are reloading the SQRL Identity that is indicated by the current contextual
            // ID just in case a user has cancelled the identity creation process. We plan to
            // refactor this at some point in the future with something more elegant.
            long id = SqrlApplication.getCurrentId(this.getApplication());
            SqrlApplication.setCurrentId(this.getApplicationContext(), id);
            mIdentitySelector.update();
        }
    }
}