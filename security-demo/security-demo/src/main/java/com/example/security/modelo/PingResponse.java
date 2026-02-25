package com.example.security.modelo;

import java.util.List;

public class PingResponse {
    private String output;
    private List<Integer> pingTimes;
    private int packetsLost;
    
    // Getters y setters
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    
    public List<Integer> getPingTimes() { return pingTimes; }
    public void setPingTimes(List<Integer> pingTimes) { this.pingTimes = pingTimes; }
    
    public int getPacketsLost() { return packetsLost; }
    public void setPacketsLost(int packetsLost) { this.packetsLost = packetsLost; }
}