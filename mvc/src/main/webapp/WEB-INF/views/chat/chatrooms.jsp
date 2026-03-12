<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.time.LocalDateTime, java.util.Date, project.chat.message.MessageVO" %>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
<script>
    const loginMemberSeq = Number("${sessionScope.loginSess.member_seq}");
    let chat_room_seq = Number("${trade_chat_room.chat_room_seq}");
    let trade_seq = Number("${trade_chat_room.trade_seq}");
    let member_seller_seq = Number("${trade_info.member_seller_seq}");
    let isSeller = (loginMemberSeq === member_seller_seq);

    // trade 정보 (안전결제용)
    let currentTradeInfo = {
        trade_seq: Number("${trade_info.trade_seq}") || 0,
        book_img: "${fn:escapeXml(trade_info.book_img)}",
        book_title: "${fn:escapeXml(trade_info.book_title)}",
        sale_price: Number("${trade_info.sale_price}") || 0,
        delivery_cost: Number("${trade_info.delivery_cost}") || 0,
        book_st: "${fn:escapeXml(trade_info.book_st)}",
        sale_st: "${fn:escapeXml(trade_info.sale_st)}"
    };
</script>

<style>
    /* 채팅방 리스트 아이템 */
    .chatroom-item.active {
        background-color: #eef4ff;
        border-left-color: #0046FF;
    }

    /* 스크롤바 */
    #chatContainer::-webkit-scrollbar,
    #chatroomsList::-webkit-scrollbar {
        width: 6px;
    }
    #chatContainer::-webkit-scrollbar-track,
    #chatroomsList::-webkit-scrollbar-track {
        background: #f1f3f5;
    }
    #chatContainer::-webkit-scrollbar-thumb,
    #chatroomsList::-webkit-scrollbar-thumb {
        background: #ced4da;
        border-radius: 3px;
    }

    /* 메시지 정렬 */
    .msg-left,
    .msg-right {
        display: flex;
        flex-direction: column;
        max-width: 70%;
        margin: 12px 0;
    }
    .msg-left {
        align-items: flex-start;
        margin-right: auto;
    }
    .msg-right {
        align-items: flex-end;
        margin-left: auto;
    }

    /* 말풍선 */
    .msg-left .content {
        background: #fff;
        border: 1px solid #e9ecef;
        color: #212529;
        border-radius: 16px;
        border-top-left-radius: 4px;
    }
    .msg-right .content {
        background: #0046FF;
        color: #fff;
        border-radius: 16px;
        border-top-right-radius: 4px;
    }
    .content {
        padding: 10px 14px;
        font-size: 14px;
        line-height: 1.5;
        word-break: break-word;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }

    /* 시간 표시 */
    .msg-time {
        font-size: 10px;
        color: #868e96;
        margin-top: 4px;
        display: flex;
        align-items: center;
        gap: 4px;
    }

    .msg-nicknm {
        font-size: 12px;
        color: blue;
        margin-top: 4px;
        display: flex;
        align-items: center;
        gap: 4px;
    }

    /* + 버튼 및 안전결제 메뉴 */
    .plus-btn-wrapper {
        position: relative;
    }
    .plus-btn {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        background: #f1f3f5;
        border: 1px solid #e9ecef;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
    }
    .plus-btn:hover {
        background: #e9ecef;
    }
    .plus-menu {
        position: absolute;
        bottom: 54px;
        left: 0;
        background: #fff;
        border: 1px solid #e9ecef;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        padding: 8px 0;
        min-width: 180px;
        display: none;
        z-index: 100;
    }
    .plus-menu.show {
        display: block;
    }
    .plus-menu-item {
        padding: 10px 16px;
        cursor: pointer;
        font-size: 14px;
        color: #212529;
        transition: background 0.2s;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .plus-menu-item:hover {
        background: #f8f9fa;
    }

    /* 안전결제 요청 카드 */
    .safe-payment-card {
        background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
        border: 1px solid #e9ecef;
        border-radius: 16px;
        padding: 16px;
        max-width: 280px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    }
    .safe-payment-card .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        padding-bottom: 12px;
        border-bottom: 1px solid #e9ecef;
    }
    .safe-payment-card .card-header svg {
        color: #0046FF;
    }
    .safe-payment-card .card-title {
        font-weight: 700;
        font-size: 14px;
        color: #212529;
    }
    .safe-payment-card .timer {
        font-size: 12px;
        color: #fa5252;
        font-weight: 600;
    }
    .safe-payment-card .product-info {
        display: flex;
        gap: 12px;
        margin-bottom: 12px;
    }
    .safe-payment-card .product-img {
        width: 60px;
        height: 80px;
        border-radius: 8px;
        object-fit: cover;
        background: #f1f3f5;
    }
    .safe-payment-card .product-detail {
        flex: 1;
        min-width: 0;
    }
    .safe-payment-card .product-title {
        font-size: 13px;
        font-weight: 600;
        color: #212529;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .safe-payment-card .product-status {
        font-size: 11px;
        color: #868e96;
        margin-bottom: 6px;
    }
    .safe-payment-card .product-price {
        font-size: 14px;
        font-weight: 700;
        color: #0046FF;
    }
    .safe-payment-card .product-delivery {
        font-size: 11px;
        color: #868e96;
        margin-top: 2px;
    }
    .safe-payment-card .action-btn {
        width: 100%;
        padding: 10px 16px;
        border-radius: 10px;
        font-size: 13px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
        border: none;
    }
    .safe-payment-card .action-btn.primary {
        background: #0046FF;
        color: #fff;
    }
    .safe-payment-card .action-btn.primary:hover {
        background: #0039d1;
    }
    .safe-payment-card .action-btn:disabled {
        background: #ced4da;
        cursor: not-allowed;
    }
    .safe-payment-card .expired-notice {
        text-align: center;
        font-size: 12px;
        color: #868e96;
        padding: 10px;
    }
</style>

