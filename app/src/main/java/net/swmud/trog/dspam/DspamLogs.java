package net.swmud.trog.dspam;

import com.google.gson.Gson;

public class DspamLogs {
    public Dspam parse(String logs) {
        Gson gson = new Gson();
        Dspam dspam = gson.fromJson(logs, Dspam.class);
        dspam.dspam.add(null);
        dspam.dspam.add(null);
        removeNulls(dspam);
        addCalculations(dspam);
        return dspam;
    }

    private static void removeNulls(Dspam dspam) {
        int length = dspam.dspam.size();
        for (int i = 0; i < length; i++) {
            if (dspam.dspam.get(i) == null) {
                dspam.dspam.remove(i);
                --i;
                --length;
            }
        }
    }

    private static void addCalculations(Dspam dspam) {
        for (DspamEntry outer: dspam.dspam) {
            long signatureCount = 0;
            for (DspamEntry inner: dspam.dspam) {
                if (outer.getSignature().equals(inner.getSignature())) {
                    ++signatureCount;
                }
            }
            outer.setSignatureCount(signatureCount);
            outer.setSpamStatus();
            outer.setDate();
        }
    }
}
