package mn.usug.dis_news_service.Controller;

import mn.usug.dis_news_service.Entity.*;
import mn.usug.dis_news_service.Service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ref")
public class ReferenceController {
    @Autowired
    MainService mainService;

    @GetMapping("/department/getAll")
    public List<Department> getAllDepartments() {
        return mainService.getAllDepartments();
    }

    @GetMapping("/department/getDepartmentById")
    public Department getDepartmentById(@RequestParam("id") Integer id) {
        return mainService.getDepartmentById(id);
    }

    @GetMapping("/department/searchDepartment")
    public List<Department> searchDepartment(@RequestParam("name") String name) {
        return mainService.getDepartmentsByName(name);
    }

    @GetMapping("/department/delete")
    public ResponseEntity deleteDepartment(@RequestParam("id") Integer id) {
        return mainService.deleteDepartmentById(id);
    }

    @GetMapping("/department/add")
    public Department addDepartment(@RequestParam("name") String name, @RequestParam("employeeCount") Integer employeeCount, @RequestParam("chairmanId") Integer chairmanId) {
        return mainService.addDepartment(name, employeeCount, chairmanId);
    }

    @GetMapping("/department/edit")
    public Department editDepartment(@RequestParam("id") Integer id, @RequestParam("name") String name, @RequestParam("employeeCount") Integer employeeCount, @RequestParam("chairmanId") Integer chairmanId) {
        return mainService.editDepartment(id, name, employeeCount, chairmanId);
    }

    @GetMapping("/position/getAll")
    public List<Position> getAllPositions() {
        return mainService.getAllPositions();
    }


    @GetMapping("/position/searchPosition")
    public List<Position> searchPosition(@RequestParam("depId") Integer depId, @RequestParam("name") String name) {
        return mainService.getPositionsByName(depId, name);
    }

    @GetMapping("/position/getPositionByDepId")
    public List<Position> getPositionById(@RequestParam("depId") Integer id) {
        return mainService.getPositionsByDepId(id);
    }
    @GetMapping("/position/add")
    public Position addPosition(@RequestParam("depId") Integer depId, @RequestParam("name") String name, @RequestParam("employeeCount") Integer employeeCount) {
        Position position = new Position();
        position.setDepId(depId);
        position.setName(name);
        return mainService.createPosition(position);
    }
    @GetMapping("/position/edit")
    public Position editPosition(@RequestParam("id") Integer id, @RequestParam("depId") Integer depId, @RequestParam("name") String name, @RequestParam("employeeCount") Integer employeeCount) {
        Position position = new Position();
        position.setId(id);
        position.setDepId(depId);
        position.setName(name);
        return mainService.updatePosition(position);
    }
    @GetMapping("/position/delete")
    public ResponseEntity deletePosition(@RequestParam("id") Integer id) {
        return mainService.deletePositionById(id);
    }

    @GetMapping("/station/getAll")
    public ResponseEntity getAllStationsByDepId(@RequestParam("depId") Integer depId) {
        return mainService.getAllStations(depId);
    }
    @GetMapping("/station/findById")
    public ResponseEntity getStationById(@RequestParam("id") Integer id) {
        return mainService.getStationById(id);
    }
    @PostMapping("/station/add")
    public ResponseEntity addStation(
            @RequestParam("depId") Integer depId,
            @RequestParam("name") String name,
            @RequestParam("chefId") Integer chefId,
            @RequestParam("isWaterSupply") Integer isWaterSupply,
            @RequestParam("poolCount") Integer poolCount,
            @RequestParam("firstGenCount") Integer firstGenCount,
            @RequestParam("secondGenCount") Integer secondGenCount,
            @RequestParam("wellsNumber") Integer wellsNumber

    ) {
        User user = mainService.getUserById(chefId);
        if(user == null){
            return ResponseEntity.status(404).body("Chef not found");
        }
        Department department = mainService.getDepartmentById(depId);
        if(department == null){
            return ResponseEntity.status(404).body("Department not found");
        }

        Station station = new Station();
        station.setName(name);
        station.setDepartmentId(depId);
        station.setIsWaterSupply(isWaterSupply);
        station.setPoolCount(poolCount);
        station.setFirstGeneratorCount(firstGenCount);
        station.setSecondGeneratorCount(secondGenCount);
        station.setWellsNumber(wellsNumber);
        station.setChefId(chefId);

        return mainService.addStation(station);
    }
    @PostMapping("/station/update")
    public ResponseEntity updateStaion(
            @RequestParam("id") Integer id,
            @RequestParam("depId") Integer depId,
            @RequestParam("name") String name,
            @RequestParam("chefId") Integer chefId,
            @RequestParam("isWaterSupply") Integer isWaterSupply,
            @RequestParam("poolCount") Integer poolCount,
            @RequestParam("firstGenCount") Integer firstGenCount,
            @RequestParam("secondGenCount") Integer secondGenCount,
            @RequestParam("wellsNumber") Integer wellsNumber){
        User user = mainService.getUserById(chefId);
        if(user == null){
            return ResponseEntity.status(404).body("Chef not found");
        }
        Department department = mainService.getDepartmentById(depId);
        if(department == null){
            return ResponseEntity.status(404).body("Department not found");
        }

        Station station = new Station();
        station.setName(name);
        station.setDepartmentId(depId);
        station.setIsWaterSupply(isWaterSupply);
        station.setPoolCount(poolCount);
        station.setFirstGeneratorCount(firstGenCount);
        station.setSecondGeneratorCount(secondGenCount);
        station.setWellsNumber(wellsNumber);
        station.setChefId(chefId);
        return mainService.updateStation(station);
    }

    @GetMapping("/user/getAll")
    public List<User> getAllUsers() {
        return mainService.getAllUsers();
    }
    @GetMapping("/user/getAllByFilter")
    public List<User> getAllUsers(@RequestParam("depId") Integer depId, @RequestParam("positionId") Integer positionId) {
        return mainService.getAllUsersByFilter(depId, positionId);
    }
    @GetMapping("/user/findById")
    public User getUserById(@RequestParam("id") Integer id) {
        return mainService.getUserById(id);
    }
    @GetMapping("/user/searchUser")
    public List<User> searchUser(@RequestParam("username") String username) {
        return mainService.getAllUsers();
    }
}
