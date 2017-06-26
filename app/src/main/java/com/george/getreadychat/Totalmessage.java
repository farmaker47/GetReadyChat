package com.george.getreadychat;

/**
 * Created by farmaker1 on 26/06/2017.
 */

public class Totalmessage {

    private String firstEntry;
    private String secondEnrty;

    public Totalmessage(){

    }

    public Totalmessage(String firstentry,String secondentry){
        this.firstEntry = firstentry;
        this.secondEnrty = secondentry;
    }

    public String getFirstEntry(){
        return firstEntry;
    }
    public  void setFirstEntry(String firstentry){
        this.firstEntry = firstentry;
    }
    public String getSecondEnrty(){
        return secondEnrty;
    }
    public void setSecondEnrty(String secondentry){
        this.secondEnrty = secondentry;
    }
}