<div class="min-h-[calc(100vh-200px)]">
    <!-- 페이지 헤더 -->
    <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">채팅</h1>
        <p class="text-sm text-gray-500 mt-1">거래 관련 대화를 나눠보세요</p>
    </div>
    <div class="flex gap-6 h-[600px]">
        <!-- 왼쪽 채팅방 리스트 -->
        <div class="w-80 bg-white rounded-2xl border border-gray-200 shadow-sm flex flex-col overflow-hidden">
            <div class="px-5 py-4 border-b border-gray-100 bg-gray-50">
                <div class="flex items-center justify-between">
                    <h3 class="font-bold text-gray-800 flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-primary-500"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                        내 채팅방
                    </h3>
                    <select id="saleStatusFilter" class="text-xs px-2 py-1 border border-gray-200 rounded-lg bg-white focus:outline-none focus:ring-1 focus:ring-primary-300">
                        <option value="">전체</option>
                        <option value="SALE">판매중</option>
                        <option value="SOLD">판매완료</option>
                    </select>
                </div>
            </div>

            <div id="chatroomsList" class="flex-1 overflow-y-auto">
                <c:choose>
                    <c:when test="${not empty chatrooms}">
                        <c:forEach var="room" items="${chatrooms}">
                            <div class="chatroom-item px-5 py-4 border-b border-gray-100 cursor-pointer transition-all hover:bg-gray-50 border-l-4 border-l-transparent ${room.chat_room_seq == trade_chat_room.chat_room_seq ? 'active' : ''}"
                                 data-chat-room-seq="${room.chat_room_seq}">
                                <div class="flex items-start gap-3">
                                    <div class="relative w-10 h-10 bg-primary-50 rounded-lg flex items-center justify-center">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="text-primary-500 block" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"/>
                                        </svg>
                                        <c:if test="${room.msg_unread}">
                                            <span class="unread-dot absolute top-1 right-1 w-2.5 h-2.5 bg-red-500 rounded-full"></span>
                                        </c:if>
                                    </div>

                                    <div class="flex-1 min-w-0">
                                        <div class="flex items-center justify-between gap-2">
                                            <div class="font-semibold text-gray-900 text-sm truncate flex items-center gap-2">
                                                <c:out value="${room.sale_title}"/>
                                                <c:choose>
                                                    <c:when test="${room.sale_st.name() == 'SOLD'}">
                                                        <span class="text-xs px-1.5 py-0.5 rounded bg-gray-200 text-gray-600 whitespace-nowrap">판매완료</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-xs px-1.5 py-0.5 rounded bg-blue-100 text-blue-600 whitespace-nowrap">판매중</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <c:if test="${not empty room.last_msg_dtm}">
                                                <span class="text-xs text-gray-400 whitespace-nowrap flex-shrink-0">
                                                    <fmt:formatDate value="${room.lastMsgDtmAsDate}" pattern="MM/dd HH:mm"/>
                                                </span>
                                            </c:if>
                                        </div>

                                        <div class="text-xs text-primary-600 font-medium mt-1 truncate room-nickname">
                                            <c:choose>
                                                <c:when test="${sessionScope.loginSess.member_seq == room.member_seller_seq}">
                                                    <c:out value="${room.member_buyer_nicknm}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${room.member_seller_nicknm}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <div class="text-xs text-gray-500 mt-1 truncate">
                                            <c:choose>
                                                <c:when test="${not empty room.last_msg}"><c:out value="${room.last_msg}"/></c:when>
                                                <c:otherwise><span class="text-gray-400 italic">아직 메시지가 없습니다</span></c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div id="emptyRoomNotice" class="flex flex-col items-center justify-center h-full py-12 text-gray-400">
                            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="mb-3 text-gray-300"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                            <p class="text-sm">채팅방이 없습니다</p>
                        </div>
                    </c:otherwise>
                </c:choose>
                <!-- 로딩 인디케이터 -->
                <div id="chatroomLoadingIndicator" class="hidden py-4 text-center">
                    <svg class="animate-spin h-5 w-5 mx-auto text-primary-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <p class="text-xs text-gray-400 mt-2">불러오는 중...</p>
                </div>
                <!-- 더 이상 채팅방 없음 표시 -->
                <div id="noMoreRoomsNotice" class="hidden py-3 text-center text-xs text-gray-400">
                    더 이상 채팅방이 없습니다
                </div>
            </div>
        </div>

        <!-- 오른쪽 채팅 영역 -->
        <div id="chatArea" class="flex-1 bg-white rounded-2xl border border-gray-200 shadow-sm flex flex-col overflow-hidden">
            <!-- 채팅 헤더 -->
            <div id="chatHeader" class="px-6 py-4 border-b border-gray-100 bg-white">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-primary-50 rounded-lg flex items-center justify-center">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18"
                             viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                             class="text-primary-500">
                            <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"/>
                        </svg>
                    </div>
                    <div>
                        <h4 id="chatHeaderTitle" class="font-bold text-gray-900">
                            <c:choose>
                                <c:when test="${not empty trade_info}">
                                    <c:out value="${trade_info.sale_title}"/>
                                </c:when>
                                <c:otherwise>
                                    채팅방을 선택해주세요
                                </c:otherwise>
                            </c:choose>
                        </h4>
                        <p id="chatHeaderSub" class="text-xs text-gray-500">
                                <c:if test="${not empty trade_chat_room}">
                                    <c:choose>
                                        <c:when test="${sessionScope.loginSess.member_seq == trade_chat_room.member_seller_seq}">
                                             <c:out value="${trade_chat_room.member_buyer_nicknm}"/> 님과의 채팅
                                        </c:when>
                                        <c:otherwise>
                                             <c:out value="${trade_chat_room.member_seller_nicknm}"/> 님과의 채팅
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                           </p>
                    </div>
                </div>
            </div>
            <!-- 채팅 헤더 아래에 책 정보 표시 -->
            <div id="chatBookInfo"
                 class="px-6 py-3 border-b border-gray-100 bg-white-50 flex items-center gap-4"
                 <c:if test="${empty trade_info}">style="display:none"</c:if>>

                <img id="chatBookImg"
                     src="<c:out value='${trade_info.book_img}'/>"
                     alt="책 이미지"
                     class="w-12 h-16 object-cover rounded-lg">

                <div class="flex flex-col">
                    <div id="chatBookTitle" class="font-semibold text-gray-900 text-sm">
                        <c:out value="${trade_info.book_title}"/>
                    </div>

                    <div id="chatBookStatus" class="text-xs
                        <c:choose>
                            <c:when test="${trade_info.book_st eq 'USED'}">text-gray-400</c:when>
                            <c:otherwise>text-gray-500</c:otherwise>
                        </c:choose>">

                        <c:choose>
                            <c:when test="${trade_info.book_st eq 'LIKE_NEW'}">
                                거의 새책
                            </c:when>
                            <c:when test="${trade_info.book_st eq 'GOOD'}">
                                좋음
                            </c:when>
                            <c:when test="${trade_info.book_st eq 'USED'}">
                                사용됨
                            </c:when>
                            <c:when test="${trade_info.book_st eq 'NEW'}">
                                새책
                            </c:when>
                        </c:choose>
                    </div>

                    <div id="chatBookPrice" class="font-semibold text-red-900 text-sm">
                        <c:set var="totalPrice" value="${trade_info.sale_price + trade_info.delivery_cost}" />
                        <fmt:formatNumber value="${totalPrice}" pattern="#,###"/>원
                    </div>
                </div>

                <div class="ml-auto text-right">
                    <h4 id="chatSale_st" class="font-bold text-sm"></h4>
                </div>

            </div>
            <!-- 채팅 메시지 영역 -->
            <div id="chatContainer" class="flex-1 overflow-y-auto px-6 py-4 bg-gray-50">
                <c:choose>
                    <c:when test="${not empty messages}">
                        <c:forEach var="msg" items="${messages}">
                            <div class="${msg.sender_seq == sessionScope.loginSess.member_seq ? 'msg-right' : 'msg-left'}">
                                <c:if test="${msg.sender_seq != sessionScope.loginSess.member_seq}">
                                    <div class="msg-nicknm">
                                        <b><c:out value="${msg.member_seller_nicknm}"/></b>
                                    </div>
                                </c:if>
                                <div class="content"><c:out value="${msg.chat_cont}"/></div>
                                <div class="msg-time">
                                    <%
                                        Object obj = pageContext.findAttribute("msg");
                                        if(obj != null) {
                                            MessageVO message = (MessageVO) obj;
                                            LocalDateTime ldt = message.getSent_dtm();
                                            Date date = null;
                                            if(ldt != null) {
                                                date = Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
                                            }
                                    %>
                                    <fmt:formatDate value="<%=date%>" pattern="yyyy/MM/dd HH:mm"/>
                                    <%
                                        }
                                    %>
                                    <c:if test="${msg.sender_seq == sessionScope.loginSess.member_seq && msg.read_yn}">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#0046FF" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div id="emptyNotice" class="flex flex-col items-center justify-center h-full text-gray-400">
                            <svg xmlns="http://www.w3.org/2000/svg" width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="mb-4 text-gray-300"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                            <p class="text-sm font-medium">이전 메시지가 없습니다</p>
                            <p class="text-xs text-gray-400 mt-1">첫 메시지를 보내보세요!</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- 메시지 입력 영역 -->
            <div id="messageInputArea" class="px-6 py-4 border-t border-gray-100 bg-white">
                <div class="flex items-center gap-3">
                    <!-- + 버튼 (채팅방 선택 시 모두에게 보임) -->
                    <div class="plus-btn-wrapper" id="plusBtnWrapper" style="display: none;">
                        <button type="button" class="plus-btn" id="plusBtn">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#495057" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="12" y1="5" x2="12" y2="19"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                            </svg>
                        </button>
                        <div class="plus-menu" id="plusMenu">
                            <!-- 사진 전송 (모든 사용자) -->
                            <div class="plus-menu-item" id="imageUploadBtn">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                    <polyline points="21 15 16 10 5 21"></polyline>
                                </svg>
                                사진 전송
                                <span class="text-xs text-gray-400 ml-1">(1장)</span>
                            </div>
                            <!-- 안전 결제 요청 (판매자만) -->
                            <div class="plus-menu-item" id="safePaymentRequestBtn" style="display: none;">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#0046FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                                    <line x1="1" y1="10" x2="23" y2="10"></line>
                                </svg>
                                안전 결제 요청 보내기
                            </div>
                        </div>
                    </div>
                    <!-- 숨겨진 파일 input -->
                    <input type="file" id="imageInput" accept="image/*" style="display: none;" />
                    <div class="flex flex-col flex-1">
                        <input type="text" id="message"
                               placeholder="메시지를 입력하세요..."
                               class="px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl
                                      focus:outline-none focus:ring-2 focus:ring-primary-200
                                      focus:border-primary-500 transition-all text-sm" />
                        <p id="messageError" class="mt-1 text-xs text-red-500 hidden">
                            메시지는 최대 1000자까지 입력할 수 있습니다.
                        </p>
                    </div>
                    <button id="sendBtn" type="button" class="px-5 py-3 bg-primary-500 hover:bg-primary-600 text-white rounded-xl font-semibold text-sm transition-all shadow-sm hover:shadow-md flex items-center gap-2">
                        전송
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    // 서버에서 내려준 값
    const enterChatRoomSeq = '${enter_chat_room_seq}';
    console.log('json변환처리 확인', enterChatRoomSeq);
