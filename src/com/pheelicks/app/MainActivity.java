/**
 * Copyright 2011, Felix Palmer
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.app;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.pheelicks.visualizer.R;
import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.BarGraphRenderer;
import com.pheelicks.visualizer.renderer.CircleBarRenderer;
import com.pheelicks.visualizer.renderer.CircleRenderer;
import com.pheelicks.visualizer.renderer.LineRenderer;

/**
 * Demo to show how to use VisualizerView
 */
public class MainActivity extends Activity {
  private MediaPlayer mPlayer;
  private VisualizerView mVisualizerView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    init();
  }

  @Override
  protected void onPause()
  {
    cleanUp();
    super.onPause();
  }

  @Override
  protected void onDestroy()
  {
    cleanUp();
    super.onDestroy();
  }

  private void init()
  {
    mPlayer = MediaPlayer.create(this, R.raw.test);
    mPlayer.setLooping(true);
    mPlayer.start();

    // We need to link the visualizer view to the media player so that
    // it displays something
    mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
    mVisualizerView.link(mPlayer);

    // Start with just line renderer
    addLineRenderer();
  }

  private void cleanUp()
  {
    if (mPlayer != null)
    {
      mVisualizerView.release();
      mPlayer.release();
      mPlayer = null;
    }
  }

  // Methods for adding renderers to visualizer
  private void addBarGraphRenderers()
  {
    Paint paint = new Paint();
    paint.setStrokeWidth(50f);
    paint.setAntiAlias(true);
    paint.setColor(Color.argb(200, 56, 138, 252));
    BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
    mVisualizerView.addRenderer(barGraphRendererBottom);

    Paint paint2 = new Paint();
    paint2.setStrokeWidth(12f);
    paint2.setAntiAlias(true);
    paint2.setColor(Color.argb(200, 181, 111, 233));
    BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
    mVisualizerView.addRenderer(barGraphRendererTop);
  }

  private void addCircleBarRenderer()
  {
    Paint paint = new Paint();
    paint.setStrokeWidth(8f);
    paint.setAntiAlias(true);
    paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
    paint.setColor(Color.argb(255, 222, 92, 143));
    CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
    mVisualizerView.addRenderer(circleBarRenderer);
  }

  private void addCircleRenderer()
  {
    Paint paint = new Paint();
    paint.setStrokeWidth(3f);
    paint.setAntiAlias(true);
    paint.setColor(Color.argb(255, 222, 92, 143));
    CircleRenderer circleRenderer = new CircleRenderer(paint, true);
    mVisualizerView.addRenderer(circleRenderer);
  }

  private void addLineRenderer()
  {
    Paint linePaint = new Paint();
    linePaint.setStrokeWidth(1f);
    linePaint.setAntiAlias(true);
    linePaint.setColor(Color.argb(88, 0, 128, 255));

    Paint lineFlashPaint = new Paint();
    lineFlashPaint.setStrokeWidth(5f);
    lineFlashPaint.setAntiAlias(true);
    lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
    LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
    mVisualizerView.addRenderer(lineRenderer);
  }

  // Actions for buttons defined in xml
  public void startPressed(View view) throws IllegalStateException, IOException
  {
    if(mPlayer.isPlaying())
    {
      return;
    }
    mPlayer.prepare();
    mPlayer.start();
  }

  public void stopPressed(View view)
  {
    mPlayer.stop();
  }

  public void barPressed(View view)
  {
    addBarGraphRenderers();
  }

  public void circlePressed(View view)
  {
    addCircleRenderer();
  }

  public void circleBarPressed(View view)
  {
    addCircleBarRenderer();
  }

  public void linePressed(View view)
  {
    addLineRenderer();
  }

  public void clearPressed(View view)
  {
    mVisualizerView.clearRenderers();
  }
}