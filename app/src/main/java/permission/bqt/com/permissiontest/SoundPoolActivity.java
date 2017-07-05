package permission.bqt.com.permissiontest;

import android.app.ListActivity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundPoolActivity extends ListActivity {
	private SoundPool soundPool;
	private List<Integer> soundIdList = new ArrayList<>();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"s1_ding",
				"s1_fire",
				"s1_global",
				"s1_message",
				"s1_system",

				"s10_bgm，可以完整播放",
				"cf_bgm1，只能播放一点点",
				"cf_bgm2，只能播放一点点",
				"cf_bgm3，只能播放一点点",
				"cf_bgm4，只能播放一点点",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
		
		soundPool = new SoundPool.Builder()
				.setMaxStreams(5)
				.setAudioAttributes(new AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_MEDIA)
						.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
				.build();
		
		Context context = this.getApplicationContext();//不管使用哪个Context，退出当前Activity后都会且会在延迟几秒钟后停止播放
		soundIdList.add(soundPool.load(context, R.raw.s1_ding, 0));
		soundIdList.add(soundPool.load(context, R.raw.s1_fire, 0));
		soundIdList.add(soundPool.load(context, R.raw.s1_global, 0));
		soundIdList.add(soundPool.load(context, R.raw.s1_message, 0));
		soundIdList.add(soundPool.load(context, R.raw.s1_system, 0));

		soundIdList.add(soundPool.load(context, R.raw.s10_bgm, 0));
		soundIdList.add(soundPool.load(context, R.raw.cf_bgm1, 0));
		soundIdList.add(soundPool.load(context, R.raw.cf_bgm2, 0));
		soundIdList.add(soundPool.load(context, R.raw.cf_bgm3, 0));
		soundIdList.add(soundPool.load(context, R.raw.cf_bgm4, 0));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		soundPool.play(soundIdList.get(position), 1, 1, 0, 0, 1);
	}
}