</script>
<script src="${pageContext.request.contextPath}/resources/js/chat/chat.js"></script>

<script>
console.log('=== 안전결제 스크립트 로드 시작 ===');
console.log('chat.js fetchMessages 존재 여부:', typeof fetchMessages);

// =====================================================
// 안전결제 기능 스크립트
// =====================================================

// 타이머 관리 객체
const safePaymentTimers = {};

// 페이지 로드 시 판매자 여부에 따라 + 버튼 표시
document.addEventListener("DOMContentLoaded", function() {
    updatePlusButtonVisibility();
    setupPlusButtonEvents();
});

function removeActivePaymentCard() {
    const btn = document.querySelector('.safe-payment-card .action-btn');
    if (btn) {
        btn.closest('.msg-left, .msg-right')?.remove();
    }
}

// + 버튼 표시/숨김 (채팅방 선택 시 모두에게 보임, 안전결제는 판매자만)
function updatePlusButtonVisibility() {
    const plusBtnWrapper = document.getElementById('plusBtnWrapper');
    const safePaymentRequestBtn = document.getElementById('safePaymentRequestBtn');

    console.log('=== updatePlusButtonVisibility ===');
    console.log('chat_room_seq:', chat_room_seq);
    console.log('isSeller:', isSeller);

    // + 버튼: 채팅방이 선택되면 모든 사용자에게 보임
    if (plusBtnWrapper && chat_room_seq > 0) {
        plusBtnWrapper.style.display = 'block';
        console.log('+ 버튼 표시');
    } else if (plusBtnWrapper) {
        plusBtnWrapper.style.display = 'none';
        console.log('+ 버튼 숨김');
    }

    // 안전결제 버튼: 판매자이고, 판매중(SOLD가 아닌) 상품에만 보임
    if (safePaymentRequestBtn) {
        const isSold = currentTradeInfo.sale_st === 'SOLD';
        if (isSeller && chat_room_seq > 0 && !isSold) {
            safePaymentRequestBtn.style.display = 'flex';
            console.log('안전결제 버튼 표시');
        } else {
            safePaymentRequestBtn.style.display = 'none';
            console.log('안전결제 버튼 숨김 (SOLD:', isSold, ')');
        }
        if (currentTradeInfo.sale_st === 'SOLD') {
            safePaymentRequestBtn.style.display = 'none';
            console.log('안전결제 버튼 숨김 (SOLD:', isSold, ')');
        }
    }
}

// + 버튼 이벤트 설정
function setupPlusButtonEvents() {
    const plusBtn = document.getElementById('plusBtn');
    const plusMenu = document.getElementById('plusMenu');
    const safePaymentRequestBtn = document.getElementById('safePaymentRequestBtn');
    const imageUploadBtn = document.getElementById('imageUploadBtn');
    const imageInput = document.getElementById('imageInput');

    if (plusBtn && plusMenu) {
        // + 버튼 클릭 시 메뉴 토글
        plusBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            plusMenu.classList.toggle('show');
        });

        // 다른 곳 클릭 시 메뉴 닫기
        document.addEventListener('click', function() {
            plusMenu.classList.remove('show');
        });
    }

    // 사진 전송 버튼 클릭 시 파일 선택창 열기
    if (imageUploadBtn && imageInput) {
        imageUploadBtn.addEventListener('click', function() {
            plusMenu.classList.remove('show');
            imageInput.click();
        });

        // 파일 선택 시 업로드
        imageInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                uploadAndSendImage(this.files[0]);
                this.value = ''; // 같은 파일 재선택 가능하도록 초기화
            }
        });
    }

    if (safePaymentRequestBtn) {
        safePaymentRequestBtn.addEventListener('click', function() {
            sendSafePaymentRequest();
            plusMenu.classList.remove('show');
        });
    }
}

// 안전 결제 요청 보내기
function sendSafePaymentRequest() {
    console.log('안전 결제 요청 시도:', {
        stompConnected: stompClient && stompClient.connected,
        isSeller: isSeller,
        trade_seq: trade_seq,
        chat_room_seq: chat_room_seq
    });

    if (!stompClient || !stompClient.connected) {
        alert('채팅 연결이 필요합니다.');
        return;
    }

    if (!isSeller) {
        alert('판매자만 안전 결제 요청을 보낼 수 있습니다.');
        return;
    }

    if (!trade_seq || trade_seq <= 0) {
        alert('거래 정보를 찾을 수 없습니다. 페이지를 새로고침 해주세요.');
        console.error('trade_seq가 유효하지 않음:', trade_seq);
        return;
    }

    // 특별한 메시지 형식으로 안전 결제 요청 전송
    stompClient.send(
        "/sendMessage/chat/" + chat_room_seq,
        {},
        JSON.stringify({
            chat_room_seq: chat_room_seq,
            chat_cont: "[SAFE_PAYMENT_REQUEST]",
            sender_seq: loginMemberSeq,
            trade_seq: trade_seq
        })
    );

    console.log('안전 결제 요청 전송 완료');
}

// 기존 showMessage 함수 오버라이드
const originalShowMessage = showMessage;
showMessage = function(msg) {
    const chatCont = msg.chat_cont || '';

    // 안전 결제 진행 중 메시지인 경우 (추가)
    if (chatCont === '[SAFE_PAYMENT_IN_PROGRESS]') {
        showSafePaymentInProgress(msg);
        return;
    }

    // 안전 결제 요청 메시지인 경우
    if (chatCont === '[SAFE_PAYMENT_REQUEST]') {
        showSafePaymentRequest(msg);
        return;
    }

    // 구매 요청 수락 메시지인 경우
    if (chatCont === '[SAFE_PAYMENT_ACCEPT]') {
        showSafePaymentAccept(msg);
        return;
    }

    // 결제 완료 메시지인 경우
    if (chatCont === '[SAFE_PAYMENT_COMPLETE]') {
        showSafePaymentComplete(msg);
        return;
    }

    // 결제 실패 메시지인 경우
    if (chatCont === '[SAFE_PAYMENT_FAILED]') {
        showSafePaymentFailed(msg);
        return;
    }

    // 일반 메시지는 기존 함수 사용
    originalShowMessage(msg);
};

