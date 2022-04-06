package clearvolume.renderer.cleargl;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

import org.apache.commons.lang.SystemUtils;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import cleargl.util.arcball.ArcBall;
import clearvolume.renderer.RenderAlgorithm;

/**
 * Class MouseControl
 * 
 * This class implements interface MouseListener and provides mouse controls for
 * the JoglPBOVolumeRender.
 *
 * @author Loic Royer 2014
 *
 */
class MouseControl extends MouseAdapter implements MouseListener
{

  private final double mMouseWheelFactor =
                                         SystemUtils.IS_OS_WINDOWS ? 10
                                                                   : 1;

  /**
   * Reference of the renderer
   */
  private final ClearGLVolumeRenderer mRenderer;

  /**
   * Previous mouse positions.
   */
  private int mSavedMouseX, mSavedMouseY;

  /**
   * ArcBall class for easier 3D control
   */
  private final ArcBall mArcBall;

  /**
   * True if moving the mouse moves the light
   */
  private volatile boolean mMoveLightMode = false;

  /**
   * @param pJoglVolumeRenderer
   */
  MouseControl(final ClearGLVolumeRenderer pClearVolumeRenderer)
  {
    mRenderer = pClearVolumeRenderer;
    mArcBall = new ArcBall();
    mArcBall.setBounds(mRenderer.getViewportWidth(),
                       mRenderer.getViewportHeight());
  }

  public void toggleMoveLightMode()
  {
    mMoveLightMode = !mMoveLightMode;
  }

  private void setSavedMousePosition(final MouseEvent pMouseEvent)
  {
    mSavedMouseX = pMouseEvent.getX();
    mSavedMouseY = pMouseEvent.getY();
  }

  /**
   * Interface method implementation
   * 
   * @see com.jogamp.newt.event.MouseAdapter#mouseClicked(com.jogamp.newt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(final MouseEvent pMouseEvent)
  {
    if (mRenderer.notifyEyeRayListeners(mRenderer, pMouseEvent))
      return;

    if (pMouseEvent.getClickCount() == 1)
    {
      handleGammaMinMax(pMouseEvent);

    }
    else if (pMouseEvent.getClickCount() == 2)
    {
      mRenderer.toggleFullScreen();
      mRenderer.notifyChangeOfVolumeRenderingParameters();
    }

  }

  /**
   * Interface method implementation
   * 
   * @see com.jogamp.newt.event.MouseAdapter#mouseDragged(com.jogamp.newt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(final MouseEvent pMouseEvent)
  {
    if (mRenderer.notifyEyeRayListeners(mRenderer, pMouseEvent))
      return;

    handleArcBall(pMouseEvent);

    handleMoveLight(pMouseEvent);

    handleTranslation(pMouseEvent);
    handleGammaMinMax(pMouseEvent);
    handleAlphaBlending(pMouseEvent);
    mRenderer.notifyChangeOfVolumeRenderingParameters();

  }

  protected void handleMoveLight(final MouseEvent pMouseEvent)
  {

    if (!mMoveLightMode
        && mRenderer.getRenderAlgorithm(mRenderer.getCurrentRenderLayerIndex()) == RenderAlgorithm.IsoSurface
        && !pMouseEvent.isMetaDown()
        && !pMouseEvent.isShiftDown()
        && !pMouseEvent.isControlDown()
        && pMouseEvent.isAltDown()
        && !pMouseEvent.isAltGraphDown()
        && !pMouseEvent.isButtonDown(1))
    {
      moveLight(pMouseEvent);
    }
  }

  protected void handleArcBall(final MouseEvent pMouseEvent)
  {
    if (!pMouseEvent.isMetaDown() && !pMouseEvent.isShiftDown()
        && !pMouseEvent.isAltDown()
        && !pMouseEvent.isControlDown()
        && pMouseEvent.isButtonDown(1))
    {
      final float lMouseX = pMouseEvent.getX();
      final float lMouseY = pMouseEvent.getY();
      mArcBall.setBounds(mRenderer.getViewportWidth(),
                         mRenderer.getViewportHeight());
      mRenderer.setQuaternion(mArcBall.drag(lMouseX, lMouseY));

    }
  }

  /**
   * Interface method implementation
   * 
   * @see com.jogamp.newt.event.MouseAdapter#mouseMoved(com.jogamp.newt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(final MouseEvent pMouseEvent)
  {
    if (mRenderer.notifyEyeRayListeners(mRenderer, pMouseEvent))
      return;

    if (mMoveLightMode) // &&
      // mRenderer.getAdaptiveLODController().getNumberOfPasses()
      // == 1
      moveLight(pMouseEvent);

    setSavedMousePosition(pMouseEvent);
  }

  /**
   * Interface method implementation
   * 
   * @see com.jogamp.newt.event.MouseAdapter#mouseWheelMoved(com.jogamp.newt.event.MouseEvent)
   */
  @Override
  public void mouseWheelMoved(final MouseEvent pMouseEvent)
  {
    if (mRenderer.notifyEyeRayListeners(mRenderer, pMouseEvent))
      return;

    final float[] lWheelRotation = pMouseEvent.getRotation();

    double lZoomWheelFactor = 0.0125f * mMouseWheelFactor;

    if (pMouseEvent.isMetaDown())
    {
      mRenderer.addFOV(lWheelRotation[1] * lZoomWheelFactor);
    }
    else
    {
      lZoomWheelFactor /= mRenderer.getFOV();
      mRenderer.addTranslationZ(lWheelRotation[1] * lZoomWheelFactor);
    }

    setSavedMousePosition(pMouseEvent);

    mRenderer.notifyChangeOfVolumeRenderingParameters();

  }

