package net.swmud.trog.dspam;

import net.swmud.trog.dspam.json.JsonRpc;
import net.swmud.trog.dspam.json.RetrainRequest;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonRpcTest {
    @Test
    public void paramsTest() throws Exception {
        final String expected = "{\"jsonrpc\":\"2.0\",\"method\":\"retrain\",\"params\":{\"entries\":[{\"signature\":\"sig_0\",\"classification\":\"status_0\"},{\"signature\":\"sig_1\",\"classification\":\"status_1\"}],\"user\":\"X\"},\"id\":1}";
        List<RetrainRequest.RetrainEntry> entries = new LinkedList<>();
        for (int i = 0; i < 2; ++i) {
            entries.add(new RetrainRequest.RetrainEntry("sig_" + i, "status_" + i));
        }
        Map<String, Object> args = new HashMap<>();
        args.put("user", "X");
        args.put("entries", entries);
        String actual = JsonRpc.getRequest("retrain", args);
//        System.out.println(actual);
        assertEquals("incorrect getRequest output", expected, actual);
    }

}
