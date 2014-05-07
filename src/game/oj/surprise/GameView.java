package game.oj.surprise;

import java.util.Random;
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
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.view.WindowManager;

public class GameView extends SurfaceView implements SurfaceHolder.Callback
{

	// will be used to randomly generate fruits and bombs
	private static final Random RNG = new Random();

	class OjThread extends Thread
	{

		// Keys used for saving the state of the game
		private static final String JAR_X = "jarX";
		private static final String JAR_Y = "jarY";
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

		private long score;
		private long movementDelay = 600;

		private TextView statusText;

		private int canvasWidth = 1100;
		private int canvasHeight = 1;
		
		// Draw background as bitmap: more efficient
		private Bitmap bgrnd;

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
	   	
		// Movement speeds for fruit and jar respectively
		private double fallSpeed;
		private double moveSpeed;

		private Handler handler;

		private Context context;

		private int jarHeight;
		private int jarWidth;

		private long lastTime;

		private Paint juiceMeter;

		private boolean runnable; 

		private SurfaceHolder surfaceHolder;

		private int jarX = 550;
		private int jarY = 550;

		
		
		public OjThread(SurfaceHolder sfh, Context context, Handler handler)
		{
			surfaceHolder = sfh;
			this.context = context;
			this.handler = handler;

			Resources res = context.getResources();

			//jar = res.getDrawable(R.drawable.ceramic);
			//monkey = res.getDrawable(R.drawable.monkey);
		//	orange = res.getDrawable(R.drawable.orange);

			 bgrnd = BitmapFactory.decodeResource(res, R.drawable.background);
			//orange = BitmapFactory.decodeResource(res, R.drawable.orange);
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
	   		monkeyDraw = new BitmapDrawable(getResources(), monkey);
	   		appleDraw = new BitmapDrawable(getResources(), apple);
	   		bananaDraw = new BitmapDrawable(getResources(),  banana);
	   	 	cherryDraw = new BitmapDrawable(getResources(), cherry);
	   		grapeDraw = new BitmapDrawable(getResources(), grape);
	   		pineappleDraw = new BitmapDrawable(getResources(), pineapple);
	   		watermelonDraw = new BitmapDrawable(getResources(),  watermelon);

			//Bitmap jarScale = Bitmap.createScaledBitmap(jar, 500, 300, false);
			//jarWidth = jar.getIntrinsicWidth();
			//jarHeight = jar.getIntrinsicHeight();
	   		 
			juiceMeter = new Paint();
			juiceMeter.setAntiAlias(true);
			juiceMeter.setARGB(255, 120, 180, 0);

			score = 0;

		}

