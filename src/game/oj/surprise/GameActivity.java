package game.oj.surprise;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class GameActivity extends Activity {

	private int[] FRUIT = { R.drawable.orange, R.drawable.orange,
			R.drawable.orange, R.drawable.orange, R.drawable.apple,
			R.drawable.banana, R.drawable.watermelon, R.drawable.grape,
			R.drawable.cherry };

	private Rect mDisplaySize = new Rect();

	private RelativeLayout mRootLayout;
	private ArrayList<View> mAllImageViews = new ArrayList<View>();

	private float mScale;

	private Context context;

	private BitmapDrawable jarDraw;
	private int jarLeft = 550;
	private int jarTop = 550;
	private int jarRight = jarLeft + 210;
	private int jarBottom = 800;

	private static int transLeft = 1;
	private static int transRight = 1;
	
	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		// Bitmap bgrnd = BitmapFactory.decodeResource(context.getResources(),
		// R.drawable.jungle5);

		// Canvas canvas = new Canvas ();

		// Rect dest = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
		Paint paint = new Paint();

		paint.setFilterBitmap(true);

		// canvas.drawBitmap(bgrnd, null, dest, paint);

		Display display = getWindowManager().getDefaultDisplay();
		display.getRectSize(mDisplaySize);

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		mScale = metrics.density;

		mRootLayout = (RelativeLayout) findViewById(R.id.game_layout);

		new Timer().schedule(new ExeTimerTask(), 0, 1000); // amount of leaves
															// falling
	}

	public static final int MAX_DELAY = 6000;
	public static final int ANIM_DURATION = 5000;
	public static final int EMPTY_MESSAGE_WHAT = 0x001;

	public void startAnimation(final ImageView aniView) {

		aniView.setPivotX(aniView.getWidth() / 2);
		aniView.setPivotY(aniView.getHeight() / 2);

		long delay = new Random().nextInt(MAX_DELAY);

		final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.setDuration(ANIM_DURATION); // falling speed
		animator.setInterpolator(new AccelerateInterpolator());
		animator.setStartDelay(delay);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			int angle = 50 + (int) (Math.random() * 101);
			int movexR = new Random().nextInt(mDisplaySize.right);
			int toggle = new Random().nextInt(2);

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = ((Float) (animation.getAnimatedValue()))
						.floatValue();

				aniView.setRotation(angle * value);
				if (toggle % 2 == 0) {
					aniView.setTranslationX((movexR - 150) * value / 2);
				} else {
					aniView.setTranslationX((movexR - 150) * -value / 2);
				}

				aniView.setTranslationY((mDisplaySize.bottom + (150 * mScale))
						* value + 20);
			}
		});
		animator.start();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// draw the jar

			// LayoutInflater inflate1 = LayoutInflater.from(GameActivity.this);
			// ImageView imageView1 = (ImageView) inflate1.inflate(
			// R.layout.game_image, null);
			// imageView1.setImageDrawable(j);
			// mRootLayout.addView(imageView);
			// mAllImageViews.add(imageView);

			// LayoutParams animationLayout1 = (LayoutParams) imageView1
			// .getLayoutParams();
			// animationLayout1.setMargins(0, (int) (10 * mScale), 0, 0);
			// animationLayout1.width = (int) (150 * mScale);
			// animationLayout1.height = (int) (110 * mScale);

			int viewId = new Random().nextInt(FRUIT.length);
			Drawable d = getResources().getDrawable(FRUIT[viewId]);

			LayoutInflater inflate = LayoutInflater.from(GameActivity.this);
			ImageView imageView = (ImageView) inflate.inflate(
					R.layout.game_image, null);
			ImageView jView = (ImageView) inflate.inflate(R.layout.game_image,
					null);
			jView.setId(10);

			Drawable j = getResources().getDrawable(R.drawable.ceramic);
			((ImageView) jView).setImageDrawable(j);

			imageView.setImageDrawable(d);

			mRootLayout.addView(jView);
			//
			mAllImageViews.add(jView);
			//
			LayoutParams jarLayout = (LayoutParams) jView.getLayoutParams();
			jarLayout.setMargins(jarLeft, 550, jarRight, 900);
			jarLayout.width = (int) (100 * mScale);
			jarLayout.height = (int) (60 * mScale);

			mRootLayout.addView(imageView);
			mAllImageViews.add(imageView);

			LayoutParams animationLayout = (LayoutParams) imageView
					.getLayoutParams();
			animationLayout.setMargins(700, (int) (-150 * mScale), 700, 0);
			animationLayout.width = (int) (60 * mScale);
			animationLayout.height = (int) (60 * mScale);

			startAnimation(imageView);
		}
	};

	public void moveLeft(View v) {

		final ImageView jar = (ImageView) findViewById(10);
		if (jar.getLeft() > 100) {
			jar.setTranslationX(-27 * transLeft);
			transLeft++;
		}

	}

	private class ExeTimerTask extends TimerTask {
		@Override
		public void run() {
			// we don't really use the message 'what' but we have to specify
			// something.
			mHandler.sendEmptyMessage(EMPTY_MESSAGE_WHAT);
		}
	}

}
