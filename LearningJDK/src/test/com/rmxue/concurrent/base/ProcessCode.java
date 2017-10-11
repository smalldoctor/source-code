package com.rmxue.concurrent.base;

public enum ProcessCode {
    Ord_Sec_SerialNewGroup("Ord_Sec_SerialNewGroup", "串行群组组网流程");

    private String processCode;
    private String processName;

    ProcessCode(String processCode, String processName) {
        this.processCode = processCode;
        this.processName = processName;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
