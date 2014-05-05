package game.oj.surprise;

import game.oj.surprise.GameView.OjThread;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.os.Build;

public class GameActivity extends Activity
{
	private static final int MENU_PAUSE = 1;
	private static final int MENU_RESUME = 2;
	private static final int MENU_START = 3;
	private static final int MENU_STOP = 4;

	private OjThread thread;
	private GameView gView;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_PAUSE, 0, "PAUSE");
		menu.add(0, MENU_RESUME, 0, "RESUME");
		menu.add(0, MENU_START, 0, "START");
		menu.add(0, MENU_STOP, 0, "STOP");

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id)
		{
			case MENU_PAUSE:
				thread.pause();
				return true;
			case MENU_RESUME:
				thread.unpause();
				return true;
			case MENU_START:
				thread.doStart();
				return true;
			case MENU_STOP:
				thread.setState(OjThread.LOSE, "STOPPED");
				return true;
		}

		return false;

	}

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.oj_layout);

		gView = (GameView) findViewById(R.id.oj);
		thread = gView.getThread();

		if (savedInstanceState == null)
		{
			thread.setState(OjThread.READY);
			Log.w(this.getClass().getName(), "SIS is null");
		}

		else
		{

			thread.restoreState(savedInstanceState);
			Log.w(this.getClass().getName(), "SIS is not null");

		}
	}

	protected void onPause()
	{

		super.onPause();
		gView.getThread().pause();

	}

	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		thread.saveState(outState);
		Log.w(this.getClass().getName(), "SIS is called");
	}

}
