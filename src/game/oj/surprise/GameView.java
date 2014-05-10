package game.oj.surprise;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private ArrayList<BitmapDrawable> pictures = new ArrayList<>();
	
	// will be used to randomly generate fruits and bombs
	private static final Random RNG = new Random();
	private long score;

	private Bitmap jar;
	private Bitmap monkey;
	private Bitmap orange;
	private Bitmap apple;
	private Bitmap banana;
	private Bitmap cherry;
	private Bitmap grape;
	private Bitmap pineapple;
	private Bitmap watermelon;
	private Bitmap bomb;
	private Bitmap dynamite;
	private Bitmap anvil;
	private Bitmap cannonball;
	
	private Context theContext;
	
	class OjThread extends Thread {

		// Keys used for saving the state of the game
		private static final String JAR_X = "jarLeft";
		private static final String JAR_Y = "jarTop";
		private static final String SCORE = "score";

		private int gameMode = READY;
		public static final int PAUSE = 0;
		public static final int READY = 1;
		public static final int RUNNING = 2;
		public static final int LOSE = 3;

		private int movement = STILL;

		// only jar can move left/right
		private static final int STILL = 0;
		private static final int LEFT = -1;
		private static final int RIGHT = 1;

		// Only fruit/explosives can move down.
		private static final int DOWN = 2;

		// Drawables
		private static final int JAR = 1;

		private long movementDelay = 600;

		private TextView statusText;

		private int canvasWidth = 1100;
		private int canvasHeight = 1;

		// Draw background as bitmap: more efficient
		private Bitmap bgrnd;

		private BitmapDrawable jarDraw;
		private BitmapDrawable orangeDraw;
		private BitmapDrawable monkeyDraw;
		private BitmapDrawable appleDraw;
		private BitmapDrawable bananaDraw;
		private BitmapDrawable cherryDraw;
		private BitmapDrawable grapeDraw;
		private BitmapDrawable pineappleDraw;
		private BitmapDrawable watermelonDraw;
		private BitmapDrawable bombDraw;
		private BitmapDrawable dynamiteDraw;
		private BitmapDrawable anvilDraw;
		private BitmapDrawable cannonballDraw;

		public Handler handler;

		private int jarHeight;
		private int jarWidth;

		private long lastTime;

		private Paint juiceMeter;

		private boolean runnable;

		public SurfaceHolder surfaceHolder;

		private int jarLeft = 550;
		private int jarTop = 550;
		private int jarRight = jarLeft + 210;
		private int jarBottom = 800;

		private int fruitLeft = 300;
		private int fruitTop = 300;
		private int fruitRight = 400;
		private int fruitBottom = 400;

		public OjThread(SurfaceHolder sfh, Context context, Handler handler) {
			surfaceHolder = sfh;
			theContext = context;
			this.handler = handler;

			Resources res = theContext.getResources();

			bgrnd = BitmapFactory.decodeResource(res, R.drawable.jungle5);
			// orange = BitmapFactory.decodeResource(res, R.drawable.orange);
			orange = BitmapFactory.decodeResource(res, R.drawable.orange);
			jar = BitmapFactory.decodeResource(res, R.drawable.ceramic);
			monkey = BitmapFactory.decodeResource(res, R.drawable.monkey);
			apple = BitmapFactory.decodeResource(res, R.drawable.apple);
			banana = BitmapFactory.decodeResource(res, R.drawable.banana);
			cherry = BitmapFactory.decodeResource(res, R.drawable.cherry);
			grape = BitmapFactory.decodeResource(res, R.drawable.grape);
			pineapple = BitmapFactory.decodeResource(res, R.drawable.pineapple);
			watermelon = BitmapFactory.decodeResource(res,
					R.drawable.watermelon);

			jarDraw = new BitmapDrawable(getResources(), jar);
			orangeDraw = new BitmapDrawable(getResources(), orange);
			bananaDraw = new BitmapDrawable(getResources(), banana);
			monkeyDraw = new BitmapDrawable(getResources(), monkey);
			appleDraw = new BitmapDrawable(getResources(), apple);
			cherryDraw = new BitmapDrawable(getResources(), cherry);
			grapeDraw = new BitmapDrawable(getResources(), grape);
			pineappleDraw = new BitmapDrawable(getResources(), pineapple);
			watermelonDraw = new BitmapDrawable(getResources(), watermelon);
			
			pictures.add(orangeDraw);
			pictures.add(appleDraw);
			pictures.add(cherryDraw);
			pictures.add(grapeDraw);
			pictures.add(bananaDraw);
			pictures.add(pineappleDraw);
			pictures.add(watermelonDraw);

			juiceMeter = new Paint();
			juiceMeter.setAntiAlias(true);
			juiceMeter.setARGB(255, 120, 180, 0);

			score = 0;
		}

		public void doStart() {
			synchronized (surfaceHolder) {

				jarLeft = canvasWidth / 2;
				jarTop = canvasHeight - jarHeight / 2;

				lastTime = System.currentTimeMillis() + 100;
				setState(RUNNING);

			}

		}

		/**
		 * @return void
		 * 
		 *         Pauses the game, defined below
		 * 
		 */
		public void pause() {
			synchronized (surfaceHolder) {
				if (gameMode == RUNNING) {
					setState(PAUSE);
				}

			}

		}

		/**
		 * @param savedState
		 * @return void
		 * 
		 *         Restores previous state if surfaceview is destroyed
		 * 
		 */
		public synchronized void restoreState(Bundle savedState) {
			setState(PAUSE);
			movement = 0;

			jarLeft = savedState.getInt(JAR_X);
			jarTop = savedState.getInt(JAR_Y);

			score = savedState.getInt(SCORE);

		}

		public void run() {

			while (runnable) {
				Canvas c = null;
				try {
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {

						if (gameMode == RUNNING) {
							update();
						}
						;
						doDraw(c);

					}

				} finally {
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);

					}

				}

			}

		}

		/**
		 * @param map
		 * @return
		 * @return Bundle
		 * 
		 *         Gets the position of the monkey, bombs, and fruits and the
		 *         jar
		 * 
		 */
		public Bundle saveState(Bundle map) {
			if (map != null) {
				map.putLong(SCORE, Long.valueOf(score));
				map.putDouble(JAR_X, Double.valueOf(jarLeft));
				map.putDouble(JAR_Y, Double.valueOf(jarTop));

			}

			return map;

		}

		public void setRunning(boolean b) {
			runnable = b;

		}

		public void setState(int mode) {
			synchronized (surfaceHolder) {

				setState(mode, null);

			}

		}

		public void setState(int mode, CharSequence message) {
			gameMode = mode;

			if (gameMode == RUNNING) {
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "");
				b.putInt("visible", View.INVISIBLE);
				msg.setData(b);
				handler.sendMessage(msg);

			} else {
				Resources res = theContext.getResources();
				CharSequence str = "";

				if (gameMode == READY) {
					str = "READY";

				}

				else if (gameMode == PAUSE) {
					str = "PAUSED";

				}

				else if (gameMode == LOSE) {
					str = "GAME OVER";

				}

				if (message != null) {
					str = message + "\n" + str;

				}

				if (gameMode == LOSE) {
					score = 0;

				}

				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", str.toString());
				b.putInt("viz", View.VISIBLE);
				msg.setData(b);
				handler.sendMessage(msg);

			}
		}

		public void unpause() {
			synchronized (surfaceHolder) {
				lastTime = System.currentTimeMillis() + 100;

			}
			setState(RUNNING);

		}
