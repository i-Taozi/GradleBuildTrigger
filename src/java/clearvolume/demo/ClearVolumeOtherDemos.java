package clearvolume.demo;

import static java.lang.Math.abs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseEvent;

import clearvolume.controller.ExternalRotationController;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.RenderAlgorithm;
import clearvolume.renderer.cleargl.ClearGLVolumeRenderer;
import clearvolume.renderer.cleargl.utils.ScreenToEyeRay.EyeRay;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.renderer.listeners.EyeRayListener;
import clearvolume.renderer.listeners.VolumeCaptureListener;
import clearvolume.renderer.opencl.OpenCLVolumeRenderer;
import clearvolume.transferf.TransferFunctions;
import coremem.enums.NativeTypeEnum;

public class ClearVolumeOtherDemos
{

	public static void main(final String[] argv) throws ClassNotFoundException
	{
		if (argv.length == 0)
		{
			final Class<?> c = Class.forName("clearvolume.demo.ClearVolumeOtherDemos");

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
			final ClearVolumeOtherDemos cvdemo = new ClearVolumeOtherDemos();
			Method m;

			try
			{
				m = cvdemo.getClass().getMethod(argv[0]);
			}
			catch (final Exception e)
			{
				System.out.println("Could not launch "	+ argv[0]
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
	public void demoRendererInJFrame()	throws InterruptedException,
																			IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																				512,
																																																				512,
																																																				NativeTypeEnum.UnsignedByte,
																																																				512,
																																																				512,
																																																				1,
																																																				true);
		final NewtCanvasAWT lNewtCanvasAWT = lClearVolumeRenderer.getNewtCanvasAWT();

		final JFrame lJFrame = new JFrame("ClearVolume");
		lJFrame.setLayout(new BorderLayout());
		final Container lContainer = new Container();
		lContainer.setLayout(new BorderLayout());
		lContainer.add(lNewtCanvasAWT, BorderLayout.CENTER);
		lJFrame.setSize(new Dimension(1024, 1024));
		lJFrame.add(lContainer);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				lJFrame.setVisible(true);
			}
		});

		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 512;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;
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

		while (lClearVolumeRenderer.isShowing() && lJFrame.isVisible())
		{
			Thread.sleep(100);
			lJFrame.setTitle("BRAVO! THIS IS A JFRAME! It WORKS!");
		}

