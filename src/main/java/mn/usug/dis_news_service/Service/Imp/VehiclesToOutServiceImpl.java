package mn.usug.dis_news_service.Service.Imp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Model.VehiclesToOutRowDto;
import mn.usug.dis_news_service.DAO.VehiclesToOutRepository;
import mn.usug.dis_news_service.Service.VehiclesToOutService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiclesToOutServiceImpl implements VehiclesToOutService {

    private final VehiclesToOutRepository repo;
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

    // ✅ ШИНЭ: Front-д хэрэгтэй хэлбэрээр (legacy_data-с нөхөөд) буцаах
    public List<VehiclesToOutRowDto> findRowsByDate(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        return repo.findAllByCreatedDateBetweenOrderByIdAsc(from, to).stream()
                .map(this::toRowDtoFilledFromLegacy)
                // хэрэггүй хоосон мөрүүдийг хүсвэл энд шүүж болно
                .filter(r -> !isAllBlank(r))
                .toList();
    }

    private VehiclesToOutRowDto toRowDtoFilledFromLegacy(VehiclesToOut v) {
        JsonNode legacy = parseLegacy(v.getLegacyData());

        String dep = pickFirstNonBlank(
                null,
                textAt(legacy, "zahialga_ogson_heltes")
        );

        String work = pickFirstNonBlank(
                null,
                textAt(legacy, "hiigdeh_ajil")
        );

        String mech = pickFirstNonBlank(
                null,
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
                .createdDate(v.getCreatedDate())
                .build();
    }

    private boolean isAllBlank(VehiclesToOutRowDto r) {
        return isBlank(r.getDepartment())
                && isBlank(r.getWorkDescription())
                && isBlank(r.getVehicleMechanism())
                && isBlank(r.getVehicleRegistration())
                && isBlank(r.getPhone());
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
