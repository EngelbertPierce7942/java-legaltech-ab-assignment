package com.example.legaltech;

public final class BucketDecision {
    private BucketDecision() {}

    public static String fromFlagResponse(String envelope) throws InfraiError {
        if (!envelope.contains("\"ok\":true") && !envelope.contains("\"ok\" : true")) {
            throw new InfraiError(200, "BAD_ENVELOPE", "flag response was not successful");
        }
        if (envelope.contains("treatment")) return "treatment";
        if (envelope.contains("control")) return "control";
        throw new InfraiError(200, "BAD_ENVELOPE", "flag response has no assignment");
    }
}
