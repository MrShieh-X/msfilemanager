package com.mrshiehx.file.viewer.beans;

public class HexItem {
    public static final int NULL=0;
    private int first;
    private int second;
    private int third;
    private int fourth;
    private int fifth;
    private int sixth;
    private int seventh;
    private int eighth;

    public HexItem(int first, int second, int third, int fourth, int fifth, int sixth, int seventh, int eighth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
        this.seventh = seventh;
        this.eighth = eighth;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public void setThird(int third) {
        this.third = third;
    }

    public void setFourth(int fourth) {
        this.fourth = fourth;
    }

    public void setFifth(int fifth) {
        this.fifth = fifth;
    }

    public void setSixth(int sixth) {
        this.sixth = sixth;
    }

    public void setSeventh(int seventh) {
        this.seventh = seventh;
    }

    public void setEighth(int eighth) {
        this.eighth = eighth;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public int getThird() {
        return third;
    }

    public int getFourth() {
        return fourth;
    }

    public int getFifth() {
        return fifth;
    }

    public int getSixth() {
        return sixth;
    }

    public int getSeventh() {
        return seventh;
    }

    public int getEighth() {
        return eighth;
    }

    public CharSequence getRightFirst() {
        return first!=NULL?new String(new byte[]{(byte)first}):"";
    }

    public CharSequence getRightSecond() {
        return second!=NULL?new String(new byte[]{(byte)second}):"";
    }

    public CharSequence getRightThird() {
        return third!=NULL?new String(new byte[]{(byte)third}):"";
    }

    public CharSequence getRightFourth() {
        return fourth!=NULL?new String(new byte[]{(byte)fourth}):"";
    }

    public CharSequence getRightFifth() {
        return fifth!=NULL?new String(new byte[]{(byte)fifth}):"";
    }

    public CharSequence getRightSixth() {
        return sixth!=NULL?new String(new byte[]{(byte)sixth}):"";
    }

    public CharSequence getRightSeventh() {
        return seventh!=NULL?new String(new byte[]{(byte)seventh}):"";
    }

    public CharSequence getRightEighth() {
        return eighth!=NULL?new String(new byte[]{(byte)eighth}):"";
    }
}
