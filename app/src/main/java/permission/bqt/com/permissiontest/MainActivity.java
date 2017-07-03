package permission.bqt.com.permissiontest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ListActivity {
	
	private static final int REQUESTCODE = 20094;
	private static final int REQUESTCODE2 = 20095;
	private static final int REQUESTCODE3 = 20096;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"在没有申请权限的情况下在SD卡创建文件会失败",
				"完整的授权过程演示",
				"",
				"",
				"",
				"",};
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				createFileWithoutRequestPermission(this);
				break;
			case 1:
				requestPermissionBeforeCreateFile();
				break;
			case 2:
				break;
			case 3:
				
				break;
			case 4:
				
				break;
		}
	}

	/**
	 * 如果将targetSdkVersion改为22或以下，可以成功创建文件
	 * 相反，如果改为23或以上，则失败
	 */
	public static void createFileWithoutRequestPermission(Context context) {
		String fileName = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss", Locale.getDefault()).format(new Date());
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName + ".txt");
		boolean createFile = false;
		try {
			createFile = file.createNewFile();//没有申请权限时就在SD卡创建文件会失败（当然，如果文件已经存在，返回值也是false）
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Toast.makeText(context, "创建文件结果：" + createFile, Toast.LENGTH_SHORT).show();
		}
	}

	private void requestPermissionBeforeCreateFile() {
		//检查权限。结果：PERMISSION_GRANTED=0，有权限；PERMISSION_DENIED=-1，没有权限
		int state = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		Toast.makeText(this, "写SD卡权限状态：" + state, Toast.LENGTH_SHORT).show();
		
		if (state != PackageManager.PERMISSION_GRANTED) {// 没有权限，申请权限
			//是否应该显示请求权限的说明。
			//加这个提醒的好处在于：第一次申请权限时不需要麻烦用户，但如果用户拒绝过一次权限后我们再次申请时
			//可以提醒用户授予该权限的必要性，免得再次申请时用户勾选"不再提醒"并拒绝，导致下次申请权限直接失败
			//返回值的特点：第一次请求权限之前调用返回false（不应该提醒）；如果用户拒绝了，则下次调用返回true（应该提醒）
			//如果之后再次请求权限时，用户拒绝了并选择了"不再提醒"，则下次调用返回false（不应该提醒，且不弹授权对话框）
			boolean state2 = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (state2) {
				// 一般是通过弹一个自定义的对话框告诉用户，我们为什么需要这个权限
				new AlertDialog.Builder(this).setTitle("请求读写SD卡权限").setMessage("请求SD卡权限，作用是给你保存妹子图片")
						.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityCompat.requestPermissions(MainActivity.this,
										new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTCODE);
							}
						}).create().show();
			} else {
				//请求用户授权几个权限，调用后系统会显示一个请求用户授权的提示对话框，开发者不能修改这个对话框
				ActivityCompat.requestPermissions(MainActivity.this,//api 23以后可以调用Activity的requestPermissions方法
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},//需要申请的权限数组
						REQUESTCODE);//请求码，会在回调onRequestPermissionsResult()时返回
			}
		} else {// 有权限了，去放肆吧。
			createFileWithoutRequestPermission(this);
		}
	}

	@Override
	//当用户处理完授权操作时，会回调Activity或Fragment的此方法，参数为：权限数组、授权结果数组
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0) {
			switch (requestCode) {
				case REQUESTCODE: {
					if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						createFileWithoutRequestPermission(this);
					} else {
						Toast.makeText(this, "你竟然拒绝了权限，哼，我将在3秒后关闭！", Toast.LENGTH_SHORT).show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								finish();
							}
						}, 3 * 1000);
					}
				}
			}
		}
	}
}