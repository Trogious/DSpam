package net.swmud.trog.dspam.json;


import com.google.gson.Gson;

public class JsonRpc {
    protected static long requestId;

    synchronized protected static long getNextId() {
        return ++requestId;
    }

    public static JsonRequest getRequest(String method, Object args) {
        Request request = new Request(method, args, getNextId());
        return new JsonRequest(new Gson().toJson(request), request.id);
    }

    public static class JsonRequest {
        private String str;
        private long id;

        private JsonRequest(String requestStr, long requestId) {
            this.str = requestStr;
            this.id = requestId;
        }

        @Override
        public String toString() {
            return str;
        }

        public long getId() {
            return id;
        }
    }

    private static class Request {
        private final String jsonrpc = "2.0";
        private String method;
        private Object params;
        private long id;

        public Request(String method, Object params, long id) {
            this.method = method;
            this.params = params;
            this.id = id;
        }
    }
}
