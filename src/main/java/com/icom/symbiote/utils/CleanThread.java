package com.icom.symbiote.utils;

public class CleanThread extends Thread{
    public CleanThread(){

    }
    public void run(){
        LocalData.cleanInfo();;
    }
}