		lClearVolumeRenderer.close();
		lJFrame.dispose();

	}

	@Test
	public void demoDitheringAndResolutionOpenCL()	throws InterruptedException,
																									IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																				512,
																																																				512,
																																																				NativeTypeEnum.UnsignedByte,
																																																				512,
																																																				512,
																																																				1,
																																																				false);

		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 512;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;

					final int lCenter = lResolutionZ / 2;

					final int lDistance = (x - lCenter) * (x - lCenter)
																	+ (y - lCenter) * (y - lCenter)
																+ (z - lCenter) * (z - lCenter);

					lVolumeDataArray[lIndex] = (byte) (255
																							* Math.exp(-.001
																													* abs(lDistance
																																- lCenter
																																		* lCenter
																																	/ 5)));

					/*
					 * mVolumeDataArray[lIndex] = (byte) (255 * (1.0 / (1.0 +
					 * 0.5 * abs(z - lResolutionZ / 2))));/*
					 */
				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							ByteBuffer.wrap(lVolumeDataArray),
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoAspectRatio()	throws InterruptedException,
																IOException
	{
		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														new OpenCLVolumeRenderer(	"ClearVolumeTest",
																																											512,
																																											512,
																																											NativeTypeEnum.UnsignedByte,
																																											512,
																																											512,
																																											1,
																																											false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 128;
		final int lResolutionY = 128;
		final int lResolutionZ = 128;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;
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

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoAspectRatioPreset()	throws InterruptedException,
																			IOException
	{
		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														new OpenCLVolumeRenderer(	"ClearVolumeTest",
																																											512,
																																											512,
																																											NativeTypeEnum.UnsignedByte,
																																											512,
																																											512,
																																											1,
																																											false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 400;
		final int lResolutionY = 100;
		final int lResolutionZ = 200;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;
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

		lClearVolumeRenderer.setVoxelSize(0,
																			lResolutionX * 5.0,
																			lResolutionY * 4.0,
																			lResolutionZ * 3.0);
		lClearVolumeRenderer.requestDisplay();

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoBoundingBox()	throws InterruptedException,
																IOException
	{
		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																				512,
																																																				512,
																																																				NativeTypeEnum.UnsignedByte,
																																																				512,
																																																				512,
																																																				1,
																																																				false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 200;
		final int lResolutionY = 210;
		final int lResolutionZ = 220;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		final int x0 = lResolutionX / 2, y0 = lResolutionY / 2,
				z0 = lResolutionZ / 2;

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;
					lVolumeDataArray[lIndex] = (byte) (255
																							* Math.exp(-.0001
																													* (5	* (x
																																	- x0)
																															* (x - x0)
																																+ (y - y0)
																																* (y - y0)
																															+ (z - z0)
																																* (z - z0))));

				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							ByteBuffer.wrap(lVolumeDataArray),
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);

		lClearVolumeRenderer.requestDisplay();

		float x = 0, y = 1, z = 0;
		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(100);

			lClearVolumeRenderer.setClipBox(new float[]
			{ -x, x, -y, y, -z, z });

			x += 0.01;
			y -= 0.02;
			z += 0.04;

			if (x > 1)
				x = 0;
			if (y < 0)
				y = 1;
			if (z > 1)
				z = 0;

		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoWithGeneratedDatasetWithEgg3D()	throws InterruptedException,
																									IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer8Bit(	"ClearVolumeTest",
																																																						512,
																																																						512,
																																																						false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);
		lClearVolumeRenderer.setRenderAlgorithm(RenderAlgorithm.MaxProjection);

		ExternalRotationController lEgg3DController = null;
		try
		{
			lEgg3DController = new ExternalRotationController(ExternalRotationController.cDefaultEgg3DTCPport,
																												lClearVolumeRenderer);
			lClearVolumeRenderer.addRotationController(lEgg3DController);
			lEgg3DController.connectAsynchronouslyOrWait();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		final int lResolutionX = 128;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;
					lVolumeDataArray[lIndex] = (byte) (x ^ y ^ z);
				}

		final ByteBuffer lWrappedArray = ByteBuffer.wrap(lVolumeDataArray);
		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							lWrappedArray,
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);

		lClearVolumeRenderer.requestDisplay();

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(100);
		}

		lClearVolumeRenderer.close();

		if (lEgg3DController != null)
			lEgg3DController.close();

	}

	@Test
	public void demoCaptureVolumeData()	throws InterruptedException,
																			IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																				512,
																																																				512,
																																																				NativeTypeEnum.UnsignedByte,
																																																				512,
																																																				512,
																																																				1,
																																																				false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		lClearVolumeRenderer.addVolumeCaptureListener(new VolumeCaptureListener()
		{

			@Override
			public void capturedVolume(	final ByteBuffer pCaptureBuffers,
																	final NativeTypeEnum pNativeTypeEnum,
																	final long pVolumeWidth,
																	final long pVolumeHeight,
																	final long pVolumeDepth,
																	final double pVoxelWidth,
																	final double pVoxelHeight,
																	final double pVoxelDepth)
			{
				System.out.format("Captured volume type=%s (%d, %d, %d) (%g, %g, %g) \n",
													pNativeTypeEnum,
													pVolumeWidth,
													pVolumeHeight,
													pVolumeDepth,
													pVoxelWidth,
													pVoxelHeight,
													pVoxelDepth);

			}
		});

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		lClearVolumeRenderer.requestDisplay();

		for (int i = 0; i < 20 && lClearVolumeRenderer.isShowing(); i++)
		{
			Thread.sleep(500);

			for (int z = 0; z < lResolutionZ; z++)
				for (int y = 0; y < lResolutionY; y++)
					for (int x = 0; x < lResolutionX; x++)
					{
						final int lIndex = x	+ lResolutionX * y
																+ lResolutionX * lResolutionY * z;
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
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoEyeRayListener()	throws InterruptedException,
																		IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																				512,
																																																				512,
																																																				NativeTypeEnum.UnsignedByte,
																																																				512,
																																																				512,
																																																				1,
																																																				false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 512;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;
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

		lClearVolumeRenderer.addEyeRayListener(new EyeRayListener()
		{

			@Override
			public boolean notifyEyeRay(ClearGLVolumeRenderer pRenderer,
																	MouseEvent pMouseEvent,
																	EyeRay pEyeRay)
			{
				if (pMouseEvent.getButton() != 2
						&& pMouseEvent.getEventType() != MouseEvent.EVENT_MOUSE_CLICKED)
					return false;

				final int lX = pMouseEvent.getX();
				final int lY = pMouseEvent.getY();

				System.out.format("%d %d \n", lX, lY);

				System.out.println(pMouseEvent);
				System.out.println(pEyeRay);

				float x = pEyeRay.org[0];
				float y = pEyeRay.org[1];
				float z = pEyeRay.org[2];

				boolean lOnceIn = false;
				final float lStepSize = 0.001f;
				for (float i = 0; i < 100000 * lStepSize; i += lStepSize)
				{
					x += lStepSize * pEyeRay.dir[0];
					y += lStepSize * pEyeRay.dir[1];
					z += lStepSize * pEyeRay.dir[2];

					final int ix = (int) (lResolutionX * x);
					final int iy = (int) (lResolutionY * y);
					final int iz = (int) (lResolutionZ * z);

					if (ix < 0	|| ix >= lResolutionX
							|| iy < 0
							|| iy >= lResolutionY
							|| iz < 0
							|| iz >= lResolutionZ)
						if (lOnceIn)
							break;
						else
							continue;

					lOnceIn = true;
					final int lIndex = ix	+ lResolutionX * iy
															+ lResolutionX * lResolutionY * iz;

					lVolumeDataArray[lIndex] = (byte) 200;

				}

				lClearVolumeRenderer.setVolumeDataBuffer(	0,
																									ByteBuffer.wrap(lVolumeDataArray),
																									lResolutionX,
																									lResolutionY,
																									lResolutionZ);
				return false;

			}
		});

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
		}

		lClearVolumeRenderer.close();
	}

	@Test
	public void demoWithAlphaBlending()	throws InterruptedException,
																			IOException
	{

		final ClearVolumeRendererInterface lClearVolumeRenderer =
																														ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																				1024,
																																																				1024,
																																																				NativeTypeEnum.UnsignedByte,
																																																				1024,
																																																				1024,
																																																				1,
																																																				false);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
		lClearVolumeRenderer.setVisible(true);

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray =
																	new byte[lResolutionX	* lResolutionY
																						* lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x	+ lResolutionX * y
															+ lResolutionX * lResolutionY * z;

					double dx = .3 * lResolutionX;

					double r1 = Math.sqrt(
																(x - lResolutionX / 2 - dx)
																* (x - lResolutionX / 2 - dx)
																	+ (y - lResolutionY / 2)
																	* (y - lResolutionY / 2)
																+ (z - lResolutionZ / 2)
																	* (z - lResolutionZ / 2));
					double r2 = Math.sqrt(
																(x - lResolutionX / 2 + dx)
																* (x - lResolutionX / 2 + dx)
																	+ (y - lResolutionY / 2)
																	* (y - lResolutionY / 2)
																+ (z - lResolutionZ / 2)
																	* (z - lResolutionZ / 2));

					r1 /= .5f * lResolutionX;
					r2 /= .5f * lResolutionX;

					double r0 = .5;

					double res = (0
													+ 200 * Math.exp(-100 * (r1 - r0) * (r1 - r0))
												+ 200
													* Math.exp(-100 * (r2 - r0) * (r2 - r0)));

          if (res < 50)
            res = 0;

					lVolumeDataArray[lIndex] = (byte) (res);

				}

		lClearVolumeRenderer.setVolumeDataBuffer(	0,
																							ByteBuffer.wrap(lVolumeDataArray),
																							lResolutionX,
																							lResolutionY,
																							lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		while (lClearVolumeRenderer.isShowing())
		{
			Thread.sleep(500);
		}

		lClearVolumeRenderer.close();
	}
	
	
	@Test
  public void demoClipping()  throws InterruptedException,
                                    IOException
  {

    final ClearVolumeRendererInterface lClearVolumeRenderer =
                                                            ClearVolumeRendererFactory.newBestRenderer( "ClearVolumeTest",
                                                                                                        1024,
                                                                                                        1024,
                                                                                                        NativeTypeEnum.UnsignedByte,
                                                                                                        1024,
                                                                                                        1024,
                                                                                                        1,
                                                                                                        false);
    lClearVolumeRenderer.setTransferFunction(TransferFunctions.getDefault());
    lClearVolumeRenderer.setVisible(true);
    lClearVolumeRenderer.setClipBox(-1.0f,1.0f,-1.0f,1.0f,-1.0f,1f);
    lClearVolumeRenderer.setViewClipping(false);

    final int lResolutionX = 256;
    final int lResolutionY = lResolutionX;
    final int lResolutionZ = lResolutionX;

    final byte[] lVolumeDataArray =
                                  new byte[lResolutionX * lResolutionY
                                            * lResolutionZ];

    for (int z = 0; z < lResolutionZ; z++)
      for (int y = 0; y < lResolutionY; y++)
        for (int x = 0; x < lResolutionX; x++)
        {
          final int lIndex = x  + lResolutionX * y
                              + lResolutionX * lResolutionY * z;
          double r = Math.sqrt((x - lResolutionX / 2)
                                * (x - lResolutionX / 2)
                                  + (y - lResolutionY / 2)
                                  * (y - lResolutionY / 2)
                                + (z - lResolutionZ / 2)
                                  * (z - lResolutionZ / 2));

          r /= .5f * lResolutionX;

          double r0 = .98;

          double res = 240 * Math.exp(-100 * (r - r0) * (r - r0));
          //res = z > lResolutionZ / 2 ? 0.f : res;

          lVolumeDataArray[lIndex] = (byte) (res);

        }

    lClearVolumeRenderer.setVolumeDataBuffer( 0,
                                              ByteBuffer.wrap(lVolumeDataArray),
                                              lResolutionX,
                                              lResolutionY,
                                              lResolutionZ);
    lClearVolumeRenderer.requestDisplay();

    while (lClearVolumeRenderer.isShowing())
    {
      Thread.sleep(500);
    }

    lClearVolumeRenderer.close();
  }

}
