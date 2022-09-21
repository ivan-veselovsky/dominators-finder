package edu.postleadsfinder;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** NB: cannot use record here because of IllegalAccessException in Gson implementation. */
@RequiredArgsConstructor
@Getter
public class InputData {
    // optional field, in fact it is not used:
    @SerializedName("e1")
    final String entryNodeId;

    @SerializedName("e2")
    final String exitNodeId;

    @SerializedName("h")
    final String startNodeId;

    @SerializedName("graph")
    final String dotFormatGraph;
}
