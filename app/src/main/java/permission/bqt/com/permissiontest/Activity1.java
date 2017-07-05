package permission.bqt.com.permissiontest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class Activity1 extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		setContentView(tv);
		tv.setBackgroundColor(Color.YELLOW);
		tv.setText("PermissionsDispatcher完整演示");
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(v -> Activity1PermissionsDispatcher.createFileWithCheck(Activity1.this));
	}

	@NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void createFile() {
		Toast.makeText(this, "【NeedsPermission，用户允许了该权限】", Toast.LENGTH_SHORT).show();
		MainActivity.createFileWithoutRequestPermission(Activity1.this);
	}

	@OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void doOnPermissionDenied() {
		Toast.makeText(this, "【OnPermissionDenied，用户拒绝了该权限】", Toast.LENGTH_SHORT).show();
	}

	@OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void doOnShowRationale(final PermissionRequest request) {
		Toast.makeText(this, "【OnShowRationale，此时应该显示请求权限的说明】", Toast.LENGTH_SHORT).show();
		// 一般是通过弹一个自定义的对话框告诉用户，我们为什么需要这个权限
		new AlertDialog.Builder(this)
				.setTitle("请求读写SD卡权限").setMessage("请求SD卡权限，这样我才能给你保存妹子图片哦")
				.setPositiveButton("我知道了", (dialog, which) -> request.proceed())
				.setNegativeButton("我不需要", (dialog, which) -> request.cancel())
				.create().show();
	}

	@OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	void doOnNeverAskAgain() {
		Toast.makeText(this, "你拒绝了读写SD卡权限并选择了\"不再提醒\"，" +
				"\n已经没法愉快的玩耍了，我将在3秒后关闭！", Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(this::finish, 3 * 1000);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Activity1PermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}
}