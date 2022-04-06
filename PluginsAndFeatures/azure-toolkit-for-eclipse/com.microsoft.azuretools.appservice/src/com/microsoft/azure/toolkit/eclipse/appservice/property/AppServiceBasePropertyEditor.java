/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.appservice.property;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azuretools.core.components.AzureListenerWrapper;
import com.microsoft.azuretools.core.mvp.ui.webapp.WebAppProperty;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppBasePropertyMvpView;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppPropertyViewPresenter;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;

public abstract class AppServiceBasePropertyEditor extends EditorPart implements WebAppBasePropertyMvpView {

    private static final String INSIGHT_NAME = "AzurePlugin.Eclipse.Editor.WebAppPropertyEditor";
    private static final int PROGRESS_BAR_HEIGHT = 3;
    private static final String TXT_NA = "N/A";
    private static final String TXT_LOADING = "<Loading...>";
    private static final String FILE_SELECTOR_TITLE = "Choose Where You Want to Save the Publish Profile.";
    private static final String NOTIFY_PROFILE_GET_SUCCESS = "Publish Profile saved.";
    private static final String NOTIFY_PROFILE_GET_FAIL = "Failed to get Publish Profile.";
    private static final String NOTIFY_PROPERTY_UPDATE_SUCCESS = "Properties updated.";

    private final Map<String, String> cachedAppSettings;
    private final Map<String, String> editedAppSettings;
    private final WebAppBasePropertyViewPresenter presenter;
    private Text txtResourceGroup;
    private Text txtAppServicePlan;
    private Text txtStatus;
    private Text txtLocation;
    private Text txtSubscription;
    private Text txtPricingTier;
    private Text txtJavaVersion;
    private Text txtContainer;
    private Table tblAppSettings;
    private String subscriptionId;
    private String webAppId;
    private String slotName;
    private Link lnkUrl;
    private Label lblContainer;
    private Label lblJavaVersion;
    private Button btnSave;
    private Button btnGetPublishProfile;
    private Button btnDiscard;
    private Composite cpOverview;
    private Composite cpExtraInfo;
    private ProgressBar progressBar;
    private TableEditor editor;
    private Button btnNewItem;
    private Button btnDeleteItem;
    private Button btnEditItem;

    /**
     * Constructor.
     */
    public AppServiceBasePropertyEditor(WebAppBasePropertyViewPresenter presenter) {
        this.presenter = presenter;
        this.presenter.onAttachView(this);

        cachedAppSettings = new LinkedHashMap<>();
        editedAppSettings = new LinkedHashMap<>();
    }

