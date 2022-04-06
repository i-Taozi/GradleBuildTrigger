package clearvolume.volume.sink.timeshift.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import clearvolume.volume.sink.timeshift.TimeShiftingSink;
import net.miginfocom.swing.MigLayout;

public class TimeShiftingSinkJPanel extends JPanel
{

	private final JSlider mTimeShiftSlider;
	private final JProgressBar mPlayBar;
	private final Thread mGUIUpdateThread;
	private final JLabel mPresentLabel;
	private final JLabel mPastLabel;

	public static final void createJFrame(final TimeShiftingSink pTimeShiftingSink)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final JFrame lJFrame = new JFrame();

					final TimeShiftingSinkJPanel lMultiChannelTimeShiftingSinkPanel = new TimeShiftingSinkJPanel(pTimeShiftingSink);
					lJFrame.getContentPane()
							.add(lMultiChannelTimeShiftingSinkPanel);
					lJFrame.setVisible(true);
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public TimeShiftingSinkJPanel()
	{
		this(null);
	}

	/**
	 * Create the panel.
	 * 
	 * @param pTimeShiftingSink
	 *            time shifting sink
	 */
	public TimeShiftingSinkJPanel(final TimeShiftingSink pTimeShiftingSink)
	{
		setBackground(Color.WHITE);
		setLayout(new MigLayout("",
								"[14.00,grow,fill][grow][grow,fill][grow,fill][grow][grow,fill]",
								"[][26px:26px,center][grow]"));

		final JPanel lPastPresentPanel = new JPanel();
		lPastPresentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		lPastPresentPanel.setBackground(Color.WHITE);
		add(lPastPresentPanel, "cell 0 0 6 1,grow");
		lPastPresentPanel.setLayout(new BorderLayout(0, 0));

		mPastLabel = new JLabel(" " + pTimeShiftingSink.getHardMemoryHorizon()
								+ " time points past");
		lPastPresentPanel.add(mPastLabel, BorderLayout.WEST);

		mPresentLabel = new JLabel("present: timepoint " + pTimeShiftingSink.getNumberOfTimepoints()
									+ "   ");
		lPastPresentPanel.add(mPresentLabel, BorderLayout.EAST);

		final JLayeredPane lJLayeredPane = new JLayeredPane();
		lJLayeredPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		add(lJLayeredPane, "cell 0 1 6 1,grow");
		lJLayeredPane.setLayout(null);

		mPlayBar = new JProgressBar();
		mPlayBar.setBorder(new EmptyBorder(0, 0, 0, 0));
		mPlayBar.setMinimum(0);
		mPlayBar.setMaximum(Integer.MAX_VALUE);
		setPlayBarPlaying();

		mPlayBar.setBounds(6, 2, 605, 24);
		// lPlayBar.setBackground(new Color(0, 0, 0, 0));
		lJLayeredPane.add(mPlayBar);

		mTimeShiftSlider = new JSlider();
		mTimeShiftSlider.setMaximum(Integer.MAX_VALUE);
		mTimeShiftSlider.setValue(Integer.MAX_VALUE);
		mTimeShiftSlider.setBorder(new EmptyBorder(0, 0, 0, 0));
		lJLayeredPane.setLayer(mTimeShiftSlider, 1);
		mTimeShiftSlider.setPaintTrack(false);
		mTimeShiftSlider.setBounds(6, 0, 616, 29);
		if (pTimeShiftingSink != null)
			mTimeShiftSlider.addChangeListener(new ChangeListener()
			{

				@Override
				public void stateChanged(ChangeEvent e)
				{
					final double lNormalizedTimeShift = (Integer.MAX_VALUE - 1.0 * mTimeShiftSlider.getValue()) / Integer.MAX_VALUE;
					pTimeShiftingSink.setTimeShiftNormalized(lNormalizedTimeShift);

				}
			});
		lJLayeredPane.add(mTimeShiftSlider);

		final JPanel lPlayPausePanel = new JPanel();
		lPlayPausePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		lPlayPausePanel.setBackground(Color.WHITE);
		add(lPlayPausePanel, "cell 2 2 2 1,grow");
		lPlayPausePanel.setLayout(new MigLayout("",
												"[grow,center][grow,center][grow,center][grow,center]",
												"[grow,fill]"));

		final String lIconsPath = "/clearvolume/volume/sink/timeshift/gui/icons/";
		final ImageIcon lBeginningIcon = createScaledImageIcon(lIconsPath + "beginning.png");
		final ImageIcon lPauseIcon = createScaledImageIcon(lIconsPath + "pause.png");
		final ImageIcon lPlayIcon = createScaledImageIcon(lIconsPath + "play.png");
		final ImageIcon lEndIcon = createScaledImageIcon(lIconsPath + "end.png");

		final JButton lGoToBeginButton = new JButton(lBeginningIcon);
		lGoToBeginButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		if (pTimeShiftingSink != null)
			lGoToBeginButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setTimeShiftSliderToPast();
					pTimeShiftingSink.setTimeShiftNormalized(1);
				}
			});
		lPlayPausePanel.add(lGoToBeginButton, "cell 0 0");

		final JButton lPauseButton = new JButton(lPauseIcon);
		lPauseButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		if (pTimeShiftingSink != null)
			lPauseButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					setPlayBarPaused();
					pTimeShiftingSink.pause();
				}
			});
		lPlayPausePanel.add(lPauseButton, "cell 1 0");

		final JButton lPlayButton = new JButton(lPlayIcon);
		lPlayButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		if (pTimeShiftingSink != null)
			lPlayButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					setPlayBarPlaying();
					pTimeShiftingSink.play();
				}
			});
		lPlayPausePanel.add(lPlayButton, "cell 2 0");

		final JButton lGoToEndButton = new JButton(lEndIcon);
		lGoToEndButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		if (pTimeShiftingSink != null)
			lGoToEndButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					setTimeShiftSliderToNow();
					pTimeShiftingSink.setTimeShiftNormalized(0);
				}
			});
		lPlayPausePanel.add(lGoToEndButton, "cell 3 0");

		mGUIUpdateThread = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					mPresentLabel.setText("present: timepoint " + pTimeShiftingSink.getNumberOfTimepoints()
											+ "   ");

					try
					{
						Thread.sleep(2000);
					}
					catch (final InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		mGUIUpdateThread.start();

	}

	private void setTimeShiftSliderToNow()
	{
		mTimeShiftSlider.setValue(Integer.MAX_VALUE);
	}

	private void setTimeShiftSliderToPast()
	{
		mTimeShiftSlider.setValue(0);
	}

	private void setPlayBarPaused()
	{
		mPlayBar.setValue(Integer.MAX_VALUE);
	}

	private void setPlayBarPlaying()
	{
		mPlayBar.setValue(Integer.MAX_VALUE - 1);
	}

	protected ImageIcon createScaledImageIcon(String path)
	{
		final int lDownScaling = 16;
		final ImageIcon lCreatedImageIcon = createImageIcon(path);
		final Image lImage = lCreatedImageIcon.getImage()
												.getScaledInstance(	lCreatedImageIcon.getIconWidth() / lDownScaling,
																	lCreatedImageIcon.getIconHeight() / lDownScaling,
																	java.awt.Image.SCALE_SMOOTH);

		final ImageIcon lImageIcon = new ImageIcon(lImage);
		return lImageIcon;
	}

	protected ImageIcon createImageIcon(String path)
	{
		final java.net.URL imgURL = this.getClass().getResource(path);
		if (imgURL != null)
		{
			return new ImageIcon(imgURL, path);
		}
		else
		{
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}
