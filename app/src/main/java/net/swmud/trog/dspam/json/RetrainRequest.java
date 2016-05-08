package net.swmud.trog.dspam.json;

import java.util.LinkedList;
import java.util.List;

public class RetrainRequest {
    public List<RetrainEntry> retrain = new LinkedList<>();

    public RetrainRequest(Dspam d, boolean[] positionsChecked) {
        for (int i = 0; i < positionsChecked.length; ++i) {
            if (positionsChecked[i]) {
                DspamEntry entry = d.dspam.get(i);
                retrain.add(new RetrainEntry(entry.getSignature(), entry.getSpamStatusText(), "X"));
            }
        }
    }

    public RetrainRequest(DspamEntry entry) {
        retrain.add(new RetrainEntry(entry.getSignature(), entry.getSpamStatusText(), "X"));
    }

    public static class RetrainEntry {
        String signature;
        String classification;
        String user;

        public RetrainEntry(String signature, String classification, String user) {
            this.signature = signature;
            this.classification = classification;
            this.user = user;
        }
    }
}
