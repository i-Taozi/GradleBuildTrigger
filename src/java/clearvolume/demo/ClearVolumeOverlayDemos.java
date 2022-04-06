package clearvolume.demo;

import static java.lang.Math.random;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.junit.Test;

import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.cleargl.overlay.o3d.CursorOverlay;
import clearvolume.renderer.cleargl.overlay.o3d.PathOverlay;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;
import coremem.enums.NativeTypeEnum;

public class ClearVolumeOverlayDemos
{

	public static void main(final String[] argv) throws ClassNotFoundException
	{
		if (argv.length == 0)
		{
			final Class<?> c = Class.forName("clearvolume.demo.ClearVolumeDemo");

			System.out.println("Give one of the following method names as parameter:");

			for (final Member m : c.getMethods())
			{
				final String name = ((Method) m).getName();

				if (name.substring(0, 4).equals("demo"))
				{
					System.out.println("Demo: " + ((Method) m).getName());
				}
			}
		}
		else
		{
			final ClearVolumeOverlayDemos cvdemo = new ClearVolumeOverlayDemos();
			Method m;

			try
			{
				m = cvdemo.getClass().getMethod(argv[0]);
			}
			catch (final Exception e)
			{
				System.out.println("Could not launch " + argv[0]
									+ " because ...");
				e.printStackTrace();

				return;
			}

			try
			{
				System.out.println("Running " + argv[0] + "()...");
				m.invoke(cvdemo);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	@Test
	public void demoPathOverlay3D()	throws InterruptedException,
									IOException
	{
		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																												1024,
																												1024,
																												NativeTypeEnum.UnsignedByte,
																												512,
																												512,
																												1,
																												false);

		final PathOverlay lPathOverlay = new PathOverlay();
		lClearVolumeRenderer.addOverlay(lPathOverlay);

		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
													* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
										* y
										+ lResolutionX
										* lResolutionY
										* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					lVolumeDataArray[lIndex] = (byte) lCharValue;
				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
													ByteBuffer.wrap(lVolumeDataArray),
													lResolutionX,
													lResolutionY,
													lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		float x = 0, y = 0, z = 0;
		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
			lPathOverlay.addPathPoint(x, y, z);

			x += 0.01 * (random() - 0.5);
			y += 0.01 * (random() - 0.5);
			z += 0.01 * (random() - 0.5);

			lClearVolumeRenderer.requestDisplay();
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoCursor() throws InterruptedException, IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																												512,
																												512,
																												NativeTypeEnum.UnsignedByte,
																												512,
																												512,
																												1,
																												false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final CursorOverlay lCursorOverlay1 = new CursorOverlay("1");
		lCursorOverlay1.setColor(1f, 0.8f, 0.8f, 1f);
		lCursorOverlay1.setPosition(0.25f, 0.25f, 0.25f);
		lCursorOverlay1.setLineLength(0.01f);
		lCursorOverlay1.setBoxLinesAlpha(0.5f);
		lClearVolumeRenderer.addEyeRayListener(lCursorOverlay1);
		lClearVolumeRenderer.addOverlay(lCursorOverlay1);

		final CursorOverlay lCursorOverlay2 = new CursorOverlay("2");
		lCursorOverlay2.setColor(0.8f, 0.8f, 1f, 1f);
		lCursorOverlay2.setPosition(0.75f, 0.75f, 0.75f);
		lCursorOverlay2.setBoxLinesAlpha(0.1f);
		lClearVolumeRenderer.addEyeRayListener(lCursorOverlay2);
		lClearVolumeRenderer.addOverlay(lCursorOverlay2);

		final int lResolutionX = 512;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
													* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
										* y
										+ lResolutionX
										* lResolutionY
										* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z) / 2);
					if (lCharValue < 12)
						lCharValue = 0;
					lVolumeDataArray[lIndex] = (byte) lCharValue;
				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
													ByteBuffer.wrap(lVolumeDataArray),
													lResolutionX,
													lResolutionY,
													lResolutionZ);

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
		}

		lClearVolumeRenderer.close();
	}

}
