package com.littlecat.powerbank.bean;

/**
 * Created by Administrator on 2018/1/24.
 */

public class BatteryBean {

    /**
     * battery : {"id":123,"slot":1,"power":100,"voltage":1234,"current":222,"temperature":0," battery_status ":1,"slot_status":1,"sensors":"111111"}
     */

    private BatteryBean battery;

        /**
         * id : 123
         * slot : 1
         * power : 100
         * voltage : 1234
         * current : 222
         * temperature : 0
         *  battery_status  : 1
         * slot_status : 1
         * sensors : 111111
         */

    private String id;
    private String slot;
    private String power;
    private String voltage;
    private String current;
    private String temperature;
    private String battery_status;
    private String slot_status;
    private String sensors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getBattery_status() {
        return battery_status;
    }

    public void setBattery_status(String battery_status) {
        this.battery_status = battery_status;
    }

    public String getSlot_status() {
        return temperature;
    }

    public void setSlot_status(String slot_status) {
        this.slot_status = slot_status;
    }

    public String getSensors() {
        return sensors;
    }

    public void setSensors(String sensors) {
        this.sensors = sensors;
    }


    public void setBatteryInfo(String id, String slot, String power, String voltage, String current, String temperature, String battery_status, String slot_status, String sensors) {
        setId(id);
        setSlot(slot);
        setPower(power);
        setVoltage(voltage);
        setCurrent(current);
        setTemperature(temperature);
        setBattery_status(battery_status);
        setSlot_status(slot_status);
        setSensors(sensors);
    }
}
