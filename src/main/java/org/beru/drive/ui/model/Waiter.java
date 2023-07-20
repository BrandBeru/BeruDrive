package org.beru.drive.ui.model;

import java.util.function.Function;

public class Waiter extends Thread{
    private int seconds;
    private WaiterFunction function;

    public Waiter(int seconds, WaiterFunction function) {
        this.seconds = seconds;
        this.function = function;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void run(){
        while(seconds > 0){
            try {
                Thread.sleep(1000);
                seconds--;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        function.execute();
    }
}
