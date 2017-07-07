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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundPoolActivity extends ListActivity {
	private SoundPool soundPool;
	private List<Integer> soundIdList = new ArrayList<>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"0、通过一个资源ID：R.raw.s1_ding",
				"1、通过指定的路径：SD卡下的s10_bgm.ogg文件路径（不能播放网络资源）",

				"2、通过AssetFileDescriptor：assets目录下的文件",
				"3、通过AssetFileDescriptor：raw目录下的文件",

				"6、FileDescriptor：不能播放",
				"7、FileDescriptor：不能播放",};
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

		Context context = this.getApplicationContext();//不管使用哪个Context，退出当前Activity后都会且会在延迟几秒钟后停止播放

		//通过一个资源ID：Context context, int resId, int priority
		soundIdList.add(soundPool.load(context, R.raw.s1_ding, 0));

		//通过指定的路径：String path, int priority
		soundIdList.add(soundPool.load(Environment.getExternalStorageDirectory() + File.separator + "voice/caravan.mp3", 0));

		//通过AssetFileDescriptor：FileDescriptor fd, long offset, long length, int priority
		try {
			AssetFileDescriptor afd = getAssets().openNonAssetFd("voice/s10_bgm.ogg");
			Log.i("bqt", "【afd】" + afd.getStartOffset() + "," + afd.getLength() + "," + afd.getDeclaredLength());//43868,42987,42987
			soundIdList.add(soundPool.load(afd, 0));

			AssetFileDescriptor afd2 = getResources().openRawResourceFd(R.raw.s1_ding);
			Log.i("bqt", "【afd2】" + afd2.getStartOffset() + "," + afd2.getLength() + "," + afd2.getDeclaredLength());//4300928,60647,60647
			soundIdList.add(soundPool.load(afd2, 0));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			InputStream in = getResources().getAssets().open("data.txt");
			InputStream in2 = getResources().openRawResource(R.raw.s1_ding);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//通过FileDescriptor：FileDescriptor fd, long offset, long length, int priority
		try {
			FileDescriptor fd = getAssets().openFd("voice/s1_fire.ogg").getFileDescriptor();
			Log.i("bqt", "【是否有效 1】" + fd.valid());//true
			soundIdList.set(6, soundPool.load(fd, 0, 1, 0));//Unable to load sample   【onLoadComplete】5  -2147483648
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileDescriptor fd2 = getResources().openRawResourceFd(R.raw.s1_fire).getFileDescriptor();
		Log.i("bqt", "【是否有效 2】" + fd2.valid());//true
		soundIdList.set(7, soundPool.load(fd2, 0, 1, 0));//Unable to load sample   【onLoadComplete】5  -2147483648
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int soundID = soundIdList.get(position);
		Toast.makeText(this, "" + soundID, Toast.LENGTH_SHORT).show();
		soundPool.play(soundID, 1, 1, 1, 0, 1);
	}
}