// 서버 동기화 결제 타이머
function startServerSyncTimer(timerId) {
    const timerElement = document.getElementById(timerId);
    if (!timerElement) return;

    timerElement.textContent = '로딩중...';

    let localRemainingSeconds = 0; // 로컬에서 카운트다운할 초

    // 타이머 표시 업데이트
    function updateDisplay(seconds) {
        if (seconds <= 0) {
            timerElement.textContent = '만료됨';
            timerElement.style.color = '#fa5252';
            return;
        }

        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        timerElement.textContent = String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');

        // 1분 이하면 빨간색 강조
        if (seconds <= 60) {
            timerElement.style.color = '#fa5252';
        }
    }

    // 서버에서 남은 시간 조회
    function fetchAndUpdateTimer() {
        fetch('/payments/remaining-time?trade_seq=' + trade_seq)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                console.log('서버 타이머 응답:', data);

                if (data.status === 'COMPLETED') {
                    timerElement.textContent = '완료됨';
                    timerElement.style.color = '#40c057';
                    stopAllPaymentTimers();
                    return;
                }

                 if (data.status === 'NONE') {
                      // 타이머가 속한 카드 찾기
                      const card = timerElement.closest('.safe-payment-card');
                      if (card) {
                          card.style.display = 'none';
                      }
                      // 타이머 중지
                      if (window.safePaymentTimerInterval) {
                          clearInterval(window.safePaymentTimerInterval);
                      }
                      if (window.safePaymentSyncInterval) {
                          clearInterval(window.safePaymentSyncInterval);
                      }
                      return;
                  }

                if (data.remainingSeconds <= 0) {
                      const card = timerElement.closest('.safe-payment-card');
                      if (card) {
                          card.style.display = 'none';  // 카드 숨기기
                      }
                      stopAllPaymentTimers();
                      return;
                  }

                // 서버에서 받은 시간으로 로컬 동기화
                localRemainingSeconds = data.remainingSeconds;
                updateDisplay(localRemainingSeconds);
            })
            .catch(function(error) {
                console.error('타이머 동기화 실패:', error);
                timerElement.textContent = '연결 오류';
                timerElement.style.color = '#868e96';
            });
    }

    // 즉시 서버에서 시간 가져오기
    fetchAndUpdateTimer();

    // 1초마다 로컬 카운트다운
    window.safePaymentTimerInterval = setInterval(function() {
        if (localRemainingSeconds > 0) {
            localRemainingSeconds--;
            updateDisplay(localRemainingSeconds);
        }
    }, 1000);

    // 30초마다 서버와 동기화 (정확도 유지)
    window.safePaymentSyncInterval = setInterval(fetchAndUpdateTimer, 30000);
}

// 안전 결제 실패 UI 표시
function showSafePaymentFailed(msg) {
    //기존 안전결제 카드 전부 숨김
    document.querySelectorAll('.safe-payment-card').forEach(el => {
           el.style.display = 'none';
    });
    const log = document.getElementById("chatContainer");
    const emptyNotice = document.getElementById("emptyNotice");
    if (emptyNotice) emptyNotice.remove();

    const msgWrapper = document.createElement('div');
    msgWrapper.className = 'msg-center'; // 중앙에 표시 (시스템 메시지)

    const card = document.createElement('div');
    card.className = 'safe-payment-card';
    card.style.background = 'linear-gradient(135deg, #fff5f5 0%, #fff 100%)';
    card.style.borderColor = '#ffc9c9';
    card.style.maxWidth = '300px';
    card.style.margin = '0 auto';

    card.innerHTML =
        '<div class="card-header" style="justify-content: center;">' +
            '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fa5252" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                '<circle cx="12" cy="12" r="10"></circle>' +
                '<line x1="15" y1="9" x2="9" y2="15"></line>' +
                '<line x1="9" y1="9" x2="15" y2="15"></line>' +
            '</svg>' +
            '<span class="card-title" style="color: #fa5252;">결제 실패</span>' +
        '</div>' +
        '<div style="text-align:center; padding: 8px 0; color: #495057; font-size: 13px;">' +
            '안전 결제가 실패하였습니다.<br>' +
            '<span style="color: #868e96; font-size: 12px;">다시 안전 결제 요청이 가능합니다.</span>' +
        '</div>';

    msgWrapper.appendChild(card);
    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;
}

// 모든 결제 타이머 정지
function stopAllPaymentTimers() {
    if (window.safePaymentTimerInterval) {
        clearInterval(window.safePaymentTimerInterval);
        window.safePaymentTimerInterval = null;
    }
    if (window.safePaymentSyncInterval) {
        clearInterval(window.safePaymentSyncInterval);
        window.safePaymentSyncInterval = null;
    }
}

// 안전 결제 진행 중 알림 UI 표시
function showSafePaymentInProgress(msg) {
    const log = document.getElementById("chatContainer");
    const emptyNotice = document.getElementById("emptyNotice");
    if (emptyNotice) emptyNotice.remove();

    const msgWrapper = document.createElement('div');
    msgWrapper.className = 'msg-right'; // 판매자(요청자) 쪽에 표시

    const card = document.createElement('div');
    card.className = 'safe-payment-card';
    card.style.background = 'linear-gradient(135deg, #fff5f5 0%, #fff 100%)';
    card.style.borderColor = '#ffc9c9';

    card.innerHTML =
        '<div class="card-header">' +
            '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fa5252" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                '<circle cx="12" cy="12" r="10"></circle>' +
                '<line x1="12" y1="8" x2="12" y2="12"></line>' +
                '<line x1="12" y1="16" x2="12.01" y2="16"></line>' +
            '</svg>' +
            '<span class="card-title" style="color: #fa5252;">안전 결제 중</span>' +
        '</div>' +
        '<div style="text-align:center; padding: 8px 0; color: #495057; font-size: 13px;">' +
            '현재 이 상품에 대해 안전 결제가 진행 중입니다.<br>' +
            '<span style="color: #868e96; font-size: 12px;">결제가 완료되거나 취소된 후 다시 요청해주세요.</span>' +
        '</div>';

    msgWrapper.appendChild(card);
    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;

    // 5초 후 자동으로 흐리게 처리
    setTimeout(function() {
        msgWrapper.style.opacity = '0.5';
    }, 5000);
}

// 안전 결제 요청 UI 표시
function showSafePaymentRequest(msg) {
     document.querySelectorAll('.safe-payment-card').forEach(card => {
            card.remove();
        });
    const log = document.getElementById("chatContainer");
    const emptyNotice = document.getElementById("emptyNotice");
    if (emptyNotice) emptyNotice.remove();

    const msgWrapper = document.createElement('div');
    const isMyMessage = Number(msg.sender_seq) === loginMemberSeq;
    msgWrapper.className = isMyMessage ? 'msg-right' : 'msg-left';

    const card = document.createElement('div');
    card.className = 'safe-payment-card';

    const msgId = 'safe-pay-req-' + Date.now();
    card.id = msgId;

    if (isMyMessage) {
        // 판매자 본인이 보낸 경우 - 요청 완료 표시 + 남은 결제 시간
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>' +
                    '<line x1="1" y1="10" x2="23" y2="10"></line>' +
                '</svg>' +
                '<span class="card-title">안전 결제 요청</span>' +
            '</div>' +
            '<div style="text-align:center; padding: 8px 0; color: #495057; font-size: 13px;">' +
                '구매자에게 안전 결제 요청을 보냈습니다.' +
            '</div>' +
            '<div style="text-align:center; padding: 8px 0; border-top: 1px solid #e9ecef; margin-top: 8px;">' +
                '<span style="color: #868e96; font-size: 12px;">남은 결제 시간: </span>' +
                '<span class="safe-payment-timer" id="timer-' + msgId + '" style="color: #fa5252; font-weight: bold; font-size: 14px;">--:--</span>' +
            '</div>';

        // 서버에서 남은 시간 조회 후 타이머 시작
        //startServerSyncTimer('timer-' + msgId);
    } else {
        // 구매자가 받은 경우 - 구매 요청하기 버튼 표시
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>' +
                    '<line x1="1" y1="10" x2="23" y2="10"></line>' +
                '</svg>' +
                '<span class="card-title">안전 결제 요청</span>' +
            '</div>' +
            '<div style="text-align:center; padding: 8px 0; color: #495057; font-size: 13px; margin-bottom: 12px;">' +
                '판매자가 안전 결제를 요청했습니다.' +
            '</div>' +
            '<button class="action-btn primary" id="btn-' + msgId + '" onclick="acceptSafePaymentRequest(\'' + msgId + '\')">' +
                '구매 요청하기' +
            '</button>' +
            '<div style="text-align:center; padding: 8px 0; border-top: 1px solid #e9ecef; margin-top: 12px;">' +
                '<span style="color: #868e96; font-size: 12px;">남은 결제 시간: </span>' +
                '<span class="safe-payment-timer" id="payment-timer-' + msgId + '" style="color: #fa5252; font-weight: bold; font-size: 14px;">--:--</span>' +
            '</div>';

        // 1분 타이머 시작 (구매 요청 수락 제한 시간)
        //startTimer(msgId, 60, function() {
        //    expireSafePaymentRequest(msgId);
        //});

        // 서버 동기화 타이머 시작 (남은 결제 시간)
        // startServerSyncTimer('payment-timer-' + msgId);
    }

    msgWrapper.appendChild(card);
    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;

    if (isMyMessage) {
              startServerSyncTimer('timer-' + msgId);
          } else {
              startServerSyncTimer('payment-timer-' + msgId);
          }
}

