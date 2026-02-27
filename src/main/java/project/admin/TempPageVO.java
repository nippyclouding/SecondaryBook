package project.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TempPageVO {
    private Long pageSeq;
    private String title;
    private String content;
    private LocalDateTime crtDtm;
}