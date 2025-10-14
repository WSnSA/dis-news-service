package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.DAO.DepartmentDAO;
import mn.usug.dis_news_service.DAO.PositionDAO;
import mn.usug.dis_news_service.DAO.StationDAO;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.Entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainService {
    @Autowired
    DepartmentDAO departmentDAO;
    @Autowired
    PositionDAO positionDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    StationDAO stationDAO;

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User getUserById(Integer id) {
        return userDAO.findById(id).orElse(null);
    }

    public User getUserByUsername(String username) {
        User user = userDAO.findUserByUsername(username);
        return user;
    }

    public void saveUser(User user) {
        userDAO.save(user);
    }

    public List<Position> getAllPositions() {
        List<Position> list = positionDAO.findAll();
        list.forEach(item -> {
            item.setDepName(getDepartmentById(item.getDepId()).getDepName());
        });
        return list;
    }

    public Position getPositionById(Integer id) {
        Position position = positionDAO.findById(id).orElse(null);
        return position;
    }

    public Position createPosition(Position position) {
        positionDAO.save(position);
        return position;
    }

    public Position updatePosition(Position position) {
        Position oldPosition = positionDAO.findById(position.getId()).orElse(null);
        oldPosition.setDepId(position.getDepId());
        oldPosition.setName(position.getName());
        oldPosition.setEmployeeCount(position.getEmployeeCount());
        if (oldPosition.getDepId() != null) {
            oldPosition.setDepName(getDepartmentById(oldPosition.getDepId()).getDepName());
        }
        else {
            oldPosition.setDepName(null);
        }
        positionDAO.save(oldPosition);
        return oldPosition;
    }

    public List<Department> getAllDepartments() {
        List<Department> list = departmentDAO.findAll();
        return list;
    }

    public Department getDepartmentById(Integer id) {
        return departmentDAO.findById(id).orElse(null);
    }

    public List<Position> getPositionsByDepId(Integer id) {
        List<Position> list = positionDAO.findByDepId(id);
        list.forEach(item -> {
            item.setDepName(getDepartmentById(item.getDepId()).getDepName());
        });
        return list;
    }

    public List<Department> getDepartmentsByName(String name) {
        return departmentDAO.findDepartmentsByDepNameContaining(name);
    }

    public ResponseEntity deleteDepartmentById(Integer id) {
        Department department = departmentDAO.findById(id).orElse(null);
        departmentDAO.delete(department);
        return ResponseEntity.ok(department);
    }

    public Department addDepartment(String name, Integer employeeCount, Integer chairmanId) {
        Department department = new Department();

        User user = userDAO.findById(chairmanId).orElse(null);
        if(user == null){
            return null;
        }


        department.setChairmanId(chairmanId);
        department.setDepName(name);
        department.setEmployeeCount(employeeCount);
        departmentDAO.save(department);
        return department;
    }

    public Department editDepartment(Integer id, String name, Integer employeeCount, Integer chairmanId) {
        Department department = departmentDAO.findById(id).orElse(null);
        if (department == null) {
            return null;
        }
        department.setChairmanId(chairmanId);
        department.setDepName(name);
        department.setEmployeeCount(employeeCount);
        departmentDAO.save(department);
        return department;
    }

    public ResponseEntity deletePositionById(Integer id) {
        Position position = positionDAO.findById(id).orElse(null);
        positionDAO.delete(position);
        return ResponseEntity.ok(position);
    }

    public ResponseEntity getAllStations(Integer depId) {
        return ResponseEntity.ok(stationDAO.findByDepId());
    }

    public ResponseEntity getStationById(Integer id) {
        return ResponseEntity.ok(stationDAO.findById(id).orElse(null));
    }

    public ResponseEntity addStation(Station station) {
        return ResponseEntity.ok(stationDAO.save(station));
    }
    public ResponseEntity updateStation(Station station) {
        Station oldStation = stationDAO.findById(station.getId()).orElse(null);
        oldStation.setChefId(station.getChefId());
        oldStation.setDepartmentId(station.getDepartmentId());
        oldStation.setIsWaterSupply(station.getIsWaterSupply());
        oldStation.setName(station.getName());
        oldStation.setPoolCount(station.getPoolCount());
        oldStation.setFirstGeneratorCount(station.getFirstGeneratorCount());
        oldStation.setSecondGeneratorCount(station.getSecondGeneratorCount());
        oldStation.setWellsNumber(station.getWellsNumber());
        return ResponseEntity.ok(stationDAO.save(oldStation));
    }

    public List<User> getAllUsersByFilter(Integer depId, Integer positionId) {
        return userDAO.findUsersByFilter(depId,positionId);
    }

    public List<Position> getPositionsByName(Integer depId, String name) {
        return positionDAO.findByNameContaining(depId, name);
    }
}