// 구매 요청 수락 (구매자가 클릭)
function acceptSafePaymentRequest(msgId) {
    // 타이머 정지
    if (safePaymentTimers[msgId]) {
        clearInterval(safePaymentTimers[msgId]);
        delete safePaymentTimers[msgId];
    }

    // 구매 요청 수락 메시지 전송
    if (stompClient && stompClient.connected) {
        stompClient.send(
            "/sendMessage/chat/" + chat_room_seq,
            {},
            JSON.stringify({
                chat_room_seq: chat_room_seq,
                chat_cont: "[SAFE_PAYMENT_ACCEPT]",
                sender_seq: loginMemberSeq,
                trade_seq: trade_seq
            })
        );
    }

    // 기존 카드 업데이트
    const card = document.getElementById(msgId);
    if (card) {
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<polyline points="20 6 9 17 4 12"></polyline>' +
                '</svg>' +
                '<span class="card-title">구매 요청 완료</span>' +
            '</div>' +
            '<div style="text-align:center; padding: 8px 0; color: #495057; font-size: 13px;">' +
                '구매 요청을 보냈습니다.' +
            '</div>';
    }
}

// 안전 결제 수락 UI 표시 (상품 정보 + 결제하기 버튼)
function showSafePaymentAccept(msg) {

    const log = document.getElementById("chatContainer");
    const emptyNotice = document.getElementById("emptyNotice");
    if (emptyNotice) emptyNotice.remove();

    const msgWrapper = document.createElement('div');
    const isMyMessage = Number(msg.sender_seq) === loginMemberSeq;
    msgWrapper.className = isMyMessage ? 'msg-right' : 'msg-left';

    const card = document.createElement('div');
    card.className = 'safe-payment-card';

    const msgId = 'safe-pay-accept-' + Date.now();
    card.id = msgId;

    // 상품 정보 가져오기
    const trade = currentTradeInfo;
    const bookStatusText = getBookStatusText(trade.book_st);
    const totalPrice = trade.sale_price + trade.delivery_cost;
    const bookImg = trade.book_img;

    if (isMyMessage) {
        // 구매자 본인이 보낸 경우 - 결제하기 버튼 표시
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>' +
                    '<line x1="1" y1="10" x2="23" y2="10"></line>' +
                '</svg>' +
                '<span class="card-title">결제 정보</span>' +
                '<span class="timer" id="timer-' + msgId + '"></span>' +
            '</div>' +
            '<div class="product-info">' +
                '<img src="' + bookImg + '" alt="상품 이미지" class="product-img" onerror="this.src=\'/img/no-image.png\'">' +
                '<div class="product-detail">' +
                    '<div class="product-title">' + escapeHtml(trade.book_title) + '</div>' +
                    '<div class="product-status">' + bookStatusText + '</div>' +
                    '<div class="product-price">' + numberFormat(trade.sale_price) + '원</div>' +
                    '<div class="product-delivery">배송비 ' + numberFormat(trade.delivery_cost) + '원</div>' +
                '</div>' +
            '</div>' +
            '<div style="text-align:right; margin-bottom: 12px; font-size: 14px; font-weight: 700; color: #212529;">' +
                '총 결제금액: <span style="color: #0046FF;">' + numberFormat(totalPrice) + '원</span>' +
            '</div>' +
            '<button class="action-btn primary" id="btn-' + msgId + '" onclick="goToPayment(\'' + msgId + '\')">' +
                '결제하기' +
            '</button>';

        // 1분 타이머 시작
        startTimer(msgId, 60, function() {
            expirePayment(msgId);
        });
    } else {
        // 판매자가 받은 경우 - 구매자가 수락했다는 알림
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<polyline points="20 6 9 17 4 12"></polyline>' +
                '</svg>' +
                '<span class="card-title">구매 요청 수락</span>' +
            '</div>' +
            '<div class="product-info">' +
                '<img src="' + bookImg + '" alt="상품 이미지" class="product-img" onerror="this.src=\'/img/no-image.png\'">' +
                '<div class="product-detail">' +
                    '<div class="product-title">' + escapeHtml(trade.book_title) + '</div>' +
                    '<div class="product-status">' + bookStatusText + '</div>' +
                    '<div class="product-price">' + numberFormat(trade.sale_price) + '원</div>' +
                    '<div class="product-delivery">배송비 ' + numberFormat(trade.delivery_cost) + '원</div>' +
                '</div>' +
            '</div>' +
            '<div style="text-align:center; padding: 8px 0; color: #495057; font-size: 13px;">' +
                '구매자가 결제를 진행 중입니다.' +
            '</div>';
    }

    msgWrapper.appendChild(card);
    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;
}

// 결제 페이지로 이동
function goToPayment(msgId) {
    // 타이머 정지
    if (safePaymentTimers[msgId]) {
        clearInterval(safePaymentTimers[msgId]);
        delete safePaymentTimers[msgId];
    }

    const tradeSeq = currentTradeInfo.trade_seq || trade_seq;
    if (tradeSeq > 0) {
        window.location.href = '/payments?trade_seq=' + tradeSeq;
    } else {
        alert('거래 정보를 찾을 수 없습니다.');
    }
}

