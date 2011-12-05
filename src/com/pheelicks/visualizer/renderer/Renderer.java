/**
 * Copyright 2011, Felix Palmer
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.visualizer.renderer;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.pheelicks.visualizer.AudioData;
import com.pheelicks.visualizer.FFTData;

abstract public class Renderer
{
  // Have these as members, so we don't have to re-create them each time
  protected float[] mPoints;
  protected float[] mFFTPoints;
  public Renderer()
  {
  }

  // As the display of raw/FFT audio will usually look different, subclasses
  // will typically only implement one of the below methods
  /**
   * Implement this method to render the audio data onto the canvas
   * @param canvas - Canvas to draw on
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  abstract public void onRender(Canvas canvas, AudioData data, Rect rect);

  /**
   * Implement this method to render the FFT audio data onto the canvas
   * @param canvas - Canvas to draw on
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  abstract public void onRender(Canvas canvas, FFTData data, Rect rect);


  // These methods should actually be called for rendering
  /**
   * Render the audio data onto the canvas
   * @param canvas - Canvas to draw on
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  final public void render(Canvas canvas, AudioData data, Rect rect)
  {
    if (mPoints == null || mPoints.length < data.bytes.length * 4) {
      mPoints = new float[data.bytes.length * 4];
    }

    onRender(canvas, data, rect);
  }

  /**
   * Render the FFT data onto the canvas
   * @param canvas - Canvas to draw on
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  final public void render(Canvas canvas, FFTData data, Rect rect)
  {
    if (mFFTPoints == null || mFFTPoints.length < data.bytes.length * 4) {
      mFFTPoints = new float[data.bytes.length * 4];
    }

    onRender(canvas, data, rect);
  }
}
