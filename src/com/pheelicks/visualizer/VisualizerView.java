package com.pheelicks.visualizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import com.pheelicks.visualizer.renderer.BarGraphRenderer;
import com.pheelicks.visualizer.renderer.CircleRenderer;
import com.pheelicks.visualizer.renderer.LineRenderer;

/**
 * A class that draws visualizations of data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture } and
 * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
 */
public class VisualizerView extends View {
  private static final String TAG = "VisualizerView";

  private byte[] mBytes;
  private byte[] mFFTBytes;
  private Rect mRect = new Rect();
  private Visualizer mVisualizer;

  private Paint mFlashPaint = new Paint();
  private Paint mFadePaint = new Paint();

  public VisualizerView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs);
    init();
  }

  public VisualizerView(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public VisualizerView(Context context)
  {
    this(context, null, 0);
  }

  private void init() {
    mBytes = null;
    mFFTBytes = null;

    mFlashPaint.setColor(Color.argb(122, 255, 255, 255));
    mFadePaint.setColor(Color.argb(238, 255, 255, 255)); // Adjust alpha to change how quickly the image fades
    mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
  }

  /**
   * Links the visualizer to a player
   * @param player - MediaPlayer instance to link to
   */
  public void link(MediaPlayer player)
  {
    if(player == null)
    {
      throw new NullPointerException("Cannot link to null MediaPlayer");
    }

    // Create the Visualizer object and attach it to our media player.
    mVisualizer = new Visualizer(player.getAudioSessionId());
    mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

    // Pass through Visualizer data to VisualizerView
    Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener()
    {
      @Override
      public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
          int samplingRate)
      {
        updateVisualizer(bytes);
      }

      @Override
      public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
          int samplingRate)
      {
        updateVisualizerFFT(bytes);
      }
    };

    mVisualizer.setDataCaptureListener(captureListener,
        Visualizer.getMaxCaptureRate() / 2, true, true);

    // Enabled Visualizer and disable when we're done with the stream
    mVisualizer.setEnabled(true);
    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
    {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer)
      {
        mVisualizer.setEnabled(false);
      }
    });
  }

  /**
   * Call to release the resources used by VisualizerView. Like with the
   * MediaPlayer it is good practice to call this method
   */
  public void release()
  {
    mVisualizer.release();
  }


  /**
   * Pass data to the visualizer. Typically this will be obtained from the
   * Android Visualizer.OnDataCaptureListener call back. See
   * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
   * @param bytes
   */
  public void updateVisualizer(byte[] bytes) {
    mBytes = bytes;
    invalidate();
  }

  /**
   * Pass FFT data to the visualizer. Typically this will be obtained from the
   * Android Visualizer.OnDataCaptureListener call back. See
   * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
   * @param bytes
   */
  public void updateVisualizerFFT(byte[] bytes) {
    mFFTBytes = bytes;
    invalidate();
  }

  boolean mFlash = false;

  /**
   * Call this to make the visualizer flash. Useful for flashing at the start
   * of a song/loop etc...
   */
  public void flash() {
    mFlash = true;
    invalidate();
  }

  Bitmap mCanvasBitmap;
  Canvas mCanvas;

  BarGraphRenderer mBarGraphRendererTop;
  BarGraphRenderer mBarGraphRendererBottom;
  CircleRenderer mCircleRenderer;
  LineRenderer mLineRenderer;

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Create canvas & renderers once we're ready to draw
    mRect.set(0, 0, getWidth(), getHeight());

    if(mCanvasBitmap == null)
    {
      mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
    }
    if(mCanvas == null)
    {
      mCanvas = new Canvas(mCanvasBitmap);

      // Now that we have a Canvas, can create Renderers
      Paint paint = new Paint();
      paint.setStrokeWidth(50f);
      paint.setAntiAlias(true);
      paint.setColor(Color.argb(200, 233, 0, 44));
      mBarGraphRendererBottom = new BarGraphRenderer(mCanvas, 16, paint, false);

      Paint paint2 = new Paint();
      paint2.setStrokeWidth(12f);
      paint2.setAntiAlias(true);
      paint2.setColor(Color.argb(200, 11, 111, 233));
      mBarGraphRendererTop = new BarGraphRenderer(mCanvas, 4, paint2, true);

      Paint paint3 = new Paint();
      paint3.setStrokeWidth(3f);
      paint3.setAntiAlias(true);
      paint3.setColor(Color.argb(255, 222, 92, 143));
      mCircleRenderer = new CircleRenderer(mCanvas, paint3, true);

      Paint linePaint = new Paint();
      linePaint.setStrokeWidth(1f);
      linePaint.setAntiAlias(true);
      linePaint.setColor(Color.argb(88, 0, 128, 255));

      Paint lineFlashPaint = new Paint();
      lineFlashPaint.setStrokeWidth(5f);
      lineFlashPaint.setAntiAlias(true);
      lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
      mLineRenderer = new LineRenderer(mCanvas, linePaint, lineFlashPaint, true);
    }

    if (mBytes != null) {
      // Render all audio renderers
      AudioData audioData = new AudioData(mBytes);
      mCircleRenderer.render(audioData, mRect);
      mLineRenderer.render(audioData, mRect);
    }

    if (mFFTBytes != null) {
      // Render all FFT renderers
      FFTData fftData = new FFTData(mFFTBytes);
      mBarGraphRendererTop.render(fftData, mRect);
      mBarGraphRendererBottom.render(fftData, mRect);
    }

    // Fade out old contents
    mCanvas.drawPaint(mFadePaint);

    if(mFlash)
    {
      mFlash = false;
      mCanvas.drawPaint(mFlashPaint);
    }

    canvas.drawBitmap(mCanvasBitmap, new Matrix(), null);
  }
}