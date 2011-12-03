package com.pheelicks.visualizer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.View;

public class VisualizerActivity extends Activity {
  private MediaPlayer mPlayer;
  private Visualizer mVisualizer;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mPlayer = MediaPlayer.create(this, R.raw.test);
    mPlayer.setLooping(true);
    mPlayer.start();

    linkVisualizer(mPlayer);
  }

  /**
   * Links the visualizer to a player
   * TODO Refactor this into visualizer
   * @param player
   */
  private void linkVisualizer(MediaPlayer player)
  {

    final VisualizerView visualizerView = (VisualizerView) findViewById(R.id.visualizerView);

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
        visualizerView.updateVisualizer(bytes);
      }

      @Override
      public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
          int samplingRate)
      {
        visualizerView.updateVisualizerFFT(bytes);
      }
    };

    mVisualizer.setDataCaptureListener(captureListener,
        Visualizer.getMaxCaptureRate() / 2, true, true);

    // Enabled Visualizer and disable when we're done with the stream
    mVisualizer.setEnabled(true);
    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
    {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer)
      {
        mVisualizer.setEnabled(false);
      }
    });
  }

  // Cleanup
  @Override
  protected void onPause()
  {
    if (isFinishing() && (mPlayer != null))
    {
      mVisualizer.release();
      mPlayer.release();
      mPlayer = null;
    }

    super.onPause();
  }

  @Override
  protected void onDestroy()
  {
    if (mPlayer != null)
    {
      mPlayer.stop();
      mPlayer.release();
      mPlayer = null;
    }

    super.onDestroy();
  }

  // Actions for buttons defined in xml
  public void startPressed(View view)
  {
    mPlayer.start();
  }

  public void stopPressed(View view)
  {
    mPlayer.stop();
  }
}