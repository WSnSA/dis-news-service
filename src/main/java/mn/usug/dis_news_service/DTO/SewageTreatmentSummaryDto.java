package mn.usug.dis_news_service.DTO;

public record SewageTreatmentSummaryDto(
        Integer stationId,
        String groupName,
        String stationName,

        String workingCount,
        String pendingCount,
        String repairingCount,

        Double receivedWaste,
        Double receivedWool,
        Double receivedWater,
        Double substanceSpent,
        Double treatedWater
) {}
