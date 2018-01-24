package com.littlecat.powerbank.bean;

/**
 * Created by Administrator on 2018/1/24.
 */

public class DeviceBean {

    /**
     * device : {"slot_count":8,"total":5,"usable":3,"empty":3,"sdcard":1,"status":0}
     * batteries : [{"beta1":{"id":123,"slot":1,"power":100,"voltage":1234,"current":222,"temperature":0," battery_status ":1,"slot_status":1,"sensors":"111111"}}]
     */


        /**
         * slot_count : 8
         * total : 5
         * usable : 3
         * empty : 3
         * sdcard : 1
         * status : 0
         */

        private String slot_count;
        private String total;
        private String usable;
        private String empty;
        private String sdcard;
        private String status;
    /**
     * soft_ver : 1
     * device_ver : 1
     * push_id : 1
     */

    private String soft_ver;
    private String device_ver;
    private String push_id;

    public String getSlot_count() {
            return slot_count;
        }

        public void setSlot_count(String slot_count) {
            this.slot_count = slot_count;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getUsable() {
            return usable;
        }

        public void setUsable(String usable) {
            this.usable = usable;
        }

        public String getEmpty() {
            return empty;
        }

        public void setEmpty(String empty) {
            this.empty = empty;
        }

        public String getSdcard() {
            return sdcard;
        }

        public void setSdcard(String sdcard) {
            this.sdcard = sdcard;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    public String getSoft_ver() {
        return soft_ver;
    }

    public void setSoft_ver(String soft_ver) {
        this.soft_ver = soft_ver;
    }

    public String getDevice_ver() {
        return device_ver;
    }

    public void setDevice_ver(String device_ver) {
        this.device_ver = device_ver;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }
}

