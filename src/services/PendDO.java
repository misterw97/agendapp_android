package services;

import java.util.ArrayList;

class PendDO extends Pending {

    ArrayList<String[]> pends;

    PendDO() {
        pends = new ArrayList<>();
    }

    public String toString() {
        String json = "[";
        for(String[] pend : pends) {
            json+="{";
            json+="\"id\":"+pend[0]+",";
            json+="\"done\":"+pend[1];
            json+="}";
        }
        json+="]";
        return json;
    }

    void add(Work w) {
        String[] pend = new String[2];
        pend[0] = ""+w.getID();
        pend[1] = ""+w.getDone();
        pends.add(pend);
    }

    static void add(Work w) {
        Pending.pendingList[1].add(w);
    }
}