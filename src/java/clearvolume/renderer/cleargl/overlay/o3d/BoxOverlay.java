package clearvolume.renderer.cleargl.overlay.o3d;

import java.io.IOException;
import java.util.Arrays;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;

import cleargl.ClearGeometryObject;
import cleargl.GLFloatArray;
import cleargl.GLIntArray;
import cleargl.GLMatrix;
import cleargl.GLProgram;
import clearvolume.renderer.DisplayRequestInterface;
import clearvolume.renderer.SingleKeyToggable;
import clearvolume.renderer.cleargl.ClearGLVolumeRenderer;
import clearvolume.renderer.cleargl.overlay.Overlay3D;
import clearvolume.renderer.cleargl.overlay.OverlayBase;

/**
 * BoxOverlay - Nice shader based box and grid 3D overlay.
 *
 * @author Ulrik Guenther (2015), Loic Royer (2015), Martin Weigert (2015)
 *
 */
public class BoxOverlay extends OverlayBase	implements
											Overlay3D,
											SingleKeyToggable
{
	protected GLProgram mBoxGLProgram;
	protected ClearGeometryObject mClearGeometryObject;
	private volatile boolean mHasChanged = true;
	private float[] mClipBox;
	private boolean mAlignToClipBox;
	private int mNumberOfGridLines;
	private String mName;
	private float mWidth;

	public BoxOverlay(	final int pNumberOfGridlines,
						final float width,
						final boolean pAlignToClipBox,

						final String pName)
	{
		super();
		mNumberOfGridLines = pNumberOfGridlines;
		mWidth = width;
		mAlignToClipBox = pAlignToClipBox;
		mName = pName;
	}

	public BoxOverlay()
	{
		this(10, 1.f, true, "box");
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.Overlay#getName()
	 */
	@Override
	public String getName()
	{
		return mName;
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.Overlay3D#hasChanged3D()
	 */
	@Override
	public boolean hasChanged3D()
	{
		return mHasChanged;
	}

	@Override
	public boolean toggle()
	{
		mHasChanged = true;
		return super.toggle();
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.SingleKeyToggable#toggleKeyCode()
	 */
	@Override
	public short toggleKeyCode()
	{
		return KeyEvent.VK_B;
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.SingleKeyToggable#toggleKeyModifierMask()
	 */
	@Override
	public int toggleKeyModifierMask()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.Overlay#init(javax.media.opengl.GL, clearvolume.renderer.DisplayRequestInterface)
	 */
	@Override
	public void init(	GL pGL,
						DisplayRequestInterface pDisplayRequestInterface)
	{

		try
		{
			mBoxGLProgram = GLProgram.buildProgram(	pGL,
													BoxOverlay.class,
													"shaders/box_vert.glsl",
													"shaders/box_frag.glsl");

			mClearGeometryObject = new ClearGeometryObject(	mBoxGLProgram,
															3,
															GL.GL_TRIANGLES);

			mBoxGLProgram.getUniform("NumberOfGridLines")
							.setInt(mNumberOfGridLines);

			mBoxGLProgram.getUniform("MainBoxWidth").setFloat(mWidth);

			final GLFloatArray lVerticesFloatArray = new GLFloatArray(	24,
																		3);

			final float w = 1.0f;

			// Front
			lVerticesFloatArray.add(-w, -w, w);
			lVerticesFloatArray.add(w, -w, w);
			lVerticesFloatArray.add(w, w, w);
			lVerticesFloatArray.add(-w, w, w);
			// Right
			lVerticesFloatArray.add(w, -w, w);
			lVerticesFloatArray.add(w, -w, -w);
			lVerticesFloatArray.add(w, w, -w);
			lVerticesFloatArray.add(w, w, w);
			// Back
			lVerticesFloatArray.add(-w, -w, -w);
			lVerticesFloatArray.add(-w, w, -w);
			lVerticesFloatArray.add(w, w, -w);
			lVerticesFloatArray.add(w, -w, -w);
			// Left
			lVerticesFloatArray.add(-w, -w, w);
			lVerticesFloatArray.add(-w, w, w);
			lVerticesFloatArray.add(-w, w, -w);
			lVerticesFloatArray.add(-w, -w, -w);
			// Bottom
			lVerticesFloatArray.add(-w, -w, w);
			lVerticesFloatArray.add(-w, -w, -w);
			lVerticesFloatArray.add(w, -w, -w);
			lVerticesFloatArray.add(w, -w, w);
			// Top
			lVerticesFloatArray.add(-w, w, w);
			lVerticesFloatArray.add(w, w, w);
			lVerticesFloatArray.add(w, w, -w);
			lVerticesFloatArray.add(-w, w, -w);

			final GLFloatArray lNormalArray = new GLFloatArray(24, 3);

			// Front
			lNormalArray.add(0.0f, 0.0f, 1.0f);
			lNormalArray.add(0.0f, 0.0f, 1.0f);
			lNormalArray.add(0.0f, 0.0f, 1.0f);
			lNormalArray.add(0.0f, 0.0f, 1.0f);
			// Right
			lNormalArray.add(1.0f, 0.0f, 0.0f);
			lNormalArray.add(1.0f, 0.0f, 0.0f);
			lNormalArray.add(1.0f, 0.0f, 0.0f);
			lNormalArray.add(1.0f, 0.0f, 0.0f);
			// Back
			lNormalArray.add(0.0f, 0.0f, -1.0f);
			lNormalArray.add(0.0f, 0.0f, -1.0f);
			lNormalArray.add(0.0f, 0.0f, -1.0f);
			lNormalArray.add(0.0f, 0.0f, -1.0f);
			// Left
			lNormalArray.add(-1.0f, 0.0f, 0.0f);
			lNormalArray.add(-1.0f, 0.0f, 0.0f);
			lNormalArray.add(-1.0f, 0.0f, 0.0f);
			lNormalArray.add(-1.0f, 0.0f, 0.0f);
			// Bottom
			lNormalArray.add(0.0f, -1.0f, 0.0f);
			lNormalArray.add(0.0f, -1.0f, 0.0f);
			lNormalArray.add(0.0f, -1.0f, 0.0f);
			lNormalArray.add(0.0f, -1.0f, 0.0f);
			// Top
			lNormalArray.add(0.0f, 1.0f, 0.0f);
			lNormalArray.add(0.0f, 1.0f, 0.0f);
			lNormalArray.add(0.0f, 1.0f, 0.0f);
			lNormalArray.add(0.0f, 1.0f, 0.0f);

			final GLIntArray lIndexIntArray = new GLIntArray(36, 1);

			lIndexIntArray.add(	0,
								1,
								2,
								0,
								2,
								3,
								4,
								5,
								6,
								4,
								6,
								7,
								8,
								9,
								10,
								8,
								10,
								11,
								12,
								13,
								14,
								12,
								14,
								15,
								16,
								17,
								18,
								16,
								18,
								19,
								20,
								21,
								22,
								20,
								22,
								23);

			final GLFloatArray lTexCoordFloatArray = new GLFloatArray(	24,
																		2);

			lTexCoordFloatArray.add(0.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 0.0f);
			lTexCoordFloatArray.add(1.0f, 1.0f);
			lTexCoordFloatArray.add(0.0f, 1.0f);

			mClearGeometryObject.setVerticesAndCreateBuffer(lVerticesFloatArray.getFloatBuffer());
			mClearGeometryObject.setNormalsAndCreateBuffer(lNormalArray.getFloatBuffer());
			mClearGeometryObject.setTextureCoordsAndCreateBuffer(lTexCoordFloatArray.getFloatBuffer());

			mClearGeometryObject.setIndicesAndCreateBuffer(lIndexIntArray.getIntBuffer());

		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	public void setVertices()
	{
		setVertices(new float[]
		{ -1.f, 1.f, -1.f, 1.f, -1.f, 1.f });

	}

	public void setVertices(final float[] clipbox)
	{

		final GLFloatArray lVerticesFloatArray = new GLFloatArray(	24,
																	3);

		final float x1 = clipbox[0];
		final float x2 = clipbox[1];
		final float y1 = clipbox[2];
		final float y2 = clipbox[3];
		final float z1 = clipbox[4];
		final float z2 = clipbox[5];

		// Front
		lVerticesFloatArray.add(x1, y1, z2);
		lVerticesFloatArray.add(x2, y1, z2);
		lVerticesFloatArray.add(x2, y2, z2);
		lVerticesFloatArray.add(x1, y2, z2);

		// Right
		lVerticesFloatArray.add(x2, y1, z2);
		lVerticesFloatArray.add(x2, y1, z1);
		lVerticesFloatArray.add(x2, y2, z1);
		lVerticesFloatArray.add(x2, y2, z2);

		// Back
		lVerticesFloatArray.add(x1, y1, z1);
		lVerticesFloatArray.add(x1, y2, z1);
		lVerticesFloatArray.add(x2, y2, z1);
		lVerticesFloatArray.add(x2, y1, z1);

		// Left
		lVerticesFloatArray.add(x1, y1, z2);
		lVerticesFloatArray.add(x1, y2, z2);
		lVerticesFloatArray.add(x1, y2, z1);
		lVerticesFloatArray.add(x1, y1, z1);

		// Bottom
		lVerticesFloatArray.add(x1, y1, z2);
		lVerticesFloatArray.add(x1, y1, z1);
		lVerticesFloatArray.add(x2, y1, z1);
		lVerticesFloatArray.add(x2, y1, z2);

		// Top
		lVerticesFloatArray.add(x1, y2, z2);
		lVerticesFloatArray.add(x2, y2, z2);
		lVerticesFloatArray.add(x2, y2, z1);
		lVerticesFloatArray.add(x1, y2, z1);
		mClearGeometryObject.setVerticesAndCreateBuffer(lVerticesFloatArray.getFloatBuffer());
	}

	/* (non-Javadoc)
	 * @see clearvolume.renderer.cleargl.overlay.Overlay3D#render3D(javax.media.opengl.GL, cleargl.GLMatrix, cleargl.GLMatrix)
	 */
	@Override
	public void render3D(	ClearGLVolumeRenderer pClearGLVolumeRenderer,
							GL pGL,
							int pWidth,
							int pHeight,
							GLMatrix pProjectionMatrix,
							GLMatrix pModelViewMatrix)
	{

		if (isDisplayed())
		{
			// if this flag is set, the box should be drawn with the clip box,
			// otherwise the full range -1,1 is used

			if (mAlignToClipBox)
			{
				float[] lClipBox = pClearGLVolumeRenderer.getClipBox();
				if (!Arrays.equals(mClipBox, lClipBox))
					updateClipBox(lClipBox);
			}
			mClearGeometryObject.setModelView(pModelViewMatrix);
			mClearGeometryObject.setProjection(pProjectionMatrix);

			pGL.glDisable(GL.GL_DEPTH_TEST);
			pGL.glEnable(GL.GL_CULL_FACE);
			pGL.glEnable(GL.GL_BLEND);
			pGL.glBlendFunc(GL.GL_ONE, GL.GL_ONE);
			pGL.glBlendEquation(GL2ES3.GL_MAX);
			pGL.glFrontFace(GL.GL_CW);
			mBoxGLProgram.use(pGL);
			mClearGeometryObject.draw();

			mHasChanged = false;
		}
	}

	private void updateClipBox(final float[] clipbox)
	{
		mClipBox = Arrays.copyOf(clipbox, clipbox.length);
		setVertices(mClipBox);
	}
}
