package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mn.usug.dis_news_service.DAO.UserDAO;
import mn.usug.dis_news_service.DAO.VehicleOrderRepository;
import mn.usug.dis_news_service.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Суудлын машины (orderType=1) хүсэлтийн албаны дотоод баталгаажуулалтыг
 * алгасах бодлого.
 *
 * Алгасах эрхтэй хэрэглэгчийн хүсэлт `deptApproved=true`-тэй үүсэж, шууд
 * автобаазын хуваарилалтын жагсаалтад ордог. Автобаазын дарга урьдын адил
 * баталгаажуулаад (status 0→1) машин хувиарлана (1→2).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleOrderApprovalPolicy {

    private final UserDAO userDAO;
    private final VehicleOrderRepository orderRepo;

    /** Албаны баталгаажуулалт алгасах хэрэглэгчийн id-ууд (таслалаар) */
    @Value("${vehicle-order.dept-approval-skip.user-ids:258}")
    private List<Integer> skipUserIds;

    /** Албан тушаалын id-аар алгасуулах шаардлагатай бол (таслалаар, хоосон = ашиглахгүй) */
    @Value("${vehicle-order.dept-approval-skip.position-ids:}")
    private List<Integer> skipPositionIds;

    /** Тухайн хэрэглэгч албаны баталгаажуулалтыг алгасах эрхтэй эсэх */
    public boolean skipsDeptApproval(Integer userId) {
        if (userId == null) return false;
        if (skipUserIds != null && skipUserIds.contains(userId)) return true;
        if (skipPositionIds == null || skipPositionIds.isEmpty()) return false;
        User u = userDAO.findById(userId).orElse(null);
        return u != null && u.getPositionId() != null && skipPositionIds.contains(u.getPositionId());
    }

    /** Алгасах эрхтэй бүх хэрэглэгчийн id (тохиргооны id + албан тушаалаар олдсон) */
    public List<Integer> resolveSkipUserIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        if (skipUserIds != null) ids.addAll(skipUserIds);
        if (skipPositionIds != null && !skipPositionIds.isEmpty()) {
            for (User u : userDAO.findAll()) {
                if (u.getPositionId() != null && skipPositionIds.contains(u.getPositionId())) {
                    ids.add(u.getId());
                }
            }
        }
        return new ArrayList<>(ids);
    }

    /**
     * Хуучин (өмнө нь үүссэн) хүлээгдэж буй хүсэлтүүдийг албаны баталгаажуулалтаас
     * гаргаж, шууд автобаазын жагсаалт руу шилжүүлнэ. Апп асах бүрд ажиллана,
     * давхардаж ажиллахад аюулгүй (зөвхөн status=0, deptApproved!=true мөрийг хөндөнө).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void promoteExistingPendingOrders() {
        List<Integer> ids = resolveSkipUserIds();
        if (ids.isEmpty()) return;
        int moved = orderRepo.promoteDeptApproval(ids);
        if (moved > 0) {
            log.info("Албаны баталгаажуулалт алгасав: {} хүлээгдэж буй хүсэлт автобаазын жагсаалт руу шилжлээ (userIds={})",
                    moved, ids);
        }
    }
}
