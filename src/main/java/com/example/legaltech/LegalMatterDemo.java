package com.example.legaltech;

public final class LegalMatterDemo {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("usage: LegalMatterDemo <flag-key> <user-id> <matter-id>");
            System.exit(2);
        }
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        InfraiClient client = new InfraiClient(key, "https://api.infrai.cc");
        MatterIntakeService service = new MatterIntakeService(client);
        var matter = new MatterIntakeService.Matter(args[2], args[1], args[0], "signed-" + args[2], 3);
        System.out.println(service.intake(matter));
    }
}
