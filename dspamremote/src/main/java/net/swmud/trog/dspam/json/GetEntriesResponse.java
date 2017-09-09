package net.swmud.trog.dspam.json;

import java.util.ArrayList;
import java.util.List;

public class GetEntriesResponse {
    private String jsonrpc;
    private String id;
    private Result result;

    public List<DspamEntry> getEntries() {
        return result.entries;
    }

    private static class Result {
        ArrayList<DspamEntry> entries;
    }
}
