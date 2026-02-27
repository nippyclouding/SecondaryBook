let stompClient = null;

window.onload = function () {
    connect();
    setupChatroomClick();
};

/* -------------------------------
   STOMP 연결
-------------------------------- */
function connect() {
    const socket = new SockJS('/chatEndPoint');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('STOMP Connected:', frame);
        subscribeCurrentRoom();
    });
}

function subscribeCurrentRoom() {
    if (!stompClient) return;

    // 메시지 수신 구독
    stompClient.subscribe('/chatroom/' + chat_room_seq, function (message) {
        const msg = JSON.parse(message.body);
        showMessage(msg);

        // 상대방이 보낸 메시지를 실시간으로 받으면 읽음 처리 요청
        if (Number(msg.sender_seq) !== loginMemberSeq) {
            sendReadEvent();
        }
    });

    // 읽음 이벤트 구독 (상대방이 내 메시지를 읽었을 때)
    stompClient.subscribe('/chatroom/' + chat_room_seq + '/read', function (message) {
        const readerSeq = JSON.parse(message.body);
        // 상대방이 읽었으면 내가 보낸 메시지들에 체크 표시
        if (Number(readerSeq) !== loginMemberSeq) {
            updateReadStatus();
        }
    });
}

// 읽음 이벤트 전송 (내가 상대방 메시지를 읽었음을 알림)
function sendReadEvent() {
    if (!stompClient || !stompClient.connected) return;

    stompClient.send(
        "/sendMessage/chat/" + chat_room_seq + "/read",
        {},
        JSON.stringify({
            chat_room_seq: chat_room_seq,
            reader_seq: loginMemberSeq
        })
    );
}

// 내가 보낸 메시지들의 읽음 상태 업데이트
function updateReadStatus() {
    const myMessages = document.querySelectorAll('.msg-right .msg-time');
    myMessages.forEach(function(timeEl) {
        if (!timeEl.textContent.includes('✔')) {
            timeEl.textContent = timeEl.textContent + ' ✔';
        }
    });
}

const MAX_LENGTH = 1000;
const input = document.getElementById("message");
const errorEl = document.getElementById("messageError");


/* -------------------------------
   메시지 전송
-------------------------------- */
function sendMessage() {
    if (!stompClient || !stompClient.connected) return;

    const input = document.getElementById("message");
    const msg = input.value.trim();
    if (!msg) return;
    if (msg.length > MAX_LENGTH) {
        errorEl.classList.remove("hidden");
        return;
    }
    stompClient.send(
        "/sendMessage/chat/" + chat_room_seq,
        {},
        JSON.stringify({
            chat_room_seq: chat_room_seq,
            chat_cont: msg,
            sender_seq: loginMemberSeq,
            trade_seq: trade_seq
        })
    );

    input.value = '';
}

/* -------------------------------
   메시지 렌더링
-------------------------------- */
function showMessage(msg) {
    const log = document.getElementById("chatContainer");

    const emptyNotice = document.getElementById("emptyNotice");
    if (emptyNotice) emptyNotice.remove();

    const msgWrapper = document.createElement('div');

    // 보낸 사람 기준 정렬
    if (Number(msg.sender_seq) === loginMemberSeq) {
        msgWrapper.className = 'msg-right';
    } else {
        msgWrapper.className = 'msg-left';
    }

    const msgContent = document.createElement('div');
    msgContent.className = 'content';
    msgContent.textContent = msg.chat_cont || '';

    // sent_dtm JSON 처리
    let dateObj;
    if (Array.isArray(msg.sent_dtm)) {
        dateObj = parseLocalDateTime(msg.sent_dtm);
    } else {
        dateObj = new Date();
    }

    const timeStr =
        dateObj.getFullYear() + "/" +
        String(dateObj.getMonth() + 1).padStart(2, '0') + "/" +
        String(dateObj.getDate()).padStart(2, '0') + " " +
        String(dateObj.getHours()).padStart(2, '0') + ":" +
        String(dateObj.getMinutes()).padStart(2, '0');

    const msgTime = document.createElement('div');
    msgTime.className = 'msg-time';

    // 내가 보낸 메시지인 경우에만 읽음 표시
    const isMyMessage = Number(msg.sender_seq) === loginMemberSeq;
    const readMark = isMyMessage && msg.read_yn ? ' ✔' : '';
    msgTime.textContent = timeStr + readMark;

    msgWrapper.appendChild(msgContent);
    msgWrapper.appendChild(msgTime);

    log.appendChild(msgWrapper);
    log.scrollTop = log.scrollHeight;
}

