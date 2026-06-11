package mn.usug.dis_news_service.Service;

import lombok.RequiredArgsConstructor;
import mn.usug.dis_news_service.DAO.*;
import mn.usug.dis_news_service.DTO.BriefingNewsDto;
import mn.usug.dis_news_service.Entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Шуурхайн мэдээ (§3.4).
 *  - Нэгж бүр хурлаас өмнө (Даваа 16:00 хүртэл) 7 хоногийн онцлох ажлаа оруулна.
 *  - Танилцуулга нь briefing_unit.sort_order-оор автоматаар жагсана (§4.1.3).
 *  - Фото/файл нь file service-ээр, видео/cloud линк нь шууд хадгалагдана.
 */
@Service
@RequiredArgsConstructor
public class BriefingNewsService {

    private static final ZoneId UB = ZoneId.of("Asia/Ulaanbaatar");
    private LocalDateTime now() { return LocalDateTime.now(UB); }

    private final BriefingUnitRepository unitRepo;
    private final BriefingNewsRepository newsRepo;
    private final BriefingNewsEvidenceRepository evidenceRepo;
    private final BriefingMeetingRepository meetingRepo;
    private final UserDAO userRepo;
    private final BriefingAccessService access;
    private final BriefingAuditService audit;

    /** Мэдээ оруулах эцсийн хугацаа — хурлаас өмнөх өдөр (Даваа) 16:00 */
    private LocalDateTime newsDeadlineOf(LocalDate meetingDate) {
        return meetingDate.minusDays(1).atTime(16, 0);
    }

    // ── Нэгжүүд ──────────────────────────────────────────────────────────────────

    public List<BriefingUnit> listUnits() {
        return unitRepo.findByActiveFlagOrderBySortOrderAsc(1);
    }

    // ── Хурлын мэдээ (нэгж бүрээр, дараалалд) ─────────────────────────────────────

    public List<BriefingNewsDto> getMeetingNews(Integer meetingId) {
        List<BriefingUnit> units = listUnits();
        Map<Integer, BriefingNews> byUnit = newsRepo.findByMeetingId(meetingId).stream()
                .collect(Collectors.toMap(BriefingNews::getUnitId, n -> n, (a, b) -> a));

        List<String> folderIds = byUnit.values().stream()
                .map(BriefingNews::getFolderId).filter(Objects::nonNull).collect(Collectors.toList());
        Map<String, List<BriefingNewsEvidence>> evByFolder = folderIds.isEmpty() ? Map.of()
                : evidenceRepo.findByFolderIdIn(folderIds).stream()
                    .collect(Collectors.groupingBy(BriefingNewsEvidence::getFolderId));

        Map<Integer, String> userMap = userRepo.findAll().stream()
                .collect(Collectors.toMap(User::getId, this::fullName, (a, b) -> a));

        return units.stream().map(u -> {
            BriefingNewsDto d = new BriefingNewsDto();
            d.setMeetingId(meetingId);
            d.setUnitId(u.getId());
            d.setUnitCode(u.getCode());
            d.setUnitName(u.getName());
            d.setUnitType(u.getUnitType());
            d.setSortOrder(u.getSortOrder());
            BriefingNews n = byUnit.get(u.getId());
            if (n != null) {
                d.setId(n.getId());
                d.setSummaryText(n.getSummaryText());
                d.setResult(n.getResult());
                d.setExtraProposal(n.getExtraProposal());
                d.setFolderId(n.getFolderId());
                d.setStatus(n.getStatus());
                d.setSubmittedAt(n.getSubmittedAt());
                d.setCreatedByName(n.getCreatedBy() != null ? userMap.get(n.getCreatedBy()) : null);
                d.setEvidence(evByFolder.getOrDefault(n.getFolderId(), List.of()).stream()
                        .map(this::toEvidenceDto).collect(Collectors.toList()));
            } else {
                d.setEvidence(List.of());
            }
            return d;
        }).collect(Collectors.toList());
    }

    // ── Мэдээ хадгалах (draft) ────────────────────────────────────────────────────

