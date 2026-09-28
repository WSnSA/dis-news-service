package mn.usug.dis_news_service.Controller;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.Entity.DisSetting;
import mn.usug.dis_news_service.Service.SettingService;
import mn.usug.dis_news_service.Service.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** DIS цагийн хязгааруудын тохиргоо. */
@RestController
@RequestMapping("/setting")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService service;

    @GetMapping("/getAll")
    public List<DisSetting> getAll() {
        return service.getAll();
    }

    /** Body: { "key": "...", "value": "17" } */
    @PutMapping("/update")
    public DisSetting update(@RequestBody Map<String, String> body) {
        return service.update(body.get("key"), body.get("value"), UserContext.getUserId());
    }
}
