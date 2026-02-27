package project.bookclub.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import project.util.Const;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class BookClubVO {
    private Long book_club_seq; // 독서모임 ID
    private Long book_club_leader_seq; // 모임장 ID, FK

    @NotBlank(message = "모임 이름을 입력해주세요.")
    @Size(max = 50, message = "모임 이름은 50자 이내로 입력해주세요.")
    private String book_club_name; // 독서모임명

    @NotBlank(message = "모임 소개를 입력해주세요.")
    @Size(max = 500, message = "모임 소개는 500자 이내로 입력해주세요.")
    private String book_club_desc; // 독서모임 설명

    @NotBlank(message = "활동 지역을 입력해주세요.")
    private String book_club_rg; // 독서모임 지역

    @NotNull(message = "최대 인원을 입력해주세요.")
    @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
    @Max(value = 100, message = "최대 인원은 100명 이하이어야 합니다.")
    private Integer book_club_max_member; // 독서모임의 최대 인원
    private LocalDateTime book_club_deleted_dt; // 독서모임 삭제 일시
    private String banner_img_url; // 독서모임 배너 이미지
    private String book_club_schedule; // 독서모임 정기 일정
    private LocalDateTime upd_dtm; // 독서모임의 내용 수정 일시
    private Integer joined_member_count; // 독서모임의 가입 회원 수

    // 찜 관련 (DB 매핑 아님, Controller에서 설정)
    private boolean wished; // 현재 로그인 사용자의 찜 여부
    private Integer wish_count; // 찜 개수

    private Boolean leader_yn;

    @JsonIgnore  // JSON 변환 시 제외
    private LocalDateTime crt_dtm;

    // JSON으로 반환할 포맷팅된 문자열
    @JsonProperty("crt_dtm")
    public String getCrtDtmFormatted() {
        return crt_dtm != null ? crt_dtm.format(Const.DATETIME_FORMATTER) : null;
    }
}
