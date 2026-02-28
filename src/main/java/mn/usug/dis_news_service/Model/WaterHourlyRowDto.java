package mn.usug.dis_news_service.Model;

import lombok.Builder;

@Builder
public record WaterHourlyRowDto(
        String groupName,
        Integer groupOrd,
        Integer menuId,
        String stationName,

        Integer wellWorking,
        Integer wellPending,
        Integer wellRepairing,

        Integer pool1,
        Integer pool2,
        Integer pool3,
        Integer pool4,

        Integer pipeFm1,
        Integer pipeFm7,
        Integer pipeFm8,

        String pumpWorking,   // ж: "2,1,7"
        String pumpPending,   // ж: "5,2,3,4,5,6"
        String pumpRepairing, // ж: "0" эсвэл "1,8"

        String pressureBar,   // ж: "5.6-6.0" эсвэл "6.0"
        Double chlorineMgL,   // тоон
        Double pumpedM3h      // тоон
) {}
