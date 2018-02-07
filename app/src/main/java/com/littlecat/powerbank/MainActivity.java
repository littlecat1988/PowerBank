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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.littlecat.powerbank.bean.BatteryBean;
import com.littlecat.powerbank.bean.BatteryInfo;
import com.littlecat.powerbank.bean.DeviceBean;
import com.littlecat.powerbank.bean.MachineBean;
import com.littlecat.powerbank.bean.SettingBean;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MainActivity extends Activity{
    byte uart_send_buf_10[] = {(byte) 0x55, (byte) 0xAA, 0x1A, 0x01, 0x00, 0x10,
            0x04, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x5D, 0x03, 0x00, 0x29,
            0x17, (byte) 0x8A, (byte) 0xC1, 0x00, 0x00, 0x00,
            0x1B, (byte) 0xAA, 0X55};

    byte uart_send_buf_12[] = {0x55, (byte) 0xAA, 0x1B, 0x01, 0x00, 0x12,
            (byte) 0x88, (byte) 0x80, 0x5D, 0x03, 0x00, 0x29, 0x17, (byte) 0x8B,
            0x26, 0x00, 0x00, 0x00, (byte) 0x80, (byte) 0xC4, 0x01, 0x00, 0x29,
            0x17, (byte) 0x88, (byte) 0xFD, 0x00, 0x00, 0x00,
            (byte) 0x91, (byte) 0xAA, 0X55};
    protected BaseApplication mApplication;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private byte[] mBuffer;
    Gson gson = new Gson();
    private SettingBean settingBean;
    private long sync_interval = -1;
    private long temp_sync_interval = -1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    syncSetting();
                    break;
                case 1:
                    syncBattery();
                    break;
                default:
                    break;
            }
        }
    };
    private Byte batteryCount;
    private int battery_addr;
    private Map<String,Object> batteryMap = new HashMap<String,Object>();
    private Long batteryId;
    private byte[] cmd_param_23 = new byte[23];
    private byte[] cmd_param_22 = new byte[22];
    private byte[] battery_info1 = new byte[10];
    private byte[] battery_info2 = new byte[10];

    public void setByteArray(byte[] byteArray) {
        this.mBuffer = byteArray;
    }


    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null)
                        return;
                    size = mInputStream.read(buffer);
                    byte[] read = new byte[size];
                    System.arraycopy(buffer, 0, read, 0, size);
                    if (size > 0) {
//                        onDataReceived(buffer, size);
                        parseReceiveData(read);
//                        String reader = new String(read);
                        Log.d("lixiang", "  " + read[5]);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void parseReceiveData(byte[] read) {
        //解析串口data
        int length = read.length;
        if(length < 5){
            return;
        }
        int cmd = read[5];
        switch(cmd){
            case Constant.PORT_CMD_BATTERIES_COUNT:
                batteryCount = read[6];
                sendDataRepeat(mBuffer);
                syncSetting();
                break;
                case Constant.PORT_CMD_BATTERIES_INFO:
                    if(read.length != 32){
                        Log.e(Constant.TAG,"wrong battery group info !");
                        return;
                    }
LinkedList<String> linkedList = new LinkedList<String>();
                    linkedList.poll();
                    battery_addr = read[3];
                    System.arraycopy(read,6,cmd_param_23,0,23);
                    BatteryInfo batteryInfo1 = new BatteryInfo();
                    BatteryInfo batteryInfo2 = new BatteryInfo();
                    batteryInfo1.setStatus(Constant.getHeight4(cmd_param_23[0]));
                    batteryInfo2.setStatus(Constant.getLow4(cmd_param_23[0]));
                    System.arraycopy(cmd_param_23,2,battery_info1,0,10);
                    System.arraycopy(cmd_param_23,13,battery_info1,0,10);
                    batteryInfo1.setBat_id(Constant.byteArrayToInt(battery_info2));
                    batteryInfo2.setBat_id(Constant.byteArrayToInt(battery_info2));
                    batteryMap.put(String.valueOf(battery_addr-1),batteryInfo1);
                    batteryMap.put(String.valueOf(battery_addr),batteryInfo2);

                    BatteryInfo batteryInfo = new BatteryInfo();

                    break;
        }
    }

    private void sendDataRepeat(byte[] mBuffer) {
        SendingThread mSendingThread = new SendingThread();
        mSendingThread.start();
    }

    private class SendingThread extends Thread {
        private int index = 0;

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null) {
                        if(index > batteryCount){
                            index = 0;
                        }
                        index++;
                        setByteArray(getSendData(Constant.PORT_CMD_BATTERIES_COUNT,index));
                        mOutputStream.write(mBuffer);
                        Thread.sleep(250);
                        Log.d("lixiang", "lixiang---send");
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] getSendData(int cmd,int des) {
        byte[] bytes = uart_send_buf_10;
        byte[] cmd_param;
        if(cmd == Constant.PORT_CMD_BATTERIES_INFO){
            cmd_param = new byte[23];
        }else{
            cmd_param = new byte[22];
        }
        bytes[4] = (byte)des;
        bytes[5] = (byte)cmd;
        bytes[28] = (byte)(bytes[2]+bytes[4]+bytes[5]);
        System.arraycopy(cmd_param,0,bytes,6,cmd_param.length);
        return  bytes;

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
        setByteArray(uart_send_buf_10);
        readSerialPort();
        sendDataToPort(mBuffer);
//        syncSetting();

    }

    private void sendDataToPort(byte[] mBuffer) {
        if (mSerialPort != null) {
            mOutputStream = mSerialPort.getOutputStream();
            try {
                mOutputStream.write(this.mBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void syncBattery() {
        mHandler.sendEmptyMessageDelayed(1, sync_interval * 1000);
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setSlot_count("8");
        deviceBean.setTotal("5");
        deviceBean.setUsable("1");
        deviceBean.setEmpty("4");
        deviceBean.setSdcard("1");
        deviceBean.setStatus("0");
        BatteryBean batteryBean = new BatteryBean();
        batteryBean.setBatteryInfo("123", "1", "100", "1234", "222", "0", "1", "1", "111");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", batteryBean);
        MachineBean machineBean = new MachineBean();
        machineBean.setDeviceBean(deviceBean);
        machineBean.setBatteryBeanList(map);
        String json = gson.toJson(machineBean);
        OkHttpUtils.postAync(Constant.URL + Constant.API_SYNC_BATTERY + settingBean.getDevice_id(), json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    Log.d("lixiang", "lixiang---code= " + resultDesc.getError_code() + "  msg= " + resultDesc.getReason());
                }
            }
        });
    }

    private void syncSetting() {
        mHandler.sendEmptyMessageDelayed(0, Constant.SYNC_SETTING_TIME);
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setDevice_ver("2");
        deviceBean.setSoft_ver("22");
        deviceBean.setPush_id("1");
        MachineBean machineBean = new MachineBean();
        machineBean.setDeviceBean(deviceBean);
        machineBean.setMac("123456789010000");
        String json = gson.toJson(machineBean);
        OkHttpUtils.postAync(Constant.URL + Constant.API_SYNC_SETTING, json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    String result = resultDesc.getResult();
                    if (null != result) {
                        settingBean = gson.fromJson(result, SettingBean.class);

                        try {
                            sync_interval = Long.valueOf(settingBean.getSync_interval());

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (sync_interval != -1 && sync_interval != temp_sync_interval) {
                            syncBattery();
                            temp_sync_interval = sync_interval;
                        }
                    } else {

                    }
                }
            }

            public void onFailure(int code, String message) {
            }
        });
    }

    private void sendTcpLogin() {
        if (null != iBackService) {
            String msg = Constant.getMsg(Constant.TCP_CMD_LOGIN, "10000");
            try {
                iBackService.sendMessage(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }


    private void readSerialPort() {
        try {
            mSerialPort = getSerialPort();

            if (mSerialPort != null) {
                mInputStream = mSerialPort.getInputStream();
                mReadThread = new ReadThread();
                mReadThread.start();
            }
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
        if (flag == true) {
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
    private boolean flag = true;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iBackService = IBackService.Stub.asInterface(iBinder);
//            sendTcpMsg();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iBackService = null;
        }
    };

    public void initSocket() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mReciver = new MessageBackReciver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("lixiang", "lixiang---intent= " + intent.getAction());
                String action = intent.getAction();
                if (action.equals(SocketService.SOCKET_INIT_ACTION)) {
                    sendTcpLogin();
                } else if (action.equals(Constant.INTENT_BORROW_CONFIRM)) {
                    String order_id = intent.getStringExtra(Constant.ORDER_ID);
                    borrowConfirm(order_id);
                }
            }
        };
        mServiceIntent = new Intent(this, SocketService.class);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(SocketService.HEART_BEAT_ACTION);
        mIntentFilter.addAction(SocketService.MESSAGE_ACTION);
        mIntentFilter.addAction(SocketService.SOCKET_INIT_ACTION);
        mIntentFilter.addAction(Constant.INTENT_BORROW_CONFIRM);

    }

    private void borrowConfirm(String order_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> subMap = new HashMap<String, Object>();
        map.put("orderid", order_id);
        map.put("status", 21);
        map.put("retry", 0);
        subMap.put("id", 123);
        map.put("battery", subMap);
        String json = gson.toJson(map);
        OkHttpUtils.postAync(Constant.URL + Constant.API_SYNC_BATTERY + settingBean.getDevice_id(), json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    if (resultDesc.getError_code() == 0) {
                        //Todo
                        //通知串口弹出电池

                    } else {

                    }
                }
            }

            public void onFailure(int code, String message) {
            }
        });
    }

    @Override
    protected void onStart() {
        flag = false;
        if (!flag) {
            initSocket();
            localBroadcastManager.registerReceiver(mReciver, mIntentFilter);
            flag = bindService(mServiceIntent, conn, BIND_AUTO_CREATE);
//            startService(mServiceIntent);

        }
        super.onStart();
    }

    public abstract class MessageBackReciver extends BroadcastReceiver {
        @Override
        public abstract void onReceive(Context context, Intent intent);
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            String path = "dev/ttyMT0";
            int baudrate = 38400;
            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
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
