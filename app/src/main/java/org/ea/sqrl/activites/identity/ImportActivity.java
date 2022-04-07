package org.ea.sqrl.activites.identity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.ea.sqrl.utils.QRCodeDecodeHelper;

import org.ea.sqrl.R;
import org.ea.sqrl.activites.MainActivity;
import org.ea.sqrl.activites.base.BaseActivity;
import org.ea.sqrl.processors.SQRLStorage;
import org.ea.sqrl.utils.SqrlApplication;
import org.ea.sqrl.utils.Utils;

import java.util.Arrays;

public class ImportActivity extends BaseActivity {
    private static final String TAG = "ImportActivity";
    private static final int PICK_FILE_REQUEST_CODE = 1;
    public static final String EXTRA_IMPORT_METHOD = "import_method";
    public static final String IMPORT_METHOD_QR_CODE = "qr_code";
    public static final String IMPORT_METHOD_FORWARDED_QR_CODE = "forwarded_qr_code";
    public static final String IMPORT_METHOD_FILE = "file";
    public static final String EXTRA_FORWARDED_QR_CODE = "forwarded_qr_code";

    private boolean firstIdentity = false;
    private ConstraintLayout rootView = null;
    private TextView txtImportMessage = null;
    private Button btnImportIdentityDo = null;
    private Button btnForgotPassword = null;
    private EditText txtPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        rootView = findViewById(R.id.importActivityView);
        txtImportMessage = findViewById(R.id.txtImportMessage);
        btnImportIdentityDo = findViewById(R.id.btnImportIdentityDo);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        txtPassword = findViewById(R.id.txtPassword);

        txtPassword.setEnabled(true);
        btnImportIdentityDo.setEnabled(true);
        btnForgotPassword.setEnabled(true);

        setupProgressPopupWindow(getLayoutInflater());
        setupErrorPopupWindow(getLayoutInflater());

