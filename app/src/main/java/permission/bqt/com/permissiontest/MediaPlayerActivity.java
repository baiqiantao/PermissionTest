package permission.bqt.com.permissiontest;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MediaPlayerActivity extends ListActivity {
	private MediaPlayer mediaPlayer;
	private static final int STATE_CONTINUE = 1;//继续播放
	private static final int STATE_PAUSE = 2;//暂停播放
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"播放SD卡中的音乐",
				"播放网络URL中的音乐",
				"播放assent中的音乐",
				"播放raw中的音乐",

				"暂停播放",
				"停止播放",
				"重新播放",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
		initMediaPlayer();

		//不能在onCreate（甚至onResume）中获取本ListView中的item，因为可能还没有创建呢
		getListView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getListView().getViewTreeObserver().removeOnGlobalLayoutListener(this);//使用完之后必须立刻撤销监听
				setPlayState(STATE_PAUSE);
			}
		});
	}

	private void initMediaPlayer() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(mp -> {
			Toast.makeText(MediaPlayerActivity.this, "播放完毕！", Toast.LENGTH_SHORT).show();
			mp.reset();//MediaPlayer同时只能播放一个音乐文件，若要播另一个音乐文件，需先设置为初始状态
			setPlayEnable(true);
		});
		mediaPlayer.setOnPreparedListener(mp -> {
			Log.i("bqt", "【准备好了】");
			mp.start();//只有准备好以后才能播放
		});
		mediaPlayer.setOnErrorListener((mp, what, extra) -> {
			Toast.makeText(this, "【报错了】" + what + "   " + extra, Toast.LENGTH_SHORT).show();
			return false;
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();//释放播放器资源
			mediaPlayer = null;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				stop();
				playMusicFromSDCardOrUrl(Environment.getExternalStorageDirectory() + File.separator + "voice/caravan.mp3");
				break;
			case 1:
				stop();
				playMusicFromSDCardOrUrl("http://www.baiqiantao.xyz/s10_bgm.ogg");
				break;
			case 2:
				stop();
				try {
					playMusicFromAssentOrRaw(getAssets().openFd("voice/hellow.mp3"));
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "播放失败！", Toast.LENGTH_SHORT).show();
				}
				break;
			case 3:
				stop();
				try {
					playMusicFromAssentOrRaw(getResources().openRawResourceFd(R.raw.hellow_tomorrow));
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "播放失败！", Toast.LENGTH_SHORT).show();
				}
				break;
			case 4:
				pause();
				break;
			case 5:
				stop();
				break;
			case 6:
				replay();
				break;
		}
	}

	//******************************************************播放不同来源的音乐**********************************************

	/**
	 * 播放本地或网络音乐
	 */
	private void playMusicFromSDCardOrUrl(String path) {
		if (path == null) return;
		try {
			mediaPlayer.setDataSource(path);//设置播放的数据源。参数可以是本地或网络路径
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置音频流的类型，不是必须的
			mediaPlayer.prepareAsync();//异步准备
			//mediaPlayer.prepare();//同步播放，prepare方法是native类型的，播放的逻辑是由c代码在新的线程里面执行的
			setPlayEnable(false);//播放时将“播放”按钮设置为不可点击
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "播放失败！", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 播放assent或Raw中的音乐
	 */
	@TargetApi(Build.VERSION_CODES.N)
	private void playMusicFromAssentOrRaw(AssetFileDescriptor afd) throws IOException {
		//getAssets().openFd("caravan.mp3");
		//getResources().openRawResourceFd(R.raw.cf_bgm1);

		mediaPlayer.setDataSource(afd);
		mediaPlayer.prepareAsync();//异步准备
		setPlayEnable(false);//播放时将“播放”按钮设置为不可点击
	}
	//**************************************************暂停、停止、重播***************************************************

	/**
	 * 暂停
	 */
	private void pause() {
		if (mediaPlayer == null) return;

		if (mediaPlayer.isPlaying()) {//只有播放器已初始化并且正在播放才可暂停
			mediaPlayer.pause();
			setPlayState(STATE_CONTINUE);
		} else {
			mediaPlayer.start();
			setPlayState(STATE_PAUSE);
		}
	}

	/**
	 * 停止
	 */
	private void stop() {
		if (mediaPlayer == null) return;

		if (mediaPlayer.isPlaying()) mediaPlayer.stop();
		mediaPlayer.reset();

		setPlayEnable(true);//播放时将“播放”按钮设置为不可点击
		setPlayState(STATE_PAUSE);
	}

	/**
	 * 重播
	 */
	private void replay() {
		if (mediaPlayer == null) return;

		mediaPlayer.start();//这里调用start方法没意义，对一个已经处于Started 状态的MediaPlayer对象调用start()方法没有影响
		mediaPlayer.seekTo(0);//重头开始播放本音乐

		setPlayState(STATE_PAUSE);
	}

	//******************************************************其他方法*******************************************************

	/**
	 * 设置是否能点击播放
	 *
	 * @param enable setEnabled的值
	 */
	private void setPlayEnable(boolean enable) {
		getListView().getChildAt(0).setEnabled(enable);
		getListView().getChildAt(1).setEnabled(enable);
		getListView().getChildAt(2).setEnabled(enable);
		getListView().getChildAt(3).setEnabled(enable);
	}

	/**
	 * 设置播放按钮的播放状态，进而控制显示文案
	 *
	 * @param state 暂停或播放
	 */
	private void setPlayState(int state) {
		SpannableStringBuilder mSSBuilder = new SpannableStringBuilder("");
		if (state == STATE_CONTINUE) {
			SpannableString mSString = new SpannableString("继续播放");
			ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
			mSString.setSpan(colorSpan, 0, mSString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mSSBuilder.append(mSString);
		} else if (state == STATE_PAUSE) {
			SpannableString mSString = new SpannableString("暂停播放");
			ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLUE);
			mSString.setSpan(colorSpan, 0, mSString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mSSBuilder.append(mSString);
		}
		((TextView) getListView().getChildAt(4)).setText(mSSBuilder);
	}
}