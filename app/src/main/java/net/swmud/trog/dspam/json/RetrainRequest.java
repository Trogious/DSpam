package net.swmud.trog.dspam.json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RetrainRequest {
    public List<RetrainEntry> entries = new LinkedList<>();

    public RetrainRequest(List<DspamEntry> dspamEntries, boolean[] positionsChecked) {
        for (int i = 0; i < positionsChecked.length; ++i) {
            if (positionsChecked[i]) {
                DspamEntry entry = dspamEntries.get(i);
                entries.add(new RetrainEntry(entry.getSignature(), entry.getSpamStatusText()));
            }
        }
    }

    public RetrainRequest(DspamEntry entry) {
        entries.add(new RetrainEntry(entry.getSignature(), entry.getSpamStatusText()));
    }

    public JsonRpc.JsonRequest getJsonRpcRequest() {
        Map<String, Object> args = new HashMap<>();
        args.put("user", "X");
        args.put("entries", entries);
        return JsonRpc.getRequest("retrain", args);
    }

    public static class RetrainEntry {
        String signature;
        String classification;

        public RetrainEntry(String signature, String classification) {
            this.signature = signature;
            this.classification = classification;
        }
    }
}
