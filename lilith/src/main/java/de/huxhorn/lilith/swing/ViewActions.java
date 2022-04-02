/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2020 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.huxhorn.lilith.swing;

import de.huxhorn.lilith.appender.InternalLilithAppender;
import de.huxhorn.lilith.conditions.CallLocationCondition;
import de.huxhorn.lilith.data.access.AccessEvent;
import de.huxhorn.lilith.data.eventsource.EventWrapper;
import de.huxhorn.lilith.data.eventsource.LoggerContext;
import de.huxhorn.lilith.data.eventsource.SourceIdentifier;
import de.huxhorn.lilith.data.logging.ExtendedStackTraceElement;
import de.huxhorn.lilith.data.logging.LoggingEvent;
import de.huxhorn.lilith.engine.EventSource;
import de.huxhorn.lilith.services.clipboard.AccessRequestHeadersFormatter;
import de.huxhorn.lilith.services.clipboard.AccessRequestParametersFormatter;
import de.huxhorn.lilith.services.clipboard.AccessRequestUriFormatter;
import de.huxhorn.lilith.services.clipboard.AccessRequestUrlFormatter;
import de.huxhorn.lilith.services.clipboard.AccessResponseHeadersFormatter;
import de.huxhorn.lilith.services.clipboard.ClipboardFormatter;
import de.huxhorn.lilith.services.clipboard.ClipboardFormatterData;
import de.huxhorn.lilith.services.clipboard.EventHtmlFormatter;
import de.huxhorn.lilith.services.clipboard.GroovyFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingCallLocationFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingCallStackFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingEventJsonFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingEventXmlFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingLoggerNameFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingMarkerFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingMdcFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingMessageFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingMessagePatternFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingNdcFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingThreadGroupNameFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingThreadNameFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingThrowableFormatter;
import de.huxhorn.lilith.services.clipboard.LoggingThrowableNameFormatter;
import de.huxhorn.lilith.swing.actions.ActionTooltips;
import de.huxhorn.lilith.swing.menu.ExcludeMenu;
import de.huxhorn.lilith.swing.menu.FocusMenu;
import de.huxhorn.lilith.swing.table.EventWrapperViewTable;
import de.huxhorn.sulky.buffers.Buffer;
import de.huxhorn.sulky.conditions.Condition;
import de.huxhorn.sulky.swing.KeyStrokes;
import de.huxhorn.sulky.swing.PersistentTableColumnModel;
import java.awt.AWTError;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class needs cleanup...... remove duplicated logic, make ToolBar/Menu configurable...
 */
public final class ViewActions
{
	private final Logger logger = LoggerFactory.getLogger(ViewActions.class);

	/**
	 * Taken over from Action.SELECTED_KEY for 1.5 compatibility.
	 * Does not work with 1.5 :( I was really sure that there was some selected event...
	 */
	//private static final String SELECTED_KEY = "SwingSelectedKey";

	private static final char ALT_SYMBOL = '\u2325';
	private static final char COMMAND_SYMBOL = '\u2318';

