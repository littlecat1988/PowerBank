package com.littlecat.powerbank.util;

public class Constant {

    public static final String LOG_TAG = "logTag";
    public static final String URL = "http://ftj.gongxiangchongdianbao.org";
    public static final String API_SYNC_SETTING = "/api/device/sync_setting";
    public static final String API_SYNC_BATTERY = "/api/device/sync_battery?device_id=";
    public static final String API_REMOVE_BATTERY = "/api/device/remove_battery?device_id=";
    public static final String API_BORROW_CONFIRM = "/api/device/borrow_confirm?device_id=";
    public static final String API_RETURN_BACK = "/api/device/return_back?device_id=";
    public static final String SUCCESS = "0";
    public static final String FAILURE = "1";
    public static final String MAC = "mac";
    public static final String DEVICE = "device";
    public static final String SOFT_VER = "soft_ver";
    public static final String DEVICE_VER = "device_ver";
    public static final String PUSH_ID = "push_id";
    public static final String TCP_CMD_ANS_LOGIN = "20";
    public static final String TCP_CMD_LOGIN = "10";
    public static final String TCP_CMD_ORDER = "11";
    public static final String TCP_CMD_ANS_ORDER = "21";
    public static final String TCP_CMD_SLOT = "12";
    public static final String TCP_CMD_ANS_SLOT = "22";
    public static final String TCP_CMD_RESTART = "13";
    public static final String TCP_CMD_RESETTING = "14";
    public static final String TCP_CMD_HEART = "15";
    public static final String SPLIT = "|";
    public static final String ERR_CODE = "errcode";
    public static final String ERR_MSG = "errmsg";
    public static final String RESPONSE_RESULT = "result";
    public static final String INTENT_BORROW_CONFIRM = "borrow_confirm";
    public static final String ORDER_ID = "order_id";

    public static String getMsg(String... info) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : info) {
            stringBuilder.append(str).append(Constant.SPLIT);
        }
        int checksum = 0;
        for (byte b : stringBuilder.toString().getBytes()) {
            checksum += b;
        }
        String temp = stringBuilder.append(checksum).toString();
        return temp;
    }
}