  @Override
  public void mousePressed(MouseEvent pMouseEvent)
  {
    if (mRenderer.notifyEyeRayListeners(mRenderer, pMouseEvent))
      return;

    if (!pMouseEvent.isMetaDown() && !pMouseEvent.isShiftDown()
        && !pMouseEvent.isControlDown()
        && pMouseEvent.isButtonDown(1))
    {
      final float lMouseX = pMouseEvent.getX();
      final float lMouseY = pMouseEvent.getY();
      mArcBall.setBounds(mRenderer.getViewportWidth(),
                         mRenderer.getViewportHeight());
      mArcBall.setCurrent(mRenderer.getQuaternion());
      mArcBall.click(lMouseX, lMouseY);
    }

    mRenderer.getAdaptiveLODController()
             .notifyUserInteractionInProgress();

  }

  @Override
  public void mouseReleased(MouseEvent pMouseEvent)
  {
    if (mRenderer.notifyEyeRayListeners(mRenderer, pMouseEvent))
      return;

    mRenderer.getAdaptiveLODController().notifyUserInteractionEnded();
    super.mouseReleased(pMouseEvent);

  }

  private void moveLight(final MouseEvent pMouseEvent)
  {
    final float lMouseX = pMouseEvent.getX();
    final float lMouseY = pMouseEvent.getY();
    final float light[] = new float[3];
    mArcBall.mapToSphere(lMouseX, lMouseY, light);
    mRenderer.setLightVector(light);
  }

  private void handleTranslation(final MouseEvent pMouseEvent)
  {
    final int dx = pMouseEvent.getX() - mSavedMouseX;
    final int dy = pMouseEvent.getY() - mSavedMouseY;

    // If the right button is held down, translate the object
    if (!pMouseEvent.isMetaDown() && !pMouseEvent.isControlDown()
        && (pMouseEvent.isButtonDown(3)))
    {

      mRenderer.addTranslationX(dx / 100.0f);
      mRenderer.addTranslationY(-dy / 100.0f);
    }
    setSavedMousePosition(pMouseEvent);
  }

