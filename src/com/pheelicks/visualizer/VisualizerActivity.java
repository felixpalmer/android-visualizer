package com.pheelicks.visualizer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

public class VisualizerActivity extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Just play test file
    MediaPlayer player = MediaPlayer.create(this, R.raw.test);
    player.setLooping(true);
    player.start();
  }
}