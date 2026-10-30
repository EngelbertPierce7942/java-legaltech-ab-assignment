package com.example.legaltech;

import java.io.IOException;

public final class MatterIntakeService {
    public record Matter(String matterId, String userId, String flagKey, String signedDocumentId,
                         int daysUntilDeadline) {}
    public record IntakeResult(String bucket, String documentAction, String followUpAction) {}

    private final InfraiClient infrai;

    public MatterIntakeService(InfraiClient infrai) { this.infrai = infrai; }

    public IntakeResult intake(Matter matter)
            throws IOException, InterruptedException, InfraiError {
        infrai.user(matter.userId());
        String bucket = BucketDecision.fromFlagResponse(infrai.flagValue(matter.flagKey(), matter.userId()));
        String documentAction = "treatment".equals(bucket)
                ? "deliver-signed-document:" + matter.signedDocumentId()
                : "queue-signed-document:" + matter.signedDocumentId();
        String followUpAction = matter.daysUntilDeadline() <= 3
                ? "follow-up-today" : "follow-up-in-" + matter.daysUntilDeadline() + "-days";
        return new IntakeResult(bucket, documentAction, followUpAction);
    }
}
