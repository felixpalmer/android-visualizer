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
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;

import com.pheelicks.utils.TunnelPlayerWorkaround;
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
	private MediaPlayer    mPlayer;
	private MediaPlayer    mSilentPlayer;  /* to avoid tunnel player issue */
	private VisualizerView mVisualizerView;
	private Handler        timer;

	private Runnable       sampler;

	public Handler getTimer()
	{
		return timer;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.main);
		LinearLayout surface = (LinearLayout)findViewById(R.id.visualizerView);
		mVisualizerView = new VisualizerView(this);
		surface.addView(mVisualizerView);
		timer   = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		initTunnelPlayerWorkaround();
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

		mVisualizerView.link(mPlayer);

		// Start with just line renderer
		addLineRenderer();

		
		sampler = new Runnable ()
		{
			@Override
			public void run() {

		
				mVisualizerView.mVisualizer.getFft(mVisualizerView.mFFTBytes);
				if (mVisualizerView.fft_frames.remainingCapacity()<1)
					mVisualizerView.fft_frames.clear();
				mVisualizerView.fft_frames.addFirst(mVisualizerView.mFFTBytes);

				mVisualizerView.mVisualizer.getWaveForm(mVisualizerView.mBytes);
				if (mVisualizerView.wave_frames.remainingCapacity()<1)
					mVisualizerView.wave_frames.clear();
				mVisualizerView.wave_frames.addFirst(mVisualizerView.mBytes);

				
				/* and here comes the "trick" */
				timer.postDelayed(this,  VisualizerView.TIMEOUT);
			}
			
		};

		timer.postDelayed(sampler,  VisualizerView.TIMEOUT);
		System.out.println("STARTED");
	}

	private void cleanUp()
	{
		if (mPlayer != null)
		{
			mVisualizerView.release();
			mPlayer.release();
			mPlayer = null;
		}

		if (mSilentPlayer != null)
		{
			mSilentPlayer.release();
			mSilentPlayer = null;
		}
	}

	// Workaround (for Galaxy S4)
	//
	// "Visualization does not work on the new Galaxy devices"
	//    https://github.com/felixpalmer/android-visualizer/issues/5
	//
	// NOTE: 
	//   This code is not required for visualizing default "test.mp3" file,
	//   because tunnel player is used when duration is longer than 1 minute.
	//   (default "test.mp3" file: 8 seconds)
	//
	private void initTunnelPlayerWorkaround() {
		// Read "tunnel.decode" system property to determine
		// the workaround is needed
		if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(this)) {
			mSilentPlayer = TunnelPlayerWorkaround.createSilentMediaPlayer(this);
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
		linePaint.setStrokeWidth(5f);
		linePaint.setAntiAlias(true);
		linePaint.setColor(Color.argb(128, 0, 128, 255));

		Paint lineFlashPaint = new Paint();
		lineFlashPaint.setStrokeWidth(5f);
		lineFlashPaint.setAntiAlias(true);
		lineFlashPaint.setColor(Color.argb(255, 255, 255, 255));
		LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, false);
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
		timer.postDelayed(sampler,  VisualizerView.TIMEOUT);
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