// 결제 완료 UI 표시
function showSafePaymentComplete(msg) {
    //기존 안전결제 카드 전부 숨김
    document.querySelectorAll('.safe-payment-card').forEach(el => {
           el.style.display = 'none';
    });
    const log = document.getElementById("chatContainer");
    const emptyNotice = document.getElementById("emptyNotice");
    if (emptyNotice) emptyNotice.remove();

    const msgWrapper = document.createElement('div');
    const isMyMessage = Number(msg.sender_seq) === loginMemberSeq;
    msgWrapper.className = isMyMessage ? 'msg-right' : 'msg-left';

    const card = document.createElement('div');
    card.className = 'safe-payment-card';

    // 상품 정보 가져오기
    const trade = currentTradeInfo;
    const bookStatusText = getBookStatusText(trade.book_st);
    const totalPrice = trade.sale_price + trade.delivery_cost;
    const bookImg = trade.book_img || '/img/no-image.png';

    if (isMyMessage) {
        // 구매자 본인이 보낸 경우 - 결제 완료 표시
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>' +
                    '<polyline points="22 4 12 14.01 9 11.01"></polyline>' +
                '</svg>' +
                '<span class="card-title" style="color: #22c55e;">결제 완료</span>' +
            '</div>' +
            '<div class="product-info">' +
                '<img src="' + bookImg + '" alt="상품 이미지" class="product-img" onerror="this.src=\'/img/no-image.png\'">' +
                '<div class="product-detail">' +
                    '<div class="product-title">' + escapeHtml(trade.book_title) + '</div>' +
                    '<div class="product-status">' + bookStatusText + '</div>' +
                    '<div class="product-price">' + numberFormat(trade.sale_price) + '원</div>' +
                    '<div class="product-delivery">배송비 ' + numberFormat(trade.delivery_cost) + '원</div>' +
                '</div>' +
            '</div>' +
            '<div style="text-align:center; padding: 12px 0; color: #22c55e; font-size: 13px; font-weight: 600;">' +
                '결제가 완료되었습니다!' +
            '</div>';
    } else {
        // 판매자가 받은 경우 - 구매자가 결제 완료했다는 알림
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>' +
                    '<polyline points="22 4 12 14.01 9 11.01"></polyline>' +
                '</svg>' +
                '<span class="card-title" style="color: #22c55e;">결제 완료</span>' +
            '</div>' +
            '<div class="product-info">' +
                '<img src="' + bookImg + '" alt="상품 이미지" class="product-img" onerror="this.src=\'/img/no-image.png\'">' +
                '<div class="product-detail">' +
                    '<div class="product-title">' + escapeHtml(trade.book_title) + '</div>' +
                    '<div class="product-status">' + bookStatusText + '</div>' +
                    '<div class="product-price">' + numberFormat(trade.sale_price) + '원</div>' +
                    '<div class="product-delivery">배송비 ' + numberFormat(trade.delivery_cost) + '원</div>' +
                '</div>' +
            '</div>' +
            '<div style="text-align:center; padding: 12px 0; color: #22c55e; font-size: 13px; font-weight: 600;">' +
                '구매자가 결제를 완료했습니다!' +
            '</div>';
    }

    msgWrapper.appendChild(card);
    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;
}

// 안전 결제 요청 만료
function expireSafePaymentRequest(msgId) {
    const card = document.getElementById(msgId);
    if (card) {
        card.innerHTML =
            '<div class="card-header">' +
                '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#868e96" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                    '<circle cx="12" cy="12" r="10"></circle>' +
                    '<line x1="12" y1="8" x2="12" y2="12"></line>' +
                    '<line x1="12" y1="16" x2="12.01" y2="16"></line>' +
                '</svg>' +
                '<span class="card-title" style="color: #868e96;">안전 결제 요청</span>' +
            '</div>' +
            '<div class="expired-notice">' +
                '요청 시간이 만료되었습니다.' +
            '</div>';
    }
}

// 결제 만료
function expirePayment(msgId) {
    const card = document.getElementById(msgId);
    if (card) {
        const btn = document.getElementById('btn-' + msgId);
        if (btn) {
            btn.disabled = true;
            btn.textContent = '시간 만료';
        }
        const timer = document.getElementById('timer-' + msgId);
        if (timer) {
            timer.textContent = '만료됨';
            timer.style.color = '#868e96';
        }
    }
}

// 타이머 시작
function startTimer(msgId, seconds, onExpire) {
    let remaining = seconds;
    const timerEl = document.getElementById('timer-' + msgId);

    safePaymentTimers[msgId] = setInterval(function() {
        remaining--;
        if (timerEl) {
            const mins = Math.floor(remaining / 60);
            const secs = remaining % 60;
            timerEl.textContent = String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
        }

        if (remaining <= 0) {
            clearInterval(safePaymentTimers[msgId]);
            delete safePaymentTimers[msgId];
            if (onExpire) onExpire();
        }
    }, 1000);
}

// 책 상태 텍스트 변환
function getBookStatusText(status) {
    const statusMap = {
        'NEW': '새 상품',
        'LIKE_NEW': '거의 새 것',
        'GOOD': '양호',
        'ACCEPTABLE': '사용감 있음'
    };
    return statusMap[status] || status || '-';
}

