package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DriverRepository;
import mn.usug.dis_news_service.Entity.Driver;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ref/driver")
@RequiredArgsConstructor
public class DriverController {

    private static final Integer ACTIVE   = 1;
    private static final Integer INACTIVE = 0;

    private final DriverRepository repository;

    /**
     * @param includeInactive true бол устгасан жолоочийг ч буцаана —
     *        "Ажилтан" хуудсанд саарлаар харуулахад хэрэгтэй.
     */
    @GetMapping("/getAll")
    public List<Driver> getAll(
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false")
            boolean includeInactive
    ) {
        return includeInactive
                ? repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : repository.findByActiveFlagOrderByNameAsc(ACTIVE);
    }

    /** Хуваарилалтын хайлт — устгасан жолооч сонголтод гарахгүй */
    @GetMapping("/search")
    public List<Driver> search(@RequestParam String q) {
        return repository.findByActiveFlagAndNameContainingIgnoreCase(ACTIVE, q);
    }

    @PostMapping("/save")
    public Driver save(@RequestBody Driver driver) {
        driver.setId(null);
        driver.setActiveFlag(ACTIVE);
        return repository.save(driver);
    }

    @PutMapping("/update/{id}")
    public Driver update(@PathVariable Long id, @RequestBody Driver driver) {
        Driver existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
        existing.setName(driver.getName());
        existing.setPhone(driver.getPhone());
        existing.setLicenseCategories(driver.getLicenseCategories());
        return repository.save(existing);
    }

    /** Soft delete — хуваарилалтын түүхэд жолоочийн нэр үлдэх ёстой */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(d -> {
            d.setActiveFlag(INACTIVE);
            repository.save(d);
        });
    }
}
