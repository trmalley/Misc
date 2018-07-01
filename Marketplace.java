package com.gameofrobots.robotcontroller1;

/**
 * Created by Thomas on 2017-11-16.
 */

import java.util.Timer;
import java.util.TimerTask;


public class Marketplace<E> {

    private Object[] data;
    private int manyItems;


    public Marketplace(){
        final int INITIAL_CAPACITY = 10;
        manyItems = 0;
        data = new Object[INITIAL_CAPACITY];
    }

    public Marketplace(int initialCapacity){
        if(initialCapacity< 0)
            throw new IllegalArgumentException("The initialCapacity is negative: " + initialCapacity );
        data = new Object[initialCapacity];
        manyItems = 0;
    }

    public void add(Item element){
        if(manyItems == data.length){
            ensureCapacity((manyItems + 1) * 2);
        }
        data[manyItems] = element;
        manyItems++;
    }

    public void ensureCapacity(int minimumCapacity){
        Object biggerArray[];
        if(data.length < minimumCapacity){
            biggerArray = new Object[minimumCapacity];
            System.arraycopy(data, 0, biggerArray, 0, manyItems);
            data = biggerArray;
        }
    }

    public boolean remove(Item target){
        int index;
        if(target == null){ //find first occurence of the target in the bag
            index = 0;
            while((index < manyItems) && (data[index] != null))
                index++;
        }
        else{//find first occurrence of the target in the bag
            index =0;
            while((index < manyItems) && (!target.equals(data[index])))
                index++;
        }
        if (index == manyItems) {//target was not found, nothing removed
            //add something here to catch when there are no more items of a specific kind??****
            return false;
        }
        else{//target was found at data[index]
            manyItems--;
            data[index] = data[manyItems];
            data[manyItems] = null;
            return true;
        }
    }

    //Need to have count method to ensure there are buffs available

    /* End Of Object Array Class*/





    /*
    methods to purchase and use buffs
    */

    public void buyBuff(Item i){
        add(i);
    }

    public void useSpeed(){
        //If marketplace.count(speed) > 1
        //Remove one speed
        //Send Big I to robot and start timer for speed boost
        new Reminder(5);
    }

    public void heal(){

    }

}

    //Bank class
    /*
    Not used right now but coded for future implementation of a full marketplace
     */
 class Bank {

        private int bank;

        public Bank(){
            bank = 1200;
        }
        public Bank(int startingBank){
            bank =  startingBank;
        }
    /*
    Getters and Setters for Bank
     */

        public void bank_add(int in) {
            bank += in;
        }

        public String bank_spend(int out) {
            if ((out - bank) >= 0) {
                bank -= out;
                return "Payment Successful";
            } else {
                return "Error!: Not Enough Cash!!";
            }
        }

        public int getBank() {
            return bank;
        }
//End of Getters and Setters for Bank

    }

class Item {

    int type;
    int length;
    private final int MAX_BOOST = 10;
    String name;

    /*
    Boost can be one of three types
    Type is int to allow for expansion into more "types of boosts
    Quick guide
    1 = Speed; (Speed up robot for 10 seconds)
    2 = heal; (Gain a set amount of health
    3 = invinc = invincibility (Cannot be shot for 10 seconds)

    ??Future Boosts??
        Point multiplier
        Auto Shoot
        IR Receiver immunity (Sheild?)
     */


    public Item(int type){
        if(type == 1){
            name = "Speed Boost";
            length = 10;
        }
        else if(type == 2){
            name = "Heal";
        }
        else if(type == 3){
            name = "Sheild";
        }
    }

    public void speedBoost(){

    }

    public void healRobot(){

    }



}

//may need to be public
 class Reminder {

    Timer lifeSpan;

    public  Reminder(int seconds){
        lifeSpan = new Timer();
        lifeSpan.schedule(new RemindTask(), seconds*1000);
    }

    class RemindTask extends TimerTask{
        public void run(){
            System.out.println("Times Up!");
            lifeSpan.cancel();
        }
    }
}