        txtPassword.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    doImport();
                    return true;
                default:
                    return false;
            }
        });

        btnForgotPassword.setOnClickListener(
                v -> {
                    ImportActivity.this.finish();
                    Intent resetPasswordIntent = new Intent(this, ResetPasswordActivity.class);
                    resetPasswordIntent.putExtra(SQRLStorage.NEW_IDENTITY, true);
                    startActivity(resetPasswordIntent);
                }
        );

        btnImportIdentityDo.setOnClickListener(v -> doImport());

        boolean testing = getIntent().getBooleanExtra("RUNNING_TEST", false);
        if(testing) {
            return;
        }

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getType() != null &&
                intent.getAction().equals(Intent.ACTION_VIEW) &&
                (intent.getType().startsWith("text/") || intent.getType().startsWith("application/"))) {
            handleFileIntent(intent.getData());
            return;
        }

        String importMethod = intent.getStringExtra(EXTRA_IMPORT_METHOD);
        if (importMethod == null) return;

        if (importMethod.equals(IMPORT_METHOD_FILE)) {
            chooseFile();
            return;
        }

        if (importMethod.equals(IMPORT_METHOD_QR_CODE)) {
            final IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setOrientationLocked(false);
            integrator.setBarcodeImageEnabled(false);

            integrator.setPrompt(this.getString(R.string.scan_identity));
            integrator.initiateScan();
        }

        if (importMethod.equals(IMPORT_METHOD_FORWARDED_QR_CODE)) {
            byte[] identityData = intent.getByteArrayExtra(EXTRA_FORWARDED_QR_CODE);
            if (identityData == null) return;
            try {
                readIdentityData(identityData);
            } catch (Exception e) {
                showErrorMessage(e.getMessage());
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST_CODE) {
            if (data == null) {
                this.finish();
                return;
            }

            handleFileIntent(data.getData());
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
                Snackbar.make(rootView, R.string.scan_cancel, Snackbar.LENGTH_LONG).show();
                ImportActivity.this.finish();
            } else {
                try {
                    byte[] qrCodeData = QRCodeDecodeHelper.decode(result.getRawBytes());
                    if(qrCodeData.length == 0) {
                        showErrorMessage(R.string.scan_incorrect);
                        return;
                    }

                    readIdentityData(qrCodeData);

                } catch (Exception e) {
                    if(e.getMessage().equals("Incorrect header")) {
                        showErrorMessage(R.string.scan_incorrect);
                    } else {
                        showErrorMessage(e.getMessage());
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void doImport() {
        showProgressPopup();

        new Thread(() -> {
            SQRLStorage storage = SQRLStorage.getInstance(ImportActivity.this.getApplicationContext());
            try {
                boolean decryptStatus = storage.decryptIdentityKey(txtPassword.getText().toString(), entropyHarvester, false);
                if(!decryptStatus) {
                    handler.post(() -> {
                        showErrorMessage(R.string.decrypt_identity_fail);
                        txtPassword.setText("");
                        hideProgressPopup();
                    });
                    storage.clearQuickPass();
                    storage.clear();
                    return;
                }
                storage.clearQuickPass();

                boolean encryptStatus = storage.encryptIdentityKey(txtPassword.getText().toString(), entropyHarvester);
                if (!encryptStatus) {
                    handler.post(() -> {
                        showErrorMessage(R.string.encrypt_identity_fail);
                        txtPassword.setText("");
                        hideProgressPopup();
                    });
                    return;
                }
                storage.clear();
            } catch (Exception e) {
                showErrorMessage(e.getMessage());
                Log.e(TAG, e.getMessage(), e);
            }

            if(!mDbHelper.hasIdentities()) {
                firstIdentity = true;
            }

            long newIdentityId = mDbHelper.newIdentity(
                    ImportActivity.this, storage.createSaveData());

            SqrlApplication.saveCurrentId(this.getApplication(), newIdentityId);

            handler.post(() -> {
                txtPassword.setText("");
                hideProgressPopup();

                if(newIdentityId != 0) {
                    if(firstIdentity) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        startActivity(new Intent(this, RenameActivity.class));
                    }
                    ImportActivity.this.finish();
                }
            });
        }).start();
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, getResources().getString(R.string.import_identity_file)),
                    PICK_FILE_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException ex) {
            showErrorMessage(R.string.install_file_manager_app);
        }
    }

    private void handleFileIntent(Uri contentUri) {
        int headerLength = SQRLStorage.STORAGE_HEADER.length();
        byte[] identityData = Utils.getFileIntentContent(this, contentUri);

        if (identityData == null || identityData.length < headerLength ||
                !(new String(Arrays.copyOfRange(identityData, 0, headerLength)).toLowerCase())
                        .equals(SQRLStorage.STORAGE_HEADER)) {
            handleFileIntentError();
            return;
        }

        try {
            readIdentityData(identityData);
        } catch (Exception e) {
            handleFileIntentError();
        }
    }

    private void handleFileIntentError() {
        Log.d(TAG, "Invalid file provided by file intent.");
        txtImportMessage.setText(R.string.invalid_sqrl_file);
        txtPassword.setEnabled(false);
        btnImportIdentityDo.setEnabled(false);
        btnForgotPassword.setEnabled(false);
    }

    private void readIdentityData(byte[] identityData) throws Exception {
        SQRLStorage storage = SQRLStorage.getInstance(ImportActivity.this.getApplicationContext());

        storage.read(identityData);

        if(!storage.hasEncryptedKeys()) {
            handler.postDelayed(() -> {
                Intent setPasswordIntent = new Intent(this, ResetPasswordActivity.class);
                setPasswordIntent.putExtra(SQRLStorage.NEW_IDENTITY, true);
                startActivity(setPasswordIntent);
            }, 100);
            ImportActivity.this.finish();
            return;
        }

        String recoveryBlock = storage.getVerifyingRecoveryBlock();

        handler.postDelayed(() -> {
            final TextView txtRecoveryKey = findViewById(R.id.txtRecoveryKey);
            txtRecoveryKey.setText(recoveryBlock);
            txtRecoveryKey.setMovementMethod(LinkMovementMethod.getInstance());
        }, 100);
    }
}