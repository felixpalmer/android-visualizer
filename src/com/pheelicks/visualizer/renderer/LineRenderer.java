/**
 * Copyright 2011, Felix Palmer
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.visualizer.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.pheelicks.visualizer.AudioData;
import com.pheelicks.visualizer.FFTData;

public class LineRenderer extends Renderer
{
  private Paint mPaint;
  private Paint mFlashPaint;
  private boolean mCycleColor;
  private float amplitude = 0;


  /**
   * Renders the audio data onto a line. The line flashes on prominent beats
   * @param canvas
   * @param paint - Paint to draw lines with
   * @param paint - Paint to draw flash with
   */
  public LineRenderer(Paint paint, Paint flashPaint)
  {
    this(paint, flashPaint, false);
  }

  /**
   * Renders the audio data onto a line. The line flashes on prominent beats
   * @param canvas
   * @param paint - Paint to draw lines with
   * @param paint - Paint to draw flash with
   * @param cycleColor - If true the color will change on each frame
   */
  public LineRenderer(Paint paint,
                      Paint flashPaint,
                      boolean cycleColor)
  {
    super();
    mPaint = paint;
    mFlashPaint = flashPaint;
    mCycleColor = cycleColor;
  }

  @Override
  public void onRender(Canvas canvas, AudioData data, Rect rect)
  {
    if(mCycleColor)
    {
      cycleColor();
    }

    // Calculate points for line
    for (int i = 0; i < data.bytes.length - 1; i++) {
      mPoints[i * 4] = rect.width() * i / (data.bytes.length - 1);
      mPoints[i * 4 + 1] =  rect.height() / 2
          + ((byte) (data.bytes[i] + 128)) * (rect.height() / 3) / 128;
      mPoints[i * 4 + 2] = rect.width() * (i + 1) / (data.bytes.length - 1);
      mPoints[i * 4 + 3] = rect.height() / 2
          + ((byte) (data.bytes[i + 1] + 128)) * (rect.height() / 3) / 128;
    }

    // Calc amplitude for this waveform
    float accumulator = 0;
    for (int i = 0; i < data.bytes.length - 1; i++) {
      accumulator += Math.abs(data.bytes[i]);
    }

    float amp = accumulator/(128 * data.bytes.length);
    if(amp > amplitude)
    {
      // Amplitude is bigger than normal, make a prominent line
      amplitude = amp;
      canvas.drawLines(mPoints, mFlashPaint);
    }
    else
    {
      // Amplitude is nothing special, reduce the amplitude
      amplitude *= 0.99;
      canvas.drawLines(mPoints, mPaint);
    }
  }

  @Override
  public void onRender(Canvas canvas, FFTData data, Rect rect)
  {
    // Do nothing, we only display audio data
  }

  private float colorCounter = 0;
  private void cycleColor()
  {
    int r = (int)Math.floor(128*(Math.sin(colorCounter) + 3));
    int g = (int)Math.floor(128*(Math.sin(colorCounter + 1) + 1));
    int b = (int)Math.floor(128*(Math.sin(colorCounter + 7) + 1));
    mPaint.setColor(Color.argb(128, r, g, b));
    colorCounter += 0.03;
  }
}