/*
		public void moveLeft() {
			if (jarLeft > 80) {
				jarLeft -= 27;
				jarRight -= 27;
			}

		}

		public void moveRight() {
			if (jarRight < 1200) {
				jarLeft += 27;
				jarRight += 27;
			}
		}
*/
		boolean doKeyDown(int keyCode, KeyEvent msg) {

			synchronized (surfaceHolder) {
				boolean okStart = false;

				// Can only go left or right

				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					okStart = true;
				if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					okStart = true;

				if (okStart && gameMode == READY || gameMode == LOSE) {

					doStart();
					return true;

				}

				else if (okStart && gameMode == PAUSE) {
					unpause();
					return true;

				} else if (gameMode == RUNNING) {

					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						jarLeft -= 1;
						return true;

					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						jarLeft += 1;
						return true;

					}

				}

			}

			return false;

		}

		public void doDraw(Canvas canvas) {

			Rect dest = new Rect(0, 0, getWidth(), getHeight());
			Paint paint = new Paint();

			paint.setFilterBitmap(true);

			canvas.drawBitmap(bgrnd, null, dest, paint);

			int fruit = RNG.nextInt(pictures.size());
			
			canvas.save();
			
			// jarDraw.setBounds(jarLeft, jarTop, jarLeft+210, 800);
			jarDraw.setBounds(jarLeft, jarTop, jarRight, jarBottom);
			jarDraw.draw(canvas);
			
			pictures.get(fruit).setBounds(fruitLeft, fruitTop, fruitRight, fruitBottom);
			pictures.get(fruit).draw(canvas);
			
			orangeDraw.setBounds(fruitLeft + 50 , fruitTop - 20, fruitRight + 50, fruitBottom - 20);
			orangeDraw.draw(canvas);
			
			if(fruitBottom < canvas.getHeight())
			{
			fruitTop += 8;
			fruitBottom += 8;
			}
			canvas.restore();

		}

		public void update() {
			long now = System.currentTimeMillis();

			if (lastTime > now)
				return;

		}
	}
	

	private TextView statusText;

	private OjThread thread;

	class fruitThread extends Thread
	{
		public fruitThread()
		{
		}
		
		public void run()
		{
			while (true) {
				Canvas c = null;
				try {
					c = thread.surfaceHolder.lockCanvas(null);
					synchronized (thread.surfaceHolder) {

						doDraw(c);

					}

				} finally {
					if (c != null) {
						thread.surfaceHolder.unlockCanvasAndPost(c);

					}

				}

			}

		}
		
		public void doDraw(Canvas canvas)
		{
			Resources res = theContext.getResources();
			
			orange = BitmapFactory.decodeResource(res, R.drawable.orange);
			BitmapDrawable orangeDraw = new BitmapDrawable(getResources(), orange);
			
			orangeDraw.setBounds(100, 100,  150, 150);
			orangeDraw.draw(canvas);
			
		}
		
	}
	
	
	public GameView(Context context, AttributeSet attr) {
		super(context, attr);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		thread = new OjThread(holder, context, new Handler() {
			public void handleMessage(Message m) {

			}

		});

		setFocusable(true);

	}

	/**
	 * @author user Thread responsible for generating fruits i.e populating the
	 *         fruit array and making the fruits fall.
	 */
	
	public OjThread getThread() {

		return thread;

	}

	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		return thread.doKeyDown(keyCode, msg);

	}

	public void onWindowFocusChanged(boolean hasWindowFocus) {

		if (!hasWindowFocus) {
			thread.pause();
		}

	}

	public void setTextview(TextView text) {

		statusText = text;

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Surface does not change;

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		thread.setRunning(true);
		thread.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {

			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {

			}

		}

	}
	
	

}