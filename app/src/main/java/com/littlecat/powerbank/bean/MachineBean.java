package com.littlecat.powerbank.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/1/24.
 */

public class MachineBean {
    private DeviceBean deviceBean;
    private List<BatteryBean> batteryBeanList;

    public DeviceBean getDeviceBean() {
        return deviceBean;
    }

    public void setDeviceBean(DeviceBean deviceBean) {
        this.deviceBean = deviceBean;
    }

    public List<BatteryBean> getBatteryBeanList() {
        return batteryBeanList;
    }

    public void setTotal( List<BatteryBean> batteryBeanList) {
        this.batteryBeanList = batteryBeanList;
    }
}
