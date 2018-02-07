package com.littlecat.powerbank.bean;

/**
 * Created by Administrator on 2018/2/5.
 */

public class BatteryInfo {
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSensors() {
        return sensors;
    }

    public void setSensors(int sensors) {
        this.sensors = sensors;
    }

    public long getBat_id() {
        return bat_id;
    }

    public void setBat_id(long bat_id) {
        this.bat_id = bat_id;
    }

    public int getEmpty() {
        return empty;
    }

    public void setEmpty(int empty) {
        this.empty = empty;
    }

    public int getIs_locked() {
        return is_locked;
    }

    public void setIs_locked(int is_locked) {
        this.is_locked = is_locked;
    }

    public int getError_count() {
        return error_count;
    }

    public void setError_count(int error_count) {
        this.error_count = error_count;
    }

    public int getStatus_error_count() {
        return status_error_count;
    }

    public void setStatus_error_count(int status_error_count) {
        this.status_error_count = status_error_count;
    }

    private int status;
    private int sensors;
    private long bat_id;
    private int empty;
    private int is_locked;
    private int error_count;
    private int status_error_count;
}
