package net.swmud.trog.dspam.json;


import java.util.LinkedList;

public class RetrainResponse {
    public Result result;

    public static class Result {
        public LinkedList<String> ok;
        public LinkedList<String> fail;
    }
}
