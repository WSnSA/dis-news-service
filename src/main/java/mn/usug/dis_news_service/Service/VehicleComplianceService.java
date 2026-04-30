package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.usug.dis_news_service.DAO.VehicleInspectionRepository;
import mn.usug.dis_news_service.DAO.VehicleInsuranceRepository;
import mn.usug.dis_news_service.DAO.VehiclePenaltyRepository;
import mn.usug.dis_news_service.DAO.VehicleRepository;
import mn.usug.dis_news_service.Entity.Vehicle;
import mn.usug.dis_news_service.Entity.VehicleInspection;
import mn.usug.dis_news_service.Entity.VehicleInsurance;
import mn.usug.dis_news_service.Entity.VehiclePenalty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleComplianceService {

    private final ErthubApiService erthubApi;
    private final VehicleRepository vehicleRepo;
    private final VehicleInspectionRepository inspectionRepo;
    private final VehicleInsuranceRepository insuranceRepo;
    private final VehiclePenaltyRepository penaltyRepo;

    private static final DateTimeFormatter FMT_DOT  = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter FMT_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ─────────────────────────────────────────────────────────
    //  lookup: улсын дугаараар мэдээлэл татаж, DB-д хадгалах
    // ─────────────────────────────────────────────────────────
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> lookup(String plate) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. Registration
        Map<String, Object> regRes = erthubApi.call("registration", plate, "");
        Object regData = regRes != null ? regRes.get("data") : null;
        result.put("registration", regData);

        // 2. Inspection — DB-д хадгалах
        Map<String, Object> inspRes = erthubApi.call("inspections", plate, "");
        Map<String, Object> inspData = null;
        if (inspRes != null && inspRes.get("data") instanceof Map) {
            inspData = (Map<String, Object>) inspRes.get("data");
            saveInspection(plate, inspData);
        }
        result.put("inspection", inspData);

        // 3. Insurance — DB-д хадгалах
        Map<String, Object> insRes = erthubApi.call("insurances", plate, "");
        List<Map<String, Object>> insurances = new ArrayList<>();
        if (insRes != null && insRes.get("data") instanceof List) {
            insurances = (List<Map<String, Object>>) insRes.get("data");
            saveInsurance(plate, insurances);
        }
        result.put("insurances", insurances);

        // 4. Taxes — зөвхөн харуулах
        Map<String, Object> taxRes = erthubApi.call("taxes", plate, "");
        result.put("taxes", taxRes != null ? taxRes.get("data") : new ArrayList<>());

        // 5. Penalties — зөвхөн харуулах (daily scheduler DB-д хадгална)
        Map<String, Object> penRes = erthubApi.call("penalties", plate, "new");
        result.put("penalties", penRes != null ? penRes.get("data") : null);

        return result;
    }

    // ─────────────────────────────────────────────────────────
    //  compliance: бүх машины compliance статусыг буцаах
    // ─────────────────────────────────────────────────────────
    public List<Map<String, Object>> getComplianceStatus() {
        Map<String, VehicleInspection> inspMap = inspectionRepo.findAll().stream()
            .collect(Collectors.toMap(VehicleInspection::getPlateNumber, i -> i, (a, b) -> a));

        Map<String, VehicleInsurance> insMap = insuranceRepo.findAll().stream()
            .collect(Collectors.toMap(VehicleInsurance::getPlateNumber, i -> i, (a, b) -> a));

        Map<String, Long> penMap = penaltyRepo.findByIsPaidFalse().stream()
            .collect(Collectors.groupingBy(VehiclePenalty::getPlateNumber, Collectors.counting()));

        Set<String> plates = new HashSet<>();
        plates.addAll(inspMap.keySet());
        plates.addAll(insMap.keySet());
        plates.addAll(penMap.keySet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (String plate : plates) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("plateNumber", plate);

            VehicleInspection insp = inspMap.get(plate);
            if (insp != null) {
                item.put("inspectionExpireDate", insp.getExpireDate());
                item.put("inspectionPassed", insp.getIsPassed());
            }

            VehicleInsurance ins = insMap.get(plate);
            if (ins != null) {
                item.put("insuranceExpireDate", ins.getExpireDate());
                item.put("hasActiveInsurance", ins.getIsActive());
            }

            item.put("unpaidPenaltyCount", penMap.getOrDefault(plate, 0L));
            result.add(item);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────
    //  Scheduler: өдөр бүр 02:00 — торгуул шалгах
    // ─────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledDailyPenaltyCheck() {
        List<Vehicle> vehicles = vehicleRepo.findAll();
        log.info("EртHub daily penalty check started — {} vehicles", vehicles.size());
        int ok = 0, fail = 0;

        for (Vehicle v : vehicles) {
            if (v.getPlateNumber() == null || v.getPlateNumber().isBlank()) continue;
            try {
                processPenaltiesForVehicle(v);
                ok++;
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                fail++;
                log.warn("Penalty check failed for {}: {}", v.getPlateNumber(), e.getMessage());
            }
        }
        log.info("EртHub penalty check done — ok:{} fail:{}", ok, fail);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void processPenaltiesForVehicle(Vehicle vehicle) {
        String plate = vehicle.getPlateNumber();
        Map<String, Object> res = erthubApi.call("penalties", plate, "new");
        if (res == null || !(res.get("data") instanceof Map)) return;

        Map<String, Object> data = (Map<String, Object>) res.get("data");

        List<Map<String, Object>> all = new ArrayList<>();
        extractPenalties(data.get("old"), all);
        extractPenalties(data.get("new"), all);

        for (Map<String, Object> p : all) {
            String barCode = (String) p.get("bar_code");
            if (barCode == null) continue;
            Boolean isPaid = (Boolean) p.get("is_paid");

            if (Boolean.TRUE.equals(isPaid)) {
                penaltyRepo.findByBarCode(barCode).ifPresent(penaltyRepo::delete);
            } else {
                VehiclePenalty vp = penaltyRepo.findByBarCode(barCode)
                    .orElse(new VehiclePenalty());
                vp.setPlateNumber(plate);
                vp.setBarCode(barCode);
                vp.setAmount(toInt(p.get("amount")));
                vp.setLocalName((String) p.get("local_name"));
                vp.setIsPaid(false);
                vp.setReasonType((String) p.get("reason_type"));
                vp.setReasonTypeCode(toInt(p.get("reason_type_code")));
                vp.setPassDate(parseDateTime((String) p.get("pass_date")));
                vp.setCheckedAt(LocalDateTime.now());
                penaltyRepo.save(vp);
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  DB save helpers
    // ─────────────────────────────────────────────────────────
    private void saveInspection(String plate, Map<String, Object> d) {
        VehicleInspection e = inspectionRepo.findByPlateNumber(plate)
            .orElse(new VehicleInspection());
        e.setPlateNumber(plate);
        e.setInspectedDate(parseDate((String) d.get("inspected_date")));
        e.setExpireDate(parseDate((String) d.get("expire_date")));
        e.setIsPassed(Boolean.TRUE.equals(d.get("is_passed")));
        e.setCheckedAt(LocalDateTime.now());
        inspectionRepo.save(e);
    }

    @SuppressWarnings("unchecked")
    private void saveInsurance(String plate, List<Map<String, Object>> list) {
        Map<String, Object> active = list.stream()
            .filter(i -> Boolean.TRUE.equals(i.get("is_active")))
            .findFirst().orElse(null);

        VehicleInsurance e = insuranceRepo.findByPlateNumber(plate)
            .orElse(new VehicleInsurance());
        e.setPlateNumber(plate);

        if (active != null) {
            e.setPolicyNumber((String) active.get("policy_number"));
            e.setInsuranceCompany((String) active.get("insurance_company"));
            e.setExpireDate(parseInsuranceDate((String) active.get("expired_at")));
            e.setIsActive(true);
        } else {
            e.setIsActive(false);
        }
        e.setCheckedAt(LocalDateTime.now());
        insuranceRepo.save(e);
    }

    // ─────────────────────────────────────────────────────────
    //  Util helpers
    // ─────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void extractPenalties(Object obj, List<Map<String, Object>> out) {
        if (!(obj instanceof Map)) return;
        Object data = ((Map<String, Object>) obj).get("data");
        if (data instanceof List) out.addAll((List<Map<String, Object>>) data);
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s, FMT_DOT); } catch (Exception ignored) {}
        try { return LocalDate.parse(s); } catch (Exception ignored) {}
        return null;
    }

    private LocalDateTime parseInsuranceDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.parse(s, FMT_DASH); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s.length() > 16 ? s.substring(0, 16) : s, FMT_DASH); }
        catch (Exception ignored) {}
        return null;
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            // "2023-10-22T05:33:54.000Z"
            String clean = s.replace("Z", "").replaceAll("\\.\\d+$", "");
            return LocalDateTime.parse(clean);
        } catch (Exception ignored) {}
        return null;
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer) return (Integer) v;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }
}