	static
	{
		final Logger logger = LoggerFactory.getLogger(ViewActions.class);

		JMenuItem item = new JMenuItem();
		Font font = item.getFont();
		if(logger.isDebugEnabled()) logger.debug("Can display '{}': {}", ALT_SYMBOL, font.canDisplay(ALT_SYMBOL));
		if(logger.isDebugEnabled()) logger.debug("Can display '{}': {}", COMMAND_SYMBOL, font.canDisplay(COMMAND_SYMBOL));

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new EggListener());
	}

	private final JToolBar toolBar;
	private final JMenuBar menuBar;

	private final MainFrame mainFrame;
	private final ApplicationPreferences applicationPreferences;
	private final JToggleButton tailButton;

	private final ExportMenuAction exportMenuAction;
	private final AttachAction attachToolBarAction;
	private final AttachAction attachMenuAction;
	private final DisconnectAction disconnectToolBarAction;
	private final DisconnectAction disconnectMenuAction;
	private final FindPreviousAction findPreviousToolBarAction;
	private final FindPreviousAction findPreviousMenuAction;
	private final FindNextAction findNextToolBarAction;
	private final FindNextAction findNextMenuAction;
	private final FindPreviousActiveAction findPreviousActiveAction;
	private final FindPreviousActiveAction findPreviousActiveToolBarAction;
	private final FindNextActiveAction findNextActiveAction;
	private final FindNextActiveAction findNextActiveToolBarAction;
	private final ResetFindAction resetFindAction;
	private final TailAction tailMenuAction;
	private final EditSourceNameMenuAction editSourceNameMenuAction;
	private final SaveLayoutAction saveLayoutAction;
	private final ResetLayoutAction resetLayoutAction;
	private final SaveConditionMenuAction saveConditionMenuAction;

	private final ZoomInMenuAction zoomInMenuAction;
	private final ZoomOutMenuAction zoomOutMenuAction;
	private final ResetZoomMenuAction resetZoomMenuAction;

	private final NextViewAction nextViewAction;
	private final PreviousViewAction previousViewAction;
	private final CloseFilterAction closeFilterAction;
	private final CloseOtherFiltersAction closeOtherFiltersAction;
	private final CloseAllFiltersAction closeAllFiltersAction;

	private final RemoveInactiveAction removeInactiveAction;
	private final CloseAllAction closeAllAction;
	private final CloseOtherAction closeOtherAction;
	private final MinimizeAllAction minimizeAllAction;
	private final MinimizeAllOtherAction minimizeAllOtherAction;

	private final JMenuItem removeInactiveItem;

	private final JMenu windowMenu;
	private final FindAction findMenuAction;
	private final JMenu searchMenu;
	private final JMenu viewMenu;
	private final JMenu columnsMenu;
	private final ClearAction clearMenuAction;
	private final FocusMessageAction focusMessageAction;
	private final FocusEventsAction focusEventsAction;
	private final ChangeListener containerChangeListener;
	private final TailAction tailToolBarAction;
	private final ClearAction clearToolBarAction;
	private final FindAction findToolBarAction;
	private final CopySelectionAction copySelectionAction;
	private final CopyToClipboardAction copyEventAction;
	private final ShowUnfilteredEventAction showUnfilteredEventAction;
	private final JPopupMenu popup;
	private final GoToSourceAction goToSourceAction;
	private final FocusMenu focusMenu;
	private final ExcludeMenu excludeMenu;
	private final FocusMenu focusPopupMenu;
	private final ExcludeMenu excludePopupMenu;
	private final JMenu filterPopupMenu;
	private final JMenu copyPopupMenu;
	private final PropertyChangeListener containerPropertyChangeListener;
	private final JMenuItem showTaskManagerItem;
	private final JMenuItem closeAllItem;
	private final JMenuItem minimizeAllItem;
	private final JMenuItem closeAllOtherItem;
	private final JMenuItem minimizeAllOtherItem;
	private final JMenu recentFilesMenu;
	private final ClearRecentFilesAction clearRecentFilesAction;
	private final JMenu customCopyMenu;
	private final JMenu customCopyPopupMenu;
	private final List<CopyToClipboardAction> copyLoggingActions;
	private final List<CopyToClipboardAction> copyAccessActions;
	private final Map<KeyStroke, CopyToClipboardAction> keyStrokeActionMapping;

	private ViewContainer viewContainer;
	private EventWrapper eventWrapper;
	private Map<String, CopyToClipboardAction> groovyClipboardActions;
	private Map<String, ClipboardFormatterData> groovyClipboardData;

	public ViewActions(MainFrame mainFrame, ViewContainer viewContainer)
	{
		this.mainFrame = Objects.requireNonNull(mainFrame, "mainFrame must not be null!");
		this.applicationPreferences = mainFrame.getApplicationPreferences();
		// usingScreenMenuBar is used to determine whether HTML tooltips in menu are supported or not
		// swing supports HTML tooltip, native macOS menu bar isn't.
		final boolean usingScreenMenuBar = applicationPreferences.isUsingScreenMenuBar();

		containerChangeListener = e -> updateActions();

		containerPropertyChangeListener = evt -> {
			if(ViewContainer.SELECTED_EVENT_PROPERTY_NAME.equals(evt.getPropertyName()))
			{
				setEventWrapper((EventWrapper) evt.getNewValue());
			}

		};

		keyStrokeActionMapping = new HashMap<>();
		// ##### Menu Actions #####
		// File
		OpenMenuAction openMenuAction = new OpenMenuAction();
		clearRecentFilesAction=new ClearRecentFilesAction();
		OpenInactiveLogMenuAction openInactiveLogMenuAction = new OpenInactiveLogMenuAction();
		ImportMenuAction importMenuAction = new ImportMenuAction();
		exportMenuAction = new ExportMenuAction();
		CleanAllInactiveLogsMenuAction cleanAllInactiveLogsMenuAction = new CleanAllInactiveLogsMenuAction();
		PreferencesAction preferencesMenuAction = new PreferencesAction(false);
		ExitMenuAction exitMenuAction = new ExitMenuAction();

		// Edit
		showUnfilteredEventAction = new ShowUnfilteredEventAction();
		goToSourceAction = new GoToSourceAction();
		copySelectionAction = new CopySelectionAction();
		copyEventAction = new CopyToClipboardAction(new EventHtmlFormatter(mainFrame));
		copyLoggingActions = new ArrayList<>();
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingEventJsonFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingEventXmlFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingMessageFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingMessagePatternFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingLoggerNameFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingThrowableFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingThrowableNameFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingCallLocationFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingCallStackFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingThreadNameFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingThreadGroupNameFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingMarkerFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingMdcFormatter()));
		copyLoggingActions.add(new CopyToClipboardAction(new LoggingNdcFormatter()));
		copyAccessActions = new ArrayList<>();
		copyAccessActions.add(new CopyToClipboardAction(new AccessRequestUriFormatter()));
		copyAccessActions.add(new CopyToClipboardAction(new AccessRequestUrlFormatter()));
		copyAccessActions.add(new CopyToClipboardAction(new AccessRequestHeadersFormatter()));
		copyAccessActions.add(new CopyToClipboardAction(new AccessRequestParametersFormatter()));
		copyAccessActions.add(new CopyToClipboardAction(new AccessResponseHeadersFormatter()));

		prepareClipboardActions(copyLoggingActions, keyStrokeActionMapping);
		prepareClipboardActions(copyAccessActions, keyStrokeActionMapping);

		// Search
		findMenuAction = new FindAction(false);
		findPreviousMenuAction = new FindPreviousAction(false);
		findNextMenuAction = new FindNextAction(false);
		findPreviousActiveAction = new FindPreviousActiveAction(false);
		findNextActiveAction = new FindNextActiveAction(false);
		resetFindAction = new ResetFindAction();

		// View
		tailMenuAction = new TailAction(false);
		clearMenuAction = new ClearAction(false);
		attachMenuAction = new AttachAction(false);
		disconnectMenuAction = new DisconnectAction(false);

		focusMessageAction = new FocusMessageAction();
		focusEventsAction = new FocusEventsAction();

		editSourceNameMenuAction = new EditSourceNameMenuAction();
		saveLayoutAction = new SaveLayoutAction();
		resetLayoutAction = new ResetLayoutAction();
		saveConditionMenuAction = new SaveConditionMenuAction(!usingScreenMenuBar);

		zoomInMenuAction = new ZoomInMenuAction();
		zoomOutMenuAction = new ZoomOutMenuAction();
		resetZoomMenuAction = new ResetZoomMenuAction();

		previousViewAction = new PreviousViewAction();
		nextViewAction = new NextViewAction();
		closeFilterAction = new CloseFilterAction();
		closeOtherFiltersAction = new CloseOtherFiltersAction();
		closeAllFiltersAction = new CloseAllFiltersAction();

		// Window
		ShowTaskManagerAction showTaskManagerAction = new ShowTaskManagerAction();
		closeAllAction = new CloseAllAction();
		closeOtherAction = new CloseOtherAction();
		minimizeAllAction = new MinimizeAllAction();
		minimizeAllOtherAction = new MinimizeAllOtherAction();
		removeInactiveAction = new RemoveInactiveAction();
		//clearAndRemoveInactiveAction=new ClearAndRemoveInactiveAction();

		// Help
		HelpTopicsAction helpTopicsAction = new HelpTopicsAction();
		ShowLoveAction showLoveMenuAction = new ShowLoveAction(false);
		TipOfTheDayAction tipOfTheDayAction = new TipOfTheDayAction();
		DebugAction debugAction = new DebugAction();
		AboutAction aboutAction = new AboutAction();
		CheckForUpdateAction checkForUpdateAction = new CheckForUpdateAction();
		TroubleshootingAction troubleshootingAction = new TroubleshootingAction();

		// ##### ToolBar Actions #####
		tailToolBarAction = new TailAction(true);
		clearToolBarAction = new ClearAction(true);
		disconnectToolBarAction = new DisconnectAction(true);

		findToolBarAction = new FindAction(true);
		findPreviousToolBarAction = new FindPreviousAction(true);
		findNextToolBarAction = new FindNextAction(true);
		findPreviousActiveToolBarAction = new FindPreviousActiveAction(true);
		findNextActiveToolBarAction = new FindNextActiveAction(true);

		attachToolBarAction = new AttachAction(true);

		PreferencesAction preferencesToolBarAction = new PreferencesAction(true);
		ShowLoveAction showLoveToolbarAction = new ShowLoveAction(true);

		showTaskManagerItem = new JMenuItem(showTaskManagerAction);
		closeAllItem = new JMenuItem(closeAllAction);
		closeAllOtherItem = new JMenuItem(closeOtherAction);
		minimizeAllItem = new JMenuItem(minimizeAllAction);
		minimizeAllOtherItem = new JMenuItem(minimizeAllOtherAction);
		removeInactiveItem = new JMenuItem(removeInactiveAction);

		toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);

		tailButton = new JToggleButton(tailToolBarAction);
		toolBar.add(tailButton);
		toolBar.add(new JButton(clearToolBarAction));
		toolBar.add(new JButton(disconnectToolBarAction));

		toolBar.addSeparator();

		toolBar.add(new JButton(findToolBarAction));
		toolBar.add(new JButton(findPreviousToolBarAction));
		toolBar.add(new JButton(findNextToolBarAction));
		toolBar.add(new JButton(findPreviousActiveToolBarAction));
		toolBar.add(new JButton(findNextActiveToolBarAction));

		toolBar.addSeparator();

		toolBar.add(new JButton(attachToolBarAction));

		toolBar.addSeparator();

		toolBar.add(new JButton(preferencesToolBarAction));

		toolBar.addSeparator();

		toolBar.add(new JButton(showLoveToolbarAction));

		recentFilesMenu=new JMenu(new RecentFilesAction());

		menuBar = new JMenuBar();

		// File
		JMenu fileMenu = new JMenu(new FileAction());
		fileMenu.add(openMenuAction);
		fileMenu.add(recentFilesMenu);
		fileMenu.add(openInactiveLogMenuAction);
		fileMenu.add(cleanAllInactiveLogsMenuAction);
		fileMenu.add(importMenuAction);
		fileMenu.add(exportMenuAction);
		// TODO if(!app.isMac())
		{
			fileMenu.addSeparator();
			fileMenu.add(preferencesMenuAction);
			fileMenu.addSeparator();
			fileMenu.add(exitMenuAction);
		}

		// Edit
		JMenu editMenu = new JMenu(new EditAction());
		editMenu.add(copySelectionAction);
		editMenu.addSeparator();
		editMenu.add(copyEventAction);
		editMenu.addSeparator();

		copyLoggingActions.forEach(editMenu::add);

		editMenu.addSeparator();

		copyAccessActions.forEach(editMenu::add);

		editMenu.addSeparator();
		CustomCopyAction customCopyAction = new CustomCopyAction();
		customCopyMenu = new JMenu(customCopyAction);
		customCopyPopupMenu = new JMenu(customCopyAction);
		editMenu.add(customCopyMenu);
		editMenu.addSeparator();
		PasteStackTraceElementAction pasteStackTraceElementAction = new PasteStackTraceElementAction();
		editMenu.add(goToSourceAction);
		editMenu.add(pasteStackTraceElementAction);

		// Search
		searchMenu = new JMenu(new SearchAction());
		searchMenu.add(findMenuAction);
		searchMenu.add(resetFindAction);
		searchMenu.add(findPreviousMenuAction);
		searchMenu.add(findNextMenuAction);
		searchMenu.add(findPreviousActiveAction);
		searchMenu.add(findNextActiveAction);
		searchMenu.addSeparator();
		searchMenu.add(saveConditionMenuAction);
		searchMenu.addSeparator();

		focusMenu = new FocusMenu(applicationPreferences, !usingScreenMenuBar);
		excludeMenu = new ExcludeMenu(applicationPreferences, !usingScreenMenuBar);
		searchMenu.add(focusMenu);
		searchMenu.add(excludeMenu);
		searchMenu.addSeparator();
		searchMenu.add(showUnfilteredEventAction);

		// View
		viewMenu = new JMenu(new ViewAction());
		viewMenu.add(tailMenuAction);
		viewMenu.add(clearMenuAction);
		viewMenu.add(attachMenuAction);
		viewMenu.add(disconnectMenuAction);
		viewMenu.add(focusEventsAction);
		viewMenu.add(focusMessageAction);
		viewMenu.add(editSourceNameMenuAction);
		viewMenu.addSeparator();
		viewMenu.add(zoomInMenuAction);
		viewMenu.add(zoomOutMenuAction);
		viewMenu.add(resetZoomMenuAction);
		viewMenu.addSeparator();
		JMenu layoutMenu = new JMenu(new LayoutAction());
		columnsMenu = new JMenu(new ColumnsAction());
		layoutMenu.add(columnsMenu);
		layoutMenu.addSeparator();
		layoutMenu.add(saveLayoutAction);
		layoutMenu.add(resetLayoutAction);
		viewMenu.add(layoutMenu);
		viewMenu.addSeparator();
		viewMenu.add(nextViewAction);
		viewMenu.add(previousViewAction);
		viewMenu.addSeparator();
		viewMenu.add(closeFilterAction);
		viewMenu.add(closeOtherFiltersAction);
		viewMenu.add(closeAllFiltersAction);

		// Window
		windowMenu = new JMenu(new WindowAction());

		// Help
		JMenu helpMenu = new JMenu(new HelpAction());

		helpMenu.add(helpTopicsAction);
		helpMenu.add(showLoveMenuAction);
		helpMenu.add(tipOfTheDayAction);
		helpMenu.add(checkForUpdateAction);
		helpMenu.add(troubleshootingAction);
		helpMenu.addSeparator();
		helpMenu.add(debugAction);
		// TODO if(!app.isMac())
		{
			helpMenu.addSeparator();
			helpMenu.add(aboutAction);
		}


		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(searchMenu);
		menuBar.add(viewMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);

		updateWindowMenu();
		updateRecentFiles();

		popup = new JPopupMenu();
		JMenuItem showUnfilteredMenuItem = new JMenuItem(showUnfilteredEventAction);
		Font f = showUnfilteredMenuItem.getFont();
		Font boldFont = f.deriveFont(Font.BOLD);
		showUnfilteredMenuItem.setFont(boldFont);

		popup.add(showUnfilteredMenuItem);

		filterPopupMenu = new JMenu("Filter");
		popup.add(filterPopupMenu);
		filterPopupMenu.add(closeFilterAction);
		filterPopupMenu.add(closeOtherFiltersAction);
		filterPopupMenu.add(closeAllFiltersAction);

		popup.addSeparator();
		popup.add(saveConditionMenuAction);
		popup.addSeparator();

		focusPopupMenu = new FocusMenu(applicationPreferences, true);
		excludePopupMenu = new ExcludeMenu(applicationPreferences, true);

		popup.add(focusPopupMenu);
		popup.add(excludePopupMenu);
		popup.addSeparator();

		updateCustomCopyMenu(this.eventWrapper);

		copyPopupMenu = new JMenu("Copy");
		popup.add(copyPopupMenu);
		copyPopupMenu.add(copySelectionAction);
		copyPopupMenu.addSeparator();
		copyPopupMenu.add(copyEventAction);

		copyPopupMenu.addSeparator();

		copyLoggingActions.forEach(copyPopupMenu::add);

		copyPopupMenu.addSeparator();

		copyAccessActions.forEach(copyPopupMenu::add);

		copyPopupMenu.addSeparator();
		copyPopupMenu.add(customCopyPopupMenu);

		popup.add(goToSourceAction);

		setViewContainer(viewContainer, false);
	}

	JToolBar getToolBar()
	{
		return toolBar;
	}

	JMenuBar getMenuBar()
	{
		return menuBar;
	}

	public void setViewContainer(ViewContainer<?> viewContainer)
	{
		setViewContainer(viewContainer, true);
	}

	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	private void setViewContainer(ViewContainer<?> viewContainer, boolean initialized)
	{
		if(this.viewContainer != viewContainer || !initialized)
		{
			if(this.viewContainer != null)
			{
				this.viewContainer.removeChangeListener(containerChangeListener);
				this.viewContainer.removePropertyChangeListener(containerPropertyChangeListener);
			}
			this.viewContainer = viewContainer;
			if(this.viewContainer != null)
			{
				this.viewContainer.addChangeListener(containerChangeListener);
				this.viewContainer.addPropertyChangeListener(containerPropertyChangeListener);

				setEventWrapper(this.viewContainer.getSelectedEvent());
			}
			else
			{
				setEventWrapper(null);
			}
			updateActions();
		}
	}

	public ViewContainer<?> getViewContainer()
	{
		return viewContainer;
	}

	public void updateWindowMenu()
	{
		updateWindowMenu(windowMenu);
	}

	private void updateActions()
	{
		boolean hasView = false;
		boolean hasFilter = false;
		boolean isActive = false;
		EventSource eventSource = null;
		EventWrapperViewPanel eventWrapperViewPanel=null;
		if(viewContainer != null)
		{
			hasView = true;
			eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventSource = eventWrapperViewPanel.getEventSource();
				hasFilter = eventWrapperViewPanel.getFilterCondition() != null;
				isActive = eventWrapperViewPanel.getState() == LoggingViewState.ACTIVE;
			}
		}
		copySelectionAction.setView(eventWrapperViewPanel);

		if(logger.isDebugEnabled()) logger.debug("updateActions() eventSource={}", eventSource);

		// File
		exportMenuAction.setView(eventWrapperViewPanel);

		// Search
		searchMenu.setEnabled(hasView);
		findMenuAction.setEnabled(hasView);
		resetFindAction.setEnabled(hasFilter);
		findPreviousMenuAction.setEnabled(hasFilter);
		findPreviousToolBarAction.setEnabled(hasFilter);
		findNextMenuAction.setEnabled(hasFilter);
		findNextToolBarAction.setEnabled(hasFilter);

		Condition condition = mainFrame.getFindActiveCondition();
		boolean activeEnabled = hasView && condition != null;
		findPreviousActiveAction.setEnabled(activeEnabled);
		findPreviousActiveToolBarAction.setEnabled(activeEnabled);
		findNextActiveAction.setEnabled(activeEnabled);
		findNextActiveToolBarAction.setEnabled(activeEnabled);

		// View
		viewMenu.setEnabled(hasView);
		tailMenuAction.setEnabled(hasView);
		editSourceNameMenuAction.setEnabled(hasView);
		saveLayoutAction.setEnabled(hasView);
		resetLayoutAction.setEnabled(hasView);
		clearMenuAction.setEnabled(hasView/* && !hasFilteredBuffer*/);
		attachMenuAction.setEnabled(hasView);
		disconnectMenuAction.setEnabled(isActive);
		focusEventsAction.setEnabled(hasView);
		focusMessageAction.setEnabled(hasView);
		updateShowHideMenu();
		previousViewAction.updateAction();
		nextViewAction.updateAction();

		disconnectToolBarAction.setEnabled(isActive);

		tailMenuAction.updateAction();
		editSourceNameMenuAction.updateAction();
		saveConditionMenuAction.updateAction();
		zoomInMenuAction.updateAction();
		zoomOutMenuAction.updateAction();
		resetZoomMenuAction.updateAction();

		attachMenuAction.updateAction();

		closeFilterAction.updateAction();
		closeOtherFiltersAction.updateAction();
		closeAllFiltersAction.updateAction();

		tailButton.setSelected(isScrollingToBottom());
		attachToolBarAction.updateAction();

		tailToolBarAction.setEnabled(hasView);
		clearToolBarAction.setEnabled(hasView/* && !hasFilteredBuffer*/);
		findToolBarAction.setEnabled(hasView);
		attachToolBarAction.setEnabled(hasView);
		disconnectToolBarAction.setEnabled(isActive);

		if(eventSource != null)
		{
			showUnfilteredEventAction.setEnabled((eventSource.getFilter() != null));
		}
		else
		{
			showUnfilteredEventAction.setEnabled(false);
		}
	}

	private void updateShowHideMenu()
	{
		columnsMenu.removeAll();
		if(viewContainer != null)
		{
			EventWrapperViewPanel<?> viewPanel = viewContainer.getSelectedView();
			if(viewPanel != null)
			{
				EventWrapperViewTable<?> table = viewPanel.getTable();
				if(table != null)
				{
					PersistentTableColumnModel tableColumnModel = table.getTableColumnModel();
					List<PersistentTableColumnModel.TableColumnLayoutInfo> cli = tableColumnModel
						.getColumnLayoutInfos();
					for(PersistentTableColumnModel.TableColumnLayoutInfo current : cli)
					{
						boolean visible = current.isVisible();
						JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem(new ShowHideAction(tableColumnModel, current.getColumnName(), visible)); // NOPMD - AvoidInstantiatingObjectsInLoops
						checkBoxMenuItem.setSelected(visible);
						columnsMenu.add(checkBoxMenuItem);
					}
				}
			}

		}
	}

	private boolean isScrollingToBottom()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				return eventWrapperViewPanel.isScrollingToBottom();
			}
		}
		return false;
	}

	private void setScrollingToBottom(boolean scrollingToBottom)
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.setScrollingToBottom(scrollingToBottom);
			}
		}
	}

	void clear()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.clear();
			}
		}
	}

	private void focusTable()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.focusTable();
			}
		}
	}

	private void editCondition()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				Condition currentFilter = eventWrapperViewPanel.getTable().getFilterCondition();

				Condition condition = eventWrapperViewPanel.getCombinedCondition(currentFilter);
				if(condition != null)
				{
					mainFrame.getPreferencesDialog().editCondition(condition);
				}
			}
		}
	}

	private void attachDetach()
	{
		ViewContainer container = getViewContainer();
		if(container != null)
		{
			MainFrame mainFrame = container.getMainFrame();
			ViewWindow window = container.resolveViewWindow();

			if(window instanceof JFrame)
			{
				window.closeWindow();
				mainFrame.showInternalFrame(container);
			}
			else if(window instanceof JInternalFrame)
			{
				window.closeWindow();
				mainFrame.showFrame(container);
			}
		}
		focusTable();
	}

	private void disconnect()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.closeConnection(eventWrapperViewPanel.getEventSource().getSourceIdentifier());
			}
		}
	}

	private void focusMessage()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.focusMessagePane();
			}
		}
	}

	private void focusEvents()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.focusTable();
			}
		}
	}

	private void findNext()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel
					.findNext(eventWrapperViewPanel.getSelectedRow(), eventWrapperViewPanel.getFilterCondition());
			}
		}
	}

	private void findNextActive()
	{
		Condition condition = mainFrame.getFindActiveCondition();
		if(viewContainer != null && condition != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel
					.findNext(eventWrapperViewPanel.getSelectedRow(), condition);
			}
		}
	}

	private void findPrevious()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel
					.findPrevious(eventWrapperViewPanel.getSelectedRow(), eventWrapperViewPanel.getFilterCondition());
			}
		}
	}

	private void findPreviousActive()
	{
		Condition condition = mainFrame.getFindActiveCondition();
		if(viewContainer != null && condition != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel
					.findPrevious(eventWrapperViewPanel.getSelectedRow(), condition);
			}
		}
	}

	private void resetFind()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.resetFind();
			}
		}
	}

	private void closeCurrentFilter()
	{
		if(viewContainer != null)
		{
			viewContainer.closeCurrentFilter();
		}
	}

	private void closeOtherFilters()
	{
		if(viewContainer != null)
		{
			viewContainer.closeOtherFilters();
		}
	}

	private void closeAllFilters()
	{
		if(viewContainer != null)
		{
			viewContainer.closeAllFilters();
		}
	}


	private void previousTab()
	{
		if(logger.isDebugEnabled()) logger.debug("PreviousTab");
		if(viewContainer != null)
		{
			int viewCount = viewContainer.getViewCount();
			int viewIndex = viewContainer.getViewIndex();
			if(viewIndex > -1)
			{
				int newView = viewIndex - 1;
				if(newView < 0)
				{
					newView = viewCount - 1;
				}
				if(newView >= 0 && newView < viewCount)
				{
					viewContainer.setViewIndex(newView);
				}
			}
		}
	}

	private void nextTab()
	{
		if(logger.isDebugEnabled()) logger.debug("NextTab");
		if(viewContainer != null)
		{
			int viewIndex = viewContainer.getViewIndex();
			int viewCount = viewContainer.getViewCount();
			if(viewIndex > -1)
			{
				int newView = viewIndex + 1;
				if(newView >= viewCount)
				{
					newView = 0;
				}
				if(newView >= 0)
				{
					viewContainer.setViewIndex(newView);
				}
			}
		}
	}

	private void showUnfilteredEvent()
	{
		if(viewContainer != null)
		{
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel != null)
			{
				eventWrapperViewPanel.showUnfilteredEvent();
			}
		}
	}

	private void setEventWrapper(EventWrapper wrapper)
	{
		if(logger.isDebugEnabled()) logger.debug("setEventWrapper: {}", wrapper);
		this.eventWrapper = wrapper;
		goToSourceAction.setEventWrapper(eventWrapper);
		copyEventAction.setEventWrapper(eventWrapper);
		for(CopyToClipboardAction current : copyLoggingActions)
		{
			current.setEventWrapper(eventWrapper);
		}
		for(CopyToClipboardAction current : copyAccessActions)
		{
			current.setEventWrapper(eventWrapper);
		}
		updateCustomCopyMenu(eventWrapper);
		focusMenu.setViewContainer(viewContainer);
		focusMenu.setEventWrapper(eventWrapper);
		excludeMenu.setViewContainer(viewContainer);
		excludeMenu.setEventWrapper(eventWrapper);
	}

	private void updateCustomCopyMenu(EventWrapper wrapper)
	{
		String[] scripts = applicationPreferences.getClipboardFormatterScriptFiles();
		boolean changed = false;
		if(groovyClipboardActions == null)
		{
			groovyClipboardActions = new HashMap<>();
			changed = true;
		}
		if(groovyClipboardData == null)
		{
			groovyClipboardData = new HashMap<>();
			changed = true;
		}
		if(scripts == null || scripts.length == 0)
		{
			if(!groovyClipboardActions.isEmpty())
			{
				groovyClipboardActions.clear();
				groovyClipboardData.clear();
				changed = true;
			}
		}
		else
		{
			List<String> scriptsList = Arrays.asList(scripts);
			// add missing formatters
			for(String current : scriptsList)
			{
				if(!groovyClipboardActions.containsKey(current))
				{
					GroovyFormatter newFormatter = new GroovyFormatter(); // NOPMD - AvoidInstantiatingObjectsInLoops
					newFormatter.setGroovyFileName(applicationPreferences.resolveClipboardFormatterScriptFile(current).getAbsolutePath());
					CopyToClipboardAction newAction = new CopyToClipboardAction(newFormatter); // NOPMD - AvoidInstantiatingObjectsInLoops
					groovyClipboardActions.put(current, newAction);
					changed = true;
				}
			}

			// find deleted formatters
			List<String> deletedList = groovyClipboardActions.entrySet().stream()
					.filter(current -> !scriptsList.contains(current.getKey()))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());

			// remove deleted formatters
			for(String current : deletedList)
			{
				groovyClipboardActions.remove(current);
				changed = true;
			}
		}

		for(Map.Entry<String, CopyToClipboardAction> current : groovyClipboardActions.entrySet())
		{
			String key = current.getKey();
			CopyToClipboardAction value = current.getValue();
			ClipboardFormatter formatter = value.getClipboardFormatter();
			if(formatter == null)
			{
				continue;
			}
			ClipboardFormatterData data = new ClipboardFormatterData(formatter); // NOPMD - AvoidInstantiatingObjectsInLoops
			if(!data.equals(groovyClipboardData.get(key)))
			{
				changed = true;
				groovyClipboardData.put(key, data);
				value.setClipboardFormatter(formatter); // this re-initializes the action
			}
		}

		if(changed)
		{
			customCopyMenu.removeAll();
			customCopyPopupMenu.removeAll();
			boolean enabled = false;
			if(!groovyClipboardActions.isEmpty())
			{
				enabled = true;
				SortedSet<CopyToClipboardAction> sorted = new TreeSet<>(CopyToClipboardByNameComparator.INSTANCE);
				// sort the actions by name
				sorted.addAll(groovyClipboardActions.entrySet().stream()
						.map(Map.Entry::getValue)
						.collect(Collectors.toList()));

				Map<KeyStroke, CopyToClipboardAction> freshMapping = new HashMap<>(keyStrokeActionMapping);
				prepareClipboardActions(sorted, freshMapping);

				// add the sorted actions to the menus.
				for(CopyToClipboardAction current : sorted)
				{
					customCopyMenu.add(current);
					customCopyPopupMenu.add(current);
				}
			}
			customCopyMenu.setEnabled(enabled);
			customCopyPopupMenu.setEnabled(enabled);
		}

		for(Map.Entry<String, CopyToClipboardAction> current : groovyClipboardActions.entrySet())
		{
			CopyToClipboardAction value = current.getValue();
			value.setEventWrapper(wrapper);
		}
	}

	private void prepareClipboardActions(Collection<CopyToClipboardAction> actions, Map<KeyStroke, CopyToClipboardAction> mapping)
	{
		if(actions == null)
		{
			throw new IllegalArgumentException("actions must not be null!");
		}
		if(mapping == null)
		{
			throw new IllegalArgumentException("mapping must not be null!");
		}
		for(CopyToClipboardAction current : actions)
		{

			Object obj = current.getValue(Action.ACCELERATOR_KEY);
			if(!(obj instanceof KeyStroke))
			{
				continue;
			}
			ClipboardFormatter formatter = current.getClipboardFormatter();
			if(formatter == null)
			{
				// oO?
				continue;
			}
			boolean reset = false;
			String name = formatter.getName();
			KeyStroke currentKeyStroke = (KeyStroke) obj;
			if(!formatter.isNative())
			{
				String existingActionName = LilithKeyStrokes.getActionName(currentKeyStroke);
				if (existingActionName != null)
				{
					if (logger.isWarnEnabled())
						logger.warn("KeyStroke '{}' of formatter '{}' would collide with native Lilith action '{}'. Ignoring...", currentKeyStroke, name, existingActionName);
					reset = true;
				}
			}
			CopyToClipboardAction existingAction = mapping.get(currentKeyStroke);
			if(existingAction != null)
			{
				String existingFormatterName = null;
				ClipboardFormatter existingFormatter = existingAction.getClipboardFormatter();
				if(existingFormatter != null)
				{
					existingFormatterName = existingFormatter.getName();
				}
				if(logger.isWarnEnabled()) logger.warn("KeyStroke '{}' of formatter '{}' would collide with other formatter '{}'. Ignoring...", currentKeyStroke, name, existingFormatterName);
				reset = true;
			}

			if(reset)
			{
				if(logger.isInfoEnabled()) logger.info("Resetting accelerator for formatter '{}'.", name);
				current.putValue(Action.ACCELERATOR_KEY, null);
			}
			else
			{
				mapping.put(currentKeyStroke, current);
			}
		}
	}

	private void updateWindowMenu(JMenu windowMenu)
	{
		// must be executed later because the ancestor-change-event is fired
		// while parent is still != null...
		// see JComponent.removeNotify source for comment.
		EventQueue.invokeLater(new UpdateWindowMenuRunnable(windowMenu));
	}

	private void updatePopup()
	{
		if(logger.isDebugEnabled()) logger.debug("updatePopup()");
		boolean enableCopyMenu = false;
		if(eventWrapper != null)
		{
			EventWrapper<LoggingEvent> loggingEventWrapper = asLoggingEventWrapper(eventWrapper);
			EventWrapper<AccessEvent> accessEventWrapper = asAccessEventWrapper(eventWrapper);
			enableCopyMenu = loggingEventWrapper != null || accessEventWrapper != null;
		}
		boolean enableFilterMenu = closeFilterAction.isEnabled() || closeOtherFiltersAction.isEnabled() || closeAllFiltersAction.isEnabled();
		filterPopupMenu.setEnabled(enableFilterMenu);
		copyPopupMenu.setEnabled(enableCopyMenu);
		focusPopupMenu.setViewContainer(viewContainer);
		focusPopupMenu.setEventWrapper(eventWrapper);
		excludePopupMenu.setViewContainer(viewContainer);
		excludePopupMenu.setEventWrapper(eventWrapper);
	}

	JPopupMenu getPopupMenu()
	{
		updatePopup();

		return popup;
	}

	public void updateRecentFiles()
	{
		List<String> recentFilesStrings = applicationPreferences.getRecentFiles();
		if(recentFilesStrings == null || recentFilesStrings.isEmpty())
		{
			recentFilesMenu.removeAll();
			recentFilesMenu.setEnabled(false);
		}
		else
		{
			boolean fullPath=applicationPreferences.isShowingFullRecentPath();

			recentFilesMenu.removeAll();

			for(String current:recentFilesStrings)
			{
				recentFilesMenu.add(new OpenFileAction(current, fullPath)); // NOPMD - AvoidInstantiatingObjectsInLoops
			}
			recentFilesMenu.addSeparator();
			recentFilesMenu.add(clearRecentFilesAction);
			recentFilesMenu.setEnabled(true);
		}
	}

	public void setConditionNames(List<String> conditionNames)
	{
		focusMenu.setConditionNames(conditionNames);
		focusPopupMenu.setConditionNames(conditionNames);
		excludeMenu.setConditionNames(conditionNames);
		excludePopupMenu.setConditionNames(conditionNames);
	}

	private class OpenFileAction
		extends AbstractAction
	{
		private static final long serialVersionUID = 3138705799791457944L;

		private final String absoluteName;

		OpenFileAction(String absoluteName, boolean fullPath)
		{
			super();

			this.absoluteName=absoluteName;
			String name=absoluteName;
			if(!fullPath)
			{
				File f=new File(absoluteName);
				name=f.getName();
			}
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, Icons.resolveEmptyMenuIcon());
			putValue(Action.SHORT_DESCRIPTION, absoluteName);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.open(new File(absoluteName));
		}
	}

	private class ClearRecentFilesAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 5931366119837395385L;

		ClearRecentFilesAction()
		{
			super(LilithActionId.CLEAR_RECENT_FILES);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			applicationPreferences.clearRecentFiles();
		}
	}

	private class RemoveInactiveAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -4899555487517384044L;

		RemoveInactiveAction()
		{
			super(LilithActionId.REMOVE_INACTIVE);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.removeInactiveViews(false);
			mainFrame.updateWindowMenus();
		}
	}

	private class ShowTaskManagerAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 8903964233879357068L;

		ShowTaskManagerAction()
		{
			super(LilithActionId.TASK_MANAGER);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.showTaskManager();
		}
	}

	private class CloseAllAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -1422391104698136853L;

		CloseAllAction()
		{
			super(LilithActionId.CLOSE_ALL);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.closeAllViews(null);
			mainFrame.updateWindowMenus();
		}
	}

	private class CloseOtherAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 7428424871185431400L;

		CloseOtherAction()
		{
			super(LilithActionId.CLOSE_ALL_OTHER);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.closeAllViews(viewContainer);
			mainFrame.updateWindowMenus();
		}
	}

	private class MinimizeAllAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -5691488945878472747L;

		MinimizeAllAction()
		{
			super(LilithActionId.MINIMIZE_ALL);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.minimizeAllViews(null);
			mainFrame.updateWindowMenus();
		}
	}

	private class MinimizeAllOtherAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 4359591467723034614L;

		MinimizeAllOtherAction()
		{
			super(LilithActionId.MINIMIZE_OTHER);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.minimizeAllViews(viewContainer);
			mainFrame.updateWindowMenus();
		}
	}

	private class ClearAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -4713267797278778997L;

		ClearAction(boolean toolbar)
		{
			super(LilithActionId.CLEAR, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			clear();
		}
	}

	private class ZoomInMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -7888485901625353999L;

		ZoomInMenuAction()
		{
			super(LilithActionId.ZOOM_IN);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.zoomIn();
		}

		void updateAction()
		{
			boolean enable = false;
			if(viewContainer != null)
			{
				EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
				if(eventWrapperViewPanel != null)
				{
					enable = true;
				}
			}
			setEnabled(enable);
		}
	}

	private class ZoomOutMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 4775486230433866870L;

		ZoomOutMenuAction()
		{
			super(LilithActionId.ZOOM_OUT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.zoomOut();
		}

		void updateAction()
		{
			boolean enable = false;
			if(viewContainer != null)
			{
				EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
				if(eventWrapperViewPanel != null)
				{
					enable = true;
				}
			}
			setEnabled(enable);
		}
	}

	private class ResetZoomMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2666064072732620530L;

		ResetZoomMenuAction()
		{
			super(LilithActionId.RESET_ZOOM);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.resetZoom();
		}

		void updateAction()
		{
			boolean enable = false;
			if(viewContainer != null)
			{
				EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
				if(eventWrapperViewPanel != null)
				{
					enable = true;
				}
			}
			setEnabled(enable);
		}
	}

	private class SaveConditionMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -5736668292947348845L;

		private final boolean htmlTooltip;

		SaveConditionMenuAction(boolean htmlTooltip)
		{
			super(LilithActionId.SAVE_CONDITION);
			this.htmlTooltip = htmlTooltip;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			editCondition();
		}

		void updateAction()
		{
			if(viewContainer != null)
			{
				EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
				if(eventWrapperViewPanel != null)
				{
					Condition currentFilter = eventWrapperViewPanel.getTable().getFilterCondition();

					Condition condition = eventWrapperViewPanel.getCombinedCondition(currentFilter);
					if(condition != null)
					{
						ActionTooltips.initializeConditionTooltip(condition, this, htmlTooltip);
						setEnabled(true);
						return;
					}
				}
			}
			putValue(Action.SHORT_DESCRIPTION, getId().getDescription());
			setEnabled(false);
		}
	}

	private class EditSourceNameMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -1769963136846922348L;

		EditSourceNameMenuAction()
		{
			super(LilithActionId.EDIT_SOURCE_NAME);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(viewContainer == null)
			{
				return;
			}
			EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
			if(eventWrapperViewPanel == null)
			{
				return;
			}

			EventSource eventSource = eventWrapperViewPanel.getEventSource();
			if(eventSource.isGlobal())
			{
				return;
			}

			String identifier = eventSource.getSourceIdentifier().getIdentifier();
			if(InternalLilithAppender.IDENTIFIER_NAME.equals(identifier))
			{
				return;
			}
			mainFrame.getPreferencesDialog().editSourceName(identifier);
		}


		void updateAction()
		{
			boolean enable = false;
			if(viewContainer != null)
			{
				EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
				if(eventWrapperViewPanel != null)
				{
					EventSource eventSource = eventWrapperViewPanel.getEventSource();
					if(!eventSource.isGlobal())
					{
						String identifier = eventSource.getSourceIdentifier().getIdentifier();
						if (!InternalLilithAppender.IDENTIFIER_NAME.equals(identifier))
						{
							enable = true;
						}
					}
				}
			}
			setEnabled(enable);
		}
	}

	private class AttachAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -6686061036755515933L;

		AttachAction(boolean toolbar)
		{
			super(LilithActionId.ATTACH, toolbar);
			updateAction();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			attachDetach();
			updateAction();
		}

		final void updateAction()
		{
			ViewContainer container = getViewContainer();
			if(container != null)
			{
				ViewWindow window = container.resolveViewWindow();
				if(window instanceof JFrame)
				{
					if(isToolbar())
					{
						putValue(Action.SMALL_ICON, Icons.ATTACH_TOOLBAR_ICON);
					}
					else
					{
						putValue(Action.SMALL_ICON, Icons.ATTACH_MENU_ICON);
						putValue(Action.NAME, "Attach");
					}
					return;
				}
			}
			// default/init to Detach
			if(isToolbar())
			{
				putValue(Action.SMALL_ICON, Icons.DETACH_TOOLBAR_ICON);
			}
			else
			{
				putValue(Action.SMALL_ICON, Icons.DETACH_MENU_ICON);
				putValue(Action.NAME, "Detach");
			}
		}
	}

	private class FindAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 8686689881059491581L;

		FindAction(boolean toolbar)
		{
			super(LilithActionId.FIND, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(viewContainer != null)
			{
				EventWrapperViewPanel eventWrapperViewPanel = viewContainer.getSelectedView();
				if(eventWrapperViewPanel != null)
				{
					eventWrapperViewPanel.setShowingFilters(true);
				}
			}
		}
	}

	private class DisconnectAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -7608467965704213937L;

		DisconnectAction(boolean toolbar)
		{
			super(LilithActionId.DISCONNECT, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			disconnect();
		}
	}


	private class FocusMessageAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -7138801117961747361L;

		FocusMessageAction()
		{
			super(LilithActionId.FOCUS_MESSAGE);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			focusMessage();
		}
	}

	private class FocusEventsAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -8861583468969887725L;

		FocusEventsAction()
		{
			super(LilithActionId.FOCUS_EVENTS);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			focusEvents();
		}
	}

	private class FindNextAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -8423353519237402903L;

		FindNextAction(boolean toolbar)
		{
			super(LilithActionId.FIND_NEXT, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			findNext();
		}

	}

	private class FindPreviousAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 5500344754940498682L;

		FindPreviousAction(boolean toolbar)
		{
			super(LilithActionId.FIND_PREVIOUS, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			findPrevious();
		}
	}

	private class FindNextActiveAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 5173104146045140691L;

		FindNextActiveAction(boolean toolbar)
		{
			super(LilithActionId.FIND_NEXT_ACTIVE, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			findNextActive();
		}

	}

	private class FindPreviousActiveAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2678420403434984064L;

		FindPreviousActiveAction(boolean toolbar)
		{
			super(LilithActionId.FIND_PREVIOUS_ACTIVE, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			findPreviousActive();
		}
	}

	private class ResetFindAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 4133654759522587356L;

		ResetFindAction()
		{
			super(LilithActionId.RESET_FIND);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			resetFind();
		}
	}

	private static class FileAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 6713037299656243140L;

		FileAction()
		{
			super(LilithActionId.FILE);
			putValue(Action.SMALL_ICON, null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class EditAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -2716967152517099080L;

		EditAction()
		{
			super(LilithActionId.EDIT);
			putValue(Action.SMALL_ICON, null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class SearchAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 4657376136768632219L;

		SearchAction()
		{
			super(LilithActionId.SEARCH);
			putValue(Action.SMALL_ICON, null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class ViewAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 3606162173106275568L;

		ViewAction()
		{
			super(LilithActionId.VIEW);
			putValue(Action.SMALL_ICON, null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class WindowAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 9045231748351787117L;

		WindowAction()
		{
			super(LilithActionId.WINDOW);
			putValue(Action.SMALL_ICON, null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class HelpAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2294357096466137621L;

		HelpAction()
		{
			super(LilithActionId.HELP);
			putValue(Action.SMALL_ICON, null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class LayoutAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2989496510791574495L;

		LayoutAction()
		{
			super(LilithActionId.LAYOUT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class ColumnsAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 4884985802967711064L;

		ColumnsAction()
		{
			super(LilithActionId.COLUMNS);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class RecentFilesAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 3479806600315336050L;

		RecentFilesAction()
		{
			super(LilithActionId.RECENT_FILES);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private static class CustomCopyAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -5009668707700082616L;

		CustomCopyAction()
		{
			super(LilithActionId.CUSTOM_COPY);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// nothing
		}
	}

	private class TailAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -8499706451642163124L;

		TailAction(boolean toolbar)
		{
			super(LilithActionId.TAIL, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			boolean tail = !isScrollingToBottom();
			setScrollingToBottom(tail);
			if(logger.isDebugEnabled()) logger.debug("tail={}", tail);
			focusTable();
		}

		void updateAction()
		{
			if(isToolbar())
			{
				return;
			}
			if(isScrollingToBottom())
			{
				putValue(Action.SMALL_ICON, Icons.resolveMenuIcon(getId()));
			}
			else
			{
				putValue(Action.SMALL_ICON, Icons.resolveEmptyMenuIcon());
			}
		}
	}

	private class CloseFilterAction
		extends AbstractLilithAction
	{
		private static final long serialVersionUID = -842677137302613585L;

		CloseFilterAction()
		{
			super(LilithActionId.CLOSE_FILTER);
		}

		void updateAction()
		{
			if(viewContainer != null)
			{
				int viewIndex = viewContainer.getViewIndex();
				if(viewIndex > 0)
				{
					setEnabled(true);
				}
				else
				{
					setEnabled(false);
				}
			}
			else
			{
				setEnabled(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			closeCurrentFilter();
		}

	}

	private class CloseOtherFiltersAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -6399148183817841417L;

		CloseOtherFiltersAction()
		{
			super(LilithActionId.CLOSE_OTHER_FILTERS);
		}

		void updateAction()
		{
			if(viewContainer != null)
			{
				int viewIndex = viewContainer.getViewIndex();
				int viewCount = viewContainer.getViewCount();
				if(viewIndex > -1 && ((viewIndex == 0 && viewCount > 1) || viewCount > 2))
				{
					setEnabled(true);
				}
				else
				{
					setEnabled(false);
				}
			}
			else
			{
				setEnabled(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			closeOtherFilters();
		}

	}

	private class CloseAllFiltersAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -5157911937552703061L;

		CloseAllFiltersAction()
		{
			super(LilithActionId.CLOSE_ALL_FILTERS);
		}

		void updateAction()
		{
			int viewCount = 0;
			if(viewContainer != null)
			{
				viewCount = viewContainer.getViewCount();
			}
			if(viewCount > 1)
			{
				setEnabled(true);
			}
			else
			{
				setEnabled(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			closeAllFilters();
		}
	}


	class ViewLoggingAction
		extends AbstractAction
	{
		private static final long serialVersionUID = 6967472316665780683L;

		private final EventSource<LoggingEvent> eventSource;

		ViewLoggingAction(String title, String tooltipText, EventSource<LoggingEvent> eventSource)
		{
			super(title);
			this.eventSource = eventSource;

			LilithActionId actionId = null;
			if(eventSource.isGlobal())
			{
				actionId = LilithActionId.VIEW_GLOBAL_CLASSIC_LOGS;
			}
			else
			{
				SourceIdentifier si = eventSource.getSourceIdentifier();
				if(InternalLilithAppender.IDENTIFIER_NAME.equals(si.getIdentifier()))
				{
					// internal Lilith log
					actionId = LilithActionId.VIEW_LILITH_LOGS;
				}
			}

			if(actionId != null)
			{
				AbstractLilithAction.initMenuActionFromActionId(this, actionId);
			}
			else
			{
				putValue(Action.SHORT_DESCRIPTION, tooltipText);
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt)
		{
			mainFrame.showLoggingView(eventSource);
		}

	}

	class ViewAccessAction
		extends AbstractAction
	{
		private static final long serialVersionUID = 8054851261518410946L;

		private final EventSource<AccessEvent> eventSource;

		ViewAccessAction(String title, String tooltipText, EventSource<AccessEvent> eventSource)
		{
			super(title);
			this.eventSource = eventSource;

			LilithActionId actionId = null;
			if(eventSource.isGlobal())
			{
				actionId = LilithActionId.VIEW_GLOBAL_ACCESS_LOGS;
			}

			if(actionId != null)
			{
				AbstractLilithAction.initMenuActionFromActionId(this, actionId);
			}
			else
			{
				putValue(Action.SHORT_DESCRIPTION, tooltipText);
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt)
		{
			mainFrame.showAccessView(eventSource);
		}

	}

	static String resolveSourceTitle(ViewContainer container, Map<String, String> sourceNames, boolean showingPrimaryIdentifier, boolean showingSecondaryIdentifier)
	{
		EventWrapperViewPanel defaultView = container.getDefaultView();
		EventSource eventSource = defaultView.getEventSource();
		boolean global=eventSource.isGlobal();

		String name=null;
		if(!global)
		{
			name = resolveApplicationName(defaultView.getSourceBuffer());
		}

		SourceIdentifier si = eventSource.getSourceIdentifier();
		String title = resolveSourceTitle(si, name, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);

		Class clazz = container.getWrappedClass();
		if (clazz == LoggingEvent.class)
		{
			title = title + " (Logging)";
		}
		else if (clazz == AccessEvent.class)
		{
			title = title + " (Access)";
		}

		return title;
	}

	private static String resolveSourceTitle(SourceIdentifier identifier, String name, Map<String, String> sourceNames, boolean showingPrimaryIdentifier, boolean showingSecondaryIdentifier)
	{
		String primary = getPrimarySourceTitle(identifier.getIdentifier(), sourceNames, showingPrimaryIdentifier);
		String secondary = identifier.getSecondaryIdentifier();
		if(secondary == null || !showingSecondaryIdentifier || secondary.equals(primary))
		{
			if(name == null)
			{
				return primary;
			}
			return primary + " - " + name;
		}

		if(name == null)
		{
			return primary + " - " + secondary;
		}
		return primary + " - " +name + " - " + secondary;
	}

	static String resolveApplicationName(Buffer<?> buffer)
	{
		Object event=null;
		if(buffer != null)
		{
			event = buffer.get(0);
		}
		return resolveName(event);
	}

	private static String resolveName(Object eventWrapperObj)
	{
		String name;
		String appId=null;
		if(eventWrapperObj instanceof EventWrapper)
		{
			EventWrapper wrapper= (EventWrapper) eventWrapperObj;
			Serializable evtObject = wrapper.getEvent();
			LoggerContext context = null;
			if(evtObject instanceof LoggingEvent)
			{
				context = ((LoggingEvent) evtObject).getLoggerContext();
			}
			else if(evtObject instanceof AccessEvent)
			{
				context = ((AccessEvent) evtObject).getLoggerContext();
			}
			if(context != null)
			{
				name=context.getName();
				if("default".equals(name) || "".equals(name))
				{
					name = null;
				}
				Map<String, String> props = context.getProperties();
				if(props!= null)
				{
					appId=props.get(LoggerContext.APPLICATION_IDENTIFIER_PROPERTY_NAME);
				}

				if(name != null)
				{
					if(appId == null || name.equals(appId))
					{
						return name;
					}
					return name+"/"+appId;
				}
				return appId;
			}
		}
		return null;
	}

	static String getPrimarySourceTitle(String primaryIdentifier, Map<String, String> sourceNames, boolean showingPrimaryIdentifier)
	{
		if(primaryIdentifier == null)
		{
			return null;
		}

		String resolvedName = null;
		if(sourceNames != null)
		{
			resolvedName = sourceNames.get(primaryIdentifier);
		}
		if(resolvedName != null && !resolvedName.equals(primaryIdentifier))
		{
			if(showingPrimaryIdentifier)
			{
				return resolvedName + " [" + primaryIdentifier + "]";
			}
			else
			{
				return resolvedName;
			}
		}
		return primaryIdentifier;
	}

	class UpdateWindowMenuRunnable
		implements Runnable
	{
		private final JMenu windowMenu;

		UpdateWindowMenuRunnable(JMenu windowMenu)
		{
			this.windowMenu = windowMenu;
		}

		@Override
		public void run()
		{
			// remove loggingViews that were closed in the meantime...
			mainFrame.removeInactiveViews(true);

			Map<String, String> sourceNames = applicationPreferences.getSourceNames();
			boolean showingPrimaryIdentifier = applicationPreferences.isShowingPrimaryIdentifier();
			boolean showingSecondaryIdentifier = applicationPreferences.isShowingSecondaryIdentifier();
			boolean globalLoggingEnabled = applicationPreferences.isGlobalLoggingEnabled();

			if(logger.isDebugEnabled()) logger.debug("Updating Window-Menu.");

			windowMenu.removeAll();
			windowMenu.add(showTaskManagerItem);
			windowMenu.addSeparator();
			windowMenu.add(closeAllItem);
			windowMenu.add(closeAllOtherItem);
			windowMenu.add(minimizeAllItem);
			windowMenu.add(minimizeAllOtherItem);
			windowMenu.add(removeInactiveItem);

			int activeCounter = 0;
			int inactiveCounter = 0;
			int viewCounter = 0;

			boolean first;

			SortedMap<EventSource<LoggingEvent>, ViewContainer<LoggingEvent>> sortedLoggingViews =
					mainFrame.getSortedLoggingViews();

			SortedMap<EventSource<AccessEvent>, ViewContainer<AccessEvent>> sortedAccessViews =
					mainFrame.getSortedAccessViews();

			first = true;
			// Lilith logging
			for(Map.Entry<EventSource<LoggingEvent>, ViewContainer<LoggingEvent>> entry : sortedLoggingViews.entrySet())
			{
				EventSource<LoggingEvent> key = entry.getKey();
				SourceIdentifier si = key.getSourceIdentifier();
				if(InternalLilithAppender.IDENTIFIER_NAME.equals(si.getIdentifier()))
				{
					ViewContainer<LoggingEvent> value = entry.getValue();
					if(value.resolveViewWindow() != null)
					{
						viewCounter++;
					}
					if(first)
					{
						first = false;
						windowMenu.addSeparator();
					}
					JMenuItem menuItem = createLoggingMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
					windowMenu.add(menuItem);
				}
			}
			// global (Logging)
			for(Map.Entry<EventSource<LoggingEvent>, ViewContainer<LoggingEvent>> entry : sortedLoggingViews.entrySet())
			{
				EventSource<LoggingEvent> key = entry.getKey();
				SourceIdentifier si = key.getSourceIdentifier();
				if(!InternalLilithAppender.IDENTIFIER_NAME.equals(si.getIdentifier()))
				{
					ViewContainer<LoggingEvent> value = entry.getValue();
					if(value.resolveViewWindow() != null)
					{
						viewCounter++;
					}
					if(key.isGlobal())
					{
						if(first)
						{
							first = false;
							windowMenu.addSeparator();
						}
						JMenuItem menuItem = createLoggingMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
						menuItem.setEnabled(globalLoggingEnabled);
						windowMenu.add(menuItem);
					}
				}
			}
			// global (Access)
			for(Map.Entry<EventSource<AccessEvent>, ViewContainer<AccessEvent>> entry : sortedAccessViews.entrySet())
			{
				EventSource<AccessEvent> key = entry.getKey();
				ViewContainer<AccessEvent> value = entry.getValue();
				if(value.resolveViewWindow() != null)
				{
					viewCounter++;
				}
				if(key.isGlobal())
				{
					if(first)
					{
						first = false;
						windowMenu.addSeparator();
					}
					JMenuItem menuItem = createAccessMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
					menuItem.setEnabled(globalLoggingEnabled);
					windowMenu.add(menuItem);
				}
			}

			first = true;
			// Logging (active)
			for(Map.Entry<EventSource<LoggingEvent>, ViewContainer<LoggingEvent>> entry : sortedLoggingViews.entrySet())
			{
				EventSource<LoggingEvent> key = entry.getKey();
				SourceIdentifier si = key.getSourceIdentifier();
				if(!InternalLilithAppender.IDENTIFIER_NAME.equals(si.getIdentifier()))
				{
					ViewContainer<LoggingEvent> value = entry.getValue();
					EventWrapperViewPanel<LoggingEvent> panel = value.getDefaultView();
					if(!key.isGlobal() && (LoggingViewState.ACTIVE == panel.getState()))
					{
						if(first)
						{
							first = false;
							windowMenu.addSeparator();
						}
						JMenuItem menuItem = createLoggingMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
						windowMenu.add(menuItem);
						activeCounter++;
					}
				}
			}
			// Logging (inactive)
			for(Map.Entry<EventSource<LoggingEvent>, ViewContainer<LoggingEvent>> entry : sortedLoggingViews.entrySet())
			{
				EventSource<LoggingEvent> key = entry.getKey();
				SourceIdentifier si = key.getSourceIdentifier();
				if(!InternalLilithAppender.IDENTIFIER_NAME.equals(si.getIdentifier()))
				{
					ViewContainer<LoggingEvent> value = entry.getValue();
					EventWrapperViewPanel<LoggingEvent> panel = value.getDefaultView();
					if(!key.isGlobal() && (LoggingViewState.ACTIVE != panel.getState()))
					{
						if(first)
						{
							first = false;
							windowMenu.addSeparator();
						}
						JMenuItem menuItem = createLoggingMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
						windowMenu.add(menuItem);
						inactiveCounter++;
					}
				}
			}

			// Access (active)
			first = true;
			for(Map.Entry<EventSource<AccessEvent>, ViewContainer<AccessEvent>> entry : sortedAccessViews.entrySet())
			{
				EventSource<AccessEvent> key = entry.getKey();
				ViewContainer<AccessEvent> value = entry.getValue();
				EventWrapperViewPanel<AccessEvent> panel = value.getDefaultView();
				if(!key.isGlobal() && (LoggingViewState.ACTIVE == panel.getState()))
				{
					if(first)
					{
						first = false;
						windowMenu.addSeparator();
					}
					JMenuItem menuItem = createAccessMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
					windowMenu.add(menuItem);
					activeCounter++;
				}
			}
			// Access (inactive)
			for(Map.Entry<EventSource<AccessEvent>, ViewContainer<AccessEvent>> entry : sortedAccessViews.entrySet())
			{
				EventSource<AccessEvent> key = entry.getKey();
				ViewContainer<AccessEvent> value = entry.getValue();
				EventWrapperViewPanel<AccessEvent> panel = value.getDefaultView();
				if(!key.isGlobal() && (LoggingViewState.ACTIVE != panel.getState()))
				{
					if(first)
					{
						first = false;
						windowMenu.addSeparator();
					}
					JMenuItem menuItem = createAccessMenuItem(value, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
					windowMenu.add(menuItem);
					inactiveCounter++;
				}
			}

			// update status text
			boolean hasInactive = (inactiveCounter != 0);
			//clearAndRemoveInactiveAction.setEnabled(hasInactive);
			removeInactiveAction.setEnabled(hasInactive);
			boolean hasViews = viewCounter != 0;
			minimizeAllAction.setEnabled(hasViews);
			closeAllAction.setEnabled(hasViews);
			if(viewContainer == null || viewCounter <= 1)
			{
				minimizeAllOtherAction.setEnabled(false);
				closeOtherAction.setEnabled(false);
			}
			else
			{
				minimizeAllOtherAction.setEnabled(true);
				closeOtherAction.setEnabled(true);
			}

			mainFrame.setActiveConnectionsCounter(activeCounter);

			if(windowMenu.isPopupMenuVisible())
			{
				// I've not been able to find a more elegant solution to prevent
				// repaint artifacts if the menu contents change while the menu is still open...
				windowMenu.setPopupMenuVisible(false);
				windowMenu.setPopupMenuVisible(true);
			}
		}

		private JMenuItem createLoggingMenuItem(ViewContainer<LoggingEvent> viewContainer, Map<String, String> sourceNames, boolean showingPrimaryIdentifier, boolean showingSecondaryIdentifier)
		{
			EventSource<LoggingEvent> eventSource = viewContainer.getEventSource();
			String title=resolveSourceTitle(viewContainer, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
			String tooltipText=resolveSourceTitle(viewContainer, sourceNames, true, true);
			JMenuItem result = new JMenuItem(new ViewLoggingAction(title, tooltipText, eventSource));
			Container compParent = viewContainer.getParent();
			if(logger.isDebugEnabled()) logger.debug("\n\nParent for {}: {}\n", eventSource.getSourceIdentifier(), compParent);

			boolean disabled = false;
			if(compParent == null)
			{
				disabled = true;
			}
			result.setIcon(Icons.resolveFrameIcon(viewContainer.getState(), disabled));
			return result;
		}

		private JMenuItem createAccessMenuItem(ViewContainer<AccessEvent> viewContainer, Map<String, String> sourceNames, boolean showingPrimaryIdentifier, boolean showingSecondaryIdentifier)
		{
			EventSource<AccessEvent> eventSource = viewContainer.getEventSource();
			String title=resolveSourceTitle(viewContainer, sourceNames, showingPrimaryIdentifier, showingSecondaryIdentifier);
			String tooltipText=resolveSourceTitle(viewContainer, sourceNames, true, true);
			JMenuItem result = new JMenuItem(new ViewAccessAction(title, tooltipText, eventSource));
			Container compParent = viewContainer.getParent();
			if(logger.isDebugEnabled()) logger.debug("\n\nParent for {}: {}\n", eventSource.getSourceIdentifier(), compParent);

			boolean disabled = false;
			if(compParent == null)
			{
				disabled = true;
			}
			result.setIcon(Icons.resolveFrameIcon(viewContainer.getState(), disabled));
			return result;
		}
	}


	class AboutAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -372250750198620913L;

		AboutAction()
		{
			super(LilithActionId.ABOUT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.showAboutDialog();
		}
	}

	class SaveLayoutAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 5923537670097606664L;

		SaveLayoutAction()
		{
			super(LilithActionId.SAVE_LAYOUT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(viewContainer != null)
			{
				EventWrapperViewPanel<?> viewPanel = viewContainer.getSelectedView();
				if(viewPanel != null)
				{
					EventWrapperViewTable<?> table = viewPanel.getTable();
					if(table != null)
					{
						table.saveLayout();
					}
				}
			}
		}
	}

	class ResetLayoutAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 7012243855415503341L;

		ResetLayoutAction()
		{
			super(LilithActionId.RESET_LAYOUT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(viewContainer != null)
			{
				EventWrapperViewPanel<?> viewPanel = viewContainer.getSelectedView();
				if(viewPanel != null)
				{
					EventWrapperViewTable<?> table = viewPanel.getTable();
					if(table != null)
					{
						table.resetLayout();
					}
				}
			}
		}
	}

	class CheckForUpdateAction
		extends AbstractLilithAction
	{
		private static final long serialVersionUID = 529742851501771901L;

		CheckForUpdateAction()
		{
			super(LilithActionId.CHECK_FOR_UPDATE);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.checkForUpdate(true);
		}
	}

	class TroubleshootingAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 7272512675892611518L;

		TroubleshootingAction()
		{
			super(LilithActionId.TROUBLESHOOTING);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.troubleshooting();
		}
	}

	class HelpTopicsAction
		extends AbstractLilithAction
	{
		private static final long serialVersionUID = 4151080083718877643L;

		HelpTopicsAction()
		{
			super(LilithActionId.HELP_TOPICS);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.showHelp();
		}
	}

	class TipOfTheDayAction
		extends AbstractLilithAction
	{
		private static final long serialVersionUID = -3353245047244667056L;

		TipOfTheDayAction()
		{
			super(LilithActionId.TIP_OF_THE_DAY);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.showTipOfTheDayDialog();
		}
	}

	class PreferencesAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -3163817872447126174L;

		PreferencesAction(boolean toolbar)
		{
			super(LilithActionId.PREFERENCES, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.showPreferencesDialog();
		}
	}

	class ShowLoveAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -3947801116776349469L;

		ShowLoveAction(boolean toolbar)
		{
			super(LilithActionId.LOVE, toolbar);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.openHelp("love.xhtml");
		}
	}

	class DebugAction
		extends AbstractLilithAction
	{
		private static final long serialVersionUID = -8094926165037948097L;

		DebugAction()
		{
			super(LilithActionId.DEBUG);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.showDebugDialog();
		}
	}

	class ExitMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -2089144337780503904L;

		ExitMenuAction()
		{
			super(LilithActionId.EXIT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.exit();
		}
	}

	class OpenInactiveLogMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2115685255203253178L;

		OpenInactiveLogMenuAction()
		{
			super(LilithActionId.OPEN_INACTIVE);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.openInactiveLogs();
		}
	}

	class OpenMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 8743907596320539587L;

		OpenMenuAction()
		{
			super(LilithActionId.OPEN);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.open();
		}
	}

	class ImportMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -6465137339088031499L;

		ImportMenuAction()
		{
			super(LilithActionId.IMPORT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.importFile();
		}
	}

	class ExportMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2447331397548766700L;

		private EventWrapperViewPanel view;

		ExportMenuAction()
		{
			super(LilithActionId.EXPORT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.exportFile(view);
		}

		public void setView(EventWrapperViewPanel eventWrapperViewPanel)
		{
			this.view=eventWrapperViewPanel;
			setEnabled(view != null);
		}
	}

	class CleanAllInactiveLogsMenuAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 626049491764655228L;

		CleanAllInactiveLogsMenuAction()
		{
			super(LilithActionId.CLEAN_ALL_INACTIVE_LOGS);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.cleanAllInactiveLogs();
		}
	}

	private boolean hasMultipleViews()
	{
		return viewContainer != null && viewContainer.getViewCount() > 1;
	}

	private class PreviousViewAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -8929161486658826998L;

		PreviousViewAction()
		{
			super(LilithActionId.PREVIOUS_VIEW);
		}

		void updateAction()
		{
			setEnabled(hasMultipleViews());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			previousTab();
		}
	}

	private class NextViewAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -6274063652679458643L;

		NextViewAction()
		{
			super(LilithActionId.NEXT_VIEW);
		}

		void updateAction()
		{
			setEnabled(hasMultipleViews());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			nextTab();
		}
	}


	private class CopySelectionAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -349395646886330659L;
		private EventWrapperViewPanel view;

		CopySelectionAction()
		{
			super(LilithActionId.COPY_SELECTION);
			setView(null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(view != null)
			{
				view.copySelection();
			}
		}

		public void setView(EventWrapperViewPanel view)
		{
			this.view = view;
			setEnabled(view != null);
		}
	}

	private class PasteStackTraceElementAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 2254657459692349413L;

		private final Logger logger = LoggerFactory.getLogger(PasteStackTraceElementAction.class);

		private Clipboard clipboard;

		PasteStackTraceElementAction()
		{
			super(LilithActionId.PASTE_STACK_TRACE_ELEMENT);
			boolean enable = true;
			try
			{
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				clipboard = toolkit.getSystemClipboard();
			}
			catch(AWTError | HeadlessException | SecurityException ex)
			{
				enable = false;
			}
			setEnabled(enable);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(clipboard == null)
			{
				return;
			}
			try
			{
				Transferable transferable = clipboard.getContents(null /*unused*/);
				if(transferable == null)
				{
					return;
				}
				DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
				if(logger.isDebugEnabled()) logger.debug("DataFlavors on clipboard: {}", (Object)dataFlavors);
				DataFlavor bestTextFlavor = DataFlavor.selectBestTextFlavor(dataFlavors);
				if(logger.isDebugEnabled()) logger.debug("bestTextFlavor from clipboard: {}", bestTextFlavor);
				if(bestTextFlavor == null)
				{
					// no text on clipboard
					return;
				}

				try(BufferedReader reader = new BufferedReader(bestTextFlavor.getReaderForText(transferable)))
				{
					reader.lines()
							.map(CallLocationCondition::parseStackTraceElement)
							.filter(Objects::nonNull)
							.findFirst()
							.ifPresent(mainFrame::goToSource);
				}
			}
			catch(Throwable ex)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while obtaining StackTraceElement from clipboard!", ex);
			}
		}
	}

	private static class CopyToClipboardAction
		extends AbstractAction
	{
		private static final long serialVersionUID = 7832452126107208925L;

		private final Logger logger = LoggerFactory.getLogger(CopyToClipboardAction.class);

		private ClipboardFormatter clipboardFormatter;
		private transient EventWrapper wrapper;

		CopyToClipboardAction(ClipboardFormatter clipboardFormatter)
		{
			setClipboardFormatter(clipboardFormatter);
			setEventWrapper(null);
		}

		ClipboardFormatter getClipboardFormatter()
		{
			return clipboardFormatter;
		}

		final void setClipboardFormatter(ClipboardFormatter clipboardFormatter)
		{
			if(clipboardFormatter == null)
			{
				throw new IllegalArgumentException("clipboardFormatter must not be null!");
			}
			this.clipboardFormatter = clipboardFormatter;
			putValue(Action.NAME, clipboardFormatter.getName());
			putValue(Action.SHORT_DESCRIPTION, clipboardFormatter.getDescription());
			putValue(Action.MNEMONIC_KEY, clipboardFormatter.getMnemonic());
			String acc = clipboardFormatter.getAccelerator();
			if(acc != null)
			{
				KeyStroke accelerator= KeyStrokes.resolveAcceleratorKeyStroke(acc);
				if(logger.isDebugEnabled()) logger.debug("accelerator for '{}': {}", acc, accelerator);

				if(accelerator != null)
				{
					putValue(Action.ACCELERATOR_KEY, accelerator);
				}
				else
				{
					if(logger.isWarnEnabled()) logger.warn("'{}' did not represent a valid KeyStroke!", acc);
				}
			}
		}

		public void setEventWrapper(EventWrapper wrapper)
		{
			if(clipboardFormatter == null)
			{
				throw new IllegalStateException("clipboardFormatter must not be null!");
			}

			setEnabled(clipboardFormatter.isCompatible(wrapper));
			this.wrapper = wrapper;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(clipboardFormatter == null)
			{
				throw new IllegalStateException("clipboardFormatter must not be null!");
			}
			String text = clipboardFormatter.toString(this.wrapper);
			if(text != null)
			{
				MainFrame.copyText(text);
			}
		}
	}

	private class ShowUnfilteredEventAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = 1802581771634584229L;

		ShowUnfilteredEventAction()
		{
			super(LilithActionId.SHOW_UNFILTERED_EVENT);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			showUnfilteredEvent();
		}

	}

	private class GoToSourceAction
			extends AbstractLilithAction
	{
		private static final long serialVersionUID = -877335742021967857L;

		private StackTraceElement stackTraceElement;

		GoToSourceAction()
		{
			super(LilithActionId.GO_TO_SOURCE);
			setEventWrapper(null);
		}

		public void setEventWrapper(EventWrapper wrapper)
		{
			if(wrapper == null)
			{
				setExtendedStackTraceElement(null);
				return;
			}
			Serializable event = wrapper.getEvent();
			if(event instanceof LoggingEvent)
			{
				LoggingEvent loggingEvent = (LoggingEvent) event;
				ExtendedStackTraceElement[] callStack = loggingEvent.getCallStack();
				if(callStack != null && callStack.length > 0)
				{
					setExtendedStackTraceElement(callStack[0]);
					return;
				}
			}
			setExtendedStackTraceElement(null);
		}

		private void setExtendedStackTraceElement(ExtendedStackTraceElement extendedStackTraceElement)
		{
			if(extendedStackTraceElement == null)
			{
				this.stackTraceElement = null;
			}
			else
			{
				this.stackTraceElement = extendedStackTraceElement.getStackTraceElement();
			}
			setEnabled(this.stackTraceElement != null);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			mainFrame.goToSource(stackTraceElement);
		}
	}

	private static class ShowHideAction
		extends AbstractAction
	{
		private static final long serialVersionUID = 7775753128032553866L;
		private boolean visible;
		private final String columnName;
		private final PersistentTableColumnModel tableColumnModel;

		ShowHideAction(PersistentTableColumnModel tableColumnModel, String columnName, boolean visible)
		{
			super(columnName);
			this.columnName = columnName;
			this.visible = visible;
			this.tableColumnModel = tableColumnModel;
			//putValue(ViewActions.SELECTED_KEY, visible);
			// selection must be set manually
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			visible = !visible;
			Iterator<TableColumn> iter = tableColumnModel.getColumns(false);
			TableColumn found = null;
			while(iter.hasNext())
			{
				TableColumn current = iter.next();
				if(columnName.equals(current.getIdentifier()))
				{
					found = current;
					break;
				}
			}
			if(found != null)
			{
				tableColumnModel.setColumnVisible(found, visible);
			}
		}
	}

	private static class CopyToClipboardByNameComparator
		implements Comparator<CopyToClipboardAction>
	{
		static final CopyToClipboardByNameComparator INSTANCE = new CopyToClipboardByNameComparator();

		@Override
		public int compare(CopyToClipboardAction o1, CopyToClipboardAction o2)
		{
			if(o1 == o2) // NOPMD
			{
				return 0;
			}
			if(o1 == null)
			{
				return -1;
			}
			if(o2 == null)
			{
				return 1;
			}
			ClipboardFormatter f1 = o1.getClipboardFormatter();
			ClipboardFormatter f2 = o2.getClipboardFormatter();
			if(f1 == f2) // NOPMD
			{
				return 0;
			}
			if(f1 == null)
			{
				return -1;
			}
			if(f2 == null)
			{
				return 1;
			}
			String n1 = f1.getName();
			String n2 = f2.getName();
			//noinspection StringEquality
			if(n1 == n2) // NOPMD
			{
				return 0;
			}
			if(n1 == null)
			{
				return -1;
			}
			if(n2 == null)
			{
				return 1;
			}

			return n1.compareTo(n2);
		}
	}

	private static class EggListener
			implements KeyEventDispatcher
	{
		private final Logger logger = LoggerFactory.getLogger(EggListener.class);

		private int step = 0;

		@Override
		public boolean dispatchKeyEvent(KeyEvent e)
		{
			if (e.getID() == KeyEvent.KEY_RELEASED)
			{
				if ((this.step == 2 || this.step == 3) && e.getKeyCode() == KeyEvent.VK_DOWN)
				{
					step++;
				}
				else if ((this.step == 4 || this.step == 6) && e.getKeyCode() == KeyEvent.VK_LEFT)
				{
					step++;
				}
				else if ((this.step == 5 || this.step == 7) && e.getKeyCode() == KeyEvent.VK_RIGHT)
				{
					step++;
				}
				else if (this.step == 8 && e.getKeyCode() == KeyEvent.VK_B)
				{
					step++;
				}
				else if (this.step == 9 && e.getKeyCode() == KeyEvent.VK_A)
				{
					step=0;
					try
					{
						MainFrame.openUrl(new URL("http://z0r.de"));
						// I could have used http://z0r.de/1148 - so don't complain.
						if(logger.isInfoEnabled()) logger.info("Yay!");
					}
					catch (MalformedURLException ex)
					{
						if(logger.isWarnEnabled()) logger.warn("lolwut?", ex);
					}
				}
				else if ((this.step == 0 || this.step == 1) && e.getKeyCode() == KeyEvent.VK_UP)
				{
					step++;
				}
				else if (e.getKeyCode() == KeyEvent.VK_UP)
				{
					if(step != 2)
					{
						step=1;
					}
				}
				else
				{
					step = 0;
				}
			}
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private static EventWrapper<LoggingEvent> asLoggingEventWrapper(EventWrapper original)
	{
		if(original == null)
		{
			return null;
		}
		Serializable wrapped = original.getEvent();
		if(wrapped instanceof LoggingEvent)
		{
			return (EventWrapper<LoggingEvent>) original;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static EventWrapper<AccessEvent> asAccessEventWrapper(EventWrapper original)
	{
		if(original == null)
		{
			return null;
		}
		Serializable wrapped = original.getEvent();
		if(wrapped instanceof AccessEvent)
		{
			return (EventWrapper<AccessEvent>) original;
		}
		return null;
	}
}
