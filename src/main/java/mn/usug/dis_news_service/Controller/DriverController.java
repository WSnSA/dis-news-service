package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.DriverRepository;
import mn.usug.dis_news_service.Entity.Driver;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ref/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository repository;

    @GetMapping("/getAll")
    public List<Driver> getAll() {
        return repository.findAll();
    }

    @GetMapping("/search")
    public List<Driver> search(@RequestParam String q) {
        return repository.findByNameContainingIgnoreCase(q);
    }

    @PostMapping("/save")
    public Driver save(@RequestBody Driver driver) {
        driver.setId(null);
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

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
