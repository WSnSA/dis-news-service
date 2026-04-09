package mn.usug.dis_news_service.Service.Imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;
import mn.usug.dis_news_service.DAO.VehiclesToOutRepository;
import mn.usug.dis_news_service.Service.VehiclesToOutService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
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

        // Batch: vehicle_order.created_by → user.first_name
        Set<Long> orderIds = records.stream()
                .map(VehiclesToOut::getVehicleOrderId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toSet());

        Map<Integer, Integer> orderToUser = new HashMap<>();
        if (!orderIds.isEmpty()) {
            vehicleOrderRepo.findAllById(orderIds)
                    .forEach(o -> { if (o.getCreatedBy() != null) orderToUser.put(o.getId().intValue(), o.getCreatedBy()); });
        }

        Map<Integer, String> userNames = new HashMap<>();
        Set<Integer> userIds = new HashSet<>(orderToUser.values());
        if (!userIds.isEmpty()) {
            userDAO.findAllById(userIds)
                    .forEach(u -> userNames.put(u.getId(), u.getFirstName()));
        }

        return records.stream()
                .map(v -> {
                    String name = null;
                    if (v.getVehicleOrderId() != null) {
                        Integer uid = orderToUser.get(v.getVehicleOrderId());
                        if (uid != null) name = userNames.get(uid);
                    }
                    return toRowDtoFilledFromLegacy(v, name);
                })
                .filter(r -> !isAllBlank(r))
                .toList();
    }

    private VehiclesToOutRowDto toRowDtoFilledFromLegacy(VehiclesToOut v) {
        return toRowDtoFilledFromLegacy(v, null);
    }

    private VehiclesToOutRowDto toRowDtoFilledFromLegacy(VehiclesToOut v, String orderCreatedByName) {
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
