package project.bookclub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import project.bookclub.ENUM.JoinRequestResult;
import project.bookclub.ENUM.JoinStatus;
import project.bookclub.ENUM.RequestStatus;
import project.bookclub.dto.BookClubJoinRequestDTO;
import project.bookclub.dto.BookClubManageMemberDTO;
import project.bookclub.vo.BookClubVO;
import project.util.exception.bookclub.BookClubInvalidRequestException;
import project.util.imgUpload.ImgService;
import project.util.imgUpload.S3Service;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookClubServiceTest {

    @Mock
    BookClubMapper bookClubMapper;

    @Mock
    S3Service s3Service;

    @Mock
    ImgService imgService;

    @InjectMocks
    BookClubService bookClubService;

    // ========== 헬퍼 메서드 ==========

    private BookClubVO createBookClub(Long seq, Long leaderSeq, String name, int maxMember) {
        BookClubVO vo = new BookClubVO();
        vo.setBook_club_seq(seq);
        vo.setBook_club_leader_seq(leaderSeq);
        vo.setBook_club_name(name);
        vo.setBook_club_max_member(maxMember);
        return vo;
    }

    private BookClubJoinRequestDTO createRequest(Long requestSeq, Long bookClubSeq, Long memberSeq, RequestStatus status) {
        BookClubJoinRequestDTO dto = new BookClubJoinRequestDTO();
        dto.setRequestSeq(requestSeq);
        dto.setBookClubSeq(bookClubSeq);
        dto.setRequestMemberSeq(memberSeq);
        dto.setRequestSt(status);
        return dto;
    }

    private BookClubManageMemberDTO createMember(JoinStatus joinSt, String leaderYn) {
        BookClubManageMemberDTO dto = new BookClubManageMemberDTO();
        dto.setJoinSt(joinSt);
        dto.setLeaderYn(leaderYn);
        return dto;
    }

    // ========================================================================
    // createJoinRequest - 가입 신청
    // ========================================================================
    @Nested
    @DisplayName("createJoinRequest - 가입 신청")
    class CreateJoinRequest {

        @Test
        @DisplayName("정상 가입 신청 - SUCCESS")
        void 정상신청_SUCCESS() {
            // given
            when(bookClubMapper.selectJoinedMemberCount(1L, 2L)).thenReturn(0);
            when(bookClubMapper.selectPendingRequestCount(1L, 2L)).thenReturn(0);

            // when
            JoinRequestResult result = bookClubService.createJoinRequest(1L, 2L, "가입하고 싶습니다");

            // then
            assertThat(result).isEqualTo(JoinRequestResult.SUCCESS);
            verify(bookClubMapper).insertJoinRequest(1L, 2L, "가입하고 싶습니다");
        }

        @Test
        @DisplayName("이미 가입된 멤버 - ALREADY_JOINED")
        void 이미가입_ALREADY_JOINED() {
            // given
            when(bookClubMapper.selectJoinedMemberCount(1L, 2L)).thenReturn(1);

            // when
            JoinRequestResult result = bookClubService.createJoinRequest(1L, 2L, "메시지");

            // then
            assertThat(result).isEqualTo(JoinRequestResult.ALREADY_JOINED);
            verify(bookClubMapper, never()).insertJoinRequest(anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("이미 신청 대기중 - ALREADY_REQUESTED")
        void 대기중_ALREADY_REQUESTED() {
            // given
            when(bookClubMapper.selectJoinedMemberCount(1L, 2L)).thenReturn(0);
            when(bookClubMapper.selectPendingRequestCount(1L, 2L)).thenReturn(1);

            // when
            JoinRequestResult result = bookClubService.createJoinRequest(1L, 2L, "메시지");

            // then
            assertThat(result).isEqualTo(JoinRequestResult.ALREADY_REQUESTED);
        }

        @Test
        @DisplayName("파라미터 null - INVALID_PARAMETERS")
        void 파라미터null_INVALID() {
            assertThat(bookClubService.createJoinRequest(null, 2L, "메시지"))
                    .isEqualTo(JoinRequestResult.INVALID_PARAMETERS);
            assertThat(bookClubService.createJoinRequest(1L, null, "메시지"))
                    .isEqualTo(JoinRequestResult.INVALID_PARAMETERS);
        }

        @Test
        @DisplayName("동시성 이슈 - DB UNIQUE 위반 시 ALREADY_REQUESTED")
        void 동시성_UNIQUE위반() {
            // given
            when(bookClubMapper.selectJoinedMemberCount(1L, 2L)).thenReturn(0);
            when(bookClubMapper.selectPendingRequestCount(1L, 2L)).thenReturn(0);
            doThrow(new DataIntegrityViolationException("Unique constraint"))
                    .when(bookClubMapper).insertJoinRequest(1L, 2L, "메시지");

            // when
            JoinRequestResult result = bookClubService.createJoinRequest(1L, 2L, "메시지");

            // then
            assertThat(result).isEqualTo(JoinRequestResult.ALREADY_REQUESTED);
        }
    }

    // ========================================================================
    // approveJoinRequest - 가입 승인
    // ========================================================================
    @Nested
    @DisplayName("approveJoinRequest - 가입 승인")
    class ApproveJoinRequest {

        @Test
        @DisplayName("신규 멤버 승인 - 성공")
        void 신규멤버_승인성공() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.WAIT));
            when(bookClubMapper.selectById(1L)).thenReturn(createBookClub(1L, 100L, "모임", 10));
            when(bookClubMapper.getTotalJoinedMemberCount(1L)).thenReturn(5);
            when(bookClubMapper.selectMemberJoinSt(1L, 2L)).thenReturn(null); // 신규

            // when
            bookClubService.approveJoinRequest(1L, 10L, 100L);

            // then
            verify(bookClubMapper).insertMember(1L, 2L);
            verify(bookClubMapper).updateRequestStatus(10L, RequestStatus.APPROVED);
        }

        @Test
        @DisplayName("재가입 멤버 승인 - LEFT 상태에서 JOINED 복구")
        void 재가입멤버_승인성공() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.WAIT));
            when(bookClubMapper.selectById(1L)).thenReturn(createBookClub(1L, 100L, "모임", 10));
            when(bookClubMapper.getTotalJoinedMemberCount(1L)).thenReturn(5);
            when(bookClubMapper.selectMemberJoinSt(1L, 2L)).thenReturn(JoinStatus.LEFT);
            when(bookClubMapper.restoreMemberToJoined(1L, 2L)).thenReturn(1);

            // when
            bookClubService.approveJoinRequest(1L, 10L, 100L);

            // then
            verify(bookClubMapper).restoreMemberToJoined(1L, 2L);
            verify(bookClubMapper).updateRequestStatus(10L, RequestStatus.APPROVED);
        }

        @Test
        @DisplayName("정원 초과 - IllegalStateException")
        void 정원초과_예외() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.WAIT));
            when(bookClubMapper.selectById(1L)).thenReturn(createBookClub(1L, 100L, "모임", 5));
            when(bookClubMapper.getTotalJoinedMemberCount(1L)).thenReturn(5); // 정원 꽉 참

            // when & then
            assertThatThrownBy(() -> bookClubService.approveJoinRequest(1L, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("정원");
        }

        @Test
        @DisplayName("이미 JOINED 상태인 멤버 - IllegalStateException")
        void 이미가입_예외() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.WAIT));
            when(bookClubMapper.selectById(1L)).thenReturn(createBookClub(1L, 100L, "모임", 10));
            when(bookClubMapper.getTotalJoinedMemberCount(1L)).thenReturn(5);
            when(bookClubMapper.selectMemberJoinSt(1L, 2L)).thenReturn(JoinStatus.JOINED);

            // when & then
            assertThatThrownBy(() -> bookClubService.approveJoinRequest(1L, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("이미 가입된");
        }

        @Test
        @DisplayName("이미 처리된 신청 - IllegalStateException")
        void 이미처리된신청_예외() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.APPROVED));

            // when & then
            assertThatThrownBy(() -> bookClubService.approveJoinRequest(1L, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("이미 처리된");
        }

        @Test
        @DisplayName("파라미터 null - IllegalArgumentException")
        void 파라미터null_예외() {
            assertThatThrownBy(() -> bookClubService.approveJoinRequest(null, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }

        @Test
        @DisplayName("삭제된 모임 - IllegalStateException")
        void 삭제된모임_예외() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> bookClubService.approveJoinRequest(1L, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("종료된");
        }
    }

    // ========================================================================
    // rejectJoinRequest - 가입 거절
    // ========================================================================
    @Nested
    @DisplayName("rejectJoinRequest - 가입 거절")
    class RejectJoinRequest {

        @Test
        @DisplayName("정상 거절")
        void 정상거절() {
            // given
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.WAIT));

            // when
            bookClubService.rejectJoinRequest(1L, 10L, 100L);

            // then
            verify(bookClubMapper).updateRequestStatus(10L, RequestStatus.REJECTED);
        }

        @Test
        @DisplayName("이미 처리된 신청 거절 시도 - IllegalStateException")
        void 이미처리된_예외() {
            // given
            when(bookClubMapper.selectRequestById(10L)).thenReturn(createRequest(10L, 1L, 2L, RequestStatus.APPROVED));

            // when & then
            assertThatThrownBy(() -> bookClubService.rejectJoinRequest(1L, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }

        @Test
        @DisplayName("파라미터 null - IllegalArgumentException")
        void 파라미터null_예외() {
            assertThatThrownBy(() -> bookClubService.rejectJoinRequest(null, 10L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }
    }

    // ========================================================================
    // kickMember - 멤버 강퇴
    // ========================================================================
    @Nested
    @DisplayName("kickMember - 멤버 강퇴")
    class KickMember {

        @Test
        @DisplayName("정상 강퇴")
        void 정상강퇴() {
            // given
            when(bookClubMapper.selectMemberBySeq(1L, 3L)).thenReturn(createMember(JoinStatus.JOINED, "N"));
            when(bookClubMapper.updateMemberToKicked(1L, 3L)).thenReturn(1);

            // when
            bookClubService.kickMember(1L, 100L, 3L);

            // then
            verify(bookClubMapper).updateMemberToKicked(1L, 3L);
        }

        @Test
        @DisplayName("모임장 강퇴 시도 - IllegalStateException")
        void 모임장강퇴_예외() {
            // given
            when(bookClubMapper.selectMemberBySeq(1L, 100L)).thenReturn(createMember(JoinStatus.JOINED, "Y"));

            // when & then
            assertThatThrownBy(() -> bookClubService.kickMember(1L, 100L, 100L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("모임장은 강퇴할 수 없습니다");
        }

        @Test
        @DisplayName("JOINED가 아닌 멤버 강퇴 시도 - IllegalStateException")
        void 비가입멤버_예외() {
            // given
            when(bookClubMapper.selectMemberBySeq(1L, 3L)).thenReturn(createMember(JoinStatus.LEFT, "N"));

            // when & then
            assertThatThrownBy(() -> bookClubService.kickMember(1L, 100L, 3L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("가입된 멤버가 아닙니다");
        }

        @Test
        @DisplayName("존재하지 않는 멤버 - IllegalStateException")
        void 존재하지않는멤버_예외() {
            // given
            when(bookClubMapper.selectMemberBySeq(1L, 3L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> bookClubService.kickMember(1L, 100L, 3L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }

        @Test
        @DisplayName("파라미터 null - IllegalArgumentException")
        void 파라미터null_예외() {
            assertThatThrownBy(() -> bookClubService.kickMember(null, 100L, 3L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }
    }

    // ========================================================================
    // leaveBookClub - 모임 탈퇴
    // ========================================================================
    @Nested
    @DisplayName("leaveBookClub - 모임 탈퇴")
    class LeaveBookClub {

        @Test
        @DisplayName("일반 멤버 탈퇴 - 성공")
        void 일반멤버_탈퇴() {
            // given
            Map<String, Object> memberStatus = new HashMap<>();
            memberStatus.put("join_st", "JOINED");
            memberStatus.put("leader_yn", false);

            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectMyMemberStatus(1L, 2L)).thenReturn(memberStatus);
            when(bookClubMapper.updateMemberToLeft(1L, 2L)).thenReturn(1);

            // when
            Map<String, Object> result = bookClubService.leaveBookClub(1L, 2L);

            // then
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("ctaStatus")).isEqualTo("NONE");
            verify(bookClubMapper).updateMemberToLeft(1L, 2L);
        }

        @Test
        @DisplayName("모임장 탈퇴 - 다른 멤버에게 승계")
        void 모임장탈퇴_승계() {
            // given
            Map<String, Object> memberStatus = new HashMap<>();
            memberStatus.put("join_st", "JOINED");
            memberStatus.put("leader_yn", true);

            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectMyMemberStatus(1L, 100L)).thenReturn(memberStatus);
            when(bookClubMapper.selectNextLeaderCandidate(1L)).thenReturn(3L); // 승계 대상 있음
            when(bookClubMapper.updateMemberToLeft(1L, 100L)).thenReturn(1);
            when(bookClubMapper.updateMemberToLeader(1L, 3L)).thenReturn(1);
            when(bookClubMapper.updateBookClubLeader(1L, 3L)).thenReturn(1);

            // when
            Map<String, Object> result = bookClubService.leaveBookClub(1L, 100L);

            // then
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("leaderChanged")).isEqualTo(true);
            assertThat(result.get("newLeaderSeq")).isEqualTo(3L);
            verify(bookClubMapper).updateMemberToLeader(1L, 3L);
            verify(bookClubMapper).updateBookClubLeader(1L, 3L);
        }

        @Test
        @DisplayName("모임장 탈퇴 - 마지막 멤버, 모임 종료")
        void 모임장탈퇴_모임종료() {
            // given
            Map<String, Object> memberStatus = new HashMap<>();
            memberStatus.put("join_st", "JOINED");
            memberStatus.put("leader_yn", true);

            BookClubVO bookClub = createBookClub(1L, 100L, "모임", 10);
            bookClub.setBanner_img_url("https://s3.example.com/banner.jpg");

            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectMyMemberStatus(1L, 100L)).thenReturn(memberStatus);
            when(bookClubMapper.selectNextLeaderCandidate(1L)).thenReturn(null); // 승계 대상 없음
            when(bookClubMapper.selectById(1L)).thenReturn(bookClub);
            when(bookClubMapper.selectAllBoardImageUrls(1L)).thenReturn(Collections.emptyList());
            when(bookClubMapper.updateMemberToLeft(1L, 100L)).thenReturn(1);
            when(bookClubMapper.closeBookClub(1L)).thenReturn(1);

            // when
            Map<String, Object> result = bookClubService.leaveBookClub(1L, 100L);

            // then
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("clubClosed")).isEqualTo(true);
            verify(bookClubMapper).closeBookClub(1L);
        }

        @Test
        @DisplayName("이미 탈퇴한 멤버 - IllegalStateException")
        void 이미탈퇴_예외() {
            // given
            Map<String, Object> memberStatus = new HashMap<>();
            memberStatus.put("join_st", "LEFT");
            memberStatus.put("leader_yn", false);

            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectMyMemberStatus(1L, 2L)).thenReturn(memberStatus);

            // when & then
            assertThatThrownBy(() -> bookClubService.leaveBookClub(1L, 2L))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("이미 탈퇴");
        }

        @Test
        @DisplayName("존재하지 않는 멤버 - IllegalStateException")
        void 존재하지않는멤버_예외() {
            // given
            when(bookClubMapper.lockBookClubForUpdate(1L)).thenReturn(1L);
            when(bookClubMapper.selectMyMemberStatus(1L, 2L)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> bookClubService.leaveBookClub(1L, 2L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }

        @Test
        @DisplayName("파라미터 null - IllegalArgumentException")
        void 파라미터null_예외() {
            assertThatThrownBy(() -> bookClubService.leaveBookClub(null, 2L))
                    .isInstanceOf(BookClubInvalidRequestException.class);
        }
    }

    // ========================================================================
    // createBookClub - 모임 생성
    // ========================================================================
    @Nested
    @DisplayName("createBookClub - 모임 생성")
    class CreateBookClub {

        @Test
        @DisplayName("정상 생성 - 배너 이미지 포함")
        void 정상생성_배너포함() {
            // given
            BookClubVO vo = createBookClub(null, 1L, "독서모임", 10);
            vo.setBanner_img_url("https://s3.example.com/banner.jpg");

            when(bookClubMapper.countByLeaderAndName(1L, "독서모임")).thenReturn(0);
            doAnswer(invocation -> {
                BookClubVO arg = invocation.getArgument(0);
                arg.setBook_club_seq(100L); // DB가 시퀀스 생성
                return null;
            }).when(bookClubMapper).insertBookClub(vo);

            // when
            bookClubService.createBookClub(vo);

            // then
            verify(bookClubMapper).insertBookClub(vo);
            verify(bookClubMapper).insertLeaderMember(100L, 1L);
        }

        @Test
        @DisplayName("배너 이미지 없으면 기본 이미지 자동 설정")
        void 배너없으면_기본이미지() {
            // given
            BookClubVO vo = createBookClub(null, 1L, "독서모임", 10);
            vo.setBanner_img_url(null);

            when(bookClubMapper.countByLeaderAndName(1L, "독서모임")).thenReturn(0);
            doAnswer(invocation -> {
                BookClubVO arg = invocation.getArgument(0);
                arg.setBook_club_seq(100L);
                return null;
            }).when(bookClubMapper).insertBookClub(vo);

            // when
            bookClubService.createBookClub(vo);

            // then
            assertThat(vo.getBanner_img_url()).startsWith("/resources/img/bookclub/banners/bookclub");
        }

        @Test
        @DisplayName("모임 이름 중복 - IllegalArgumentException")
        void 이름중복_예외() {
            // given
            BookClubVO vo = createBookClub(null, 1L, "중복모임", 10);
            when(bookClubMapper.countByLeaderAndName(1L, "중복모임")).thenReturn(1);

            // when & then
            assertThatThrownBy(() -> bookClubService.createBookClub(vo))
                    .isInstanceOf(BookClubInvalidRequestException.class)
                    .hasMessageContaining("이미 같은 이름");
        }
    }

    // ========================================================================
    // isMemberJoined / isLeader - 상태 확인
    // ========================================================================
    @Nested
    @DisplayName("멤버 상태 확인")
    class MemberStatusCheck {

        @Test
        @DisplayName("isMemberJoined - 가입된 멤버")
        void isMemberJoined_가입됨() {
            when(bookClubMapper.selectJoinedMemberCount(1L, 2L)).thenReturn(1);
            assertThat(bookClubService.isMemberJoined(1L, 2L)).isTrue();
        }

        @Test
        @DisplayName("isMemberJoined - 미가입")
        void isMemberJoined_미가입() {
            when(bookClubMapper.selectJoinedMemberCount(1L, 2L)).thenReturn(0);
            assertThat(bookClubService.isMemberJoined(1L, 2L)).isFalse();
        }

        @Test
        @DisplayName("isMemberJoined - null 파라미터 시 false")
        void isMemberJoined_null_false() {
            assertThat(bookClubService.isMemberJoined(null, 2L)).isFalse();
            assertThat(bookClubService.isMemberJoined(1L, null)).isFalse();
        }

        @Test
        @DisplayName("isLeader - 모임장 확인")
        void isLeader_모임장() {
            when(bookClubMapper.selectById(1L)).thenReturn(createBookClub(1L, 100L, "모임", 10));
            assertThat(bookClubService.isLeader(1L, 100L)).isTrue();
        }

        @Test
        @DisplayName("isLeader - 일반 멤버")
        void isLeader_일반멤버() {
            when(bookClubMapper.selectById(1L)).thenReturn(createBookClub(1L, 100L, "모임", 10));
            assertThat(bookClubService.isLeader(1L, 2L)).isFalse();
        }

        @Test
        @DisplayName("isLeader - null 파라미터 시 false")
        void isLeader_null_false() {
            assertThat(bookClubService.isLeader(null, 2L)).isFalse();
        }
    }

    // ========================================================================
    // toggleWish - 찜하기 토글
    // ========================================================================
    @Nested
    @DisplayName("toggleWish - 찜하기 토글")
    class ToggleWish {

        @Test
        @DisplayName("찜 안한 상태 - 찜 추가, true 반환")
        void 찜추가_true() {
            when(bookClubMapper.selectWishExists(1L, 2L)).thenReturn(0);

            boolean result = bookClubService.toggleWish(1L, 2L);

            assertThat(result).isTrue();
            verify(bookClubMapper).insertWish(1L, 2L);
        }

        @Test
        @DisplayName("이미 찜한 상태 - 찜 취소, false 반환")
        void 찜취소_false() {
            when(bookClubMapper.selectWishExists(1L, 2L)).thenReturn(1);

            boolean result = bookClubService.toggleWish(1L, 2L);

            assertThat(result).isFalse();
            verify(bookClubMapper).deleteWish(1L, 2L);
        }

        @Test
        @DisplayName("null 파라미터 시 false")
        void null_false() {
            assertThat(bookClubService.toggleWish(null, 2L)).isFalse();
        }
    }

    // ========================================================================
    // toggleBoardLike - 좋아요 토글
    // ========================================================================
    @Nested
    @DisplayName("toggleBoardLike - 좋아요 토글")
    class ToggleBoardLike {

        @Test
        @DisplayName("좋아요 추가 - true 반환")
        void 좋아요추가_true() {
            when(bookClubMapper.selectBoardLikeExists(10L, 2L)).thenReturn(0);

            boolean result = bookClubService.toggleBoardLike(10L, 2L);

            assertThat(result).isTrue();
            verify(bookClubMapper).insertBoardLike(10L, 2L);
        }

        @Test
        @DisplayName("좋아요 취소 - false 반환")
        void 좋아요취소_false() {
            when(bookClubMapper.selectBoardLikeExists(10L, 2L)).thenReturn(1);

            boolean result = bookClubService.toggleBoardLike(10L, 2L);

            assertThat(result).isFalse();
            verify(bookClubMapper).deleteBoardLike(10L, 2L);
        }
    }

    // ========================================================================
    // 댓글 관련
    // ========================================================================
    @Nested
    @DisplayName("댓글 관련")
    class Comment {

        @Test
        @DisplayName("댓글 수정 - 성공")
        void 댓글수정_성공() {
            when(bookClubMapper.updateBoardComment(1L, "수정된 내용")).thenReturn(1);
            assertThat(bookClubService.updateBoardComment(1L, "수정된 내용")).isTrue();
        }

        @Test
        @DisplayName("댓글 수정 - null/빈값이면 false")
        void 댓글수정_빈값_false() {
            assertThat(bookClubService.updateBoardComment(null, "내용")).isFalse();
            assertThat(bookClubService.updateBoardComment(1L, null)).isFalse();
            assertThat(bookClubService.updateBoardComment(1L, "  ")).isFalse();
        }

        @Test
        @DisplayName("댓글 삭제 - 성공")
        void 댓글삭제_성공() {
            when(bookClubMapper.deleteBoardComment(1L)).thenReturn(1);
            assertThat(bookClubService.deleteBoardComment(1L)).isTrue();
        }

        @Test
        @DisplayName("댓글 삭제 - null이면 false")
        void 댓글삭제_null_false() {
            assertThat(bookClubService.deleteBoardComment(null)).isFalse();
        }
    }

    // ========================================================================
    // null 파라미터 방어 로직
    // ========================================================================
    @Nested
    @DisplayName("null 파라미터 방어")
    class NullDefense {

        @Test
        @DisplayName("getTotalJoinedMemberCount - null이면 0")
        void memberCount_null_zero() {
            assertThat(bookClubService.getTotalJoinedMemberCount(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("getWishCount - null이면 0")
        void wishCount_null_zero() {
            assertThat(bookClubService.getWishCount(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("getBoardLikeCount - null이면 0")
        void likeCount_null_zero() {
            assertThat(bookClubService.getBoardLikeCount(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("getRecentBoards - null이면 빈 리스트")
        void recentBoards_null_empty() {
            assertThat(bookClubService.getRecentBoards(null)).isEmpty();
        }

        @Test
        @DisplayName("getBoardDetail - null이면 null")
        void boardDetail_null() {
            assertThat(bookClubService.getBoardDetail(null, 1L)).isNull();
        }
    }
}
