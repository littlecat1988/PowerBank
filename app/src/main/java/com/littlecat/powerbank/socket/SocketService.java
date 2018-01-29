package com.littlecat.powerbank.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.littlecat.powerbank.IBackService;
import com.littlecat.powerbank.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;


public class SocketService extends Service {
    private static final String TAG = "BackService";
    //心跳包频率
    private static final long HEART_BEAT_RATE = 90 * 1000;

    public static final String HOST = "118.31.15.186";// //
    public static final int PORT = 8331;

    public static final String MESSAGE_ACTION = "com.littlecat.powerbank.socket";
    public static final String HEART_BEAT_ACTION = "com.littlecat.powerbank.socket.heart";
    public static final String SOCKET_INIT_ACTION = "com.littlecat.powerbank.socket.init";

    public static final String HEART_BEAT_STRING = "15";//心跳包内容

    private ReadThread mReadThread;

    private LocalBroadcastManager mLocalBroadcastManager;

    private WeakReference<Socket> mSocket;

    // For heart Beat
    private Handler mHandler = new Handler();
    private boolean isSuccess;
    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                mHandler.removeCallbacks(heartBeatRunnable);
                mReadThread.release();
                releaseLastSocket(mSocket);
                new InitSocketThread().start();
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    private long sendTime = System.currentTimeMillis();
    private IBackService.Stub iBackService = new IBackService.Stub() {

        @Override
        public boolean sendMessage(String message) throws RemoteException {
            sendMsg(message);
            return isSuccess;
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return iBackService;
    }

    @Override
    public void onCreate() {
        Log.d("lixiang", "lixiang---flag");
        super.onCreate();
        new InitSocketThread().start();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    public void sendMsg(final String msg) {
        if (null == mSocket || null == mSocket.get()) {
//            isSuccess = false;
//            return ;
        }

        new Thread() {
            @Override
            public void run() {
                Socket soc = mSocket.get();
                try {
                    if (!soc.isClosed() && !soc.isOutputShutdown()) {
                        OutputStream os = soc.getOutputStream();
                        String message = msg;
                        int length = message.getBytes().length;
                        byte[] bytes = new byte[length + 4];
                        bytes[0] = (byte) (bytes.length & 0xff);
                        bytes[1] = (byte) (bytes.length >> 8 & 0xff);
                        bytes[2] = (byte) (bytes.length >> 16 & 0xff);
                        bytes[3] = (byte) (bytes.length >> 24 & 0xff);
//                        bytes[0] = (byte)length;
                        System.arraycopy(message.getBytes(), 0, bytes, 4, length);
                        os.write(bytes);
                        os.flush();
//                        sendTime = S
                    } else {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initSocket() {//初始化Socket
        try {
            Socket so = new Socket(HOST, PORT);
            mSocket = new WeakReference<Socket>(so);
            mReadThread = new ReadThread(so);
            mReadThread.start();
            Intent intent = new Intent(SOCKET_INIT_ACTION);
            mLocalBroadcastManager.sendBroadcast(intent);
            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (!sk.isClosed()) {
                    sk.close();
                }
                sk = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }


    // Thread to read content from Socket
    class ReadThread extends Thread {
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mWeakSocket = new WeakReference<Socket>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown()
                            && isStart && ((length = is.read(buffer)) != -1)) {
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer,
                                    length)).trim();
                            Log.e(TAG, message);
                            String[] info = message.split("\\|");
                            try {
                                String cmd = info[0];
                                switch (cmd) {
                                    case Constant.TCP_CMD_LOGIN:
                                        sendTcpMsg(Constant.TCP_CMD_ANS_LOGIN, info[1], "0");
                                        Intent intent = new Intent(Constant.INTENT_BORROW_CONFIRM);
                                        intent.putExtra(Constant.ORDER_ID,info[1]);
                                        mLocalBroadcastManager.sendBroadcast(intent);
                                        break;
                                    case Constant.TCP_CMD_ORDER:
                                        break;
                                    case Constant.TCP_CMD_SLOT:
                                        break;
                                    case Constant.TCP_CMD_HEART:
                                        sendTime = System.currentTimeMillis();
                                        break;
                                }
                               if (cmd.equals(Constant.TCP_CMD_HEART)) {//处理心跳回复
                                    sendTime = System.currentTimeMillis();
                                    Intent intent = new Intent(HEART_BEAT_ACTION);
                                    mLocalBroadcastManager.sendBroadcast(intent);
                                } else {
                                    //其他消息回复
                                    Intent intent = new Intent(MESSAGE_ACTION);
                                    intent.putExtra("message", message);
                                    mLocalBroadcastManager.sendBroadcast(intent);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendTcpMsg(String... info) {
        String msg = Constant.getMsg(info);
        sendMsg(msg);
    }
}