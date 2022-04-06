package clearvolume.volume.sink;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import clearvolume.volume.Volume;
import clearvolume.volume.VolumeManager;

public class AsynchronousVolumeSinkAdapter	implements
											VolumeSinkInterface
{

	private final VolumeSinkInterface mDelegatedVolumeSink;

	private final BlockingQueue<Volume> mVolumeQueue;
	private final long mTimeOut;
	private final TimeUnit mTimeUnit;

	private volatile boolean mStopSignal;
	private volatile boolean mStoppedSignal;

	public AsynchronousVolumeSinkAdapter(	VolumeSinkInterface pDelegatedVolumeSink,
											int pMaxCapacity,
											long pTimeOut,
											TimeUnit pTimeUnit)
	{
		super();
		mDelegatedVolumeSink = pDelegatedVolumeSink;
		mTimeOut = pTimeOut;
		mTimeUnit = pTimeUnit;
		mVolumeQueue = new ArrayBlockingQueue<Volume>(pMaxCapacity);
	}

	@Override
	public void sendVolume(Volume pVolume)
	{
		try
		{
			if (!mVolumeQueue.offer(pVolume, mTimeOut, mTimeUnit))
				pVolume.makeAvailableToManager();
		}
		catch (final InterruptedException e)
		{
			sendVolume(pVolume);
		}
	}

	public boolean start()
	{
		final Runnable lRunnable = new Runnable()
		{

			@Override
			public void run()
			{
				while (!mStopSignal)
				{
					try
					{
						// System.out.println(mVolumeQueue.size());
						final Volume lVolume = mVolumeQueue.take();
						mDelegatedVolumeSink.sendVolume(lVolume);
					}
					catch (final Throwable e)
					{
						e.printStackTrace();
					}
				}
				mStoppedSignal = true;
			}
		};

		final Thread lThread = new Thread(	lRunnable,
											this.getClass()
												.getSimpleName());
		lThread.setDaemon(true);
		lThread.start();
		return true;
	}

	public boolean stop()
	{
		mStopSignal = true;
		return true;
	}

	public boolean waitForStop()
	{
		while (!mStoppedSignal)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public VolumeManager getManager()
	{
		return mDelegatedVolumeSink.getManager();
	}
}