// 숫자 포맷
function numberFormat(num) {
    return (num || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

// HTML 이스케이프
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// =====================================================
// 채팅방 리스트 무한 스크롤 페이징
// =====================================================

const chatroomPaging = {
    currentPage: 0,
    pageSize: 10,  // 한 번에 불러올 채팅방 수
    isLoading: false,
    hasMore: true,
    isInitialLoad: ${not empty chatrooms},  // 초기 로드 여부
    saleStatusFilter: ''  // 필터: '', 'SALE', 'SOLD'
};

// 채팅방 리스트 스크롤 이벤트
document.addEventListener('DOMContentLoaded', function() {
    const chatroomsList = document.getElementById('chatroomsList');
    if (chatroomsList) {
        chatroomsList.addEventListener('scroll', handleChatroomScroll);

        // 초기 데이터가 있으면 페이지 1부터 시작 (이미 0페이지 데이터가 렌더링됨)
        if (chatroomPaging.isInitialLoad) {
            chatroomPaging.currentPage = 1;
        }
    }

    // 필터 셀렉트 박스 이벤트
    const saleStatusFilter = document.getElementById('saleStatusFilter');
    if (saleStatusFilter) {
        saleStatusFilter.addEventListener('change', function() {
            chatroomPaging.saleStatusFilter = this.value;
            resetAndReloadChatrooms();
        });
    }
});

// 채팅방 리스트 초기화 및 다시 로드
function resetAndReloadChatrooms() {
    // 페이징 상태 초기화
    chatroomPaging.currentPage = 0;
    chatroomPaging.hasMore = true;
    chatroomPaging.isLoading = false;

    // 기존 채팅방 리스트 비우기
    const container = document.getElementById('chatroomsList');
    if (container) {
        // 로딩 인디케이터와 더 이상 없음 표시는 유지
        const loadingIndicator = document.getElementById('chatroomLoadingIndicator');
        const noMoreNotice = document.getElementById('noMoreRoomsNotice');

        container.innerHTML = '';

        if (loadingIndicator) container.appendChild(loadingIndicator);
        if (noMoreNotice) {
            noMoreNotice.classList.add('hidden');
            container.appendChild(noMoreNotice);
        }
    }

    // 첫 페이지 로드
    loadMoreChatrooms();
}

// 스크롤 이벤트 핸들러
function handleChatroomScroll() {
    const container = document.getElementById('chatroomsList');
    if (!container) return;

    // 스크롤이 하단 100px 이내에 도달했을 때
    const scrollBottom = container.scrollHeight - container.scrollTop - container.clientHeight;

    if (scrollBottom < 100 && !chatroomPaging.isLoading && chatroomPaging.hasMore) {
        loadMoreChatrooms();
    }
}

// 추가 채팅방 로드
function loadMoreChatrooms() {
    if (chatroomPaging.isLoading || !chatroomPaging.hasMore) return;

    chatroomPaging.isLoading = true;
    showChatroomLoading(true);

    const offset = chatroomPaging.currentPage * chatroomPaging.pageSize;
    let url = '/chat/rooms/list?limit=' + chatroomPaging.pageSize + '&offset=' + offset;

    // 필터가 있으면 파라미터 추가
    if (chatroomPaging.saleStatusFilter) {
        url += '&sale_st=' + chatroomPaging.saleStatusFilter;
    }

    fetch(url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
    })
    .then(function(response) {
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return response.json();
    })
    .then(function(data) {
        console.log('채팅방 페이징 응답:', data);

        if (data && Array.isArray(data.rooms)) {
            if (data.rooms.length > 0) {
                appendChatrooms(data.rooms);
                chatroomPaging.currentPage++;
            }

            // 더 이상 데이터가 없으면
            if (data.rooms.length < chatroomPaging.pageSize || !data.hasMore) {
                chatroomPaging.hasMore = false;
                showNoMoreRooms();
            }
        } else {
            chatroomPaging.hasMore = false;
            showNoMoreRooms();
        }
    })
    .catch(function(error) {
        console.error('채팅방 로드 실패:', error);
    })
    .finally(function() {
        chatroomPaging.isLoading = false;
        showChatroomLoading(false);
    });
}

// 채팅방 아이템 DOM에 추가
function appendChatrooms(rooms) {
    const container = document.getElementById('chatroomsList');
    const loadingIndicator = document.getElementById('chatroomLoadingIndicator');
    const emptyNotice = document.getElementById('emptyRoomNotice');

    if (emptyNotice) emptyNotice.remove();

    rooms.forEach(function(room) {
        const div = document.createElement('div');
        div.className = 'chatroom-item px-5 py-4 border-b border-gray-100 cursor-pointer transition-all hover:bg-gray-50 border-l-4 border-l-transparent';
        div.setAttribute('data-chat-room-seq', room.chat_room_seq);

        const lastMsg = room.last_msg
            ? escapeHtml(room.last_msg)
            : '<span class="text-gray-400 italic">아직 메시지가 없습니다</span>';

        var saleBadge = (room.sale_st === 'SOLD')
            ? '<span class="text-xs px-1.5 py-0.5 rounded bg-gray-200 text-gray-600 whitespace-nowrap">판매완료</span>'
            : '<span class="text-xs px-1.5 py-0.5 rounded bg-blue-100 text-blue-600 whitespace-nowrap">판매중</span>';

        // 마지막 메시지 시간 포맷팅
        var timeHtml = '';
        if (room.last_msg_dtm && Array.isArray(room.last_msg_dtm)) {
            const dt = room.last_msg_dtm;

            var date = new Date(
                dt[0],        // year
                dt[1] - 1,    // month (중요!!)
                dt[2],        // day
                dt[3],        // hour
                dt[4],        // minute
                dt[5]         // second
            );

            var month = String(date.getMonth() + 1).padStart(2, '0');
            var day = String(date.getDate()).padStart(2, '0');
            var hours = String(date.getHours()).padStart(2, '0');
            var minutes = String(date.getMinutes()).padStart(2, '0');

            timeHtml =
                '<span class="text-xs text-gray-400 whitespace-nowrap flex-shrink-0">'
                + month + '/' + day + ' ' + hours + ':' + minutes +
                '</span>';
        }

        div.innerHTML =
            '<div class="flex items-start gap-3">' +
                '<div class="w-10 h-10 bg-primary-50 rounded-lg flex items-center justify-center flex-shrink-0">' +
                    '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-primary-500"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"/></svg>' +
                '</div>' +
                '<div class="flex-1 min-w-0">' +
                    '<div class="flex items-center justify-between gap-2">' +
                        '<div class="font-semibold text-gray-900 text-sm truncate flex items-center gap-2">' + escapeHtml(room.sale_title || '') + saleBadge + '</div>' +
                        timeHtml +
                    '</div>' +
                    '<div class="text-xs text-gray-500 mt-1 truncate">' + lastMsg + '</div>' +
                '</div>' +
            '</div>';

        // 채팅방 클릭 이벤트 추가
        div.addEventListener('click', function() {
            selectChatroom(room.chat_room_seq);
        });

        // loadingIndicator 앞에 삽입
        if (loadingIndicator) {
            container.insertBefore(div, loadingIndicator);
        } else {
            container.appendChild(div);
        }
    });
}

// 채팅방 선택 (AJAX로 메시지 로드)
function selectChatroom(chatRoomSeq) {
    chat_room_seq = Number(chatRoomSeq);

    // 메시지 영역 초기화
    document.getElementById("chatContainer").innerHTML =
        '<div id="emptyNotice" class="flex flex-col items-center justify-center h-full text-gray-400">' +
            '<svg xmlns="http://www.w3.org/2000/svg" width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="mb-4 text-gray-300"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>' +
            '<p class="text-sm font-medium">이전 메시지가 없습니다</p>' +
            '<p class="text-xs text-gray-400 mt-1">첫 메시지를 보내보세요!</p>' +
        '</div>';

    // active 클래스 처리
    document.querySelectorAll('.chatroom-item').forEach(function(item) {
        item.classList.remove('active');
    });
    var selectedItem = document.querySelector('[data-chat-room-seq="' + chatRoomSeq + '"]');
    if (selectedItem) {
        selectedItem.classList.add('active');
    }

    // STOMP 재연결
    if (typeof stompClient !== 'undefined' && stompClient) {
        stompClient.disconnect(function() {
            connect();
        });
    }

    // 메시지 조회
    fetchMessages(chat_room_seq);
}

// 로딩 인디케이터 표시/숨김
function showChatroomLoading(show) {
    const indicator = document.getElementById('chatroomLoadingIndicator');
    if (indicator) {
        indicator.classList.toggle('hidden', !show);
    }
}

// 더 이상 채팅방 없음 표시
function showNoMoreRooms() {
    const notice = document.getElementById('noMoreRoomsNotice');
    const indicator = document.getElementById('chatroomLoadingIndicator');

    if (indicator) indicator.classList.add('hidden');
    if (notice) notice.classList.remove('hidden');
}

// fetchMessages 함수 오버라이드 (Object[] 데이터 처리)
const originalFetchMessages = fetchMessages;
fetchMessages = function(roomSeq) {
    const url = '/chat/messages?chat_room_seq=' + encodeURIComponent(roomSeq);
    console.log('=== fetchMessages 호출 ===', roomSeq);

    fetch(url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' }
    })
    .then(res => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
    })
    .then(data => {
        console.log('=== fetchMessages 응답 ===', data);

        // Object[] 형태: data[0] = 메시지 배열, data[1] = TradeVO
        if (Array.isArray(data) && data.length >= 2) {
            const messages = data[0];
            const tradeInfo = data[1];

            console.log('tradeInfo:', tradeInfo);
            currentTradeInfo = tradeInfo || {};

            // 모델이 없을 경우 JS로 DOM 채우기
            renderChatBookInfo(tradeInfo);
            // trade 정보 업데이트
            if (tradeInfo) {
                currentTradeInfo = {
                    trade_seq: tradeInfo.trade_seq || 0,
                    book_img: tradeInfo.book_img || '',
                    book_title: tradeInfo.book_title || '',
                    sale_price: tradeInfo.sale_price || 0,
                    delivery_cost: tradeInfo.delivery_cost || 0,
                    book_st: tradeInfo.book_st || '',
                    sale_title: tradeInfo.sale_title || '',
                    sale_st: tradeInfo.sale_st || ''
                };

                // trade_seq 업데이트
                if (tradeInfo.trade_seq) {
                    trade_seq = tradeInfo.trade_seq;
                }
                console.log('사진 출력 : ', tradeInfo.sale_title);

                // 판매자 여부 재확인
                if (tradeInfo.member_seller_seq) {
                    member_seller_seq = tradeInfo.member_seller_seq;
                    isSeller = (loginMemberSeq === tradeInfo.member_seller_seq);
                    console.log('isSeller 업데이트:', isSeller, 'loginMemberSeq:', loginMemberSeq, 'member_seller_seq:', member_seller_seq);
                    updatePlusButtonVisibility();
                }
            }

            // 메시지 렌더링
            if (Array.isArray(messages)) {
                messages.forEach(msg => showMessage(msg));
            }
        } else if (Array.isArray(data)) {
            // 기존 형식 호환 (단일 배열)
            console.log('기존 형식 (단일 배열) 사용');
            const hasPaymentComplete = data.some(m => m.chat_cont === '[SAFE_PAYMENT_COMPLETE]');
            const hasPaymentFailed = data.some(m => m.chat_cont === '[SAFE_PAYMENT_FAILED]');

            data.forEach(msg => {
                const content = msg.chat_cont || '';

                if (hasPaymentComplete || hasPaymentFailed) {
                    if (content === '[SAFE_PAYMENT_REQUEST]' ||
                        content === '[SAFE_PAYMENT_ACCEPT]') {
                        return;
                    }
                }

                showMessage(msg);
            });
        }
    })
    .catch(err => console.error('채팅 메시지 로드 실패:', err));
};

