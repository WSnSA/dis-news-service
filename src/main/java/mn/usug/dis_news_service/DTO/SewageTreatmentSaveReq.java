package mn.usug.dis_news_service.DTO;

public record SewageTreatmentSaveReq(
        Integer stationId,
        String  date,           // "yyyy-MM-dd"
        Integer hour,
        String  workingCount,
        String  pendingCount,
        String  repairingCount,
        Double  receivedWaste,
        Double  receivedWool,
        Double  receivedWater,
        Double  substanceSpent,
        Double  treatedWater
) {}
