package com.pheelicks.visualizer.renderer;

import com.pheelicks.visualizer.AudioData;
import com.pheelicks.visualizer.FFTData;

import android.graphics.Canvas;
import android.graphics.Rect;

abstract public class Renderer
{
  // Canvas & Rect to render to
  protected Canvas mCanvas;

  // Have these as members, so we don't have to re-create them each time
  protected float[] mPoints;
  protected float[] mFFTPoints;
  public Renderer(Canvas canvas)
  {
    mCanvas = canvas;
  }

  // As the display of raw/FFT audio will usually look different, subclasses
  // will typically only implement one of the below methods
  /**
   * Implement this method to render the audio data onto the canvas
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  abstract public void onRender(AudioData data, Rect rect);

  /**
   * Implement this method to render the FFT audio data onto the canvas
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  abstract public void onRender(FFTData data, Rect rect);


  // These methods should actually be called for rendering
  /**
   * Render the audio data onto the canvas
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  final public void render(AudioData data, Rect rect)
  {
    if (mPoints == null || mPoints.length < data.bytes.length * 4) {
      mPoints = new float[data.bytes.length * 4];
    }

    onRender(data, rect);
  }

  /**
   * Render the FFT data onto the canvas
   * @param data - Data to render
   * @param rect - Rect to render into
   */
  final public void render(FFTData data, Rect rect)
  {
    if (mFFTPoints == null || mFFTPoints.length < data.bytes.length * 4) {
      mFFTPoints = new float[data.bytes.length * 4];
    }

    onRender(data, rect);
  }
}