  /**
   * Sets the transfer function range.
   * 
   * @param pMouseEvent
   *          mouse event
   */
  public void handleGammaMinMax(final MouseEvent pMouseEvent)
  {
    if (!pMouseEvent.isMetaDown() && !pMouseEvent.isShiftDown()
        && pMouseEvent.isControlDown()
        && pMouseEvent.isButtonDown(1))
    {

      final double lWidth = mRenderer.getViewportWidth();
      final double lHeight = mRenderer.getViewportHeight();

      final double nx = (pMouseEvent.getX()) / lWidth;
      final double ny = (lHeight - pMouseEvent.getY()) / lHeight;

      mRenderer.setTransferFunctionRange(Math.abs(Math.pow(nx, 3)),
                                         Math.abs(Math.pow(ny, 3)));

    }

    if (!pMouseEvent.isMetaDown() && pMouseEvent.isShiftDown()
        && !pMouseEvent.isControlDown()
        && pMouseEvent.isButtonDown(1))
    {
      final double lWidth = mRenderer.getViewportWidth();
      final double nx = (pMouseEvent.getX()) / lWidth;

      mRenderer.setGamma(Math.tan(Math.PI * nx / 2));

    }

    if (!pMouseEvent.isMetaDown() && pMouseEvent.isShiftDown()
        && pMouseEvent.isControlDown()
        && pMouseEvent.isButtonDown(1))
    {
      final double lWidth = mRenderer.getViewportWidth();
      final double nx = (pMouseEvent.getX()) / lWidth;

      mRenderer.setBrightness(Math.tan(Math.PI * nx / 2));

    }

    if (pMouseEvent.isMetaDown() && pMouseEvent.isButtonDown(1))
    {
      final double lWidth = mRenderer.getViewportWidth();
      double nx = (pMouseEvent.getX()) / lWidth;

      nx = (max(min(nx, 1), 0));
      nx = nx * nx;

      mRenderer.setQuality(mRenderer.getCurrentRenderLayerIndex(),
                           nx);
    }

    /*
     * System.out.println("isAltDown" + pMouseEvent.isAltDown());
     * System.out.println("isAltGraphDown" + pMouseEvent.isAltGraphDown());
     * System.out.println("isControlDown" + pMouseEvent.isControlDown());
     * System.out.println("isMetaDown" + pMouseEvent.isMetaDown());
     * System.out.println("isShiftDown" + pMouseEvent.isShiftDown());/*
     */

  }

  /**
   * Sets the transfer function range.
   * 
   * @param pMouseEvent
   *          mouse event
   */
  public void handleAlphaBlending(final MouseEvent pMouseEvent)
  {
    if (mRenderer.getRenderAlgorithm(mRenderer.getCurrentRenderLayerIndex()) == RenderAlgorithm.MaxProjection
        && !pMouseEvent.isMetaDown()
        && !pMouseEvent.isShiftDown()
        && !pMouseEvent.isControlDown()
        && pMouseEvent.isAltDown()
        && !pMouseEvent.isAltGraphDown()
        && pMouseEvent.isButtonDown(1))
    {
      final double lWidth = mRenderer.getViewportWidth();
      final double nx = (pMouseEvent.getX()) / lWidth;

      final double lAlpha = 16 * pow(nx,2);

      mRenderer.setAlphaBlending(mRenderer.getCurrentRenderLayerIndex(),
                                 lAlpha);
      // System.out.println(lAlpha);
    }

    /*
    System.out.println("_____________________________________");
    System.out.println("isAltDown=" + pMouseEvent.isAltDown());
    System.out.println("isAltGraphDown="
                       + pMouseEvent.isAltGraphDown());
    System.out.println("isControlDown="
                       + pMouseEvent.isControlDown());
    System.out.println("isMetaDown=" + pMouseEvent.isMetaDown());
    System.out.println("isShiftDown=" + pMouseEvent.isShiftDown());
    /**/

  }

}