package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Entity.VehiclesToOut;
import mn.usug.dis_news_service.Service.VehiclesToOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles-to-out")
@RequiredArgsConstructor
public class VehiclesToOutController {

    private final VehiclesToOutService service;

    @GetMapping
    public List<VehiclesToOut> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public VehiclesToOut getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public VehiclesToOut create(@RequestBody VehiclesToOut vehiclesToOut) {
        return service.save(vehiclesToOut);
    }

    @PutMapping("/{id}")
    public VehiclesToOut update(
            @PathVariable Integer id,
            @RequestBody VehiclesToOut vehiclesToOut
    ) {
        vehiclesToOut.setId(id);
        return service.save(vehiclesToOut);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.deleteById(id);
    }
}
