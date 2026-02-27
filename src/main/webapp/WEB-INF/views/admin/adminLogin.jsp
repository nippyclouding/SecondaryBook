<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SecondHandBooks Administrator</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">

    <style>
        body { font-family: 'Noto Sans KR', sans-serif; }
        .glass-effect {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
        }
    </style>
</head>
<body class="bg-slate-100 h-screen flex items-center justify-center relative overflow-hidden">

<div class="absolute top-0 left-0 w-full h-64 bg-blue-600 transform -skew-y-3 origin-top-left -z-10"></div>
<div class="absolute bottom-0 right-0 w-64 h-64 bg-blue-100 rounded-full mix-blend-multiply filter blur-3xl opacity-70 -z-10 animate-blob"></div>

<div class="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-md border border-slate-200 relative z-10">

    <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-50 mb-4 border border-blue-100">
            <i data-lucide="shield-check" class="w-8 h-8 text-blue-600"></i>
        </div>
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">관리자 로그인</h1>
        <p class="text-slate-500 text-sm mt-2">SecondHandBooks 관리 시스템 접속</p>
    </div>

    <form action="/admin/loginProcess" method="post" class="space-y-5">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="id">관리자 ID</label>
            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <i data-lucide="user" class="h-5 w-5 text-slate-400"></i>
                </div>
                <input type="text" id="id" name="id" required
                       placeholder="아이디를 입력하세요"
                       class="w-full pl-10 pr-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all placeholder-slate-400 text-sm bg-slate-50 focus:bg-white">
            </div>
        </div>

        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="pwd">비밀번호</label>
            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <i data-lucide="lock" class="h-5 w-5 text-slate-400"></i>
                </div>
                <input type="password" id="pwd" name="pwd" required
                       placeholder="비밀번호를 입력하세요"
                       class="w-full pl-10 pr-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all placeholder-slate-400 text-sm bg-slate-50 focus:bg-white">
            </div>
        </div>

        <c:if test="${param.error == 'true'}">
            <div class="flex items-center gap-2 p-3 bg-red-50 border border-red-100 rounded-lg text-red-600 text-xs font-medium animate-pulse">
                <i data-lucide="alert-circle" class="w-4 h-4"></i>
                <span>아이디 또는 비밀번호가 일치하지 않습니다.</span>
            </div>
        </c:if>

        <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3.5 rounded-lg font-bold shadow-lg shadow-blue-500/30 transition-all duration-200 transform hover:-translate-y-0.5 active:translate-y-0">
            접속하기
        </button>
    </form>

    <div class="mt-8 pt-6 border-t border-slate-100 text-center">
        <a href="/home" class="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-blue-600 transition-colors font-medium">
            <i data-lucide="arrow-left" class="w-3 h-3"></i>
            메인 페이지로 돌아가기
        </a>
        <p class="text-[10px] text-slate-300 mt-4">
            © SecondHandBooks Corp. Admin System<br>Authorized Personnel Only.
        </p>
    </div>
</div>

<script>
    // 아이콘 렌더링
    lucide.createIcons();
</script>
</body>
</html>