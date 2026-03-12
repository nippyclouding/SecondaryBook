<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>공지사항 상세</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-gray-50">

<div class="max-w-4xl mx-auto py-8 px-4">
    <!-- 헤더 -->
    <div class="mb-6 flex items-center justify-between">
        <button onclick="history.back()"
                class="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition">
            <i data-lucide="arrow-left" class="w-5 h-5"></i>
            <span class="font-medium">목록으로</span>
        </button>

        <div class="flex gap-2">
            <button onclick="location.href='/admin/notices/edit?notice_seq=${notice.notice_seq}'"
                    class="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition flex items-center gap-2">
                <i data-lucide="edit-2" class="w-4 h-4"></i>
                수정
            </button>
            <button onclick="deleteNotice(${notice.notice_seq})"
                    class="px-4 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition flex items-center gap-2">
                <i data-lucide="trash-2" class="w-4 h-4"></i>
                삭제
            </button>
        </div>
    </div>

    <!-- 공지사항 카드 -->
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <!-- 헤더 -->
        <div class="px-8 py-6 border-b border-gray-100">
            <div class="flex items-start gap-3 mb-4">
                <c:if test="${notice.notice_priority == 1}">
                    <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-red-50 text-red-600">
                        <i data-lucide="alert-circle" class="w-3 h-3 mr-1"></i>
                        중요 공지
                    </span>
                </c:if>

                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${notice.active ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-600'}">
                    ${notice.active ? '공개' : '비공개'}
                </span>
            </div>

            <h1 class="text-2xl font-bold text-gray-900 mb-4">
                <c:out value="${notice.notice_title}"/>
            </h1>

            <div class="flex items-center gap-6 text-sm text-gray-600">
                <div class="flex items-center gap-2">
                    <i data-lucide="user" class="w-4 h-4"></i>
                    <span><c:out value="${notice.admin_login_id}"/></span>
                </div>
                <div class="flex items-center gap-2">
                    <i data-lucide="calendar" class="w-4 h-4"></i>
                    <span>${notice.crtDtmFormatted}</span>
                </div>
                <div class="flex items-center gap-2">
                    <i data-lucide="eye" class="w-4 h-4"></i>
                    <span><fmt:formatNumber value="${notice.view_count}" pattern="#,###" /></span>
                </div>
            </div>
        </div>

        <!-- 본문 -->
        <div class="px-8 py-8">
            <div class="prose max-w-none">
                ${notice.notice_cont}
            </div>
        </div>
    </div>
</div>

<script>
    // Lucide 아이콘 초기화
    lucide.createIcons();

    // 삭제 함수
    function deleteNotice(noticeSeq) {
        if (confirm('정말로 이 공지사항을 삭제하시겠습니까?')) {
            fetch('/admin/notices/delete/' + noticeSeq, {
                method: 'DELETE'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('공지사항이 삭제되었습니다.');
                    location.href = '/admin?tab=notice';
                } else {
                    alert('삭제 중 오류가 발생했습니다: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('삭제 중 오류가 발생했습니다.');
            });
        }
    }
</script>

</body>
</html>