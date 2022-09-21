package edu.postleadsfinder;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class InputDataTest {

    @Test
    void testInputJson() {
        Gson gson = new Gson();
        InputData inputData = gson.fromJson("{\"e1\": \"1\"," +
                "\"e2\": \"7\"," +
                "\"h\":\"2\"," +
                "\"graph\": \" digraph graphname{\n1->2\n2->3\n2->5\n5->2\n3->5\n5->7}\"" +
                "}",
                InputData.class);
        then(inputData.getEntryNodeId()).isEqualTo("1");
        then(inputData.getExitNodeId()).isEqualTo("7");
        then(inputData.getStartNodeId()).isEqualTo("2");
        then(inputData.getDotFormatGraph()).isEqualTo(" digraph graphname{\n1->2\n2->3\n2->5\n5->2\n3->5\n5->7}");
    }
}