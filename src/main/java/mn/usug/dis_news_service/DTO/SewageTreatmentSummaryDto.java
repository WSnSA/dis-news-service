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
        Double treatedWater,
        Double solidWaste,

        /* ─── Шинэ цэвэрлэх байгууламжийн нэмэлт үзүүлэлт (st_facility_daily) ─── */
        /** Шахсан агаар (м³/хоног) */
        Double compressedAir,
        /** Био хий (Nм³/хоног) */
        Double biogas,
        /** Усгүйжүүлсэн лаг (м³/хоног) */
        Double dewateredSludge,
        /** Лагийн чийгшил (%) — ЛУБ */
        Double sludgeMoisture,
        /** Үйлдвэрлэсэн цахилгаан (кВт·ц/хоног) — Био хийн байгууламж */
        Double electricity,
        /** Хэрэглэсэн PAC (кг/хоног) */
        Double pacSpent,
        /** Хэрэглэсэн PAM (кг/хоног) */
        Double pamSpent,

        /** Станцын цэсний зам (ж: /st/wwtp/pump) — самбарт байгууламжийг тогтвортой ялгахад */
        String stationPath
) {
    /** Хуучин 12 талбарт зориулсан богино constructor — шинэ талбарууд 0 болно */
    public SewageTreatmentSummaryDto(
            Integer stationId, String groupName, String stationName,
            String workingCount, String pendingCount, String repairingCount,
            Double receivedWaste, Double receivedWool, Double receivedWater,
            Double substanceSpent, Double treatedWater, Double solidWaste) {
        this(stationId, groupName, stationName,
                workingCount, pendingCount, repairingCount,
                receivedWaste, receivedWool, receivedWater,
                substanceSpent, treatedWater, solidWaste,
                0d, 0d, 0d, 0d, 0d, 0d, 0d, null);
    }

    /** Хуучин 12 талбар + станцын зам */
    public SewageTreatmentSummaryDto(
            Integer stationId, String groupName, String stationName,
            String workingCount, String pendingCount, String repairingCount,
            Double receivedWaste, Double receivedWool, Double receivedWater,
            Double substanceSpent, Double treatedWater, Double solidWaste,
            String stationPath) {
        this(stationId, groupName, stationName,
                workingCount, pendingCount, repairingCount,
                receivedWaste, receivedWool, receivedWater,
                substanceSpent, treatedWater, solidWaste,
                0d, 0d, 0d, 0d, 0d, 0d, 0d, stationPath);
    }
}
