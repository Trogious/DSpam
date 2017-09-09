package net.swmud.trog.dspam.json;

public class JsonResponse {
    private String jsonrpc;
    private String id;
    private Object result;

    public long getId() {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
        }

        return 0;
    }
}
