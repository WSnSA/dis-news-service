package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.GarageRepository;
import mn.usug.dis_news_service.Entity.Garage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ref/garage")
@RequiredArgsConstructor
public class GarageController {

    private final GarageRepository repository;

    @GetMapping("/getAll")
    public List<Garage> getAll() {
        return repository.findAll();
    }

    @PostMapping("/save")
    public Garage save(@RequestBody Garage garage) {
        garage.setId(null);
        return repository.save(garage);
    }

    @PutMapping("/update/{id}")
    public Garage update(@PathVariable Long id, @RequestBody Garage garage) {
        Garage existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garage not found: " + id));
        existing.setName(garage.getName());
        existing.setLocation(garage.getLocation());
        existing.setLat(garage.getLat());
        existing.setLng(garage.getLng());
        existing.setCapacity(garage.getCapacity());
        existing.setNote(garage.getNote());
        return repository.save(existing);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
