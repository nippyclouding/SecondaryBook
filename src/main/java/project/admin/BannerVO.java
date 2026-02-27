package project.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BannerVO {
    private Long bannerSeq;       // 배너 고유 번호 (PK)
    private String title;         // 배너 제목
    private String subtitle;      // 배너 부제목
    private String bgColorFrom;   // 배경 그라데이션 시작색 (Hex code)
    private String bgColorTo;     // 배경 그라데이션 끝색 (Hex code)
    private String btnText;       // 버튼 텍스트
    private String btnLink;       // 버튼 클릭 시 이동할 URL
    private String iconName;      // 아이콘 이름 (Lucide icon)
    private Integer orderIdx;     // 배너 노출 순서
    private Boolean isActive;     // 활성화 여부
    private LocalDateTime crtDtm; // 생성 일시
}