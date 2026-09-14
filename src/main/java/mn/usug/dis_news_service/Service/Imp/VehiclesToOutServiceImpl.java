package mn.usug.dis_news_service.Service.Imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Model.DispatchDetailDto;
import mn.usug.dis_news_service.Model.DispatchStatsDto;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;
import mn.usug.dis_news_service.DAO.VehiclesToOutRepository;
import mn.usug.dis_news_service.Service.VehiclesToOutService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiclesToOutServiceImpl implements VehiclesToOutService {

    private final VehiclesToOutRepository repo;
    private final VehicleOrderRepository vehicleOrderRepo;
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<VehiclesToOut> findAll() {
        return repo.findAll();
    }

    @Override
    public VehiclesToOut findById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public VehiclesToOut save(VehiclesToOut vehiclesToOut) {
        return repo.save(vehiclesToOut);
    }

    @Override
    public void deleteById(Integer id) {
        repo.deleteById(id);
    }

    @Override
    public List<VehiclesToOut> findByDate(LocalDate date) {
        // хуучин method хэвээр үлдээж болно
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return repo.findAllByCreatedDateBetweenOrderByIdAsc(from, to);
    }

    @Override
    public VehiclesToOutRowDto findRowByOrderId(Integer vehicleOrderId) {
        return repo.findFirstByVehicleOrderIdOrderByCreatedDateDesc(vehicleOrderId)
                .map(this::toRowDtoFilledFromLegacy)
                .orElse(null);
    }

    @Override
    public List<VehiclesToOutRowDto> findRowsByOrderId(Integer vehicleOrderId) {
        return repo.findAllByVehicleOrderIdOrderByIdAsc(vehicleOrderId)
                .stream()
                .map(this::toRowDtoFilledFromLegacy)
                .toList();
    }

    // ✅ ШИНЭ: Front-д хэрэгтэй хэлбэрээр (legacy_data-с нөхөөд) буцаах
    public List<VehiclesToOutRowDto> findRowsByDate(LocalDate date) {
        List<VehiclesToOut> records = repo.findByDate(date);

        // Batch: vehicle_order — createdBy + orderType
        Set<Long> orderIds = records.stream()
                .map(VehiclesToOut::getVehicleOrderId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toSet());

        Map<Integer, Integer> orderToUser = new HashMap<>();
        Map<Integer, Integer> orderToType = new HashMap<>();
        if (!orderIds.isEmpty()) {
            vehicleOrderRepo.findAllById(orderIds).forEach(o -> {
                Integer key = o.getId().intValue();
                if (o.getCreatedBy() != null) orderToUser.put(key, o.getCreatedBy());
                if (o.getOrderType() != null) orderToType.put(key, o.getOrderType());
            });
        }

        Map<Integer, String> userNames = new HashMap<>();
        Set<Integer> userIds = new HashSet<>(orderToUser.values());
        if (!userIds.isEmpty()) {
            userDAO.findAllById(userIds)
                    .forEach(u -> userNames.put(u.getId(), buildShortName(u.getLastName(), u.getFirstName())));
        }

        return records.stream()
                .map(v -> {
                    String name = null;
                    Integer type = v.getOrderType();  // entity-д хадгалсан утга эхэлж
                    if (v.getVehicleOrderId() != null) {
                        Integer uid = orderToUser.get(v.getVehicleOrderId());
                        if (uid != null) name = userNames.get(uid);
                        if (type == null) type = orderToType.get(v.getVehicleOrderId());  // join fallback
                    }
                    return toRowDtoFilledFromLegacy(v, name, type);
                })
                .filter(r -> !isAllBlank(r))
                .toList();
    }

    /* ==================== СТАТИСТИК ==================== */

    /** Тухайн жилийн машин хуваарилалтын статистик: сар/улирал/жил + алба бүрээр төрлөөр */
    public DispatchStatsDto getStats(int year) {
        // 1) Сар бүрийн тоо
        long[] months = new long[13]; // index 1..12
        for (Object[] r : repo.countByMonth(year)) {
            int m = ((Number) r[0]).intValue();
            long c = ((Number) r[1]).longValue();
            if (m >= 1 && m <= 12) months[m] = c;
        }
        List<DispatchStatsDto.PeriodCount> byMonth = new ArrayList<>();
        long yearTotal = 0;
        for (int m = 1; m <= 12; m++) {
            byMonth.add(DispatchStatsDto.PeriodCount.builder().period(m).count(months[m]).build());
            yearTotal += months[m];
        }

        // 2) Улирал (3 сараар нэгтгэнэ)
        List<DispatchStatsDto.PeriodCount> byQuarter = new ArrayList<>();
        for (int q = 1; q <= 4; q++) {
            long c = months[(q - 1) * 3 + 1] + months[(q - 1) * 3 + 2] + months[(q - 1) * 3 + 3];
            byQuarter.add(DispatchStatsDto.PeriodCount.builder().period(q).count(c).build());
        }

        // 3) Алба + төрлөөр
        Map<String, DispatchStatsDto.DeptDispatch> deptMap = new LinkedHashMap<>();
        for (Object[] r : repo.countByDeptAndType(year)) {
            String dep = r[0] != null ? r[0].toString() : "Тодорхойгүй";
            String type = r[1] != null ? r[1].toString() : "Тодорхойгүй";
            long c = ((Number) r[2]).longValue();
            DispatchStatsDto.DeptDispatch d = deptMap.computeIfAbsent(dep, k ->
                    DispatchStatsDto.DeptDispatch.builder().department(k).total(0).types(new ArrayList<>()).build());
            d.setTotal(d.getTotal() + c);
            d.getTypes().add(DispatchStatsDto.TypeCount.builder().typeName(type).count(c).build());
        }
        List<DispatchStatsDto.DeptDispatch> byDepartment = new ArrayList<>(deptMap.values());
        byDepartment.sort((a, b) -> Long.compare(b.getTotal(), a.getTotal()));

        return DispatchStatsDto.builder()
                .year(year)
                .yearTotal(yearTotal)
                .byMonth(byMonth)
                .byQuarter(byQuarter)
                .byDepartment(byDepartment)
                .build();
    }

    /** Тайлангийн тоо бүрийн ард байгаа дэлгэрэнгүй мөрүүд (нэг жилээр) */
    public List<DispatchDetailDto> getStatsDetail(int year) {
        List<DispatchDetailDto> out = new ArrayList<>();
        for (Object[] r : repo.statsDetailByYear(year)) {
            out.add(DispatchDetailDto.builder()
                    .createdDate(str(r[0]))
                    .month(r[1] != null ? ((Number) r[1]).intValue() : 0)
                    .department(str(r[2]))
                    .typeName(str(r[3]))
                    .workDescription(str(r[4]))
                    .vehicleMechanism(str(r[5]))
                    .vehicleRegistration(str(r[6]))
                    .driverName(str(r[7]))
                    .phone(str(r[8]))
                    .build());
        }
        return out;
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    /** Нэг машин (улсын дугаараар) захиалгаар ажилд гарсан түүх */
    public List<VehiclesToOutRowDto> findRowsByPlate(String plate) {
        if (plate == null || plate.isBlank()) return List.of();
        List<VehiclesToOut> records = repo.findByPlate(plate.trim());
        return enrichRows(records);
    }

    /** vehicle_order-оос createdBy нэр + orderType-ыг batch-аар нөхөж RowDto болгоно */
    private List<VehiclesToOutRowDto> enrichRows(List<VehiclesToOut> records) {
        Set<Long> orderIds = records.stream()
                .map(VehiclesToOut::getVehicleOrderId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toSet());

        Map<Integer, Integer> orderToUser = new HashMap<>();
        Map<Integer, Integer> orderToType = new HashMap<>();
        if (!orderIds.isEmpty()) {
            vehicleOrderRepo.findAllById(orderIds).forEach(o -> {
                Integer key = o.getId().intValue();
                if (o.getCreatedBy() != null) orderToUser.put(key, o.getCreatedBy());
                if (o.getOrderType() != null) orderToType.put(key, o.getOrderType());
            });
        }

        Map<Integer, String> userNames = new HashMap<>();
        Set<Integer> userIds = new HashSet<>(orderToUser.values());
        if (!userIds.isEmpty()) {
            userDAO.findAllById(userIds)
                    .forEach(u -> userNames.put(u.getId(), buildShortName(u.getLastName(), u.getFirstName())));
        }

        return records.stream()
                .map(v -> {
                    String name = null;
                    Integer type = v.getOrderType();
                    if (v.getVehicleOrderId() != null) {
                        Integer uid = orderToUser.get(v.getVehicleOrderId());
                        if (uid != null) name = userNames.get(uid);
                        if (type == null) type = orderToType.get(v.getVehicleOrderId());
                    }
                    return toRowDtoFilledFromLegacy(v, name, type);
                })
                .toList();
    }

    private String buildShortName(String lastName, String firstName) {
        String ln = lastName == null ? "" : lastName.trim();
        String fn = firstName == null ? "" : firstName.trim();
        if (ln.isEmpty() && fn.isEmpty()) return null;
        if (ln.isEmpty()) return fn;
        return ln.charAt(0) + ". " + fn;
    }

    private VehiclesToOutRowDto toRowDtoFilledFromLegacy(VehiclesToOut v) {
        return toRowDtoFilledFromLegacy(v, null, null);
    }

    private VehiclesToOutRowDto toRowDtoFilledFromLegacy(VehiclesToOut v, String orderCreatedByName, Integer orderType) {
        JsonNode legacy = parseLegacy(v.getLegacyData());

        // Entity талбар эхлэж, байхгүй бол legacy_data-с нөхнө
        String dep = pickFirstNonBlank(
                normalize(v.getDepartment()),
                textAt(legacy, "zahialga_ogson_heltes")
        );

        String work = pickFirstNonBlank(
                normalize(v.getWorkDescription()),
                textAt(legacy, "hiigdeh_ajil")
        );

        String mech = pickFirstNonBlank(
                normalize(v.getVehicleMechanism()),
                textAt(legacy, "mashin_mehanizm")
        );

        String reg = pickFirstNonBlank(
                normalize(v.getVehicleRegistrationNumber()),
                normalize(textAt(legacy, "vehicle_reg_raw"))
        );

        String phone = pickFirstNonBlank(
                normalize(v.getDriverPhoneNumber()),
                normalize(textAt(legacy, "phone_raw"))
        );

        return VehiclesToOutRowDto.builder()
                .id(v.getId())
                .department(dep)
                .workDescription(work)
                .vehicleMechanism(mech)
                .vehicleRegistration(reg)
                .phone(phone)
                .driverName(normalize(v.getDriverName()))
                .createdDate(v.getCreatedDate())
                .orderCreatedByName(orderCreatedByName)
                .vehicleOrderId(v.getVehicleOrderId())
                .orderType(orderType)
                .build();
    }

    private boolean isAllBlank(VehiclesToOutRowDto r) {
        return isBlank(r.getDepartment())
                && isBlank(r.getWorkDescription())
                && isBlank(r.getVehicleMechanism())
                && isBlank(r.getVehicleRegistration())
                && isBlank(r.getPhone())
                && isBlank(r.getDriverName());
    }

    private JsonNode parseLegacy(String json) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readTree(json); }
        catch (Exception e) { return null; }
    }

    private String textAt(JsonNode node, String key) {
        if (node == null) return null;
        JsonNode v = node.get(key);
        if (v == null || v.isNull()) return null;
        return v.asText();
    }

    private String normalize(String s) {
        if (s == null) return null;
        // Таны дата дээр "\n", "-" зэрэг холилдсон байсан -> цэвэрлэе
        String x = s.replace("\r", "\n");
        x = x.replaceAll("[\\t ]+", " ");
        x = x.replaceAll("\\n{2,}", "\n").trim();
        // эхэндээ "-" ганцаараа байвал устгая
        x = x.replaceAll("^[-\\s]+", "").trim();
        return x.isBlank() ? null : x;
    }

    private String pickFirstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
