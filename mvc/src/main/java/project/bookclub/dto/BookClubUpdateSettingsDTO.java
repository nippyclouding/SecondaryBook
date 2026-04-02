package project.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 독서모임 정보 수정 요청 DTO
 * POST /bookclubs/{bookClubId}/manage/settings
 *
 * 클라이언트에서 JSON으로 받는 필드:
 * - name: 모임 이름
 * - description: 모임 소개
 * - region: 지역
 * - schedule: 정기 일정
 * - bannerImgUrl: 대표 이미지 URL (1차: URL 입력, 2차: 파일 업로드)
 *
 * 참고:
 * - maxMember(정원)는 수정 불가 (UI/로직에서 제외)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookClubUpdateSettingsDTO {

    @NotBlank(message = "모임 이름을 입력해주세요.")
    @Size(max = 50, message = "모임 이름은 50자 이내로 입력해주세요.")
    private String name;

    @NotBlank(message = "모임 소개를 입력해주세요.")
    @Size(max = 500, message = "모임 소개는 500자 이내로 입력해주세요.")
    private String description;

    private String region;
    private String schedule;
    private String bannerImgUrl;
}