		public void doStart()
		{
			synchronized (surfaceHolder)
			{

				jarX = canvasWidth / 2;
				jarY = canvasHeight - jarHeight / 2;

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
		public void pause()
		{
			synchronized (surfaceHolder)
			{
				if (gameMode == RUNNING)
				{
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
		public synchronized void restoreState(Bundle savedState)
		{
			setState(PAUSE);
			movement = 0;

			jarX = savedState.getInt(JAR_X);
			jarY = savedState.getInt(JAR_Y);

			score = savedState.getInt(SCORE);

		}

		public void run()
		{

			while (runnable)
			{
				Canvas c = null;
				try
				{
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder)
					{

						if (gameMode == RUNNING)
						{
							update();
						}
						;
						doDraw(c);

					}

				}
				finally
				{
					if (c != null)
					{
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
		public Bundle saveState(Bundle map)
		{
			if (map != null)
			{
				map.putLong(SCORE, Long.valueOf(score));
				map.putDouble(JAR_X, Double.valueOf(jarX));
				map.putDouble(JAR_Y, Double.valueOf(jarY));

			}

			return map;

		}

		public void setRunning(boolean b)
		{
			runnable = b;

		}

		public void setState(int mode)
		{
			synchronized (surfaceHolder)
			{

				setState(mode, null);

			}

		}

		public void setState(int mode, CharSequence message)
		{
			gameMode = mode;

			if (gameMode == RUNNING)
			{
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "");
				b.putInt("visible", View.INVISIBLE);
				msg.setData(b);
				handler.sendMessage(msg);

			}
			else
			{
				Resources res = context.getResources();
				CharSequence str = "";

				if (gameMode == READY)
				{
					str = "READY";

				}

				else if (gameMode == PAUSE)
				{
					str = "PAUSED";

				}

				else if (gameMode == LOSE)
				{
					str = "GAME OVER";

				}

				if (message != null)
				{
					str = message + "\n" + str;

				}

				if (gameMode == LOSE)
				{
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

		public void unpause()
		{
			synchronized (surfaceHolder)
			{
				lastTime = System.currentTimeMillis() + 100;

			}
			setState(RUNNING);

		}

		public void moveLeft()
		{
			jarX -= 27;
			
		}
		
		public void moveRight(){
			jarX += 27;
		}
		
		boolean doKeyDown(int keyCode, KeyEvent msg)
		{

			synchronized (surfaceHolder)
			{
				boolean okStart = false;

				// Can only go left or right

				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					okStart = true;
				if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					okStart = true;

				if (okStart && gameMode == READY || gameMode == LOSE)
				{

					doStart();
					return true;

				}

				else if (okStart && gameMode == PAUSE)
				{
					unpause();
					return true;

				}
				else if (gameMode == RUNNING)
				{

					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					{
						jarX -= 1;
						return true;

					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					{
						jarX += 1;
						return true;

					}

				}

			}

			return false;

		}

		public void doDraw(Canvas canvas)
		{

			Rect dest = new Rect(0, 0, getWidth(), getHeight());
			Paint paint = new Paint();

			paint.setFilterBitmap(true);

			canvas.drawBitmap(bgrnd, null, dest, paint);
		
			canvas.save();
			
			//canvas.drawBitmap(parachuter, parachuters.get(i).getX(), parachuters.get(i).getY(), null);
			//orange.setBounds(new Rect(0, 0, 100, 100));
			/*
			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();
		*/
			//orangeD.setBounds(new Rect(300, 400, 350, 450));
			//orangeD.draw(canvas);
			jarDraw.setBounds(jarX, jarY, jarX+210, 800);
			jarDraw.draw(canvas);
			//orangeD.BitmapDrawable(orange, getWidth()/2, getHeight()/2, paint);
			//orange.draw(canvas);
			
			//jar.setBounds(new Rect(200, 500, 380, 250));
			//jar.setHeight(100);
			//jar.setWidth(175);
			
			//jarScale.recycle();
			//canvas.drawBitmap(jarScale, getWidth()/2, getHeight(), paint);
			
			//jar.draw(canvas);
			//jar.setBounds(width/2, height/2, 300, 200);
			
			canvas.restore();

		}

		public void update()
		{
			long now = System.currentTimeMillis();

			if (lastTime > now)
				return;

		}
	}

	private Context context;

	private TextView statusText;

	private OjThread thread;

	public GameView(Context context, AttributeSet attr)
	{
		super(context, attr);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		thread = new OjThread(holder, context, new Handler()
		{
			public void handleMessage(Message m)
			{

			}

		});

		setFocusable(true);

	}

	public OjThread getThread()
	{

		return thread;

	}

	public boolean onKeyDown(int keyCode, KeyEvent msg)
	{
		return thread.doKeyDown(keyCode, msg);

	}

	
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{

		if (!hasWindowFocus)
		{
			thread.pause();
		}

	}

	public void setTextview(TextView text)
	{

		statusText = text;

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		// Surface does not change;

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		thread.setRunning(true);
		thread.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		boolean retry = true;
		thread.setRunning(false);
		while (retry)
		{

			try
			{
				thread.join();
				retry = false;
			}
			catch (InterruptedException e)
			{

			}

		}

	}

}