    @Transactional
    public BriefingNews saveNews(Integer meetingId, Integer unitId, String summaryText, String result, String extraProposal) {
        access.requireNotViewer(UserContext.getUserId());
        BriefingMeeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Хурал олдсонгүй"));
        if (now().isAfter(newsDeadlineOf(meeting.getMeetingDate())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Шуурхайн мэдээ оруулах хугацаа дууссан (Даваа 16:00)");

        BriefingNews n = newsRepo.findByMeetingIdAndUnitId(meetingId, unitId).orElseGet(() -> {
            BriefingNews nn = new BriefingNews();
            nn.setMeetingId(meetingId);
            nn.setUnitId(unitId);
            nn.setFolderId(UUID.randomUUID().toString());
            nn.setStatus(0);
            nn.setCreatedBy(UserContext.getUserId());
            nn.setCreatedDate(now());
            return nn;
        });
        n.setSummaryText(summaryText);
        n.setResult(result);
        n.setExtraProposal(extraProposal);
        n.setUpdatedAt(now());
        return newsRepo.save(n);
    }

    /** Мэдээ илгээх (submitted болгоно) */
    @Transactional
    public BriefingNews submitNews(Integer meetingId, Integer unitId) {
        access.requireNotViewer(UserContext.getUserId());
        BriefingMeeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Хурал олдсонгүй"));
        if (now().isAfter(newsDeadlineOf(meeting.getMeetingDate())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Шуурхайн мэдээ илгээх хугацаа дууссан (Даваа 16:00)");
        BriefingNews n = newsRepo.findByMeetingIdAndUnitId(meetingId, unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Эхлээд мэдээгээ хадгална уу"));
        if (n.getSummaryText() == null || n.getSummaryText().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Хийсэн ажлын товч тайлбар хоосон байна");
        n.setStatus(1);
        n.setSubmittedAt(now());
        n.setUpdatedAt(now());
        BriefingNews saved = newsRepo.save(n);
        audit.log("SUBMIT_NEWS", "NEWS", saved.getId(), null, "meetingId=" + meetingId + "; unitId=" + unitId);
        return saved;
    }

    // ── Нотлох баримт (файл эсвэл линк) ──────────────────────────────────────────

    @Transactional
    public BriefingNewsDto.Evidence addEvidence(String folderId, String objectName, String linkUrl, String evidenceType,
                                                String fileName, String contentType, Long fileSize) {
        access.requireNotViewer(UserContext.getUserId());
        BriefingNews n = newsRepo.findByFolderId(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мэдээ олдсонгүй"));
        BriefingMeeting meeting = meetingRepo.findById(n.getMeetingId()).orElse(null);
        if (meeting != null && now().isAfter(newsDeadlineOf(meeting.getMeetingDate())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Хавсаргах хугацаа дууссан (Даваа 16:00)");

        String type = (evidenceType == null || evidenceType.isBlank()) ? "FILE" : evidenceType.toUpperCase();
        if ("LINK".equals(type)) {
            if (linkUrl == null || linkUrl.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Холбоос (URL) хоосон байна");
        } else if (objectName == null || objectName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файлын objectName хоосон байна");
        }

        BriefingNewsEvidence e = new BriefingNewsEvidence();
        e.setFolderId(folderId);
        e.setObjectName(objectName);
        e.setLinkUrl(linkUrl);
        e.setEvidenceType(type);
        e.setFileName(fileName);
        e.setContentType(contentType);
        e.setFileSize(fileSize);
        e.setUploadedBy(UserContext.getUserId());
        e.setUploadedAt(now());
        return toEvidenceDto(evidenceRepo.save(e));
    }

    @Transactional
    public void deleteEvidence(Integer id) {
        access.requireNotViewer(UserContext.getUserId());
        evidenceRepo.deleteById(id);
    }

    // ── Туслах ─────────────────────────────────────────────────────────────────────

    private BriefingNewsDto.Evidence toEvidenceDto(BriefingNewsEvidence e) {
        BriefingNewsDto.Evidence ed = new BriefingNewsDto.Evidence();
        ed.setId(e.getId());
        ed.setObjectName(e.getObjectName());
        ed.setLinkUrl(e.getLinkUrl());
        ed.setEvidenceType(e.getEvidenceType());
        ed.setFileName(e.getFileName());
        ed.setContentType(e.getContentType());
        ed.setFileSize(e.getFileSize());
        ed.setUploadedAt(e.getUploadedAt());
        return ed;
    }

    private String fullName(User u) {
        String ln = u.getLastName() != null && !u.getLastName().isBlank()
                ? u.getLastName().charAt(0) + ". " : "";
        String fn = u.getFirstName() != null ? u.getFirstName() : "";
        return (ln + fn).trim();
    }
}