    @Override
    public void createPartControl(Composite parent) {

        GridLayout glCpRoot = new GridLayout(1, false);
        glCpRoot.marginWidth = 0;
        glCpRoot.verticalSpacing = 0;
        glCpRoot.horizontalSpacing = 0;
        glCpRoot.marginHeight = 0;
        Composite cpRoot = new Composite(parent, SWT.NONE);
        cpRoot.setLayout(glCpRoot);

        progressBar = new ProgressBar(cpRoot, SWT.INDETERMINATE);
        GridData gdProgressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdProgressBar.heightHint = PROGRESS_BAR_HEIGHT;
        progressBar.setLayoutData(gdProgressBar);

        ScrolledComposite scrolledComposite = new ScrolledComposite(cpRoot, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        Composite area = new Composite(scrolledComposite, SWT.NONE);
        GridLayout glArea = new GridLayout(1, false);
        area.setLayout(glArea);

        Composite composite = new Composite(area, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        GridLayout glComposite = new GridLayout(1, false);
        composite.setLayout(glComposite);

        Composite cpControlButtons = new Composite(composite, SWT.NONE);
        cpControlButtons.setLayout(new GridLayout(3, false));
        cpControlButtons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        btnGetPublishProfile = new Button(cpControlButtons, SWT.NONE);
        btnGetPublishProfile.setText("Get Publish Profile");
        btnGetPublishProfile.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages
            .IMG_ETOOL_PRINT_EDIT));
        btnGetPublishProfile
            .addListener(SWT.Selection, new AzureListenerWrapper(INSIGHT_NAME, "btnGetPublishProfile", null) {
                @Override
                protected void handleEventFunc(Event event) {
                    onBtnGetPublishProfileSelection();
                }
            });

        btnSave = new Button(cpControlButtons, SWT.NONE);
        btnSave.setText("Save");
        btnSave.setEnabled(false);
        btnSave.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT));
        btnSave.addListener(SWT.Selection, new AzureListenerWrapper(INSIGHT_NAME, "btnSave", null) {
            @Override
            protected void handleEventFunc(Event event) {
                setBtnEnableStatus(false);
                presenter
                    .onUpdateWebAppProperty(subscriptionId, webAppId, slotName, cachedAppSettings, editedAppSettings);
            }
        });

        btnDiscard = new Button(cpControlButtons, SWT.NONE);
        btnDiscard.setText("Discard");
        btnDiscard.setEnabled(false);
        btnDiscard.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
        btnDiscard.addListener(SWT.Selection, new AzureListenerWrapper(INSIGHT_NAME, "btnDiscard", null) {
            @Override
            protected void handleEventFunc(Event event) {
                updateMapStatus(editedAppSettings, cachedAppSettings);
                resetTblAppSettings(editedAppSettings);
            }
        });

        cpOverview = new Composite(composite, SWT.NONE);
        cpOverview.setLayout(new GridLayout(4, false));
        cpOverview.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblResourceGroup = new Label(cpOverview, SWT.NONE);
        lblResourceGroup.setText("Resource Group:");

        txtResourceGroup = new Text(cpOverview, SWT.NONE);
        txtResourceGroup.setEditable(false);
        txtResourceGroup.setText(TXT_LOADING);
        txtResourceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblAppServicePlan = new Label(cpOverview, SWT.NONE);
        lblAppServicePlan.setText("App Service Plan:");

        txtAppServicePlan = new Text(cpOverview, SWT.NONE);
        txtAppServicePlan.setEditable(false);
        txtAppServicePlan.setText(TXT_LOADING);
        txtAppServicePlan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblStatus = new Label(cpOverview, SWT.NONE);
        lblStatus.setText("Status:");

        txtStatus = new Text(cpOverview, SWT.NONE);
        txtStatus.setEditable(false);
        txtStatus.setText(TXT_LOADING);
        txtStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblUrl = new Label(cpOverview, SWT.NONE);
        lblUrl.setText("URL:");

        lnkUrl = new Link(cpOverview, SWT.NONE);
        lnkUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lnkUrl.setText(TXT_LOADING);
        // click to open browser
        lnkUrl.addListener(SWT.Selection, event -> Program.launch(event.text));

        Label lblLocation = new Label(cpOverview, SWT.NONE);
        lblLocation.setText("Location:");

        txtLocation = new Text(cpOverview, SWT.NONE);
        txtLocation.setEditable(false);
        txtLocation.setText(TXT_LOADING);
        txtLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPricingTier = new Label(cpOverview, SWT.NONE);
        lblPricingTier.setText("Pricing Tier:");

        txtPricingTier = new Text(cpOverview, SWT.NONE);
        txtPricingTier.setEditable(false);
        txtPricingTier.setText(TXT_LOADING);
        txtPricingTier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblSubscription = new Label(cpOverview, SWT.NONE);
        lblSubscription.setText("Subscription ID:");

        txtSubscription = new Text(cpOverview, SWT.NONE);
        txtSubscription.setEditable(false);
        txtSubscription.setText(TXT_LOADING);
        txtSubscription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

        cpExtraInfo = new Composite(composite, SWT.NONE);
        cpExtraInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        cpExtraInfo.setLayout(new GridLayout(2, false));

        lblJavaVersion = new Label(cpExtraInfo, SWT.NONE);
        lblJavaVersion.setText("Java Version:");

        txtJavaVersion = new Text(cpExtraInfo, SWT.NONE);
        txtJavaVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtJavaVersion.setEditable(false);
        txtJavaVersion.setText(TXT_LOADING);

        lblContainer = new Label(cpExtraInfo, SWT.NONE);
        lblContainer.setText("Web Container:");

        txtContainer = new Text(cpExtraInfo, SWT.NONE);
        txtContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtContainer.setEditable(false);
        txtContainer.setText(TXT_LOADING);

        Composite cpAppSettings = new Composite(composite, SWT.NONE);
        cpAppSettings.setLayout(new GridLayout(2, false));
        cpAppSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        tblAppSettings = new Table(cpAppSettings, SWT.BORDER | SWT.FULL_SELECTION);
        tblAppSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tblAppSettings.setHeaderVisible(true);
        tblAppSettings.setLinesVisible(true);
        tblAppSettings.addListener(SWT.MouseDoubleClick, event -> onTblAppSettingMouseDoubleClick(event));

        editor = new TableEditor(tblAppSettings);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;

        TableColumn tblclmnKey = new TableColumn(tblAppSettings, SWT.NONE);
        tblclmnKey.setWidth(300);
        tblclmnKey.setText("Key");

        TableColumn tblclmnValue = new TableColumn(tblAppSettings, SWT.NONE);
        tblclmnValue.setWidth(300);
        tblclmnValue.setText("Value");

        Composite cpTableButtons = new Composite(cpAppSettings, SWT.NONE);
        cpTableButtons.setLayout(new GridLayout(1, false));
        cpTableButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

        btnNewItem = new Button(cpTableButtons, SWT.NONE);
        btnNewItem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnNewItem.setText("New");
        btnNewItem.setToolTipText("New");
        btnNewItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
        btnNewItem.setEnabled(false);
        btnNewItem.addListener(SWT.Selection, event -> onBtnNewItemSelection());

        btnDeleteItem = new Button(cpTableButtons, SWT.NONE);
        btnDeleteItem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnDeleteItem.setText("Delete");
        btnDeleteItem.setToolTipText("Delete");
        btnDeleteItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
        btnDeleteItem.setEnabled(false);
        btnDeleteItem.addListener(SWT.Selection, event -> onBtnDeleteItemSelection());

        btnEditItem = new Button(cpTableButtons, SWT.NONE);
        btnEditItem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnEditItem.setText("Edit");
        btnEditItem.setToolTipText("Edit");
        btnEditItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_CLEAR));
        btnEditItem.setEnabled(false);
        btnEditItem.addListener(SWT.Selection, event -> onBtnEditItemSelection());

        scrolledComposite.setContent(area);
        scrolledComposite.setMinSize(area.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        setExtraInfoVisible(false);
        setChildrenTransparent(cpOverview);
        setChildrenTransparent(cpExtraInfo);

        btnGetPublishProfile.setFocus();
    }

    private void onBtnEditItemSelection() {
        int seletedIndex = tblAppSettings.getSelectionIndex();
        if (seletedIndex >= 0 && seletedIndex < tblAppSettings.getItemCount()) {
            updateTableActionBtnStatus(false);
            editingTableItem(tblAppSettings.getItem(seletedIndex), 1);
        }
    }

    private void onBtnDeleteItemSelection() {
        int seletedIndex = tblAppSettings.getSelectionIndex();
        if (seletedIndex >= 0 && seletedIndex < tblAppSettings.getItemCount()) {
            updateTableActionBtnStatus(false);
            tblAppSettings.remove(seletedIndex);
            updateTableActionBtnStatus(true);
            readTblAppSettings();
            updateSaveAndDiscardBtnStatus();
        }
        tblAppSettings.setFocus();
    }

    private void onBtnNewItemSelection() {
        updateTableActionBtnStatus(false);
        TableItem item = new TableItem(tblAppSettings, SWT.NONE);
        item.setText(new String[]{"<key>", "<value>"});
        tblAppSettings.setSelection(item);
        editingTableItem(item, 0);
    }

    private void setBtnEnableStatus(boolean enabled) {
        progressBar.setVisible(!enabled);
        btnGetPublishProfile.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnDiscard.setEnabled(enabled);
        btnNewItem.setEnabled(enabled);
        btnEditItem.setEnabled(enabled);
        btnDeleteItem.setEnabled(enabled);
        tblAppSettings.setEnabled(enabled);
        if (enabled) {
            updateSaveAndDiscardBtnStatus();
        }
    }

    private void onTblAppSettingMouseDoubleClick(Event event) {
        updateTableActionBtnStatus(false);
        Rectangle clientArea = tblAppSettings.getClientArea();
        Point pt = new Point(event.x, event.y);
        int index = tblAppSettings.getTopIndex();
        while (index < tblAppSettings.getItemCount()) {
            boolean visible = false;
            final TableItem item = tblAppSettings.getItem(index);
            for (int i = 0; i < tblAppSettings.getColumnCount(); i++) {
                Rectangle rect = item.getBounds(i);
                if (rect.contains(pt)) {
                    editingTableItem(item, i);
                    return;
                }
                if (!visible && rect.intersects(clientArea)) {
                    visible = true;
                }
            }
            if (!visible) {
                updateTableActionBtnStatus(true);
                return;
            }
            index++;
        }
        updateTableActionBtnStatus(true);
    }

    private void editingTableItem(TableItem item, int column) {
        final Text text = new Text(tblAppSettings, SWT.NONE);
        Listener textListener = e -> {
            switch (e.type) {
                case SWT.FocusOut:
                    item.setText(column, text.getText());
                    text.dispose();
                    readTblAppSettings();
                    updateSaveAndDiscardBtnStatus();
                    updateTableActionBtnStatus(true);
                    break;
                case SWT.Traverse:
                    switch (e.detail) {
                        case SWT.TRAVERSE_RETURN:
                            item.setText(column, text.getText());
                            // FALL THROUGH
                        case SWT.TRAVERSE_ESCAPE:
                            text.dispose();
                            e.doit = false;
                            readTblAppSettings();
                            updateSaveAndDiscardBtnStatus();
                            updateTableActionBtnStatus(true);
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        };
        text.addListener(SWT.FocusOut, textListener);
        text.addListener(SWT.Traverse, textListener);
        editor.setEditor(text, item, column);
        text.setText(item.getText(column));
        text.selectAll();
        text.setFocus();
    }

    private void updateTableActionBtnStatus(boolean enabled) {
        btnNewItem.setEnabled(enabled);
        btnDeleteItem.setEnabled(enabled);
        btnDiscard.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnEditItem.setEnabled(enabled);
    }

    private void onBtnGetPublishProfileSelection() {
        DirectoryDialog dirDialog = new DirectoryDialog(btnGetPublishProfile.getShell(), SWT.SAVE);
        dirDialog.setMessage(FILE_SELECTOR_TITLE);
        String firstPath = dirDialog.open();
        if (firstPath != null) {
            setBtnEnableStatus(false);
            presenter
                .onGetPublishingProfileXmlWithSecrets(subscriptionId, webAppId, slotName, firstPath);
        }
    }

    @Override
    public void onErrorWithException(String message, Exception ex) {
        this.presenter.getMvpView().onErrorWithException(message, ex);
    }

    @Override
    public void dispose() {
        this.presenter.onDetachView();
        super.dispose();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        this.setPartName(input.getName());
        if (input instanceof AppServicePropertyEditorInput) {
            AppServicePropertyEditorInput appServiceInput = (AppServicePropertyEditorInput) input;
            this.setPartName(appServiceInput.getName());
            this.subscriptionId = appServiceInput.getSubscriptionId();
            this.webAppId = appServiceInput.getAppServiceId();
            this.slotName = appServiceInput.getSlotName();
            this.presenter.onLoadWebAppProperty(subscriptionId, webAppId, slotName);
        }

        IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
        workbench.addWorkbenchListener(new IWorkbenchListener() {
            @Override
            public boolean preShutdown(IWorkbench workbench, boolean forced) {
                activePage.closeEditor(AppServiceBasePropertyEditor.this, true);
                return true;
            }

            @Override
            public void postShutdown(IWorkbench workbench) {
            }
        });
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void doSave(IProgressMonitor arg0) {
    }

    @Override
    public void doSaveAs() {
    }

    private void setChildrenTransparent(Composite container) {
        if (container == null) {
            return;
        }
        Color transparentColor = container.getBackground();
        for (Control control : container.getChildren()) {
            if (control instanceof Text) {
                control.setBackground(transparentColor);
            }
        }
    }

    @Override
    public void onLoadWebAppProperty(String sid, String webAppId, String slotName) {
        progressBar.setVisible(true);
        this.presenter.onLoadWebAppProperty(sid, webAppId, slotName);
    }

    @Override
    public void showProperty(WebAppProperty webAppProperty) {
        txtResourceGroup.setText(webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_RESOURCE_GRP) == null ? TXT_NA
            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_RESOURCE_GRP));
        txtStatus.setText(webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_STATUS) == null ? TXT_NA
            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_STATUS));
        txtLocation.setText(webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_LOCATION) == null ? TXT_NA
            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_LOCATION));
        txtSubscription.setText(webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_SUB_ID) == null ? TXT_NA
            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_SUB_ID));
        txtAppServicePlan.setText(webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_PLAN) == null ? TXT_NA
            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_PLAN));
        Object url = webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_URL);
        if (url == null) {
            lnkUrl.setText(TXT_NA);
        } else {
            lnkUrl.setText(String.format("<a>https://%s</a>", url));
        }
        txtPricingTier.setText(webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_PRICING) == null ? TXT_NA
            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_PRICING));
        Object os = webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_OPERATING_SYS);
        if (os != null && os instanceof OperatingSystem) {
            switch ((OperatingSystem) os) {
                case WINDOWS:
                case LINUX:
                    txtJavaVersion.setText(
                        webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_JAVA_VERSION) == null ? TXT_NA
                            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_JAVA_VERSION));
                    txtContainer.setText(
                        webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_JAVA_CONTAINER) == null ? TXT_NA
                            : (String) webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_JAVA_CONTAINER));
                    setExtraInfoVisible(true);
                    break;
                default:
                    setExtraInfoVisible(false);
                    break;
            }
        }

        // fill table of AppSettings
        cachedAppSettings.clear();
        Object appSettingsObj = webAppProperty.getValue(WebAppPropertyViewPresenter.KEY_APP_SETTING);
        if (appSettingsObj != null && appSettingsObj instanceof Map) {
            Map<String, String> appSettings = (Map<String, String>) appSettingsObj;
            for (String key : appSettings.keySet()) {
                cachedAppSettings.put(key, appSettings.get(key));
            }
        }
        updateMapStatus(editedAppSettings, cachedAppSettings);
        resetTblAppSettings(cachedAppSettings);
        progressBar.setVisible(false);
        cpOverview.getParent().layout();
    }

    private void resetTblAppSettings(Map<String, String> map) {
        tblAppSettings.removeAll();
        for (String key : map.keySet()) {
            TableItem item = new TableItem(tblAppSettings, SWT.NONE);
            item.setText(new String[]{key, map.get(key)});
        }
        updateTableActionBtnStatus(true);
        updateSaveAndDiscardBtnStatus();
    }

    private void readTblAppSettings() {
        editedAppSettings.clear();
        int row = 0;
        while (row < tblAppSettings.getItemCount()) {
            TableItem item = tblAppSettings.getItem(row);
            String key = item.getText(0);
            String value = item.getText(1);
            if (key.isEmpty() || editedAppSettings.containsKey(key)) {
                tblAppSettings.remove(row);
                continue;
            }
            editedAppSettings.put(key, value);
            ++row;
        }
    }

    private void setExtraInfoVisible(boolean b) {
        cpExtraInfo.setVisible(b);
        ((GridData) cpExtraInfo.getLayoutData()).exclude = !b;
        cpExtraInfo.getParent().layout();
    }

    private void updateMapStatus(Map<String, String> to, Map<String, String> from) {
        to.clear();
        to.putAll(from);
        updateSaveAndDiscardBtnStatus();
    }

    private void updateSaveAndDiscardBtnStatus() {
        if (mapEquals(editedAppSettings, cachedAppSettings)) {
            btnDiscard.setEnabled(false);
            btnSave.setEnabled(false);
        } else {
            btnDiscard.setEnabled(true);
            btnSave.setEnabled(true);
        }
    }

    private boolean mapEquals(Map<String, String> arg1, Map<String, String> arg2) {
        if (arg1 == arg2) {
            return true;
        }
        if (arg1 == null || arg2 == null) {
            return false;
        }
        return arg1.equals(arg2);
    }

    @Override
    public void showPropertyUpdateResult(boolean isSuccess) {
        setBtnEnableStatus(true);
        if (isSuccess) {
            updateMapStatus(cachedAppSettings, editedAppSettings);
            MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Success",
                NOTIFY_PROPERTY_UPDATE_SUCCESS);
        }
    }

    @Override
    public void showGetPublishingProfileResult(boolean isSuccess) {
        setBtnEnableStatus(true);
        if (isSuccess) {
            MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Success",
                NOTIFY_PROFILE_GET_SUCCESS);
        } else {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Failure",
                NOTIFY_PROFILE_GET_FAIL);
        }
    }

}
