package mn.usug.dis_news_service.Service;

import mn.usug.dis_news_service.DAO.BaseBuTypeDAO;
import mn.usug.dis_news_service.DAO.DepartmentDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MainService {
    @Autowired
    DepartmentDAO departmentDAO;
}
