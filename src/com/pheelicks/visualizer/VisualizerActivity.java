package com.pheelicks.visualizer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class VisualizerActivity extends Activity {
  private MediaPlayer mPlayer;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mPlayer = MediaPlayer.create(this, R.raw.test);
    mPlayer.setLooping(true);
  }

  public void startPressed(View view)
  {
    mPlayer.start();
  }

  public void stopPressed(View view)
  {
    mPlayer.stop();
  }
}