/**
 * Copyright 2011, Felix Palmer
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */
package com.pheelicks.visualizer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import android.graphics.PorterDuff;
import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;



import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



import com.pheelicks.visualizer.renderer.Renderer;

/**
 * A class that draws visualizations of data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture } and
 * {@link Visualizer.OnDataCaptureListener#onFftDataCapture }
 */
public class VisualizerView extends SurfaceView  implements SurfaceHolder.Callback{
	private static final String TAG = "VisualizerView";

	public byte[] mBytes;
	public byte[] mFFTBytes;
	private Rect mRect = new Rect();
	public  Visualizer mVisualizer;

	private SurfaceHolder holder;
	private AnimThread animThread;


	private Set<Renderer> mRenderers;

	private Paint mFlashPaint = new Paint();
	private Paint mFadePaint = new Paint();
	private Paint alphaPaint = new Paint();
	private Rect  alphaRect  = new Rect();

	public static LinkedBlockingDeque<byte[]> wave_frames;
	public static LinkedBlockingDeque<byte[]> fft_frames;

	public static int             CAPTURE_SIZE = 512;
	public static int             CAPACITY     = 5;
	public static long            TIMEOUT      = 20; 



	public VisualizerView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs);
		init();
	}

	public VisualizerView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public VisualizerView(Context context)
	{
		this(context, null, 0);
		holder = getHolder();
		holder.addCallback(this);
	}

	private void init() {
		mBytes    = new byte[CAPTURE_SIZE];
		mFFTBytes = new byte[CAPTURE_SIZE];

		mFlashPaint.setColor(Color.argb(255, 255, 255, 255));
		mFadePaint.setColor(Color.argb(128, 255, 255, 255)); // Adjust alpha to change how quickly the image fades
		mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));

		alphaPaint.setARGB(64, 0, 0, 0);
		

		mRenderers = Collections.newSetFromMap(new ConcurrentHashMap<Renderer, Boolean>());
	}

	/**
	 * Links the visualizer to a player
	 * @param player - MediaPlayer instance to link to
	 */
	public void link(MediaPlayer player)
	{
		if(player == null)
		{
			throw new NullPointerException("Cannot link to null MediaPlayer");
		}

		// Create the Visualizer object and attach it to our media player.		
		mVisualizer = new Visualizer(player.getAudioSessionId());
		mVisualizer.setCaptureSize(CAPTURE_SIZE);

		wave_frames = new LinkedBlockingDeque<byte[]>(CAPACITY);
		fft_frames  = new LinkedBlockingDeque<byte[]>(CAPACITY);


		// Enabled Visualizer and disable when we're done with the stream
		mVisualizer.setEnabled(true);

		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mediaPlayer)
			{
				mVisualizer.setEnabled(false);
			}
		});
	}

	public void addRenderer(Renderer renderer)
	{
		if(renderer != null)
		{
			mRenderers.add(renderer);
		}
	}

	public void clearRenderers()
	{
		mRenderers.clear();
	}

	/**
	 * Call to release the resources used by VisualizerView. Like with the
	 * MediaPlayer it is good practice to call this method
	 */
	public void release()
	{
		mVisualizer.release();
	}


	boolean mFlash = false;

	/**
	 * Call this to make the visualizer flash. Useful for flashing at the start
	 * of a song/loop etc...
	 */
	public void flash() {
		mFlash = true;
	}

	Bitmap mCanvasBitmap;
	Canvas mCanvas;

	long time = 0;
	long fps  = 0;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	class AnimThread extends Thread {

		private SurfaceHolder holder;
		private boolean running = true;
		int i = 0;
		private Paint fpsPaint = new Paint();
		public AnimThread(SurfaceHolder holder) {
			this.holder = holder;
		}

		@Override
		public void run() {
			fpsPaint.setColor(Color.WHITE);
			fpsPaint.setTextSize(48);

			while(running ) {
				Canvas canvas = null;

				try {
					canvas = holder.lockCanvas();
					canvas.drawRect(alphaRect,  alphaPaint);
					
					//canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
					synchronized (holder) {
		

						if (wave_frames.peekFirst()!=null)
						{
							mBytes    = wave_frames.removeFirst();
						}

						if (fft_frames.peekFirst()!=null)
						{
							mFFTBytes = fft_frames.removeFirst();
						}

						// Create canvas once we're ready to draw
						mRect.set(0, 0, getWidth(), getHeight());


						if (mBytes != null) {
							// Render all audio renderers
							AudioData audioData = new AudioData(mBytes);
							for(Renderer r : mRenderers)
							{
								r.render(canvas, audioData, mRect);
							}
						}
 
						if (mFFTBytes != null) {
							// Render all FFT renderers
							FFTData fftData = new FFTData(mFFTBytes);
							for(Renderer r : mRenderers)
							{
								r.render(canvas, fftData, mRect);
							}
						}
						
						long t = System.currentTimeMillis();
						
						fps = (fps+(1000/(t-time)))/2l;
						
						canvas.drawText("FPS:"+fps, 0, (float)getHeight(), fpsPaint);
						time = t;	
					}
				}
				finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}

		public void setRunning(boolean b) {
			running = b;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		animThread = new AnimThread(holder);
		animThread.setRunning(true);
		animThread.start();
		System.out.println("SURF CREATE");
		alphaRect  = new Rect(0,0,getWidth(),getHeight());
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		animThread.setRunning(false);
		while (retry) {
			try {
				animThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}
}