function renderChatBookInfo(tradeInfo) {
    if (!tradeInfo) return;

    const bookInfoEl = document.getElementById('chatBookInfo');
    const bookImgEl = document.getElementById('chatBookImg');
    const bookTitleEl = document.getElementById('chatBookTitle');
    const bookStatusEl = document.getElementById('chatBookStatus');
    const bookPriceEl = document.getElementById('chatBookPrice');
    const bookSale_stEl = document.getElementById('chatSale_st');
    const headerTitleEl = document.getElementById('chatHeaderTitle');

    // 채팅 헤더 제목
    if (headerTitleEl) {
        headerTitleEl.textContent = tradeInfo.sale_title || '거래 제목';
    }
    // 책 이미지
    if (bookImgEl) bookImgEl.src = tradeInfo.book_img || '/img/no-image.png';
    // 책 제목
    if (bookTitleEl) bookTitleEl.textContent = tradeInfo.book_title || '';
    // 책 가격
    if (bookPriceEl) {
        const totalPrice = (tradeInfo.sale_price || 0) + (tradeInfo.delivery_cost || 0);
        bookPriceEl.textContent = totalPrice.toLocaleString() + '원';
    }
    // 책 상태
    if (bookStatusEl) {
        switch (tradeInfo.book_st) {
            case 'LIKE_NEW': bookStatusEl.textContent = '거의 새책'; break;
            case 'GOOD': bookStatusEl.textContent = '좋음'; break;
            case 'USED': bookStatusEl.textContent = '사용됨'; break;
            case 'NEW': bookStatusEl.textContent = '새책'; break;
            default: bookStatusEl.textContent = ''; break;
        }
    }
    console.log('북상태 확인 !!!', tradeInfo.sale_st);
    // 거래 상태
    if (bookSale_stEl) {
        // 상태 변경 전에 색상 초기화
        bookSale_stEl.classList.remove('text-red-500');

        switch (tradeInfo.sale_st) {
            case 'SALE':
                bookSale_stEl.textContent = '판매중';
                break;

            case 'SOLD':
                bookSale_stEl.textContent = '판매완료';
                bookSale_stEl.classList.add('text-red-500');
                break;

            default:
                bookSale_stEl.textContent = '';
                break;
        }
    }


    // 책 정보 영역 보이게
    if (bookInfoEl) bookInfoEl.style.display = 'flex';
}

// =====================================================
// 이미지 전송 기능
// =====================================================

// 이미지 업로드 및 전송
function uploadAndSendImage(file) {
    // 파일 타입 검증
    if (!file.type.startsWith('image/')) {
        alert('이미지 파일만 전송할 수 있습니다.');
        return;
    }

    // 파일 크기 검증 (5MB 제한)
    if (file.size > 5 * 1024 * 1024) {
        alert('이미지 크기는 5MB 이하만 가능합니다.');
        return;
    }

    // 채팅방 선택 확인
    if (!chat_room_seq || chat_room_seq <= 0) {
        alert('채팅방을 선택해주세요.');
        return;
    }

    // FormData 생성
    var formData = new FormData();
    formData.append('image', file);
    formData.append('chat_room_seq', chat_room_seq);
    formData.append('trade_seq', trade_seq);

    // 업로드 중 표시
    var log = document.getElementById('chatContainer');
    var uploadingDiv = document.createElement('div');
    uploadingDiv.className = 'msg-right';
    uploadingDiv.id = 'uploading-indicator';
    uploadingDiv.innerHTML = '<div class="content" style="background: #e9ecef; color: #868e96;">이미지 업로드 중...</div>';
    log.appendChild(uploadingDiv);
    log.scrollTop = log.scrollHeight;

    // 서버로 업로드
    fetch('/chat/image/upload', {
        method: 'POST',
        credentials: 'same-origin',
        body: formData
    })
    .then(function(response) {
        if (!response.ok) throw new Error('업로드 실패');
        return response.json();
    })
    .then(function(data) {
        console.log('이미지 업로드 응답:', data);

        // 업로드 중 표시 제거
        var indicator = document.getElementById('uploading-indicator');
        if (indicator) indicator.remove();

        if (data.success && data.imageUrl) {
            // STOMP로 이미지 메시지 전송
            sendImageMessage(data.imageUrl);
        } else {
            alert('이미지 업로드에 실패했습니다.');
        }
    })
    .catch(function(error) {
        console.error('이미지 업로드 오류:', error);
        var indicator = document.getElementById('uploading-indicator');
        if (indicator) indicator.remove();
        alert('이미지 업로드 중 오류가 발생했습니다.');
    });
}

// STOMP로 이미지 메시지 전송
function sendImageMessage(imageUrl) {
    if (!stompClient || !stompClient.connected) {
        alert('채팅 연결이 필요합니다.');
        return;
    }

    stompClient.send(
        "/sendMessage/chat/" + chat_room_seq,
        {},
        JSON.stringify({
            chat_room_seq: chat_room_seq,
            chat_cont: "[IMAGE]" + imageUrl,
            sender_seq: loginMemberSeq,
            trade_seq: trade_seq
        })
    );
}

// showMessage 함수에 이미지 처리 추가
var originalShowMessageForImage = showMessage;
showMessage = function(msg) {
    var chatCont = msg.chat_cont || '';

    // 이미지 메시지인 경우
    if (chatCont.startsWith('[IMAGE]')) {
        showImageMessage(msg);
        return;
    }

    // 기존 로직 실행
    originalShowMessageForImage(msg);
};

// 이미지 메시지 표시
function showImageMessage(msg) {
    var log = document.getElementById('chatContainer');
    var emptyNotice = document.getElementById('emptyNotice');
    if (emptyNotice) emptyNotice.remove();

    var imageUrl = msg.chat_cont.replace('[IMAGE]', '');
    var isMyMessage = Number(msg.sender_seq) === loginMemberSeq;

    var msgWrapper = document.createElement('div');
    msgWrapper.className = isMyMessage ? 'msg-right' : 'msg-left';

    var imgContainer = document.createElement('div');
    imgContainer.className = 'content';
    imgContainer.style.padding = '4px';
    imgContainer.style.background = isMyMessage ? '#0046FF' : '#fff';

    var img = document.createElement('img');
    img.src = imageUrl;
    img.alt = '전송된 이미지';
    img.style.maxWidth = '200px';
    img.style.maxHeight = '200px';
    img.style.borderRadius = '12px';
    img.style.cursor = 'pointer';
    img.onerror = function() {
        this.onerror = null; // 무한 루프 차단
        this.src = '/img/no-image.png';
    };
    // 클릭 시 새 탭에서 원본 이미지 보기
    img.onclick = function() {
        window.open(imageUrl, '_blank');
    };

    imgContainer.appendChild(img);
    msgWrapper.appendChild(imgContainer);

    // 시간 표시
    var timeDiv = document.createElement('div');
    timeDiv.className = 'msg-time';
    if (msg.sent_dtm) {
        var date;
        // 배열 형식인 경우 (예: [2024, 1, 15, 10, 30, 0])
        if (Array.isArray(msg.sent_dtm)) {
            date = new Date(msg.sent_dtm[0], msg.sent_dtm[1] - 1, msg.sent_dtm[2],
                           msg.sent_dtm[3] || 0, msg.sent_dtm[4] || 0, msg.sent_dtm[5] || 0);
        } else {
            date = new Date(msg.sent_dtm);
        }

        if (!isNaN(date.getTime())) {
            var timeStr = date.getFullYear() + '/' +
                String(date.getMonth() + 1).padStart(2, '0') + '/' +
                String(date.getDate()).padStart(2, '0') + ' ' +
                String(date.getHours()).padStart(2, '0') + ':' +
                String(date.getMinutes()).padStart(2, '0');
            timeDiv.textContent = timeStr;
        }
    }
    msgWrapper.appendChild(timeDiv);

    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;
}

if (enterChatRoomSeq) {
        chat_room_seq = Number(enterChatRoomSeq);
        fetchMessages(chat_room_seq);
}
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>