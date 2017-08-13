package net.swmud.trog.dspam.json;


import com.google.gson.Gson;

public class JsonRpc {
    protected static long requestId;

    synchronized protected static long getNextId() {
        return ++requestId;
    }

    public static String getRequest(String method, Object args) {
        return new Gson().toJson(new JsonRequest(method, args, getNextId()));
    }

    private static class JsonRequest {
        private final String jsonrpc = "2.0";
        private String method;
        private Object params;
        private long id;

        public JsonRequest(String method, Object params, long id) {
            this.method = method;
            this.params = params;
            this.id = id;
        }
    }
}
