package net.apnic.whowas.intervaltree.avl;

import net.apnic.whowas.intervaltree.Interval;

import java.io.Serializable;

class IntInterval implements Interval<Integer>, Serializable {
    private final int l, h;

    IntInterval(int l, int h) {
        this.l = l;
        this.h = h;
    }

    @Override
    public Integer low() {
        return l;
    }

    @Override
    public Integer high() {
        return h;
    }

    @Override
    public String toString() {
        return "(" + l + " - " + h + ")";
    }

    @Override
    public int hashCode() {
        return 17 + l * 31 + h * 13;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof IntInterval
                && ((IntInterval)other).l == l && ((IntInterval)other).h == h;
    }

}
