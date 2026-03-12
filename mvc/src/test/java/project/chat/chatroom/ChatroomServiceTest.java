package project.chat.chatroom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatroomServiceTest {

    @Mock
    ChatroomMapper chatroomMapper;

    @InjectMocks
    ChatroomService chatroomService;

    // === searchAll ===

    @Nested
    @DisplayName("searchAll - 회원의 전체 채팅방 조회")
    class SearchAll {

        @Test
        @DisplayName("채팅방 목록을 반환한다")
        void returnsChatroomList() {
            // given
            long memberSeq = 1L;
            List<ChatroomVO> rooms = Arrays.asList(new ChatroomVO(), new ChatroomVO());
            when(chatroomMapper.findAllByMemberSeq(memberSeq)).thenReturn(rooms);

            // when
            List<ChatroomVO> result = chatroomService.searchAll(memberSeq);

            // then
            assertThat(result).hasSize(2);
            verify(chatroomMapper).findAllByMemberSeq(memberSeq);
        }

        @Test
        @DisplayName("채팅방이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList() {
            // given
            when(chatroomMapper.findAllByMemberSeq(99L)).thenReturn(Collections.emptyList());

            // when
            List<ChatroomVO> result = chatroomService.searchAll(99L);

            // then
            assertThat(result).isEmpty();
        }
    }

    // === findOrCreateRoom ===

    @Nested
    @DisplayName("findOrCreateRoom - 채팅방 조회 또는 생성")
    class FindOrCreateRoom {

        @Test
        @DisplayName("이미 채팅방이 존재하면 기존 채팅방을 반환한다")
        void returnsExistingRoom() {
            // given
            long sellerSeq = 1L, buyerSeq = 2L, tradeSeq = 10L;
            ChatroomVO existing = new ChatroomVO();
            existing.setChat_room_seq(100L);
            when(chatroomMapper.findRoom(sellerSeq, buyerSeq, tradeSeq)).thenReturn(existing);

            // when
            ChatroomVO result = chatroomService.findOrCreateRoom(sellerSeq, buyerSeq, tradeSeq);

            // then
            assertThat(result.getChat_room_seq()).isEqualTo(100L);
            verify(chatroomMapper, never()).save(any());
        }

        @Test
        @DisplayName("채팅방이 없으면 새로 생성하고 반환한다")
        void createsNewRoom() {
            // given
            long sellerSeq = 1L, buyerSeq = 2L, tradeSeq = 10L;
            when(chatroomMapper.findRoom(sellerSeq, buyerSeq, tradeSeq))
                    .thenReturn(null)  // 첫 조회: 없음
                    .thenReturn(new ChatroomVO());  // save 후 재조회
            when(chatroomMapper.save(any(ChatroomVO.class))).thenReturn(1);

            // when
            ChatroomVO result = chatroomService.findOrCreateRoom(sellerSeq, buyerSeq, tradeSeq);

            // then
            assertThat(result).isNotNull();
            verify(chatroomMapper).save(any(ChatroomVO.class));
            verify(chatroomMapper, times(2)).findRoom(sellerSeq, buyerSeq, tradeSeq);
        }

        @Test
        @DisplayName("save 실패 시 RuntimeException이 발생한다")
        void throwsWhenSaveFails() {
            // given
            long sellerSeq = 1L, buyerSeq = 2L, tradeSeq = 10L;
            when(chatroomMapper.findRoom(sellerSeq, buyerSeq, tradeSeq)).thenReturn(null);
            when(chatroomMapper.save(any(ChatroomVO.class))).thenReturn(0);

            // when & then
            assertThatThrownBy(() ->
                    chatroomService.findOrCreateRoom(sellerSeq, buyerSeq, tradeSeq)
            ).isInstanceOf(RuntimeException.class)
             .hasMessageContaining("fail to save");
        }

        @Test
        @DisplayName("동시 생성으로 DuplicateKeyException 발생 시 기존 채팅방을 조회하여 반환한다")
        void handlesDuplicateKey() {
            // given
            long sellerSeq = 1L, buyerSeq = 2L, tradeSeq = 10L;
            ChatroomVO existing = new ChatroomVO();
            existing.setChat_room_seq(200L);

            when(chatroomMapper.findRoom(sellerSeq, buyerSeq, tradeSeq))
                    .thenReturn(null)       // 첫 조회: 없음
                    .thenReturn(existing);  // DuplicateKey 후 재조회
            when(chatroomMapper.save(any(ChatroomVO.class))).thenThrow(new DuplicateKeyException("duplicate"));

            // when
            ChatroomVO result = chatroomService.findOrCreateRoom(sellerSeq, buyerSeq, tradeSeq);

            // then
            assertThat(result.getChat_room_seq()).isEqualTo(200L);
        }
    }

    // === isMemberOfChatroom ===

    @Nested
    @DisplayName("isMemberOfChatroom - 채팅방 멤버 확인")
    class IsMemberOfChatroom {

        @Test
        @DisplayName("채팅방 멤버이면 true를 반환한다")
        void returnsTrueForMember() {
            // given
            when(chatroomMapper.isMemberOfChatroom(100L, 1L)).thenReturn(true);

            // when & then
            assertThat(chatroomService.isMemberOfChatroom(100L, 1L)).isTrue();
        }

        @Test
        @DisplayName("채팅방 멤버가 아니면 false를 반환한다")
        void returnsFalseForNonMember() {
            // given
            when(chatroomMapper.isMemberOfChatroom(100L, 99L)).thenReturn(false);

            // when & then
            assertThat(chatroomService.isMemberOfChatroom(100L, 99L)).isFalse();
        }
    }

    // === findChatRoomSeqByTradeAndBuyer ===

    @Nested
    @DisplayName("findChatRoomSeqByTradeAndBuyer - 거래+구매자로 채팅방 조회")
    class FindChatRoomSeqByTradeAndBuyer {

        @Test
        @DisplayName("존재하는 채팅방 seq를 반환한다")
        void returnsSeq() {
            // given
            when(chatroomMapper.findChatRoomSeqByTradeAndBuyer(10L, 2L)).thenReturn(100L);

            // when
            Long result = chatroomService.findChatRoomSeqByTradeAndBuyer(10L, 2L);

            // then
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("채팅방이 없으면 null을 반환한다")
        void returnsNull() {
            // given
            when(chatroomMapper.findChatRoomSeqByTradeAndBuyer(10L, 99L)).thenReturn(null);

            // when & then
            assertThat(chatroomService.findChatRoomSeqByTradeAndBuyer(10L, 99L)).isNull();
        }
    }

    // === isBuyerOfTrade ===

    @Nested
    @DisplayName("isBuyerOfTrade - 해당 거래의 구매자인지 확인")
    class IsBuyerOfTrade {

        @Test
        @DisplayName("구매자이면 true를 반환한다")
        void returnsTrueForBuyer() {
            when(chatroomMapper.isBuyerOfTrade(10L, 2L)).thenReturn(true);
            assertThat(chatroomService.isBuyerOfTrade(10L, 2L)).isTrue();
        }

        @Test
        @DisplayName("구매자가 아니면 false를 반환한다")
        void returnsFalseForNonBuyer() {
            when(chatroomMapper.isBuyerOfTrade(10L, 99L)).thenReturn(false);
            assertThat(chatroomService.isBuyerOfTrade(10L, 99L)).isFalse();
        }
    }

    // === findByChatRoomSeq ===

    @Test
    @DisplayName("findByChatRoomSeq - 채팅방 seq로 채팅방을 조회한다")
    void findByChatRoomSeq() {
        // given
        ChatroomVO room = new ChatroomVO();
        room.setChat_room_seq(100L);
        when(chatroomMapper.findByChatRoomSeq(100L)).thenReturn(room);

        // when
        ChatroomVO result = chatroomService.findByChatRoomSeq(100L);

        // then
        assertThat(result.getChat_room_seq()).isEqualTo(100L);
    }

    // === searchAllWithPaging ===

    @Nested
    @DisplayName("searchAllWithPaging - 페이징 처리된 채팅방 조회")
    class SearchAllWithPaging {

        @Test
        @DisplayName("페이징 파라미터를 전달하여 결과를 반환한다")
        void returnsPagingResult() {
            // given
            List<ChatroomVO> rooms = Arrays.asList(new ChatroomVO(), new ChatroomVO());
            when(chatroomMapper.findAllByMemberSeqWithPaging(1L, 10, 0, "SALE")).thenReturn(rooms);

            // when
            List<ChatroomVO> result = chatroomService.searchAllWithPaging(1L, 10, 0, "SALE");

            // then
            assertThat(result).hasSize(2);
            verify(chatroomMapper).findAllByMemberSeqWithPaging(1L, 10, 0, "SALE");
        }
    }

    // === countAll ===

    @Test
    @DisplayName("countAll - 채팅방 전체 개수를 반환한다")
    void countAll() {
        when(chatroomMapper.countByMemberSeq(1L, "SALE")).thenReturn(5);
        assertThat(chatroomService.countAll(1L, "SALE")).isEqualTo(5);
    }

    // === updateLastMessage ===

    @Nested
    @DisplayName("updateLastMessage - 마지막 메시지 업데이트")
    class UpdateLastMessage {

        @Test
        @DisplayName("일반 메시지를 업데이트한다")
        void updatesNormalMessage() {
            // when
            chatroomService.updateLastMessage(100L, "안녕하세요");

            // then
            verify(chatroomMapper).updateLastMessage(100L, "안녕하세요");
        }

        @Test
        @DisplayName("null 메시지는 무시한다")
        void ignoresNullMessage() {
            // when
            chatroomService.updateLastMessage(100L, null);

            // then
            verify(chatroomMapper, never()).updateLastMessage(anyLong(), anyString());
        }

        @Test
        @DisplayName("빈 문자열 메시지는 무시한다")
        void ignoresEmptyMessage() {
            // when
            chatroomService.updateLastMessage(100L, "");

            // then
            verify(chatroomMapper, never()).updateLastMessage(anyLong(), anyString());
        }

        @Test
        @DisplayName("[IMAGE] 메시지를 '사진을 보냈습니다.'로 변환한다")
        void convertsImageMessage() {
            // when
            chatroomService.updateLastMessage(100L, "[IMAGE]");

            // then
            verify(chatroomMapper).updateLastMessage(100L, "사진을 보냈습니다.");
        }

        @Test
        @DisplayName("[IMAGE]로 시작하는 메시지도 '사진을 보냈습니다.'로 변환한다")
        void convertsImagePrefixMessage() {
            // when
            chatroomService.updateLastMessage(100L, "[IMAGE]https://s3.example.com/photo.jpg");

            // then
            verify(chatroomMapper).updateLastMessage(100L, "사진을 보냈습니다.");
        }

        @Test
        @DisplayName("[SAFE_PAYMENT 메시지를 '안전결제 요청'으로 변환한다")
        void convertsSafePaymentMessage() {
            // when
            chatroomService.updateLastMessage(100L, "[SAFE_PAYMENT]12345");

            // then
            verify(chatroomMapper).updateLastMessage(100L, "안전결제 요청");
        }

        @Test
        @DisplayName("50자 초과 메시지를 50자로 자르고 ...을 붙인다")
        void truncatesLongMessage() {
            // given
            String longMsg = "가".repeat(60);  // 60자

            // when
            chatroomService.updateLastMessage(100L, longMsg);

            // then
            String expected = "가".repeat(50) + "...";
            verify(chatroomMapper).updateLastMessage(100L, expected);
        }

        @Test
        @DisplayName("정확히 50자 메시지는 자르지 않는다")
        void doesNotTruncateExact50() {
            // given
            String msg50 = "가".repeat(50);

            // when
            chatroomService.updateLastMessage(100L, msg50);

            // then
            verify(chatroomMapper).updateLastMessage(100L, msg50);
        }
    }
}
