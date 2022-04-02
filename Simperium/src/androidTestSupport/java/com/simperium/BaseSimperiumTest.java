package com.simperium;

import com.simperium.client.Change;
import com.simperium.util.Logger;

import junit.framework.TestCase;

class BaseSimperiumTest extends TestCase {

    public static final String TAG="SimperiumTest";

    static protected void waitFor(Change change){
        long timeout = 200; // 100 ms timeout
        long start = System.currentTimeMillis();
        Logger.log(TAG, String.format("Waiting for change %s", change));
        tick();
        while(change.isPending()){
            tick();
            if (System.currentTimeMillis() - start > timeout) {
                throw( new RuntimeException("Change timed out") );
            }
        }
        Logger.log(TAG, String.format("Done waiting %s", change));
    }
    
    static protected void waitFor(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Logger.log("Interupted");
        }
    }

    static protected void tick(){
        waitFor(1);
    }
    

}
