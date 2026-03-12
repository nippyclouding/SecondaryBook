package project.chat.chatroom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import project.chat.message.MessageService;
import project.chat.message.MessageVO;
import project.member.MemberVO;
import project.trade.TradeService;
import project.trade.TradeVO;
import project.chat.pubsub.ChatMessagePublisher;
import project.util.Const;
import project.util.imgUpload.ImgService;
import project.util.exception.ForbiddenException;
import project.util.imgUpload.FileStore;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatroomController {

    private final ChatroomService chatroomService;
    private final TradeService tradeService;
    private final MessageService messageService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final FileStore fileStore;
    private final ImgService imgService; // @Primary → FileUploadService

    // 메인 화면 -> 채팅방 조회
    @GetMapping("/chatrooms")
    public String chat(Model model, HttpSession session,
                       @RequestParam(defaultValue = "1") int page
    ) {

        int size = 10;
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        // home 에서 로그인하지 않고 채팅방 접근 시 home 으로 리다이렉트
        if (sessionMember == null) {
            return "redirect:/";
        }

        int offset = (page - 1) * size;
        List<ChatroomVO> chatrooms = chatroomService.searchAllWithPaging(sessionMember.getMember_seq(), size, offset, null);

        model.addAttribute("chatrooms", chatrooms);

        return "chat/chatrooms";
    }

    // 판매글 -> 채팅방
    @PostMapping("/chatrooms")
    public String chat(Model model, HttpSession session, TradeVO tradeVO,
                       @RequestParam(defaultValue = "1") int page) {
        // 프론트에서 trade_seq, member_seller_seq, sale_title 이 tradeVO 로 넘어온다

        int size = 10;

        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        // 로그인하지 않고 채팅방 접근 시 home 으로 리다이렉트
        if (sessionMember == null) {
            return "redirect:/";
        }

        // 1. 판매글에서 채팅하기로 채팅에 들어왔을 경우 (프론트에서 tradeVO가 넘어올 경우) 채팅방 속 메시지 데이터 프론트에 전달
        if (tradeVO.getTrade_seq() > 0) {

            long trade_seq = tradeVO.getTrade_seq();
            long member_seller_seq = tradeVO.getMember_seller_seq();
            long member_buyer_seq = sessionMember.getMember_seq();

            // 본인 채팅 방지
            if (member_seller_seq == member_buyer_seq) {
                return "chat/chatrooms";  // 채팅방 목록만 보여줌
            }

            // 모델에 넣을 데이터 조회
            ChatroomVO tradeChatroom = chatroomService.findOrCreateRoom(member_seller_seq, member_buyer_seq, trade_seq);
            TradeVO findTrade = tradeService.search(trade_seq);
            List<MessageVO> messages = messageService.getAllMessages(tradeChatroom.getChat_room_seq(), sessionMember.getMember_seq());
            // sessionMember.getMember_seq() : 메시지 읽음 처리하는 사람의 seq, member_buyer_seq 로 넣어도 되지만 직관성을 위해 session 에서 조회한 값을 넣음
            model.addAttribute("trade_chat_room", tradeChatroom); // 현재 채팅방 전달
            model.addAttribute("trade_info", findTrade); // 현재 채팅방이 다루는 trade 전달
            //model.addAttribute("messages", messages); // 현재 채팅방의 전체 메시지 전달 (이후 페이징 처리 필요)


            // room_seq model에 담아 jsp에서 enter_chat_room_seq << null이 아니면 message재호출 로직으로 변경
            model.addAttribute("enter_chat_room_seq", tradeChatroom.getChat_room_seq());
        }


        // 2. 채팅방 모두 출력
        int offset = (page - 1) * size;
        List<ChatroomVO> chatrooms = chatroomService.searchAllWithPaging(sessionMember.getMember_seq(), size, offset, null);

        model.addAttribute("chatrooms", chatrooms);
        // model.addAttribute("member_seq", sessionMember.getMember_seq());

        return "chat/chatrooms";
    }


    // 채팅방 리스트 Json으로 리턴
    @GetMapping("/chat/rooms/list")
    @ResponseBody
    public Map<String, Object> getChatroomList(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String sale_st, // enum 타입이지만 프론트에서 값 두 개 중 하나만 받아올 수 있기 때문에 String 단순 처리
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        if (sessionMember == null) {
            result.put("rooms", Collections.emptyList());
            result.put("hasMore", false);
            return result;
        }

        long memberSeq = sessionMember.getMember_seq();

        List<ChatroomVO> rooms = chatroomService.searchAllWithPaging(memberSeq, limit, offset, sale_st);
        int totalCount = chatroomService.countAll(memberSeq, sale_st);
        boolean hasMore = (offset + rooms.size()) < totalCount;

        result.put("rooms", rooms);
        result.put("hasMore", hasMore);
        result.put("totalCount", totalCount);

        return result;
    }

    // 채팅 메시지 조회 api
    @GetMapping("/chat/messages")
    @ResponseBody
    public Object[] getMessages(@RequestParam("chat_room_seq") long chat_room_seq,  HttpSession session, Model model) {
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION); // 메시지 읽음 처리할 회원

        // 권한 체크 추가
        if (sessionMember == null || !chatroomService.isMemberOfChatroom(chat_room_seq, sessionMember.getMember_seq())) {
            log.warn("권한 없는 채팅방 메시지 조회 시도");
            return new List[]{Collections.emptyList()};
        }

        Object[] returns = new Object[2]; // 0번째 인덱스는 List<MessageVO>, 1번째 인덱스는 TradeVO

        // 화면에 보여질 메시지 목록들을 먼저 조회한다.
        List<MessageVO> messages = messageService.getAllMessages(chat_room_seq, sessionMember.getMember_seq());
        TradeVO findTrade = tradeService.findByChatRoomSeq(chat_room_seq);

        returns[0] = messages;
        returns[1] = findTrade;

        // 상대방에게 읽음 이벤트 전송 (Redis Pub/Sub)
        chatMessagePublisher.publishRead(chat_room_seq, sessionMember.getMember_seq());
        return returns;
    }

    // 이미지 업로드
    // 성공 시 Map에 success : true, imageUrl : ..
    // 실패 시 Map에 success : false, message : 실패 원인
    @PostMapping("/chat/image/upload")
    @ResponseBody
    public Map<String, Object> uploadChatImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("chat_room_seq") long chat_room_seq,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        // 채팅방 참여자 검증
        if (!chatroomService.isMemberOfChatroom(chat_room_seq, sessionMember.getMember_seq())) {
            result.put("success", false);
            result.put("message", "권한이 없습니다.");
            return result;
        }

        try {
            // ImgService를 사용하여 이미지 저장
            String imageUrl = imgService.uploadFile(image);

            result.put("success", true);
            result.put("imageUrl", imageUrl);
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            result.put("success", false);
            result.put("message", "이미지 업로드에 실패했습니다.");
        }

        return result;
    }
    /*
    이미지 저장 순서
     1. 사용자가 이미지 선택
     2. POST /chat/image/upload (FormData로 이미지 전송)
     3. FileStore.storeFile()로 서버에 저장
     4. imageUrl 반환 ("/img/uuid.jpg")
     5. STOMP로 메시지 전송: chat_cont = "[IMAGE]/img/uuid.jpg"
     6. StompController에서 메시지 저장 + last_msg 업데이트
     7. 상대방에게 브로드캐스트
     */

    // 채팅방 클릭시 닉네임 조회
    @GetMapping("/chat/memberInfo")
    @ResponseBody
    public ChatroomVO findMemberInfo (@RequestParam long chat_room_seq, HttpSession session) {
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
        if (sessionMember == null) {
            throw new ForbiddenException("로그인이 필요합니다.");
        }
        if (!chatroomService.isMemberOfChatroom(chat_room_seq, sessionMember.getMember_seq())) {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }
        return chatroomService.findByChatRoomSeq(chat_room_seq);
    }

}
