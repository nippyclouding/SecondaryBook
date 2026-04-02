package project.admin;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TempPageVO implements Serializable {
    private Long pageSeq;
    private String title;
    private String content;
    private LocalDateTime crtDtm;
}