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
    </style>
</head>
<body class="bg-slate-100 h-screen flex items-center justify-center relative overflow-hidden">

<div class="absolute top-0 left-0 w-full h-64 bg-blue-600 transform -skew-y-3 origin-top-left -z-10"></div>

<div class="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-md border border-slate-200 relative z-10">

    <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-50 mb-4 border border-blue-100">
            <i data-lucide="key-round" class="w-8 h-8 text-blue-600"></i>
        </div>
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">접근 코드 확인</h1>
        <p class="text-slate-500 text-sm mt-2">관리자 시스템 접속을 위한 인증이 필요합니다</p>
    </div>

    <form action="/admin/access" method="post" class="space-y-5">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="code1">코드 1</label>
            <input type="password" id="code1" name="code1" required
                   placeholder="접근 코드 1을 입력하세요"
                   class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all placeholder-slate-400 text-sm bg-slate-50 focus:bg-white">
        </div>

        <div>
            <label class="block text-sm font-bold text-slate-700 mb-1.5" for="code2">코드 2</label>
            <input type="password" id="code2" name="code2" required
                   placeholder="접근 코드 2를 입력하세요"
                   class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all placeholder-slate-400 text-sm bg-slate-50 focus:bg-white">
        </div>

        <c:if test="${param.error == 'true'}">
            <div class="flex items-center gap-2 p-3 bg-red-50 border border-red-100 rounded-lg text-red-600 text-xs font-medium">
                <i data-lucide="alert-circle" class="w-4 h-4"></i>
                <span>접근 코드가 올바르지 않습니다.</span>
            </div>
        </c:if>

        <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3.5 rounded-lg font-bold shadow-lg shadow-blue-500/30 transition-all duration-200 transform hover:-translate-y-0.5 active:translate-y-0">
            확인
        </button>
    </form>

    <div class="mt-8 pt-6 border-t border-slate-100 text-center">
        <a href="/home" class="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-blue-600 transition-colors font-medium">
            <i data-lucide="arrow-left" class="w-3 h-3"></i>
            메인 페이지로 돌아가기
        </a>
    </div>
</div>

<script>
    lucide.createIcons();
</script>
</body>
</html>
