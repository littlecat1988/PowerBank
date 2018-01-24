package com.littlecat.powerbank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.littlecat.powerbank.bean.DeviceBean;
import com.littlecat.powerbank.http.HttpCallback;
import com.littlecat.powerbank.http.OkHttpUtils;
import com.littlecat.powerbank.http.ResultDesc;
import com.littlecat.powerbank.serialPort.SerialPort;
import com.littlecat.powerbank.serialPort.SerialPortFinder;
import com.littlecat.powerbank.socket.SocketService;
import com.littlecat.powerbank.util.Constant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    protected BaseApplication mApplication;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private byte[] mBuffer;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
//                        onDataReceived(buffer, size);
                        Log.d("lixiang","  "+buffer.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private class SendingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(mBuffer);
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        b.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readSerialPort();
        syncSetting();

    }

    private void syncSetting() {
        Map<String, String> mMap = new HashMap<>();
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setDevice_ver("2");
        deviceBean.setSoft_ver("22");
        deviceBean.setPush_id("1");
        Gson gson = new Gson();
        gson.toJson(deviceBean);
        mMap.put(Constant.MAC, "12345678901000");
        mMap.put(Constant.DEVICE, gson.toJson(deviceBean));
        OkHttpUtils.postAsyn(Constant.URL+Constant.API_SYNC_SETTING,mMap,new HttpCallback(){
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang","lixiang---onSuccess");
            }

            public void onFailure(int code, String message) {
            }
        });
    }

    private void readSerialPort() {
        try {
            mSerialPort = getSerialPort();
            mBuffer = new byte[1024];
            Arrays.fill(mBuffer, (byte) 0x55);
            if (mSerialPort != null) {
                SendingThread mSendingThread = new SendingThread();
                mSendingThread.start();
            }
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }

//    protected abstract void onDataReceived(final byte[] buffer, final int size);

    @Override
    protected void onDestroy() {
        if(flag==true) {
            unbindService(conn);
            localBroadcastManager.unregisterReceiver(mReciver);
        }
        if (mReadThread != null)
            mReadThread.interrupt();
       closeSerialPort();
        mSerialPort = null;
        super.onDestroy();
    }

    public IBackService iBackService;



    public MessageBackReciver mReciver;
    private IntentFilter mIntentFilter;
    private Intent mServiceIntent;
    private LocalBroadcastManager localBroadcastManager;
    private boolean flag;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iBackService = IBackService.Stub.asInterface(iBinder);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iBackService = null;
        }
    };

    public void initSocket()
    {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mReciver = new MessageBackReciver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("lixiang","lixiang---intent= "+intent.getAction());
            }
        };
        mServiceIntent = new Intent(this,SocketService.class);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(SocketService.HEART_BEAT_ACTION);
        mIntentFilter.addAction(SocketService.MESSAGE_ACTION);
    }

    @Override
    protected void onStart() {
        flag = false;
        if(true)
        {
            flag = true;
            initSocket();
            localBroadcastManager.registerReceiver(mReciver,mIntentFilter);
            flag = bindService(mServiceIntent,conn,BIND_AUTO_CREATE);
//            startService(mServiceIntent);

        }
        super.onStart();
    }
    public abstract class MessageBackReciver extends BroadcastReceiver
    {
        @Override
        public abstract void onReceive(Context context, Intent intent);
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            String path = "/dev/ttyMT0";
            int baudrate = 9600;
			/* Check parameters */
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

}
