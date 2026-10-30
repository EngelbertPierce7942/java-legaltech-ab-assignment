package com.example.legaltech;

public final class BucketDecisionTest {
    public static void main(String[] args) throws Exception {
        String envelope = "{\"ok\":true,\"data\":{\"value\":\"treatment\"},\"error\":null,\"metadata\":{}}";
        String bucket = BucketDecision.fromFlagResponse(envelope);
        if (!"treatment".equals(bucket)) throw new AssertionError(bucket);
        System.out.println("stable treatment assignment passed");
    }
}
