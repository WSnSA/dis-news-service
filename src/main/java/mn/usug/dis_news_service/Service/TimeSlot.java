package mn.usug.dis_news_service.Service;

/**
 * Машин захиалгын ээлж — bitmask.
 *
 * <pre>
 *   1 = Өглөө
 *   2 = Өдөр (үдээс хойш)
 *   3 = Бүтэн өдөр (1|2)
 * </pre>
 *
 * Хоёр захиалга нэг машиныг зэрэг эзэлж байгаа эсэхийг {@link #overlaps} -оор шалгана.
 * Цагийн (HH:mm) нарийвчлалаар шалгах боломжгүй — захиалгын форм нь эхлэх цагийг л
 * чөлөөт текстээр авдаг, дуусах цаг гэж талбар байхгүй. Тиймээс өглөө/өдөр гэсэн
 * хоёр ээлжээр л мөргөлдөөнийг тодорхойлно.
 */
public final class TimeSlot {

    public static final int MORNING  = 1;
    public static final int AFTERNOON = 2;
    public static final int FULL_DAY = 3;

    private TimeSlot() {}

    /** Хоосон/буруу утгыг бүтэн өдөр болгож хэвийшүүлнэ (хамгийн болгоомжтой сонголт) */
    public static int normalize(Integer slot) {
        if (slot == null) return FULL_DAY;
        int masked = slot & FULL_DAY;
        return masked == 0 ? FULL_DAY : masked;
    }

    /** Хоёр ээлж огтлолцож байна уу */
    public static boolean overlaps(Integer a, Integer b) {
        return (normalize(a) & normalize(b)) != 0;
    }

    /** "Өглөө" / "Өдөр" / "Бүтэн өдөр" */
    public static String label(Integer slot) {
        return switch (normalize(slot)) {
            case MORNING   -> "Өглөө";
            case AFTERNOON -> "Өдөр";
            default        -> "Бүтэн өдөр";
        };
    }
}
