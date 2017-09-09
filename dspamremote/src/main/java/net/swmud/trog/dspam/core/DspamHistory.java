package net.swmud.trog.dspam.core;

import com.google.gson.Gson;

import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.GetEntriesResponse;

import java.util.List;

public class DspamHistory {
    public List<DspamEntry> parse(String history) {
        GetEntriesResponse entriesResponse = new Gson().fromJson(history, GetEntriesResponse.class);
        List<DspamEntry> entries = entriesResponse.getEntries();
        removeNulls(entries);
        addCalculations(entries);
        return entries;
    }

    private static void removeNulls(List<DspamEntry> entries) {
        int length = entries.size();
        for (int i = 0; i < length; ++i) {
            if (entries.get(i) == null) {
                entries.remove(i);
                --i;
                --length;
            }
        }
    }

    private static void addCalculations(List<DspamEntry> entries) {
        for (DspamEntry outer: entries) {
            long signatureCount = 0;
            for (DspamEntry inner: entries) {
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
