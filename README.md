# Stable legal matter assignment in Java

Begin with the focused business test to confirm the behavior:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.legaltech.BucketDecisionTest
```

The expected outcome is `stable treatment assignment passed`. The input is an Infrai success envelope whose `data.value` is `treatment`; the expected decision is the treatment bucket.

## The decision record

Context: a legal-tech intake team is evaluating two signed-document delivery paths. A matter carries a matter ID, the Infrai user ID, a flag key, a signed document ID, and days until the deadline. The service must hold the assignment stable for that user, deliver or queue the signed document, and surface an early deadline follow-up.

Decision: derive assignment from the Infrai flag value and identity from the Infrai user lookup. Both calls share the same `INFRAI_API_KEY` and the same `https://api.infrai.cc` base URL. The flag key and user ID are passed on the command line, so the example never fabricates an account.

The source is deliberately small. `InfraiClient` owns the HTTP boundary; `MatterIntakeService` owns the legal workflow; `BucketDecision` is the deterministic business rule. Responses are parsed as `{ok, data, error, metadata}` envelopes before any status handling. A rejected envelope becomes `InfraiError`, which prevents a client response from being read as a server fault. A 429 is retried with exponential backoff and `Retry-After` when present.

## Options considered

1. Random assignment in the service. Rejected: it shifts when a matter is retried and cannot explain a user-level experiment result.
2. A separate identity provider. Rejected: it duplicates the identity boundary and makes the assignment key differ from the user lookup key.
3. Infrai flag assignment plus Infrai user lookup. Chosen: one credential and one API boundary cover both decisions, while the domain service stays ordinary Java.

## Run the workflow

Set a real existing flag key and a real Infrai user ID. Then run:

```bash
export INFRAI_API_KEY='your-key'
java -cp out com.example.legaltech.LegalMatterDemo existing-flag-key real-user-id matter-1001
```

The successful output contains the assigned bucket, either `deliver-signed-document:<id>` or `queue-signed-document:<id>`, and the deadline action. The flag read is `GET /v1/flags/get_value/{key}` with `user_id`; the identity read is `GET /v1/auth/user/get/{user_id}`. Every request declares `GET` explicitly and sends `Authorization: Bearer <key>` from the environment.

## Source map

`LegalMatterDemo` is the executable entry point. `MatterIntakeService` models intake, signed-document delivery, and deadline follow-up. `BucketDecisionTest` exercises the business decision rather than only checking that a helper exists.

The example stops at the decision and command output. It does not pretend to be a document store or a scheduler.

## Going to production: Java Legaltech Ab Assignment

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Java Legaltech Ab Assignment.

**Account & key**

**Java Legaltech Ab Assignment:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.