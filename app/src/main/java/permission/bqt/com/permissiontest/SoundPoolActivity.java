package permission.bqt.com.permissiontest;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundPoolActivity extends ListActivity {
	private SoundPool soundPool;
	private List<Integer> soundIdList = new ArrayList<>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {
				//0-6
				"通过一个资源ID：R.raw.s1_message，无限循环",
				"通过一个资源ID：R.raw.s10_42kb，能完整播放，循环2次，速度1.5倍，音量右0.1",
				"通过一个资源ID：caravan_15s_59kb，能完整播放，速度0.5倍，音量左0.1",

				"通过一个资源ID：R.raw.s8_67kb，虽然时间短，占空间也小，但是不能完全播放",
				"通过一个资源ID：ljsw_35s_68kb，虽然时间比较长，但是能完整播放",
				"通过一个资源ID：ljsw_49s_102kb，但是这个就不能完整播放了",
				"通过一个资源ID：hellow_tomorrow_6s_237kb，虽然占空间比较大，但是也能完整播放",

				//7-10
				"通过指定的路径：文件路径，caravan.mp3（不能播放网络资源）",
				"通过AssetFileDescriptor：assets目录下的文件，caravan_15s_59kb.mp3",
				"通过FileDescriptor：assets目录下的文件，可以播放文件指定的某一部分",
				"通过AssetFileDescriptor：raw目录下的文件，R.raw.s1_system",

				//11-14
				"全部流的暂停播放",
				"恢复播放",
				"卸载指定soundId的音频资源",
				"释放全部资源"
		};
		for (int i = 0; i < array.length; i++) {
			array[i] = i + "、" + array[i];
		}
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));

		soundPool = new SoundPool.Builder()
				.setMaxStreams(5)
				.setAudioAttributes(new AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_MEDIA)
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
				.build();

		soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
			Log.i("bqt", "【onLoadComplete】" + sampleId + "  " + status);//status : 0 = success
		});

		new Thread(this::initReseResources).start();//在子线程中初始化，特别是当有大量资源需要初始化时
	}

	private void initReseResources() {
		Context context = this.getApplicationContext();//不管使用哪个Context，退出当前Activity后都会且会在延迟几秒钟后停止播放

		//0-6，通过一个资源ID：Context context, int resId, int priority
		soundIdList.add(soundPool.load(context, R.raw.s1_message, 0));
		soundIdList.add(soundPool.load(context, R.raw.s10_42kb, 0));
		soundIdList.add(soundPool.load(context, R.raw.caravan_15s_59kb, 0));

		soundIdList.add(soundPool.load(context, R.raw.s8_67kb, 0));
		soundIdList.add(soundPool.load(context, R.raw.ljsw_35s_68kb, 0));
		soundIdList.add(soundPool.load(context, R.raw.ljsw_49s_102kb, 0));
		soundIdList.add(soundPool.load(context, R.raw.hellow_tomorrow_6s_237kb, 0));

		//7，通过指定的路径：String path, int priority
		soundIdList.add(soundIdList.size(), 0);//如果文件不存在，则就不能set，否则会throw IndexOutOfBoundsException
		String path = Environment.getExternalStorageDirectory() + File.separator + "caravan.mp3";
		soundIdList.set(soundIdList.size() - 1, soundPool.load(path, 0));//注意：add和set时传入的index是不一样的！

		//8-9，通过AssetFileDescriptor：AssetFileDescriptor afd, int priority
		try {
			soundIdList.add(soundIdList.size(), 0);//为防止异常后后续的代码执行不到导致index错乱，我们在一开始就直接add两个
			soundIdList.add(soundIdList.size(), 0);
			AssetFileDescriptor afd = getAssets().openFd("voice/caravan_15s_59kb.mp3");//openNonAssetFd
			soundIdList.set(soundIdList.size() - 2, soundPool.load(afd, 0));

			//通过FileDescriptor：FileDescriptor fd, long offset, long length, int priority
			FileDescriptor fd = afd.getFileDescriptor();
			long offset = afd.getStartOffset(), length = afd.getLength();
			Log.i("bqt", "【afd】offset=" + offset + ",length=" + length);//offset=40786180,length=60480
			soundIdList.set(soundIdList.size() - 1, soundPool.load(fd, offset + length / 2, length / 2, 0));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//10
		soundIdList.add(soundPool.load(getResources().openRawResourceFd(R.raw.s1_global), 0));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		soundPool.release();//释放所有资源
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position <= 10) {
			int soundID = soundIdList.get(position);
			int loop = 0;//0 = no loop, -1 = loop forever
			if (position == 0) loop = -1;
			else if (position == 1) loop = 2;//3次

			//播放指定soundID的音频：【int soundID】, float leftVolume, float rightVolume,int priority, 【int loop】, float rate
			int streamID = soundPool.play(soundID, 1.0f, 1.0f, 1, loop, 1.0f);
			Toast.makeText(this, "soundID=" + soundID + "  streamID=" + streamID, Toast.LENGTH_SHORT).show();

			//指定streamID的参数进行设置，这些参数都可以在播放时指定
			soundPool.setLoop(streamID, 1);//实践证明，这里设置无效
			switch (position) {
				case 1:
					soundPool.setRate(streamID, 1.5f);//大小并没有限制，但是一般不要小于0.5，不要大于1.5，否则声音严重失真
					soundPool.setVolume(streamID, 1.0f, 0.1f);//只能降低，不能提高。The value must be in the range of 0.0 to 1.0
					break;
				case 2:
					soundPool.setRate(streamID, 0.5f);
					soundPool.setVolume(streamID, 0.1f, 1.0f);
					break;
			}
		} else {
			switch (position) {
				case 11:
					soundPool.autoPause();//可以多次调用，每次都是把当前正在播放的音乐暂停，并加入同一个列表中
					break;
				case 12:
					soundPool.autoResume();//将所有暂停的音乐从暂停位置重新开始播放
					break;
				case 13:
					Toast.makeText(this, "" + soundPool.unload(soundIdList.get(0)) + "  " + soundPool.unload(soundIdList.get(1))
							, Toast.LENGTH_SHORT).show();//unload并不能停止正在播放的音乐，特别是loop=-1的，仍会循环播放
					break;
				case 14:
					soundPool.release();//会停止正在播放的所有音乐
					break;
			}
		}
	}
}