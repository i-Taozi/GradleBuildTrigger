package org.ea.sqrl.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.zxing.FormatException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.ea.sqrl.utils.QRCodeDecodeHelper;

import org.ea.sqrl.R;
import org.ea.sqrl.activites.base.LoginBaseActivity;
import org.ea.sqrl.activites.identity.ImportActivity;
import org.ea.sqrl.processors.CommunicationFlowHandler;
import org.ea.sqrl.processors.SQRLStorage;
import org.ea.sqrl.utils.IdentitySelector;
import org.ea.sqrl.utils.SqrlApplication;
import org.ea.sqrl.utils.Utils;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Daniel Persson
 */
public class MainActivity extends LoginBaseActivity {
    private static final String TAG = "MainActivity";

    public static final String ACTION_QUICK_SCAN = "org.ea.sqrl.activites.QUICK_SCAN";

    private IdentitySelector mIdentitySelector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);

        rootView = findViewById(R.id.mainActivityView);
        communicationFlowHandler = CommunicationFlowHandler.getInstance(this, handler);

        setupErrorPopupWindow(getLayoutInflater());
        setupBasePopups(getLayoutInflater());

        mIdentitySelector = new IdentitySelector(this, true,false, true);
        mIdentitySelector.registerLayout(findViewById(R.id.identitySelector));

        findViewById(R.id.btnUseIdentity).setOnClickListener(v -> initiateScan());
        findViewById(R.id.txtScanQrCode).setOnClickListener(v -> initiateScan());

        if (ACTION_QUICK_SCAN.equals(getIntent().getAction())) {
            handler.postDelayed(() -> initiateScan(), 100L);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean runningTest = getIntent().getBooleanExtra("RUNNING_TEST", false);
        if(runningTest) return;

        if(!mDbHelper.hasIdentities()) {
            startActivity(new Intent(this, WizardPage1Activity.class));
        } else {
            long currentId = SqrlApplication.getCurrentId(this.getApplication());
            if(currentId != 0) {
                SqrlApplication.setCurrentId(this, currentId);
                mIdentitySelector.update();
            }

            setupBasePopups(getLayoutInflater());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
                Snackbar.make(rootView, R.string.scan_cancel, Snackbar.LENGTH_LONG).show();
                if(!mDbHelper.hasIdentities()) {
                    startActivity(new Intent(this, WizardPage1Activity.class));
                }
            } else {
                byte[] qrCodeData = null;

                try {
                    qrCodeData = QRCodeDecodeHelper.decode(result.getRawBytes());
                } catch (FormatException fe) {
                    Snackbar.make(rootView, R.string.scan_incorrect, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (qrCodeData == null) {
                    Snackbar.make(rootView, R.string.scan_incorrect, Snackbar.LENGTH_LONG).show();
                    return;
                }

                // If an identity qr-code was scanned instead of a login qr code,
                // simply forward it to the import activity and bail out

                if (qrCodeData.length > 8 && new String(Arrays.copyOfRange(qrCodeData, 0, 8))
                        .startsWith(SQRLStorage.STORAGE_HEADER)) {

                    Intent importIntent = new Intent(this, ImportActivity.class);
                    importIntent.putExtra(ImportActivity.EXTRA_IMPORT_METHOD, ImportActivity.IMPORT_METHOD_FORWARDED_QR_CODE);
                    importIntent.putExtra(ImportActivity.EXTRA_FORWARDED_QR_CODE, qrCodeData);
                    startActivity(importIntent);
                    return;
                }

                final String serverData = new String(qrCodeData);
                Uri serverUri = Uri.parse(serverData);

                if (!Utils.isValidSqrlUri(serverUri)) {
                    Snackbar.make(rootView, R.string.scan_incorrect, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent urlLoginIntent = new Intent(Intent.ACTION_VIEW);
                urlLoginIntent.setData(serverUri);
                urlLoginIntent.putExtra(LoginActivity.EXTRA_USE_CPS, false);
                if (ACTION_QUICK_SCAN.equals(getIntent().getAction())) urlLoginIntent.putExtra(LoginActivity.EXTRA_QUICK_SCAN, true);
                startActivity(urlLoginIntent);
            }
        }
    }

    private void initiateScan() {
        final IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setPrompt(this.getString(R.string.scan_site_code));
        integrator.initiateScan();
    }
}
