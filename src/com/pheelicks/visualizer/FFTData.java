package com.pheelicks.visualizer;

// Data class to explicitly indicate that these bytes are the FFT of audio data
public class FFTData
{
  public FFTData(byte[] bytes)
  {
    this.bytes = bytes;
  }

  public byte[] bytes;
}
