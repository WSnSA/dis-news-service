package mn.usug.dis_news_service.DTO;

public record NotificationDto(
        String category,   // ws | task | news | vehicle-order | vehicle-out
        String title,
        String message,
        String icon,
        long   timestamp
) {}