/* -------------------------------
   채팅방 클릭
-------------------------------- */
function setupChatroomClick() {
    const rooms = document.querySelectorAll('.chatroom-item');

    rooms.forEach(room => {
        room.addEventListener('click', function () {
            const selectedRoomSeq = this.getAttribute('data-chat-room-seq');
            chat_room_seq = Number(selectedRoomSeq);

            document.getElementById("chatContainer").innerHTML =
                '<div id="emptyNotice">이전 메시지가 없습니다.</div>';

            // msg_unread가 true일때 채팅방 클릭하면 css 효과 제거
            const unreadDot = this.querySelector('.unread-dot');
            if (unreadDot) {
                unreadDot.remove(); // 완전 제거
                // unreadDot.style.display = 'none'; // ← 숨김 처리 원하면 이거
            }
            // 닉네임 조회
            fetchChatRoomUserInfo(chat_room_seq).then(data => {
                let otherNick = '';
                if (data.member_seller_seq === loginMemberSeq) {
                    otherNick = data.member_buyer_nicknm;
                } else if (data.member_buyer_seq === loginMemberSeq) {
                    otherNick = data.member_seller_nicknm;
                }


                const headerSubEl = document.getElementById('chatHeaderSub');

                if (headerSubEl) {
                    headerSubEl.textContent =
                        otherNick ? `${otherNick}님과의 채팅` : '';
                }

                // 채팅방 목록 닉네임 반영
                const nicknameEl = this.querySelector('.room-nickname');
                if (nicknameEl) {
                    nicknameEl.textContent = otherNick || '닉네임 없음';
                }
                });

            const titleEl = this.querySelector('.room-title');
            const headerTitleEl = document.getElementById('chatHeaderTitle');

            if (headerTitleEl && titleEl) {
                headerTitleEl.textContent = titleEl.textContent;
            }

            if (stompClient) {
                stompClient.disconnect(() => connect());
            }

            fetchMessages(chat_room_seq);
        });
    });
}

// 닉네임 조회
function fetchChatRoomUserInfo(roomSeq) {
    return fetch(`/chat/memberInfo?chat_room_seq=${roomSeq}`)
        .then(res => res.json());
}
/* -------------------------------
   메시지 AJAX 조회
-------------------------------- */
function fetchMessages(roomSeq) {
    const url = '/chat/messages?chat_room_seq=' + encodeURIComponent(roomSeq);

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
        if (Array.isArray(data)) {
            data.forEach(msg => showMessage(msg));
        }
    })
    .catch(err => console.error('채팅 메시지 로드 실패:', err));
}

/* -------------------------------
   LocalDateTime 배열 파싱
-------------------------------- */
function parseLocalDateTime(arr) {
    if (!Array.isArray(arr)) return new Date();

    const [y, m, d, h, min, s, nano] = arr;

    return new Date(
        y,
        m - 1,
        d,
        h,
        min,
        s,
        Math.floor((nano || 0) / 1_000_000)
    );
}

/* -------------------------------
   이벤트 바인딩
-------------------------------- */
document.addEventListener("DOMContentLoaded", function () {
    const sendBtn = document.getElementById("sendBtn");
    const messageInput = document.getElementById("message"); // 입력창 id

    // 버튼 클릭
    sendBtn.addEventListener("click", () => {
        console.log("버튼 클릭 이벤트");
        sendMessage();
    });

    // Enter 키 전송
    messageInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault(); // 줄바꿈 방지
            sendMessage();      // 버튼 클릭과 동일
            console.log("Enter 이벤트");
        }
    });
});


window.addEventListener("beforeunload", function () {
    if (stompClient) stompClient.disconnect();
});
