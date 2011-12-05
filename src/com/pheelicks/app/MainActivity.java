package com.pheelicks.app;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.pheelicks.visualizer.R;
import com.pheelicks.visualizer.VisualizerView;

/**
 * Basic demo to show how to use VisualizerView
 *
 */
public class MainActivity extends Activity {
  private MediaPlayer mPlayer;
  private VisualizerView mVisualizerView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    init();
  }

  private void init()
  {
    mPlayer = MediaPlayer.create(this, R.raw.test);
    mPlayer.setLooping(true);
    mPlayer.start();

    mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
    mVisualizerView.link(mPlayer);
  }

  // Cleanup
  private void cleanUp()
  {
    if (mPlayer != null)
    {
      mVisualizerView.release();
      mPlayer.release();
      mPlayer = null;
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    if(mPlayer == null)
    {
      init();
    }
    else
    {
      mPlayer.start();
    }
  }

  @Override
  protected void onPause()
  {
    if (isFinishing())
    {
      cleanUp();
    }
    else
    {
      mPlayer.pause();
    }

    super.onPause();
  }

  @Override
  protected void onDestroy()
  {
    cleanUp();
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