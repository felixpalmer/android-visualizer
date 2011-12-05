package com.pheelicks.visualizer;

// WARNING!!! This file has more magic numbers in it than you could shake a
// stick at

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
import android.media.audiofx.Visualizer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * A class that draws visualizations of data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
 */
public class VisualizerView extends View {
  private static final String TAG = "VisualizerView";

  private byte[] mBytes;
  private byte[] mFFTBytes;
  private float[] mPoints;
  private Rect mRect = new Rect();

  private Paint mLinePaint = new Paint();
  private Paint mSpecialLinePaint = new Paint();
  private Paint mProgressLinePaint = new Paint();
  private Paint mFlashPaint = new Paint();
  private Paint mFadePaint = new Paint();

  // Usual BS of 3 constructors
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

    mProgressLinePaint.setStrokeWidth(4f);
    mProgressLinePaint.setAntiAlias(true);
    mProgressLinePaint.setColor(Color.argb(255, 22, 131, 255));


    mFlashPaint.setColor(Color.argb(122, 255, 255, 255));

    mFadePaint.setColor(Color.argb(238, 255, 255, 255)); // Adjust alpha to change how quickly the image fades
    mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
  }

  public void updateVisualizer(byte[] bytes) {
    mBytes = bytes;
    invalidate();
  }

  boolean mFlash = false;
  long mFlashTime = 0;
  long mFlashPeriod = 4000;

  public void flash() {
    mFlash = true;
    long now = SystemClock.currentThreadTimeMillis();
    mFlashPeriod = now - mFlashTime;
    mFlashTime = now;
    invalidate();
  }

  public void updateVisualizerFFT(byte[] bytes) {
    mFFTBytes = bytes;
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

    if (mBytes == null) {
      return;
    }

    if (mPoints == null || mPoints.length < mBytes.length * 4) {
      mPoints = new float[mBytes.length * 4];
    }

    mRect.set(0, 0, getWidth(), getHeight());


    if(mCanvasBitmap == null)
    {
      mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Config.ARGB_8888);
    }
    if(mCanvas == null)
    {
      mCanvas = new Canvas(mCanvasBitmap);
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




    AudioData audioData = new AudioData(mBytes);
    mCircleRenderer.render(audioData, mRect);
    mLineRenderer.render(audioData, mRect);

    // FFT time!!!!
    if (mFFTBytes == null) {
      return;
    }

    FFTData fftData = new FFTData(mFFTBytes);

    mBarGraphRendererTop.render(fftData, mRect);
    mBarGraphRendererBottom.render(fftData, mRect);

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