package mn.usug.dis_news_service.Controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DriverRepository;
import mn.usug.dis_news_service.DAO.GarageRepository;
import mn.usug.dis_news_service.DAO.VehicleDriverRepository;
import mn.usug.dis_news_service.DAO.VehicleRepository;
import mn.usug.dis_news_service.DAO.VehicleTypeRepository;
import mn.usug.dis_news_service.Entity.Driver;
import mn.usug.dis_news_service.Entity.Garage;
import mn.usug.dis_news_service.Entity.Vehicle;
import mn.usug.dis_news_service.Entity.VehicleDriver;
import mn.usug.dis_news_service.Entity.VehicleType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ref/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository       repository;
    private final VehicleTypeRepository   vehicleTypeRepository;
    private final VehicleDriverRepository driverRepository;
    private final DriverRepository        driverRefRepository;
    private final GarageRepository        garageRepository;

    // ── Vehicle CRUD ──────────────────────────────────────────

    @GetMapping("/getAll")
    public List<Vehicle> getAll() {
        List<Vehicle> vehicles = repository.findAll();

        Map<Integer, String> typeMap = vehicleTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(VehicleType::getId, VehicleType::getName));

        Map<Long, String> garageMap = garageRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Garage::getId, Garage::getName));

        Map<Long, Driver> driverRefMap = driverRefRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Driver::getId, d -> d));

        Map<Long, List<VehicleDriver>> driverMap = driverRepository.findAll()
                .stream()
                .peek(vd -> {
                    Driver ref = driverRefMap.get(vd.getDriverId());
                    if (ref != null) {
                        vd.setDriverName(ref.getName());
                        vd.setDriverPhone(ref.getPhone());
                    }
                })
                .collect(Collectors.groupingBy(VehicleDriver::getVehicleId));

        vehicles.forEach(v -> {
            if (v.getVehicleTypeId() != null)
                v.setVehicleTypeName(typeMap.get(v.getVehicleTypeId()));
            if (v.getGarageId() != null)
                v.setGarageName(garageMap.get(v.getGarageId()));
            v.setDrivers(driverMap.getOrDefault(v.getId(), List.of()));
        });

        return vehicles;
    }

    @PostMapping("/save")
    @Transactional
    public Vehicle save(@RequestBody Vehicle vehicle) {
        vehicle.setId(null);
        Vehicle saved = repository.save(vehicle);
        saveDrivers(saved.getId(), vehicle.getDrivers());
        return saved;
    }

    @PutMapping("/update/{id}")
    @Transactional
    public Vehicle update(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        Vehicle existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));

        existing.setPlateNumber(vehicle.getPlateNumber());
        existing.setVinNumber(vehicle.getVinNumber());
        existing.setCabinNumber(vehicle.getCabinNumber());
        existing.setEngineNumber(vehicle.getEngineNumber());
        existing.setBrand(vehicle.getBrand());
        existing.setModel(vehicle.getModel());
        existing.setColor(vehicle.getColor());
        existing.setYear(vehicle.getYear());
        existing.setVehicleTypeId(vehicle.getVehicleTypeId());
        existing.setGarageId(vehicle.getGarageId());
        existing.setAffiliation(vehicle.getAffiliation());
        existing.setCountryName(vehicle.getCountryName());
        existing.setVehicleDesc(vehicle.getVehicleDesc());
        existing.setOwnerType(vehicle.getOwnerType());
        existing.setClassName(vehicle.getClassName());
        existing.setImportDate(vehicle.getImportDate());
        existing.setFuelType(vehicle.getFuelType());
        existing.setManCount(vehicle.getManCount());
        existing.setAxleCount(vehicle.getAxleCount());
        existing.setCapacity(vehicle.getCapacity());
        existing.setMass(vehicle.getMass());
        existing.setWeight(vehicle.getWeight());
        existing.setLength(vehicle.getLength());
        existing.setWidth(vehicle.getWidth());
        existing.setHeight(vehicle.getHeight());
        existing.setTransmission(vehicle.getTransmission());
        existing.setWheelPosition(vehicle.getWheelPosition());
        existing.setRfid(vehicle.getRfid());
        existing.setNote(vehicle.getNote());

        repository.save(existing);
        saveDrivers(id, vehicle.getDrivers());
        return existing;
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // ── Driver CRUD ───────────────────────────────────────────

    @GetMapping("/{vehicleId}/drivers")
    public List<VehicleDriver> getDrivers(@PathVariable Long vehicleId) {
        return driverRepository.findByVehicleId(vehicleId);
    }

    @PostMapping("/{vehicleId}/drivers")
    public VehicleDriver addDriver(@PathVariable Long vehicleId,
                                   @RequestBody VehicleDriver driver) {
        driver.setId(null);
        driver.setVehicleId(vehicleId);
        return driverRepository.save(driver);
    }

    @PutMapping("/drivers/{driverId}")
    public VehicleDriver updateDriver(@PathVariable Long driverId,
                                      @RequestBody VehicleDriver driver) {
        VehicleDriver existing = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        existing.setDriverId(driver.getDriverId());
        existing.setNote(driver.getNote());
        return driverRepository.save(existing);
    }

    @DeleteMapping("/drivers/{driverId}")
    public void deleteDriver(@PathVariable Long driverId) {
        driverRepository.deleteById(driverId);
    }

    // ── Helper ────────────────────────────────────────────────

    private void saveDrivers(Long vehicleId, List<VehicleDriver> drivers) {
        if (drivers == null) return;
        driverRepository.deleteByVehicleId(vehicleId);
        drivers.forEach(d -> {
            d.setId(null);
            d.setVehicleId(vehicleId);
        });
        driverRepository.saveAll(drivers);
    }
}
