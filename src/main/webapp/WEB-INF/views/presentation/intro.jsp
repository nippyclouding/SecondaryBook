<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="ko">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>SecondHand Books - Project Journey</title>
            <link rel="stylesheet" as="style" crossorigin
                href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css" />
            <script src="https://cdn.tailwindcss.com"></script>
            <script src="https://unpkg.com/lucide@latest"></script>
            <script src="https://cdn.jsdelivr.net/npm/apexcharts"></script>
            <script src="https://d3js.org/d3.v7.min.js"></script>
            <style>
                body {
                    font-family: 'Pretendard', sans-serif;
                    background-color: #FAFAFA;
                    color: #1D1D1F;
                    overflow-x: hidden;
                }

                /* 텍스트 그라데이션 */
                .text-gradient {
                    background: linear-gradient(135deg, #0071e3 0%, #00c6fb 100%);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                }

                /* 스크롤 애니메이션 */
                .reveal-text {
                    opacity: 0;
                    transform: translateY(40px);
                    transition: all 1s cubic-bezier(0.165, 0.84, 0.44, 1);
                }

                .reveal-text.active {
                    opacity: 1;
                    transform: translateY(0);
                }

                .reveal-card {
                    opacity: 0;
                    transform: scale(0.95) translateY(20px);
                    transition: all 0.8s cubic-bezier(0.165, 0.84, 0.44, 1);
                }

                .reveal-card.active {
                    opacity: 1;
                    transform: scale(1) translateY(0);
                }

                /* Bento Grid Box Style - Clean & Readable */
                .bento-box {
                    background: #fff;
                    border-radius: 24px;
                    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
                    border: 1px solid rgba(0, 0, 0, 0.05);
                    transition: transform 0.3s ease, box-shadow 0.3s ease;
                    overflow: hidden;
                    z-index: 10;
                }

                .bento-box:hover {
                    transform: translateY(-5px);
                    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.1);
                }

                /* Code Block Style */
                .code-window {
                    background: #1e1e1e;
                    border-radius: 12px;
                    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
                    font-family: 'Menlo', 'Monaco', monospace;
                    position: relative;
                    overflow: hidden;
                }

                .code-header {
                    display: flex;
                    gap: 6px;
                    padding: 12px 16px;
                    background: #252526;
                    border-bottom: 1px solid #333;
                }

                .dot {
                    width: 12px;
                    height: 12px;
                    border-radius: 50%;
                }

                .dot.red {
                    background: #ff5f56;
                }

                .dot.yellow {
                    background: #ffbd2e;
                }

                .dot.green {
                    background: #28c93f;
                }

                /* --- INTERACTIVE 3D & MOTION --- */
                .interactive-container {
                    perspective: 1000px;
                    transform-style: preserve-3d;
                }

                .interactive-card {
                    will-change: transform;
                    transform-style: preserve-3d;
                    transition: transform 0.1s cubic-bezier(0.03, 0.98, 0.52, 0.99);
                }

                .interactive-content {
                    transform: translateZ(20px);
                }

                /* Spotlight Effect */
                .spotlight-overlay {
                    pointer-events: none;
                    position: absolute;
                    inset: 0;
                    opacity: 0;
                    background: radial-gradient(800px circle at var(--mouse-x) var(--mouse-y), rgba(255, 255, 255, 0.15), transparent 40%);
                    z-index: 50;
                    transition: opacity 0.3s;
                    border-radius: inherit;
                    mix-blend-mode: overlay;
                }

                .bento-box:hover .spotlight-overlay,
                .reveal-card:hover .spotlight-overlay {
                    opacity: 1;
                }

                /* Hero Floating Animation */
                @keyframes heroFloat {

                    0%,
                    100% {
                        transform: translateY(0) rotate(3deg);
                    }

                    50% {
                        transform: translateY(-15px) rotate(6deg) scale(1.05);
                    }
                }

                .hero-floater {
                    animation: heroFloat 6s ease-in-out infinite;
                }

                /* Custom Cursor Glow */
                .cursor-glow {
                    width: 400px;
                    height: 400px;
                    background: radial-gradient(circle, rgba(59, 130, 246, 0.15), transparent 70%);
                    position: fixed;
                    pointer-events: none;
                    transform: translate(-50%, -50%);
                    z-index: 9999;
                    mix-blend-mode: screen;
                    transition: transform 0.1s;
                }
            </style>
        </head>

        <body>

            <nav
                class="fixed top-0 w-full z-50 bg-white/80 backdrop-blur-md border-b border-gray-100 transition-all duration-300">
                <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
                    <div class="font-bold text-lg tracking-tight flex items-center gap-2">
                        <span class="w-2 h-2 rounded-full bg-blue-600"></span>
                        SecondHand Books
                    </div>
                    <div class="text-xs text-gray-500 font-medium">Shinhan DS Academy</div>
                </div>
            </nav>

            <!-- Original HERO Section -->
            <section class="min-h-screen flex flex-col justify-center items-center text-center px-4 relative pt-10">
                <div class="reveal-text">
                    <div class="mb-8 inline-block">
                        <span
                            class="px-4 py-1.5 bg-blue-50 text-blue-600 rounded-full text-sm font-bold border border-blue-100">Project
                            Presentation</span>
                    </div>
                    <h1 class="text-6xl md:text-8xl font-black mb-8 leading-tight tracking-tight text-gray-900">
                        책의 가치를 잇고,<br>
                        <span class="text-gradient">독자를 연결하다.</span>
                    </h1>
                    <p class="text-xl md:text-2xl text-gray-500 max-w-2xl mx-auto leading-relaxed font-medium">
                        중고 서적 거래 & 로컬 독서 커뮤니티 플랫폼
                    </p>
                    <div class="mt-12 text-sm font-bold text-gray-400">
                        Team 배곱시계
                        <br>발표자: 이상원
                    </div>
                </div>
                <div class="absolute bottom-10 animate-bounce text-gray-300">
                    <i data-lucide="chevron-down" class="w-8 h-8"></i>
                </div>
            </section>

            <!-- Partitioned TEAM Section -->
            <section class="py-32 bg-white border-y border-gray-100">
                <div class="max-w-7xl mx-auto px-6 text-center">
                    <h2 class="text-4xl font-bold mb-4 reveal-text">Our Team</h2>
                    <p class="text-gray-500 mb-20 reveal-text">각 분야의 전문가들이 모여 최고의 시너지를 냈을지도.......?</p>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-12 text-left">
                        <!-- Group 1 -->
                        <div
                            class="reveal-card group p-8 rounded-3xl bg-gray-50 hover:bg-white hover:shadow-xl transition-all border border-transparent hover:border-gray-100">
                            <div class="flex items-center gap-4 mb-6">
                                <div class="p-3 bg-blue-100 text-blue-600 rounded-xl">
                                    <i data-lucide="shield-check" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <h3 class="font-bold text-lg">Auth & Admin</h3>
                                    <p class="text-xs text-gray-400">Security / User Mgmt</p>
                                </div>
                            </div>
                            <div class="space-y-6">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                        K</div>
                                    <div>
                                        <p class="font-bold text-gray-900">김규태</p>
                                    </div>
                                </div>
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                        L</div>
                                    <div>
                                        <p class="font-bold text-gray-900">이승환</p>
                                    </div>
                                </div>
                            </div>
                            <div class="mt-6 pt-6 border-t border-gray-200">
                                <p class="text-xs text-gray-500 leading-relaxed">
                                    Spring Security 인증/인가 구조 설계, 관리자 운영 대시보드 및 마이페이지 통합 관리를 구현했습니다.
                                </p>
                            </div>
                        </div>

                        <!-- Group 2 -->
                        <div class="reveal-card group p-8 rounded-3xl bg-gray-50 hover:bg-white hover:shadow-xl transition-all border border-transparent hover:border-gray-100"
                            style="transition-delay: 100ms;">
                            <div class="flex items-center gap-4 mb-6">
                                <div class="p-3 bg-purple-100 text-purple-600 rounded-xl">
                                    <i data-lucide="credit-card" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <h3 class="font-bold text-lg">Core Business</h3>
                                    <p class="text-xs text-gray-400">Trade / Pay / Chat</p>
                                </div>
                            </div>
                            <div class="space-y-6">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                        L</div>
                                    <div>
                                        <p class="font-bold text-gray-900">이상원</p>
                                    </div>
                                </div>
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                        C</div>
                                    <div>
                                        <p class="font-bold text-gray-900">최범근</p>
                                    </div>
                                </div>
                            </div>
                            <div class="mt-6 pt-6 border-t border-gray-200">
                                <p class="text-xs text-gray-500 leading-relaxed">
                                    중고 거래 핵심 로직, 에스크로 안전 결제(Scheduler), STOMP 기반 실시간 채팅을 담당했습니다.
                                </p>
                            </div>
                        </div>

                        <!-- Group 3 -->
                        <div class="reveal-card group p-8 rounded-3xl bg-gray-50 hover:bg-white hover:shadow-xl transition-all border border-transparent hover:border-gray-100"
                            style="transition-delay: 200ms;">
                            <div class="flex items-center gap-4 mb-6">
                                <div class="p-3 bg-green-100 text-green-600 rounded-xl">
                                    <i data-lucide="users" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <h3 class="font-bold text-lg">Community</h3>
                                    <p class="text-xs text-gray-400">Book Club / Board</p>
                                </div>
                            </div>
                            <div class="space-y-6">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                        K</div>
                                    <div>
                                        <p class="font-bold text-gray-900">김도연</p>
                                        <p class="text-xs text-blue-600 font-bold">Team Leader</p>
                                    </div>
                                </div>
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                        L</div>
                                    <div>
                                        <p class="font-bold text-gray-900">이동희</p>
                                    </div>
                                </div>
                            </div>
                            <div class="mt-6 pt-6 border-t border-gray-200">
                                <p class="text-xs text-gray-500 leading-relaxed">
                                    위치 기반 독서 모임 찾기, 커뮤니티 게시판 및 Frontend 전반의 UI/UX를 고도화했습니다.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="py-32 bg-gray-50 border-t border-gray-100 overflow-hidden">
                            <div class="max-w-7xl mx-auto px-6 text-center">
                                <div class="mb-20 reveal-text">
                                    <span class="text-blue-600 font-bold tracking-wider text-sm uppercase mb-2 block">Growth
                                        Path</span>
                                    <h2 class="text-4xl font-black mb-4 text-gray-900">Our Project</h2>
                                    <p class="text-gray-500 font-medium">단순 구현에서 아키텍처 설계로, 끊임없는 기술적 도전의 여정</p>
                                </div>

                                <div class="flex flex-col md:flex-row justify-center items-center gap-8 md:gap-12 relative">

                                    <div
                                        class="hidden md:block absolute top-1/2 left-0 w-full h-1 bg-gradient-to-r from-gray-200 via-green-200 to-orange-200 -translate-y-1/2 z-0 opacity-50">
                                    </div>

                                    <div class="reveal-card relative z-10 w-full max-w-sm" style="transition-delay: 0ms;">
                                        <div
                                            class="bento-box p-8 border-2 border-blue-100 hover:border-blue-300 transition-all duration-300 bg-white group">
                                            <div
                                                class="absolute -top-6 left-1/2 -translate-x-1/2 bg-white px-4 py-1 rounded-full border border-blue-100 text-[10px] font-bold text-blue-400 uppercase tracking-widest shadow-sm">
                                                Mini
                                            </div>
                                            <div
                                                class="w-24 h-24 mx-auto rounded-full bg-blue-50 flex items-center justify-center mb-6 shadow-inner group-hover:scale-110 transition-transform duration-500">
                                                <i data-lucide="gamepad-2" class="w-10 h-10 text-blue-500"></i>
                                            </div>
                                            <h3 class="text-2xl font-bold text-gray-900 mb-6">Omok</h3>

                                            <div class="space-y-4 text-sm">
                                                <div>
                                                    <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Language</p>
                                                    <p
                                                        class="font-bold text-gray-700 bg-gray-100 inline-block px-3 py-1 rounded-lg">
                                                        JDK 1.8</p>
                                                </div>
                                                <div>
                                                    <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Framework</p>
                                                    <p class="font-bold text-gray-700">JSP / Servlet</p>
                                                </div>
                                                <div>
                                                    <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Database</p>
                                                    <p class="font-bold text-gray-700">MariaDB</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="reveal-card relative z-10 w-full max-w-md transform md:-translate-y-4"
                                        style="transition-delay: 200ms;">
                                        <div
                                            class="absolute -inset-1 bg-gradient-to-br from-green-400 to-emerald-400 rounded-[28px] blur opacity-30 animate-pulse">
                                        </div>
                                        <div class="bento-box p-10 border-2 border-green-500 shadow-2xl bg-white relative">
                                            <div
                                                class="absolute -top-6 left-1/2 -translate-x-1/2 bg-green-500 px-6 py-1.5 rounded-full text-[10px] font-black text-white uppercase tracking-widest shadow-lg">
                                                First (Current)
                                            </div>
                                            <div
                                                class="w-28 h-28 mx-auto rounded-full bg-green-50 flex items-center justify-center mb-8 shadow-inner">
                                                <i data-lucide="book-open-check" class="w-12 h-12 text-green-600"></i>
                                            </div>
                                            <h3 class="text-3xl font-black text-green-600 mb-8">SecondHand<br>Books</h3>

                                            <div class="space-y-5 text-base">
                                                <div class="flex items-center justify-between border-b border-gray-100 pb-2">
                                                    <span class="text-[10px] font-bold text-gray-400 uppercase">Language</span>
                                                    <span class="font-bold text-gray-900">Java 17</span>
                                                </div>
                                                <div class="flex items-center justify-between border-b border-gray-100 pb-2">
                                                    <span class="text-[10px] font-bold text-gray-400 uppercase">Framework</span>
                                                    <span class="font-bold text-gray-900">Spring Legacy (JSP)</span>
                                                </div>
                                                <div class="flex items-center justify-between">
                                                    <span class="text-[10px] font-bold text-gray-400 uppercase">Database</span>
                                                    <span class="font-bold text-gray-900">Postgres</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="reveal-card relative z-10 w-full max-w-sm" style="transition-delay: 400ms;">
                                        <div
                                            class="bento-box p-8 border-2 border-orange-100 hover:border-orange-300 transition-all duration-300 bg-white group opacity-80 hover:opacity-100">
                                            <div
                                                class="absolute -top-6 left-1/2 -translate-x-1/2 bg-white px-4 py-1 rounded-full border border-orange-100 text-[10px] font-bold text-orange-400 uppercase tracking-widest shadow-sm">
                                                Second
                                            </div>
                                            <div
                                                class="w-24 h-24 mx-auto rounded-full bg-orange-50 flex items-center justify-center mb-6 shadow-inner group-hover:rotate-12 transition-transform duration-500">
                                                <span class="text-4xl font-bold text-orange-400">?</span>
                                            </div>
                                            <h3 class="text-2xl font-bold text-gray-900 mb-6">Future Project</h3>

                                            <div class="space-y-4 text-sm">
                                                <div>
                                                    <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Language</p>
                                                    <p
                                                        class="font-bold text-gray-700 bg-gray-100 inline-block px-3 py-1 rounded-lg">
                                                        Java 21</p>
                                                </div>
                                                <div>
                                                    <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Framework</p>
                                                    <p class="font-bold text-gray-700">SpringBoot / React</p>
                                                </div>
                                                <div>
                                                    <p class="text-[10px] font-bold text-gray-400 uppercase mb-1">Database</p>
                                                    <p class="font-bold text-gray-700">MySQL</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </section>

            <!-- Project Roadmap Visualizer (Added) -->
            <section class="py-24 bg-slate-50 border-t border-slate-200 overflow-hidden"
                id="roadmap-visualizer-section">
                <div class="max-w-[1440px] mx-auto px-6">
                    <!-- Header -->
                    <div class="flex flex-col md:flex-row justify-between items-center mb-16 reveal-text">
                        <div class="text-center md:text-left mb-6 md:mb-0">
                            <span
                                class="text-indigo-600 font-bold tracking-wider text-sm uppercase mb-2 block">Development
                                Roadmap</span>
                            <h2 class="text-4xl font-black text-slate-800 tracking-tight">2026 Project Master Plan</h2>
                            <p class="text-slate-500 text-sm font-medium mt-2">Jan 05, 2026 - Feb 04, 2026 Schedule</p>
                        </div>
                        <div class="flex gap-4 items-center">
                            <div class="text-right">
                                <div class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Global
                                    Status</div>
                                <div class="text-xs font-bold text-emerald-600">On Track</div>
                            </div>
                            <div
                                class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                                <i data-lucide="check" class="w-4 h-4"></i>
                            </div>
                        </div>
                    </div>

                    <div class="flex flex-col xl:flex-row gap-6 items-start">

                        <!-- Main Roadmap Area: Three Circles -->
                        <div class="flex-1 w-full">
                            <div
                                class="bg-white rounded-[48px] shadow-2xl shadow-slate-200 border border-slate-100 p-8 md:p-12 relative overflow-hidden reveal-card">
                                <!-- Background Decoration -->
                                <div
                                    class="absolute -top-20 -left-20 w-80 h-80 bg-blue-50 rounded-full blur-3xl opacity-30">
                                </div>

                                <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-1 md:gap-2 px-0"
                                    id="roadmap-circles-container">
                                    <!-- Circles will be rendered here by D3 -->
                                </div>
                            </div>
                        </div>

                        <!-- Right Panel: Selected Details -->
                        <aside class="w-full xl:w-[350px] sticky top-[120px] reveal-card"
                            style="transition-delay: 100ms;">
                            <div id="phase-details-panel"
                                class="bg-white rounded-[40px] p-10 shadow-2xl border border-slate-100 min-h-[560px] flex flex-col transition-all duration-500 overflow-hidden relative">
                                <!-- Default State -->
                                <div id="phase-details-empty"
                                    class="flex-1 flex flex-col items-center justify-center text-center p-8">
                                    <div
                                        class="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mb-6 border border-slate-100">
                                        <i data-lucide="map" class="w-8 h-8 text-slate-200"></i>
                                    </div>
                                    <h3 class="text-xl font-black text-slate-800 mb-2 tracking-tight">Step Navigation
                                    </h3>
                                    <p class="text-slate-400 text-sm leading-relaxed max-w-[240px] font-medium">
                                        로드맵의 원형 단계를 탐색하여 각 단계별 상세 과업과 전략을 확인하세요.
                                    </p>
                                </div>

                                <!-- Content State (Hidden by default) -->
                                <div id="phase-details-content"
                                    class="hidden animate-in fade-in slide-in-from-right-4 duration-500">
                                    <div class="mb-6 flex justify-between items-start">
                                        <div id="detail-tag"
                                            class="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-widest text-white">
                                            Phase Details
                                        </div>
                                        <div id="detail-index" class="text-slate-300 font-bold text-2xl">01</div>
                                    </div>

                                    <h2 id="detail-title"
                                        class="text-4xl font-black text-slate-800 mb-2 leading-tight tracking-tight">
                                        Design & Spec
                                    </h2>
                                    <div class="flex items-center gap-2 text-slate-400 font-bold text-sm mb-10">
                                        <i data-lucide="calendar" class="w-4 h-4"></i>
                                        <span id="detail-date">Jan 01 ~ Jan 10</span>
                                    </div>

                                    <div class="space-y-8">
                                        <div>
                                            <h4
                                                class="text-[11px] font-black text-slate-300 uppercase tracking-widest mb-4">
                                                Milestone Checklist</h4>
                                            <div id="detail-tasks" class="space-y-3">
                                                <!-- Tasks injected here -->
                                            </div>
                                        </div>

                                        <div class="p-6 bg-slate-900 rounded-3xl text-white">
                                            <h4
                                                class="text-[10px] font-black text-indigo-400 uppercase tracking-widest mb-2">
                                                Scope
                                                Summary</h4>
                                            <p id="detail-desc"
                                                class="text-sm text-slate-300 leading-relaxed font-medium">
                                                Description goes here...
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </aside>
                    </div>
                </div>

                 <!-- Project Management (GitHub) -->
                            <section class="py-32 bg-gray-50">
                                <div class="max-w-[1440px] mx-auto px-6">
                                    <h2 class="text-4xl font-bold mb-16 text-center reveal-text">Develop : Collaboration & Workflow</h2>

                                    <div class="space-y-24">
                                        <!-- Part 1: Issue & Branch Board -->
                                        <div class="flex flex-col md:flex-row items-center gap-12 reveal-card">
                                            <div class="flex-1 space-y-6">
                                                <div
                                                    class="inline-flex items-center gap-2 px-4 py-2 bg-gray-900 text-white rounded-full text-sm font-bold">
                                                    <i data-lucide="trello" class="w-4 h-4"></i>
                                                    Kanban Board
                                                </div>
                                                <h3 class="text-3xl font-bold text-gray-900">투명한 진행 상황 공유</h3>
                                                <p class="text-gray-500 leading-relaxed text-lg">
                                                    GitHub Projects를 활용하여 모든 기능을 이슈 단위로 관리했습니다.
                                                    Ready -> In Progress -> In Review -> Done 파이프라인을 구축하여
                                                    팀원 간의 진행 상황을 실시간으로 동기화했습니다.
                                                </p>
                                            </div>
                                            <div class="flex-[2] rounded-3xl overflow-hidden shadow-2xl border border-gray-200 transform hover:scale-[1.02] transition-transform duration-500 cursor-pointer"
                                                onclick="openLightbox('${pageContext.request.contextPath}/resources/presentation/img/github_board.png')">
                                                <img src="${pageContext.request.contextPath}/resources/presentation/img/github_board.png"
                                                    alt="GitHub Project Board" class="w-full h-auto">
                                            </div>
                                        </div>

                                        <!-- Part 2: Table View & Labels -->
                                        <div class="flex flex-col md:flex-row-reverse items-center gap-12 reveal-card">
                                            <div class="flex-1 space-y-6">
                                                <div
                                                    class="inline-flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-full text-sm font-bold">
                                                    <i data-lucide="table" class="w-4 h-4"></i>
                                                    Structured Issues
                                                </div>
                                                <h3 class="text-3xl font-bold text-gray-900">체계적인 이슈 트래킹</h3>
                                                <p class="text-gray-500 leading-relaxed text-lg">
                                                    P0(Critical), P1(High) 등 우선순위 라벨링과 [FEAT], [FIX] 헤더 규칙을 통해
                                                    개발의 방향성을 명확히 했습니다. PR과 이슈를 연동하여 코드 변경 사항을 히스토리로 남겼습니다.
                                                </p>
                                            </div>
                                            <div class="flex-[2] rounded-3xl overflow-hidden shadow-2xl border border-gray-200 transform hover:scale-[1.02] transition-transform duration-500 cursor-pointer"
                                                onclick="openLightbox('${pageContext.request.contextPath}/resources/presentation/img/github_table.png')">
                                                <img src="${pageContext.request.contextPath}/resources/presentation/img/github_table.png"
                                                    alt="GitHub Issue Table" class="w-full h-auto">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </section>

                <script>
                    (function () {
                        const PROJECT_DATA = {
                            phases: [
                                {
                                    id: "planning",
                                    title: "Design & Specs",
                                    startDate: new Date("2026-01-05"),
                                    endDate: new Date("2026-01-11"),
                                    description: "ERD Design, API Spec, Page Flow, Wireframes",
                                    color: "#6366f1", // Indigo
                                    details: ["ERD 설계", "API 설계", "페이지 플로우 설계", "목업 페이지 와이어프레임"]
                                },
                                {
                                    id: "development",
                                    title: "Main Development",
                                    startDate: new Date("2026-01-12"),
                                    endDate: new Date("2026-01-25"),
                                    description: "Core Implementation Phase",
                                    color: "#10b981", // Emerald
                                    details: ["기능 개발 및 구현", "프론트엔드/백엔드 통합"]
                                },
                                {
                                    id: "deployment",
                                    title: "QA & Launch",
                                    startDate: new Date("2026-01-26"),
                                    endDate: new Date("2026-02-01"),
                                    description: "Testing & Initial Deployment",
                                    color: "#f59e0b", // Amber
                                    details: ["테스팅", "배포 (Initial Release)"]
                                },
                                {
                                    id: "final",
                                    title: "Final Review",
                                    startDate: new Date("2026-02-02"),
                                    endDate: new Date("2026-02-04"),
                                    description: "Final Testing & Presentation Prep",
                                    color: "#ef4444", // Red
                                    details: ["최종 테스팅", "발표 준비 및 시연"]
                                }
                            ]
                        };

                        // Group phases for circles
                        const circlesData = [
                            { phases: [PROJECT_DATA.phases[0]], title: "Design & Spec", id: "c1" },
                            { phases: [PROJECT_DATA.phases[1]], title: "Main Dev", id: "c2" },
                            { phases: [PROJECT_DATA.phases[2], PROJECT_DATA.phases[3]], title: "QA & Final", id: "c3" }
                        ];

                        function renderRoadmap() {
                            const container = document.getElementById('roadmap-circles-container');
                            if (!container) return;
                            container.innerHTML = ''; // Clear

                            circlesData.forEach((circle, index) => {
                                const wrapper = document.createElement('div');
                                wrapper.className = "flex-1 flex flex-col items-center relative";
                                wrapper.id = `roadmap-circle-${index}`;
                                container.appendChild(wrapper);

                                renderCircle(wrapper, circle.phases, circle.title);

                                // Add connector if not last
                                if (index < circlesData.length - 1) {
                                    const connector = document.createElement('div');
                                    connector.className = "hidden md:block text-slate-300";
                                    connector.innerHTML = `<i data-lucide="chevron-right" class="w-8 h-8"></i>`;
                                    container.appendChild(connector);
                                }
                            });
                        }

                        function renderCircle(wrapper, phases, title) {
                            const size = 800;
                            const width = size;
                            const height = size;
                            const margin = 15;
                            const radius = Math.min(width, height) / 2 - margin;
                            const innerRadius = radius * 0.75;

                            const svg = d3.select(wrapper)
                                .append("svg")
                                .attr("width", size)
                                .attr("height", size)
                                .attr("viewBox", `0 0 \${size} \${size}`)
                                .attr("class", "max-w-full h-auto drop-shadow-sm");

                            const g = svg.append("g")
                                .attr("transform", `translate(\${width / 2}, \${height / 2})`);

                            // Scale
                            const minDate = d3.min(phases, d => d.startDate.getTime()) || 0;
                            const maxDate = d3.max(phases, d => d.endDate.getTime()) || 0;

                            const angleScale = d3.scaleLinear()
                                .domain([minDate, maxDate + (phases.length === 1 ? 86400000 : 0)])
                                .range([0, 2 * Math.PI]);

                            // Outer Ring
                            g.append("circle")
                                .attr("r", radius)
                                .attr("fill", "transparent")
                                .attr("stroke", "#f1f5f9")
                                .attr("stroke-width", 1);

                            // Inner White
                            g.append("circle")
                                .attr("r", innerRadius)
                                .attr("fill", "#ffffff")
                                .attr("filter", "drop-shadow(0px 4px 6px rgba(0,0,0,0.05))");

                            // Arcs
                            const arcGenerator = d3.arc()
                                .innerRadius(innerRadius)
                                .outerRadius(radius)
                                .startAngle(d => angleScale(d.startDate.getTime()))
                                .endAngle(d => angleScale(d.endDate.getTime() + 86400000))
                                .cornerRadius(phases.length > 1 ? size / 40 : 0)
                                .padAngle(0);

                            g.selectAll(".phase-arc")
                                .data(phases)
                                .enter()
                                .append("path")
                                .attr("class", "phase-arc cursor-pointer transition-all duration-300")
                                .attr("d", arcGenerator)
                                .attr("fill", d => d.color)
                                .attr("opacity", 0.85)
                                .on("mouseenter", function (event, d) {
                                    d3.select(this).attr("opacity", 1).attr("transform", "scale(1.02)");
                                    updateDetailsPanel(d);
                                })
                                .on("mouseleave", function (event, d) {
                                    d3.select(this).attr("opacity", 0.85).attr("transform", "scale(1)");
                                    // Optional: updateDetailsPanel(null); // Keep last selected or clear?
                                    // Implementation choice: sticky selection or clear on leave?
                                    // Original React code clears on leave, but has 'selectedPhase' state.
                                    // Let's implement click to select for mobile friendliness, or just hover.
                                    // React code: onPhaseSelect(d) on mouseenter.
                                });

                            // Text Container
                            const textContainer = g.append("g")
                                .attr("text-anchor", "middle")
                                .style("pointer-events", "none");

                            // Title
                            textContainer.append("text")
                                .attr("y", -innerRadius * 0.5)
                                .attr("class", "fill-slate-400 font-bold text-4xl uppercase tracking-widest")
                                .text(title);

                            // Date Range
                            const dateStr = phases.length === 1
                                ? `\${phases[0].startDate.getMonth() + 1}/\${phases[0].startDate.getDate()} ~ \${phases[0].endDate.getMonth() + 1}/\${phases[0].endDate.getDate()}`
                                : `\${phases[0].startDate.getMonth() + 1}/\${phases[0].startDate.getDate()} ~ \${phases[phases.length - 1].endDate.getMonth() + 1}/\${phases[phases.length - 1].endDate.getDate()}`;

                            textContainer.append("text")
                                .attr("y", -innerRadius * 0.2)
                                .attr("class", "fill-slate-800 font-black text-6xl")
                                .text(dateStr);

                            // Tasks list (Top 4)
                            const allTasks = phases.flatMap(p => p.details);
                            const displayTasks = allTasks.slice(0, 4);

                            const taskGroup = textContainer.append("g")
                                .attr("transform", `translate(0, \${innerRadius * 0.1})`);

                            displayTasks.forEach((task, i) => {
                                taskGroup.append("text")
                                    .attr("y", i * 36)
                                    .attr("class", "fill-slate-600 font-bold text-3xl")
                                    .text(`• \${task}`);
                            });

                            if (allTasks.length > 4) {
                                taskGroup.append("text")
                                    .attr("y", displayTasks.length * 36)
                                    .attr("class", "fill-slate-300 italic text-2xl")
                                    .text(`+ \${allTasks.length - 4} more tasks`);
                            }
                        }

                        function updateDetailsPanel(phase) {
                            const emptyState = document.getElementById('phase-details-empty');
                            const contentState = document.getElementById('phase-details-content');

                            if (!phase) {
                                emptyState.classList.remove('hidden');
                                contentState.classList.add('hidden');
                                return;
                            }

                            emptyState.classList.add('hidden');
                            contentState.classList.remove('hidden');

                            // Update Content
                            const overallIndex = PROJECT_DATA.phases.findIndex(p => p.id === phase.id);

                            document.getElementById('detail-tag').style.backgroundColor = phase.color;
                            document.getElementById('detail-index').innerText = `0\${overallIndex + 1}`;
                            document.getElementById('detail-title').innerText = phase.title;

                            const dateStr = `\${phase.startDate.getMonth() + 1}월 \${phase.startDate.getDate()}일 ~ \${phase.endDate.getMonth() + 1}월 \${phase.endDate.getDate()}일`;
                            document.getElementById('detail-date').innerText = dateStr;
                            document.getElementById('detail-desc').innerText = phase.description;

                            // Update Tasks
                            const taskContainer = document.getElementById('detail-tasks');
                            taskContainer.innerHTML = phase.details.map(task => `
                                            <div class="flex items-center gap-4 p-4 bg-slate-50 rounded-2xl border border-slate-100 hover:border-indigo-200 hover:bg-white transition-all group">
                                              <div class="w-5 h-5 rounded-full border-2 border-slate-200 flex items-center justify-center group-hover:border-indigo-400 transition-colors">
                                                <div class="w-2 h-2 rounded-full bg-indigo-400 opacity-0 group-hover:opacity-100 transition-opacity"></div>
                                              </div>
                                              <span class="text-sm font-bold text-slate-700">\${task}</span>
                                            </div>
                                        `).join('');
                        }

                        // Initialize
                        document.addEventListener('DOMContentLoaded', () => {
                            if (typeof d3 !== 'undefined') {
                                renderRoadmap();
                            } else {
                                window.addEventListener('load', () => {
                                    if (typeof d3 !== 'undefined') renderRoadmap();
                                });
                            }
                        });


                    })();
                </script>
            </section>

            <section class="py-32 bg-gray-50 border-t border-gray-200">
                <div class="max-w-[1440px] mx-auto px-6">
                    <h2 class="text-4xl font-bold mb-16 text-center reveal-text">Testing : Quality Assurance</h2>

                    <div class="space-y-24">

                        <div class="flex flex-col md:flex-row items-center gap-12 reveal-card">
                            <div class="flex-1 space-y-6">
                                <div
                                    class="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-full text-sm font-bold">
                                    <i data-lucide="clipboard-check" class="w-4 h-4"></i>
                                    System Validation
                                </div>
                                <h3 class="text-3xl font-bold text-gray-900">기능 테스트 리포트</h3>
                                <p class="text-gray-500 leading-relaxed text-lg">
                                    단위 기능부터 통합 시나리오까지, 엑셀을 활용해 테스트 케이스(TC)를 체계적으로 관리했습니다.
                                    성공/실패 여부를 명확히 기록하고, 발견된 버그의 수정 현황을 추적하여 배포 전 시스템의 안정성을 확보했습니다.
                                </p>
                            </div>
                            <div class="flex-[2] rounded-3xl overflow-hidden shadow-2xl border border-gray-200 transform hover:scale-[1.02] transition-transform duration-500 cursor-pointer group interactive-3d"
                                onclick="openLightbox('${pageContext.request.contextPath}/resources/presentation/img/excel_test_func.jpg')">
                                <div class="relative">
                                    <img src="${pageContext.request.contextPath}/resources/presentation/img/excel_test_func.jpg"
                                        alt="Functional Test Report" class="w-full h-auto object-cover">
                                    <div
                                        class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors flex items-center justify-center">
                                        <i data-lucide="zoom-in"
                                            class="w-12 h-12 text-white opacity-0 group-hover:opacity-100 transition-opacity drop-shadow-lg"></i>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="flex flex-col md:flex-row-reverse items-center gap-12 reveal-card">
                            <div class="flex-1 space-y-6">
                                <div
                                    class="inline-flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-full text-sm font-bold">
                                    <i data-lucide="monitor-smartphone" class="w-4 h-4"></i>
                                    User Experience
                                </div>
                                <h3 class="text-3xl font-bold text-gray-900">UI/UX 개선 내역</h3>
                                <p class="text-gray-500 leading-relaxed text-lg">
                                    사용자 관점에서의 시나리오 테스트를 수행하여 사용성을 점검했습니다.
                                    반응형 레이아웃, 인터랙션 피드백, 에러 메시지 노출 등 디테일한 UI 이슈를 기록하고 개선하여 직관적이고 완성도 높은 경험을 제공합니다.
                                </p>
                            </div>
                            <div class="flex-[2] rounded-3xl overflow-hidden shadow-2xl border border-gray-200 transform hover:scale-[1.02] transition-transform duration-500 cursor-pointer group interactive-3d"
                                onclick="openLightbox('${pageContext.request.contextPath}/resources/presentation/img/excel_test_uiux.jpg')">
                                <div class="relative">
                                    <img src="${pageContext.request.contextPath}/resources/presentation/img/excel_test_uiux.jpg"
                                        alt="UI/UX Test Report" class="w-full h-auto object-cover">
                                    <div
                                        class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors flex items-center justify-center">
                                        <i data-lucide="zoom-in"
                                            class="w-12 h-12 text-white opacity-0 group-hover:opacity-100 transition-opacity drop-shadow-lg"></i>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </section>

            <!-- Triple Circle Comparison Layout -->
            <div class="relative mb-32">
                <!-- Desktop Connection Line -->
                <div> <br> <br> <br> <br> <br> <br> <br> </div>
                <div
                    class="hidden lg:block absolute top-1/2 left-1/4 right-1/4 h-0.5 border-t-2 border-dashed border-slate-200 -translate-y-1/2 z-0">
                </div>
                <div><h2 class="text-4xl font-bold mb-16 text-center reveal-text">Why SecondHand Books?</h2></div>

                <div class="grid grid-cols-1 lg:grid-cols-3 gap-12 items-center relative z-10">

                    <!-- Left: Danggeun / Joonggonara -->
                    <div class="flex flex-col items-center reveal-card" style="transition-delay: 100ms;">
                        <div
                            class="w-full max-w-[360px] aspect-square rounded-[40px] bg-white border border-slate-100 shadow-2xl p-10 flex flex-col items-center justify-center text-center hover:scale-105 transition-transform duration-500">
                            <div
                                class="w-16 h-16 bg-orange-50 rounded-2xl flex items-center justify-center text-orange-500 mb-6 shadow-inner">
                                <span class="text-3xl">🥕</span>
                            </div>
                            <h4 class="text-xl font-bold mb-4 text-slate-800">당근 / 중고나라</h4>
                            <div class="space-y-4 w-full">
                                <div class="text-sm p-3 bg-slate-50 rounded-xl">
                                    <span class="block font-bold text-emerald-600 mb-1">장점</span>
                                    <p class="text-slate-500 leading-tight text-xs">최저가, 수수료 없음</p>
                                </div>
                                <div class="text-sm p-3 bg-slate-50 rounded-xl">
                                    <span class="block font-bold text-red-400 mb-1">단점</span>
                                    <p class="text-slate-500 leading-tight text-xs">상태 불확실, 사기 위험</p>
                                </div>
                            </div>
                        </div>
                        <p class="mt-8 text-slate-400 font-bold text-sm tracking-wide">"싸지만 불안한 거래"</p>
                    </div>

                    <!-- Middle: SecondHand Books (THE SYNERGY) -->
                    <div class="flex flex-col items-center reveal-card relative z-20"
                        style="transition-delay: 200ms;">
                        <div
                            class="w-full max-w-[420px] aspect-square rounded-[48px] bg-gradient-to-br from-indigo-600 to-blue-700 shadow-2xl shadow-indigo-200 p-12 flex flex-col items-center justify-center text-center relative overflow-hidden group hover:scale-105 transition-transform duration-500">
                            <!-- Animated light effect -->
                            <div
                                class="absolute inset-0 bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none">
                            </div>

                            <div class="relative z-10 flex flex-col items-center">
                                <div
                                    class="inline-block px-4 py-1.5 bg-white/20 backdrop-blur-md text-white text-[10px] font-black rounded-full mb-8 tracking-widest uppercase border border-white/10">
                                    The Perfect Bridge</div>
                                <div
                                    class="w-20 h-20 bg-white rounded-3xl flex items-center justify-center text-indigo-600 mb-6 shadow-xl transform group-hover:rotate-12 transition-transform duration-500">
                                    <span class="text-4xl font-black">S</span>
                                </div>
                                <h4 class="text-2xl font-black mb-4 text-white tracking-tight">SecondHand Books
                                </h4>
                                <p class="text-indigo-100 text-sm leading-relaxed font-medium">
                                    기업형 검수 시스템으로<br>
                                    개인간 거래의 혁신을 실현
                                </p>
                                <div class="mt-8 flex gap-2">
                                    <div class="w-1.5 h-1.5 rounded-full bg-white animate-pulse"></div>
                                    <div class="w-1.5 h-1.5 rounded-full bg-white/50 animate-pulse delay-75">
                                    </div>
                                    <div class="w-1.5 h-1.5 rounded-full bg-white/20 animate-pulse delay-150">
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <!-- Right: Aladin / Corporate -->
                    <div class="flex flex-col items-center reveal-card" style="transition-delay: 300ms;">
                        <div
                            class="w-full max-w-[360px] aspect-square rounded-[40px] bg-white border border-slate-100 shadow-2xl p-10 flex flex-col items-center justify-center text-center hover:scale-105 transition-transform duration-500">
                            <div
                                class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-500 mb-6 shadow-inner">
                                <i data-lucide="book-open" class="w-8 h-8"></i>
                            </div>
                            <h4 class="text-xl font-bold mb-4 text-slate-800">알라딘 / 예스24</h4>
                            <div class="space-y-4 w-full">
                                <div class="text-sm p-3 bg-slate-50 rounded-xl">
                                    <span class="block font-bold text-emerald-600 mb-1">장점</span>
                                    <p class="text-slate-500 leading-tight text-xs">확실한 품질, 배송</p>
                                </div>
                                <div class="text-sm p-3 bg-slate-50 rounded-xl">
                                    <span class="block font-bold text-red-400 mb-1">단점</span>
                                    <p class="text-slate-500 leading-tight text-xs">비싼 가격, 매입 제한</p>
                                </div>
                            </div>
                        </div>
                        <p class="mt-8 text-slate-400 font-bold text-sm tracking-wide">"편하지만 비싼 구매"</p>
                    </div>

                </div>
            </div>

            <!-- MOTIVATION Section -->
            <section class="py-32 bg-gray-50 relative overflow-hidden">
                <div class="max-w-5xl mx-auto px-6 relative z-10">
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                        <div class="bento-box p-8 reveal-card">
                            <div
                                class="w-12 h-12 bg-red-50 text-red-500 rounded-2xl flex items-center justify-center mb-6">
                                <i data-lucide="keyboard" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-bold mb-3">등록의 정확성</h3>
                            <p class="text-gray-500 leading-relaxed text-sm">
                                책 제목, 저자, 출판사 검색 시 Kakao 도서 API를 이용하여 구매자와 판매자에게 높은 정확도의 경험을 제공합니다.
                            </p>
                        </div>
                        <div class="bento-box p-8 reveal-card" style="transition-delay: 100ms;">
                            <div
                                class="w-12 h-12 bg-orange-50 text-orange-500 rounded-2xl flex items-center justify-center mb-6">
                                <i data-lucide="alert-triangle" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-bold mb-3">거래의 안정성</h3>
                            <p class="text-gray-500 leading-relaxed text-sm">
                                "돈을 보냈는데 물건이 안 오면?"이라는 걱정이 없도록, 비대면 중고 거래의 진입 장벽을 허물고 신뢰할 수 있는 환경을 만듭니다.
                            </p>
                        </div>
                        <div class="bento-box p-8 reveal-card" style="transition-delay: 200ms;">
                            <div
                                class="w-12 h-12 bg-gray-100 text-gray-600 rounded-2xl flex items-center justify-center mb-6">
                                <i data-lucide="users" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-bold mb-3">독서의 연결성</h3>
                            <p class="text-gray-500 leading-relaxed text-sm">
                                단순 거래를 넘어 취향을 공유하는 커뮤니티를 통해, 혼자 읽는 독서에서 함께 나누는 즐거움으로 경험을 확장합니다.
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            <section class="py-32 bg-gray-50 border-t border-gray-100">
                <div class="max-w-3xl mx-auto px-6">
                    <div class="text-center mb-16 reveal-text">
                        <span class="text-blue-600 font-bold tracking-wider text-sm uppercase mb-2 block">User
                            Journey</span>
                        <h2 class="text-4xl font-black mb-4 leading-tight tracking-tight text-gray-900">
                            거래 <span class="text-gradient">Flow</span>
                        </h2>
                        <p class="text-gray-500 font-medium text-lg">
                            사용자 관점의 중고책 거래 및 결제 흐름도
                        </p>
                    </div>

                    <div class="w-full max-w-lg mx-auto space-y-2">

                        <div class="reveal-card">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="edit-3" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-blue-600 uppercase tracking-widest block mb-1">Step
                                        01</span>
                                    <h3 class="text-xl font-bold text-gray-900">판매글 등록</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">카카오 REST API를 활용한 도서 정보 자동 입력
                                        및 판매글 작성</p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 100ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <div class="reveal-card" style="transition-delay: 200ms;">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="eye" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-blue-600 uppercase tracking-widest block mb-1">Step
                                        02</span>
                                    <h3 class="text-xl font-bold text-gray-900">판매글 확인</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">구매자가 등록된 판매글 목록 및 상세 정보 열람</p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 300ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <div class="reveal-card" style="transition-delay: 400ms;">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="message-circle" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-blue-600 uppercase tracking-widest block mb-1">Step
                                        03</span>
                                    <h3 class="text-xl font-bold text-gray-900">채팅 (1:1)</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">판매자와 구매자 간의 실시간 대화 및 거래 조율</p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-4" style="transition-delay: 500ms;">
                            <div
                                class="bg-gray-200 rounded-full px-3 py-1 text-[10px] font-bold text-gray-500 inline-block mb-2">
                                거래 방식 선택
                            </div>
                            <div class="flex justify-center gap-32 text-gray-300">
                                <i data-lucide="corner-left-down" class="w-6 h-6"></i>
                                <i data-lucide="corner-right-down" class="w-6 h-6"></i>
                            </div>
                        </div>

                        <div class="reveal-card grid grid-cols-2 gap-4" style="transition-delay: 600ms;">
                            <div
                                class="bento-box p-5 flex flex-col items-center text-center hover:border-blue-200 hover:bg-blue-50/30 transition-colors">
                                <div
                                    class="w-10 h-10 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-3">
                                    <i data-lucide="shield-check" class="w-5 h-5"></i>
                                </div>
                                <span class="text-[10px] font-bold text-blue-600 uppercase mb-1">Option A</span>
                                <h3 class="font-bold text-gray-900">안전결제</h3>
                                <p class="text-xs text-gray-500 mt-2 leading-relaxed">
                                    <strong>TossPay</strong> 연동<br>
                                </p>
                            </div>

                            <div
                                class="bento-box p-5 flex flex-col items-center text-center hover:border-green-200 hover:bg-green-50/30 transition-colors">
                                <div
                                    class="w-10 h-10 bg-green-100 text-green-600 rounded-xl flex items-center justify-center mb-3">
                                    <i data-lucide="banknote" class="w-5 h-5"></i>
                                </div>
                                <span class="text-[10px] font-bold text-green-600 uppercase mb-1">Option B</span>
                                <h3 class="font-bold text-gray-900">계좌이체</h3>
                                <p class="text-xs text-gray-500 mt-2 leading-relaxed">
                                    채팅방 입금 확인<br>
                                    (판매자 수동 확인)
                                </p>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 700ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <div class="reveal-card" style="transition-delay: 800ms;">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="list-checks" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-blue-600 uppercase tracking-widest block mb-1">Step
                                        04</span>
                                    <h3 class="text-xl font-bold text-gray-900">판매/구매 내역 확인</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">거래 완료 상태 변경 및 내역 리스트 조회</p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 900ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <div class="reveal-card" style="transition-delay: 1000ms;">
                            <div class="bento-box p-6 flex items-center gap-6 border-blue-100 bg-blue-50/10">
                                <div
                                    class="w-12 h-12 bg-green-100 text-green-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="check-circle" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-green-600 uppercase tracking-widest block mb-1">Final
                                        Step</span>
                                    <h3 class="text-xl font-bold text-gray-900">구매 확정</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">구매자 확정 처리 시 판매자에게 대금 지급 <br>
                                        (미확정 시 15일 후 자동 확정)</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- BookClub User Journey Section -->
            <section class="py-32 bg-white border-t border-gray-100">
                <div class="max-w-3xl mx-auto px-6">
                    <div class="text-center mb-16 reveal-text">
                        <span class="text-purple-600 font-bold tracking-wider text-sm uppercase mb-2 block">BookClub
                            Journey</span>
                        <h2 class="text-4xl font-black mb-4 leading-tight tracking-tight text-gray-900">
                            독서 모임 <span class="text-gradient">Flow</span>
                        </h2>
                        <p class="text-gray-500 font-medium text-lg">
                            검색부터 가입, 활동, 탈퇴까지의 전체 프로세스
                        </p>
                    </div>

                    <div class="w-full max-w-lg mx-auto space-y-2">

                        <!-- Step 1: Search -->
                        <div class="reveal-card">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-purple-50 text-purple-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="search" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-purple-600 uppercase tracking-widest block mb-1">Step
                                        01</span>
                                    <h3 class="text-xl font-bold text-gray-900">모임 탐색</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">검색(키워드) + 정렬(최신/활동) + 페이지네이션
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 100ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <!-- Step 2: Join Request -->
                        <div class="reveal-card" style="transition-delay: 200ms;">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-purple-50 text-purple-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="user-plus" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-purple-600 uppercase tracking-widest block mb-1">Step
                                        02</span>
                                    <h3 class="text-xl font-bold text-gray-900">가입 신청</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">신청 상태는 <strong
                                            class="text-amber-600">대기(WAIT)</strong>로 저장</p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 300ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <!-- Step 3: Approval Decision -->
                        <div class="reveal-card" style="transition-delay: 400ms;">
                            <div class="bento-box p-6 flex items-center gap-6 bg-purple-50/20">
                                <div
                                    class="w-12 h-12 bg-purple-50 text-purple-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="user-check" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-purple-600 uppercase tracking-widest block mb-1">Step
                                        03</span>
                                    <h3 class="text-xl font-bold text-gray-900">모임장 승인/거절</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">승인 시 멤버 <strong
                                            class="text-green-600">JOINED</strong> 확정</p>
                                </div>
                            </div>
                        </div>

                        <!-- Concurrency Protection Highlight -->
                        <div class="reveal-card" style="transition-delay: 450ms;">
                            <div
                                class="px-4 py-3 bg-gradient-to-r from-red-50 to-orange-50 rounded-xl border-l-4 border-red-400">
                                <div class="flex items-center gap-3">
                                    <i data-lucide="shield-alert" class="w-5 h-5 text-red-600 shrink-0"></i>
                                    <div>
                                        <p class="text-xs font-bold text-red-700 mb-1">동시성 보호 (비관적 락)</p>
                                        <p class="text-xs text-red-600">가입 승인 시 정원 초과 방지 - <code
                                                class="bg-red-100 px-1 rounded text-[10px]">FOR UPDATE</code></p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 500ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <!-- Step 4: Activity -->
                        <div class="reveal-card" style="transition-delay: 600ms;">
                            <div class="bento-box p-6 flex items-center gap-6">
                                <div
                                    class="w-12 h-12 bg-purple-50 text-purple-600 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="users" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-purple-600 uppercase tracking-widest block mb-1">Step
                                        04</span>
                                    <h3 class="text-xl font-bold text-gray-900">활동 / 탈퇴</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">
                                        탈퇴는 <strong class="text-gray-600">LEFT</strong>, 강퇴는 <strong
                                            class="text-red-600">KICKED</strong>로 상태 관리
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div class="reveal-card text-center py-2" style="transition-delay: 700ms;">
                            <i data-lucide="arrow-down" class="w-6 h-6 text-gray-300 mx-auto"></i>
                        </div>

                        <!-- Step 5: Owner Leave (Special Case) -->
                        <div class="reveal-card" style="transition-delay: 800ms;">
                            <div class="bento-box p-6 flex items-center gap-6 border-purple-200 bg-purple-50/30">
                                <div
                                    class="w-12 h-12 bg-purple-100 text-purple-700 rounded-2xl flex items-center justify-center shrink-0">
                                    <i data-lucide="crown" class="w-6 h-6"></i>
                                </div>
                                <div>
                                    <span
                                        class="text-[10px] font-bold text-purple-700 uppercase tracking-widest block mb-1">Special
                                        Case</span>
                                    <h3 class="text-xl font-bold text-gray-900">모임장 탈퇴</h3>
                                    <p class="text-sm text-gray-500 mt-1 leading-relaxed">
                                        멤버 있으면 <strong class="text-blue-600">자동 승계</strong>, 없으면 <strong
                                            class="text-red-600">모임 종료</strong> (Soft Delete)
                                    </p>
                                </div>
                            </div>
                        </div>

                        <!-- Technical Implementation Note -->
                        <div class="reveal-card mt-8" style="transition-delay: 900ms;">
                            <div
                                class="p-6 bg-gradient-to-br from-purple-50 via-indigo-50 to-blue-50 rounded-2xl border border-purple-100">
                                <h4 class="text-sm font-bold text-purple-900 mb-4 flex items-center gap-2">
                                    <i data-lucide="cpu" class="w-4 h-4"></i>
                                    핵심 기술 구현
                                </h4>
                                <div class="grid grid-cols-1 gap-3">
                                    <div class="flex items-start gap-3 bg-white p-3 rounded-xl">
                                        <div
                                            class="w-8 h-8 bg-red-100 text-red-600 rounded-lg flex items-center justify-center shrink-0">
                                            <i data-lucide="lock" class="w-4 h-4"></i>
                                        </div>
                                        <div>
                                            <p class="text-xs font-bold text-gray-900">비관적 락 (FOR UPDATE)</p>
                                            <p class="text-xs text-gray-600 mt-1">가입 승인 시 정원 초과 방지</p>
                                        </div>
                                    </div>
                                    <div class="flex items-start gap-3 bg-white p-3 rounded-xl">
                                        <div
                                            class="w-8 h-8 bg-blue-100 text-blue-600 rounded-lg flex items-center justify-center shrink-0">
                                            <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                                        </div>
                                        <div>
                                            <p class="text-xs font-bold text-gray-900">모임장 승계 로직</p>
                                            <p class="text-xs text-gray-600 mt-1">탈퇴 시 가장 오래된 멤버에게 자동 양도</p>
                                        </div>
                                    </div>
                                    <div class="flex items-start gap-3 bg-white p-3 rounded-xl">
                                        <div
                                            class="w-8 h-8 bg-purple-100 text-purple-600 rounded-lg flex items-center justify-center shrink-0">
                                            <i data-lucide="database" class="w-4 h-4"></i>
                                        </div>
                                        <div>
                                            <p class="text-xs font-bold text-gray-900">Soft Delete</p>
                                            <p class="text-xs text-gray-600 mt-1">멤버 없는 모임은 deleted_at 타임스탬프로 관리</p>
                                        </div>
                                    </div>
                                    <div class="flex items-start gap-3 bg-white p-3 rounded-xl">
                                        <div
                                            class="w-8 h-8 bg-green-100 text-green-600 rounded-lg flex items-center justify-center shrink-0">
                                            <i data-lucide="image" class="w-4 h-4"></i>
                                        </div>
                                        <div>
                                            <p class="text-xs font-bold text-gray-900">이미지 수정 <code class="bg-gray-100 px-1 rounded text-[10px]">updateBookClub()</code></p>
                                            <p class="text-xs text-gray-600 mt-1">새 이미지 업로드 후 기존 이미지 안전 삭제 <code class="bg-green-100 px-1 rounded text-[10px]">@Transactional afterCommit()</code></p>
                                        </div>
                                    </div>
                                    <div class="flex items-start gap-3 bg-white p-3 rounded-xl">
                                        <div
                                            class="w-8 h-8 bg-orange-100 text-orange-600 rounded-lg flex items-center justify-center shrink-0">
                                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                                        </div>
                                        <div>
                                            <p class="text-xs font-bold text-gray-900">게시글 삭제 <code class="bg-gray-100 px-1 rounded text-[10px]">deleteBoardPost()</code></p>
                                            <p class="text-xs text-gray-600 mt-1">Soft Delete 성공 시에만 첨부 이미지 제거 <code class="bg-orange-100 px-1 rounded text-[10px]">@Transactional</code></p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- TECHNICAL DEEP DIVE Section (Light Mode Redesign) -->

            <section class="py-32 px-6 bg-white border-t border-gray-100">
                <div class="max-w-7xl mx-auto">
                    <div class="text-center mb-20 reveal-text">
                        <span class="text-blue-600 font-bold tracking-wider text-sm uppercase mb-2 block">Technical Deep
                            Dive</span>
                        <h2 class="text-5xl font-bold text-gray-900">Non-Blocking I/O</h2>
                    </div>

                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
                        <!-- Legacy Card -->
                        <div class="reveal-card">
                            <div
                                class="bg-white p-8 rounded-3xl border border-gray-200 shadow-xl mb-8 relative overflow-hidden group hover:-translate-y-1 transition-all duration-300">
                                <div
                                    class="absolute top-0 right-0 p-6 opacity-10 font-black text-9xl text-gray-300 pointer-events-none select-none">
                                    BIO
                                </div>
                                <div class="flex items-center justify-between mb-6 relative z-10">
                                    <h3 class="text-2xl font-bold text-gray-800">Legacy: RestTemplate</h3>
                                    <span
                                        class="px-3 py-1 bg-red-100 text-red-600 text-xs font-bold rounded-full border border-red-200">Blocking</span>
                                </div>
                                <p class="text-gray-500 leading-relaxed mb-6 relative z-10">
                                    전통적인 동기(Synchronous) 방식입니다. 외부 API 요청 시 응답이 올 때까지 스레드가 대기(Block)합니다.
                                    대량 트래픽 발생 시 스레드 풀 고갈(Thread Pool Exhaustion)로 이어질 수 있습니다.
                                </p>
                                <div
                                    class="font-mono text-xs text-gray-400 bg-gray-50 p-4 rounded-xl border border-gray-100 relative z-10">
                                    Thread-1: Wait... (Blocked 3s)<br>
                                    Thread-2: Wait... (Blocked 3s)<br>
                                    <span class="text-red-500 font-bold">Error: Connection Timeout</span>
                                </div>
                            </div>
                        </div>

                        <!-- Modern Card -->
                        <div class="reveal-card relative">
                            <div
                                class="absolute -inset-1 bg-gradient-to-r from-blue-400 to-cyan-400 rounded-3xl opacity-20 blur-xl">
                            </div>
                            <div
                                class="relative bg-white p-8 rounded-3xl border border-blue-100 shadow-2xl group hover:-translate-y-1 transition-all duration-300">
                                <div
                                    class="absolute top-0 right-0 p-6 opacity-5 font-black text-9xl text-blue-500 pointer-events-none select-none">
                                    NIO
                                </div>
                                <div class="flex items-center justify-between mb-6 relative z-10">
                                    <h3 class="text-2xl font-bold text-gray-900">Adoption: WebClient</h3>
                                    <span
                                        class="px-3 py-1 bg-blue-100 text-blue-600 text-xs font-bold rounded-full border border-blue-200">Non-Blocking</span>
                                </div>
                                <p class="text-gray-600 leading-relaxed mb-6 relative z-10">
                                    Spring WebFlux 기반의 비동기 클라이언트입니다. Event Loop 방식을 사용하여 단일 스레드로도 수많은 동시 요청을 효율적으로
                                    처리합니다.
                                </p>
                                <div class="code-window relative z-10 shadow-lg">
                                    <div class="code-header bg-gray-800 border-gray-700">
                                        <div class="dot red"></div>
                                        <div class="dot yellow"></div>
                                        <div class="dot green"></div>
                                        <span class="ml-2 text-xs text-gray-400">ReactiveStream.java</span>
                                    </div>
                                    <div class="p-4 text-xs font-mono text-blue-300 bg-[#1e1e1e]">
                                        <pre>
webClient.get().uri(url)
    .retrieve()
    .bodyToMono(String.class)
    .subscribe(); <span class="text-green-400">// Async Callback</span>
</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>







<!-- Query Optimization Section -->
<section class="py-32 bg-white">
    <div class="max-w-7xl mx-auto px-6">
        <h3 class="text-2xl font-bold mb-12 pl-4 border-l-4 border-green-500">
            Query Optimization
        </h3>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

            <!-- Card 1 -->
            <div class="p-8 rounded-2xl shadow-xl border border-gray-200 hover:shadow-2xl transition cursor-pointer"
                 onclick="openDiffModal('main')">
                <div class="flex justify-between items-start mb-6">
                    <div>
                        <h4 class="text-xl font-bold text-gray-900">메인 페이지 조회</h4>
                        <p class="mt-1 font-mono text-xs text-gray-500">
                            Mapper ID: findAllWithPaging
                        </p>
                    </div>
                    <span
                        class="inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold bg-green-100 text-green-800">
                        +14.6%
                    </span>
                </div>

                <div class="grid grid-cols-2 gap-6 mb-6">
                    <div>
                        <p class="text-xs text-gray-400">기존 성능</p>
                        <p class="text-2xl font-bold text-red-500">150.4 ms</p>
                    </div>
                    <div>
                        <p class="text-xs text-gray-400">개선 성능</p>
                        <p class="text-2xl font-bold text-blue-600">128.4 ms</p>
                    </div>
                </div>

                <div class="pt-4 border-t text-sm text-gray-600">
                    <span class="font-medium">개선 포인트</span><br />
                    서브쿼리 제거 (SELECT COUNT → LEFT JOIN)
                </div>
            </div>

            <!-- Card 2 -->
            <div class="p-8 rounded-2xl shadow-xl border border-gray-200 hover:shadow-2xl transition cursor-pointer"
                 onclick="openDiffModal('payment')">
                <div class="flex justify-between items-start mb-6">
                    <div>
                        <h4 class="text-xl font-bold text-gray-900">결제창 진입</h4>
                        <p class="mt-1 font-mono text-xs text-gray-500">
                            Mapper ID: getPaymentCheckInfo
                        </p>
                    </div>
                    <span
                        class="inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold bg-green-100 text-green-800">
                        +21%
                    </span>
                </div>

                <div class="grid grid-cols-2 gap-6 mb-6">
                    <div>
                        <p class="text-xs text-gray-400">기존 성능</p>
                        <p class="text-2xl font-bold text-red-500">168.0 ms</p>
                    </div>
                    <div>
                        <p class="text-xs text-gray-400">개선 성능</p>
                        <p class="text-2xl font-bold text-blue-600">132.0 ms</p>
                    </div>
                </div>

                <div class="pt-4 border-t text-sm text-gray-600">
                    <span class="font-medium">개선 포인트</span><br />
                    3회 Select → 1회 통합 조회
                </div>
            </div>

        </div>
    </div>
</section>



            <!-- Redis Cache Strategy Detail (AJAX) -->
            <section class="py-32 bg-gray-50 border-t border-gray-100" id="redis-visualizer-section">
                <div class="max-w-7xl mx-auto px-6">
                    <h3 class="text-3xl font-bold mb-8 text-center text-gray-900 reveal-text">Redis Caching Optimization</h3>
                    <p class="text-center text-gray-500 mb-12 max-w-2xl mx-auto">
                        Spring Cache와 AOP를 활용하여 구현된 고성능 캐싱 레이어의 동작 원리를 시각화했습니다.
                    </p>
                    <div id="redis-ajax-container" class="min-h-[600px] flex items-center justify-center">
                        <div class="flex flex-col items-center gap-4 text-gray-500">
                            <i data-lucide="loader-2" class="w-10 h-10 animate-spin"></i>
                            <span class="text-sm font-mono">Loading Redis Strategy Data...</span>
                        </div>
                    </div>
            </section>

            <!-- Dynamic Architecture Visualizer (AJAX) -->
            <section class="py-32 bg-white border-t border-gray-100 relative overflow-hidden"
                id="infra-visualizer-section">
                <!-- Background Glow -->
                <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full max-w-7xl pointer-events-none">
                    <div class="absolute top-0 left-1/4 w-[500px] h-[500px] bg-blue-500/10 rounded-full blur-[100px]">
                    </div>
                    <div
                        class="absolute bottom-0 right-1/4 w-[500px] h-[500px] bg-purple-500/10 rounded-full blur-[100px]">
                    </div>
                </div>

                <div class="max-w-7xl mx-auto px-6 relative z-10">
                    <div id="infra-ajax-container" class="min-h-[600px] flex items-center justify-center">
                        <div class="flex flex-col items-center gap-4 text-gray-500">
                            <i data-lucide="loader-2" class="w-10 h-10 animate-spin"></i>
                            <span class="text-sm font-mono">Loading Infrastructure Data...</span>
                        </div>
                    </div>
                </div>
            </section>



            <!-- Load Test Visualizer (Ported from React App) -->
            <section class="py-24 bg-slate-50 border-t border-slate-200 relative overflow-hidden"
                id="loadtest-visualizer-section">

                <div class="max-w-7xl mx-auto px-6 relative z-10">
                    <!-- Header with Tab Switcher -->
                    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-12">
                        <div class="flex items-center space-x-3">
                            <div class="bg-blue-600 p-2 rounded-lg shadow-sm">
                                <i data-lucide="zap" class="text-white w-6 h-6"></i>
                            </div>
                            <div>
                                <h2 class="text-2xl font-bold text-slate-900 tracking-tight">성능 분석 및 최적화 보고서</h2>
                                <p class="text-xs text-slate-500 font-medium tracking-wide uppercase">Shinhan 6th -
                                    SecondaryBook Project</p>
                            </div>
                        </div>

                        <!-- Tab Switcher -->
                        <div class="inline-flex p-1 bg-slate-200 rounded-xl border border-slate-300">
                            <button onclick="switchPerfTab('load')" id="tab-btn-load"
                                class="px-4 py-2 text-sm font-bold rounded-lg transition-all bg-white text-blue-600 shadow-sm">
                                Load (30)
                            </button>
                            <button onclick="switchPerfTab('stress')" id="tab-btn-stress"
                                class="px-4 py-2 text-sm font-bold rounded-lg transition-all text-slate-500 hover:text-slate-700">
                                Stress (500)
                            </button>
                            <button onclick="switchPerfTab('tuning')" id="tab-btn-tuning"
                                class="px-4 py-2 text-sm font-bold rounded-lg transition-all text-slate-500 hover:text-slate-700">
                                JVM Tuning
                            </button>
                        </div>
                    </div>

                    <!-- Intro Card -->
                    <div
                        class="bg-white rounded-2xl border border-slate-200 p-8 shadow-sm mb-12 transition-all duration-300">
                        <div class="flex items-center space-x-4 mb-4">
                            <div id="perf-indicator"
                                class="w-2 h-8 rounded-full bg-blue-500 transition-colors duration-300"></div>
                            <h2 id="perf-title" class="text-2xl font-extrabold text-slate-900">기본 부하 테스트 (30 VU)</h2>
                        </div>
                        <p id="perf-desc" class="text-slate-600 max-w-3xl leading-relaxed">
                            발표 시 예상되는 학급 전원 접속 상황을 재현한 안정성 테스트
                        </p>
                    </div>

                    <!-- Environment & Config (Grid) -->
                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-12">
                        <!-- Infra Config (Static) -->
                        <div class="lg:col-span-1 space-y-6">
                            <div class="mb-4">
                                <h3 class="text-lg font-bold text-slate-900">인프라 구성 환경</h3>
                                <p class="text-sm text-slate-500">최적화가 적용된 AWS 인프라 리소스</p>
                            </div>
                            <div class="bg-white rounded-2xl border border-slate-200 p-6 space-y-4 shadow-sm">
                                <div class="flex items-center justify-between py-2 border-b border-slate-50">
                                    <div class="flex items-center space-x-3">
                                        <i data-lucide="server" class="w-4 h-4 text-slate-400"></i>
                                        <span class="text-sm text-slate-600 font-medium">서버 인스턴스</span>
                                    </div>
                                    <span class="text-sm font-semibold text-slate-800">AWS EC2 t3.small</span>
                                </div>
                                <div class="flex items-center justify-between py-2 border-b border-slate-50">
                                    <div class="flex items-center space-x-3">
                                        <i data-lucide="zap" class="w-4 h-4 text-slate-400"></i>
                                        <span class="text-sm text-slate-600 font-medium">메모리 (RAM)</span>
                                    </div>
                                    <span class="text-sm font-semibold text-slate-800">2GB</span>
                                </div>
                                <div class="flex items-center justify-between py-2 border-b border-slate-50">
                                    <div class="flex items-center space-x-3">
                                        <i data-lucide="activity" class="w-4 h-4 text-slate-400"></i>
                                        <span class="text-sm text-slate-600 font-medium">오토스케일링</span>
                                    </div>
                                    <span class="text-sm font-semibold text-slate-800 text-right">ALB AutoScaling (Min:
                                        1, Max: 8)</span>
                                </div>
                                <div class="flex items-center justify-between py-2 border-b border-slate-50">
                                    <div class="flex items-center space-x-3">
                                        <i data-lucide="database" class="w-4 h-4 text-slate-400"></i>
                                        <span class="text-sm text-slate-600 font-medium">데이터베이스</span>
                                    </div>
                                    <span class="text-sm font-semibold text-slate-800">RDS Managed (PostgreSQL)</span>
                                </div>
                                <div class="flex items-center justify-between py-2">
                                    <div class="flex items-center space-x-3">
                                        <i data-lucide="zap" class="w-4 h-4 text-blue-400"></i>
                                        <span class="text-sm text-slate-600 font-medium">인메모리 DB</span>
                                    </div>
                                    <span class="text-sm font-semibold text-slate-800">ElastiCache Redis</span>
                                </div>
                            </div>
                        </div>

                        <!-- Code Block (Dynamic) -->
                        <div class="lg:col-span-2 space-y-6">
                            <div class="mb-4">
                                <h3 id="code-title" class="text-lg font-bold text-slate-900">테스트 시나리오 (k6)</h3>
                                <p id="code-subtitle" class="text-sm text-slate-500">k6 스크립트 실행 조건</p>
                            </div>
                            <div
                                class="bg-slate-900 rounded-2xl p-6 shadow-xl overflow-hidden relative border border-slate-800 h-[320px] overflow-y-auto custom-scrollbar">
                                <div id="code-badge"
                                    class="absolute top-4 right-4 text-slate-500 font-mono text-[10px] uppercase tracking-widest">
                                    presentationTest.js
                                </div>
                                <pre class="text-blue-400 font-mono text-sm leading-relaxed"><code id="perf-code">export const options = {
    stages: [
        { duration: '2m', target: 30 },   // 워밍업
        { duration: '26m', target: 30 },  // 30명 유지
        { duration: '2m', target: 0 },    // 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.05'],
    },
};</code></pre>
                            </div>
                        </div>
                    </div>

                    <!-- === TAB CONTENT: Results & Stats === -->
                    <div id="perf-content-area" class="space-y-12">
                        <!-- Content will be injected via JS -->
                    </div>

                    <!-- Conclusion Card (Dynamic) -->
                    <div
                        class="bg-white rounded-3xl border border-slate-200 p-10 shadow-sm relative overflow-hidden transition-all duration-500 mt-12">
                        <div id="conclusion-bg"
                            class="absolute top-0 right-0 w-48 h-48 rounded-full -mr-24 -mt-24 opacity-10 bg-blue-500">
                        </div>
                        <div class="flex flex-col md:flex-row items-start md:items-center gap-8 relative z-10">
                            <div class="flex-shrink-0">
                                <div id="conclusion-icon-bg"
                                    class="w-20 h-20 rounded-2xl flex items-center justify-center shadow-inner bg-green-100">
                                    <i id="conclusion-icon" data-lucide="check-circle"
                                        class="w-10 h-10 text-green-600"></i>
                                </div>
                            </div>
                            <div>
                                <h2 class="text-2xl font-bold text-slate-900 mb-3 flex items-center gap-2">
                                    종합 결론: <span id="conclusion-title"
                                        class="text-green-600 underline underline-offset-8 decoration-slate-200">발표 환경
                                        최적화 완료</span>
                                </h2>
                                <p id="conclusion-text" class="text-slate-600 leading-relaxed text-lg font-medium">
                                    30명의 가상 사용자를 투입한 결과, 에러율 0% 및 p95 응답 시간 60ms 미만이라는 압도적인 성능을 보였습니다. 현재의 t3.small 단일
                                    인스턴스로도 매우 여유롭게 서비스가 가능합니다.
                                </p>
                            </div>
                        </div>
                    </div>

                </div>

                <script>
                    // === DATA CONSTANTS ===
                    const SCENARIOS = {
                        'load': {
                            name: "기본 부하 테스트 (30 VU)",
                            desc: "발표 시 예상되는 학급 전원 접속 상황을 재현한 안정성 테스트",
                            color: "blue",
                            scriptName: "presentationTest.js",
                            code: `export const options = {
    stages: [
        { duration: '2m', target: 30 },   // 워밍업
        { duration: '26m', target: 30 },  // 30명 유지
        { duration: '2m', target: 0 },    // 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.05'],
    },
};`,
                            isTuning: false,
                            stats: {
                                p95: "59.7ms", p95Sub: "기준 1.0s 대비 쾌적",
                                error: "0.00%", errorSub: "13,695건 중 0건 실패",
                                tps: "7.6/s", tpsSub: "초당 요청 처리 수",
                                users: "30 VUs", active: 1, activeSub: "CPU 3% 미만 유지",
                                med: "21.52ms", avg: "26.73ms", data: "568 MB", check: "100%"
                            },
                            conclusion: {
                                title: "발표 환경 최적화 완료",
                                text: "30명의 가상 사용자를 투입한 결과, 에러율 0% 및 p95 응답 시간 60ms 미만이라는 압도적인 성능을 보였습니다. 현재의 t3.small 단일 인스턴스로도 매우 여유롭게 서비스가 가능합니다.",
                                badge: "t3.small (2GB) 환경 최적화"
                            }
                        },
                        'stress': {
                            name: "한계 스트레스 테스트 (500 VU)",
                            desc: "시스템이 견딜 수 있는 최대 동시 접속 임계치를 확인하기 위한 고부하 테스트",
                            color: "purple",
                            scriptName: "stressTest.js",
                            code: `export const options = {
    stages: [
        { duration: '4m', target: 30 },
        { duration: '6m', target: 100 },
        { duration: '6m', target: 200 },
        { duration: '6m', target: 350 },
        { duration: '6m', target: 500 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.10'],
    },
};`,
                            isTuning: false,
                            stats: {
                                p95: "1.85s", p95Sub: "기준 3.0s 대비 우수",
                                error: "0.00%", errorSub: "458,004건 중 0건 실패",
                                tps: "254.4/s", tpsSub: "초당 요청 처리 수",
                                users: "500 VUs", active: 2, activeSub: "500명 부하 시 오토스케일링 2대 작동",
                                med: "149.0ms", avg: "452.9ms", data: "19 GB", check: "99.99%"
                            },
                            conclusion: {
                                title: "대규모 트래픽 수용 능력 입증",
                                text: "최대 500명의 동시 접속자가 발생하는 극한 상황에서도 에러율 0%대를 유지했습니다. 트래픽 증가에 따라 오토스케일링이 동작하여 인스턴스가 2대로 증설되었으며, 안정적인 서비스가 유지되었습니다.",
                                badge: "Scale-Out: 2 Nodes"
                            }
                        },
                        'tuning': {
                            name: "JVM 성능 최적화 (Tuning)",
                            desc: "제한된 인프라 리소스(RAM 2GB) 내에서 GC 성능을 극대화하고 응답 지연(Tail Latency)을 최소화하기 위한 설정",
                            color: "emerald",
                            scriptName: "catalina.sh / setenv.sh",
                            code: `export CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=Asia/Seoul"
export CATALINA_OPTS="$CATALINA_OPTS -Xms1024m -Xmx1024m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxMetaspaceSize=128m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"`,
                            isTuning: true,
                            tuningDetails: [
                                { title: "Heap Memory 고정", icon: "memory-stick", tag: "-Xms1024m", desc: "JVM 힙 크기를 1GB로 고정하여 런타임 확장 비용 제거", effect: "STW 시간 50% 단축" },
                                { title: "Metaspace 제한", icon: "box-select", tag: "MaxMetaspace=128m", desc: "메타데이터 공간을 제한하여 피지컬 메모리 고갈 방지", effect: "OOM(Kill) 방어" },
                                { title: "G1GC 활성화", icon: "server-cog", tag: "-XX:+UseG1GC", desc: "대용량 힙에 최적화된 G1 Collector로 지연 시간 개선", effect: "p99 60ms 미만 안정화" }
                            ],
                            conclusion: {
                                title: "Tail Latency 최적화 성공",
                                text: "t3.small의 2GB RAM 환경에서 JVM 힙을 1GB로 고정하고 G1GC를 적용함으로써, 부하 발생 시 응답 속도가 튀는 'Tail Latency' 현상을 억제하고 전체적인 서비스 가용성을 높였습니다.",
                                badge: "Tail Latency 안정화"
                            }
                        }
                    };

                    function switchPerfTab(tabKey) {
                        const data = SCENARIOS[tabKey];
                        const contentArea = document.getElementById('perf-content-area');

                        // 1. Buttons Update
                        ['load', 'stress', 'tuning'].forEach(key => {
                            const btn = document.getElementById('tab-btn-' + key);
                            if (key === tabKey) {
                                btn.className = "px-4 py-2 text-sm font-bold rounded-lg transition-all bg-white text-blue-600 shadow-sm";
                                if (key === 'stress') btn.classList.replace('text-blue-600', 'text-purple-600');
                                if (key === 'tuning') btn.classList.replace('text-blue-600', 'text-emerald-600');
                            } else {
                                btn.className = "px-4 py-2 text-sm font-bold rounded-lg transition-all text-slate-500 hover:text-slate-700";
                            }
                        });

                        // 2. Intro Card Update
                        document.getElementById('perf-title').innerText = data.name;
                        document.getElementById('perf-desc').innerText = data.desc;
                        const indicator = document.getElementById('perf-indicator');
                        indicator.className = `w-2 h-8 rounded-full bg-\${data.color}-500 transition-colors duration-300`;

                        // 3. Code Block Update
                        document.getElementById('code-badge').innerText = data.scriptName;
                        if (data.isTuning) {
                            document.getElementById('code-title').innerText = "JVM 최적화 설정 코드";
                            document.getElementById('code-subtitle').innerText = "Tomcat/JVM 옵션 적용";
                        } else {
                            document.getElementById('code-title').innerText = "테스트 시나리오 (k6)";
                            document.getElementById('code-subtitle').innerText = "k6 스크립트 실행 조건";
                        }
                        // document.getElementById('perf-code').innerText = data.code;
                        // -> innerText changes format, use textContent then highlight? HTML content safer for preserving format
                        document.getElementById('perf-code').textContent = data.code;

                        // 4. Main Content (Stats vs Tuning)
                        let html = '';
                        if (!data.isTuning) {
                            html = `
                            <div>
                                <h3 class="text-xl font-bold text-slate-900 mb-6">성능 측정 결과 <span class="text-sm font-normal text-slate-500 ml-2">처리량 및 응답 지표 분석</span></h3>
                                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4">
                                     <!-- 95th -->
                                     <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-start space-x-4">
                                        <div class="p-3 rounded-xl bg-blue-50"><i data-lucide="clock" class="text-blue-600 w-6 h-6"></i></div>
                                        <div><p class="text-sm font-medium text-slate-500">95th Percentile</p><h3 class="text-2xl font-bold text-slate-900 mt-1">\${data.stats.p95}</h3><p class="text-xs text-slate-400 mt-1">\${data.stats.p95Sub}</p></div>
                                     </div>
                                     <!-- Error -->
                                     <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-start space-x-4">
                                        <div class="p-3 rounded-xl bg-green-50"><i data-lucide="alert-circle" class="text-green-600 w-6 h-6"></i></div>
                                        <div><p class="text-sm font-medium text-slate-500">HTTP 에러율</p><h3 class="text-2xl font-bold text-slate-900 mt-1">\${data.stats.error}</h3><p class="text-xs text-slate-400 mt-1">\${data.stats.errorSub}</p></div>
                                     </div>
                                     <!-- TPS -->
                                     <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-start space-x-4">
                                        <div class="p-3 rounded-xl bg-amber-50"><i data-lucide="zap" class="text-amber-600 w-6 h-6"></i></div>
                                        <div><p class="text-sm font-medium text-slate-500">평균 처리량</p><h3 class="text-2xl font-bold text-slate-900 mt-1">\${data.stats.tps}</h3><p class="text-xs text-slate-400 mt-1">\${data.stats.tpsSub}</p></div>
                                     </div>
                                     <!-- Users -->
                                     <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-start space-x-4">
                                        <div class="p-3 rounded-xl bg-purple-50"><i data-lucide="users" class="text-purple-600 w-6 h-6"></i></div>
                                        <div><p class="text-sm font-medium text-slate-500">최대 가상 유저</p><h3 class="text-2xl font-bold text-slate-900 mt-1">\${data.stats.users}</h3><p class="text-xs text-slate-400 mt-1">동시 접속자 목표 달성</p></div>
                                     </div>
                                     <!-- Active Instances -->
                                     <div class="bg-blue-600 rounded-2xl p-6 shadow-lg text-white flex flex-col justify-center border border-blue-500">
                                        <div class="flex items-center space-x-3 mb-2"><i data-lucide="layout" class="w-5 h-5 opacity-80"></i><h3 class="text-sm font-bold opacity-90 uppercase tracking-tight">활성 인스턴스</h3></div>
                                        <div class="flex items-baseline space-x-2"><span class="text-4xl font-black tracking-tighter">\${data.stats.active}</span><span class="text-sm opacity-80 font-medium">/ 8 Nodes</span></div>
                                        <p class="mt-2 text-[10px] text-blue-100 leading-tight">\${data.stats.activeSub}</p>
                                     </div>
                                </div>
                                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
                                    <div class="bg-white p-4 rounded-xl border border-slate-100 shadow-sm flex justify-between items-center"><span class="text-xs font-semibold text-slate-400">중간 응답 시간</span><span class="text-sm font-bold text-slate-700">\${data.stats.med}</span></div>
                                    <div class="bg-white p-4 rounded-xl border border-slate-100 shadow-sm flex justify-between items-center"><span class="text-xs font-semibold text-slate-400">평균 응답 시간</span><span class="text-sm font-bold text-slate-700">\${data.stats.avg}</span></div>
                                    <div class="bg-white p-4 rounded-xl border border-slate-100 shadow-sm flex justify-between items-center"><span class="text-xs font-semibold text-slate-400">총 수신 데이터</span><span class="text-sm font-bold text-slate-700">\${data.stats.data}</span></div>
                                    <div class="bg-white p-4 rounded-xl border border-slate-100 shadow-sm flex justify-between items-center"><span class="text-xs font-semibold text-slate-400">성공 체크율</span><span class="text-sm font-bold text-green-600">\${data.stats.check}</span></div>
                                </div>
                            </div>
                            `;
                        } else {
                            // Tuning Detail
                            html = `
                            <div>
                                <h3 class="text-xl font-bold text-slate-900 mb-6">JVM 튜닝 상세 내용 <span class="text-sm font-normal text-slate-500 ml-2">p95 지표 안정화 및 Stop-The-World 최소화 전략</span></h3>
                                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                                \${data.tuningDetails.map(d => `
                                < div class="bg-white rounded-2xl border border-slate-200 p-6 flex flex-col h-full shadow-sm hover:shadow-xl hover:border-emerald-300 hover:-translate-y-1 transition-all duration-300 group/jvm relative overflow-hidden" >
                                        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover/jvm:opacity-10 transition-opacity">
                                            <i data-lucide="\${d.icon}" class="w-24 h-24 text-emerald-600"></i>
                                        </div>
                                        <div class="flex items-center gap-3 mb-4 relative z-10">
                                            <div class="p-2 bg-emerald-50 rounded-lg group-hover/jvm:bg-emerald-100 transition-colors">
                                                <i data-lucide="\${d.icon}" class="w-6 h-6 text-emerald-600"></i>
                                            </div>
                                            <div>
                                                <h3 class="font-bold text-slate-800 leading-tight text-lg group-hover/jvm:text-emerald-700 transition-colors">\${d.title}</h3>
                                                <span class="text-[10px] font-mono text-slate-400">\${d.tag}</span>
                                            </div>
                                        </div>
                                        <p class="text-sm text-slate-500 mb-6 flex-grow leading-relaxed relative z-10 p-2 bg-slate-50/50 rounded-lg">\${d.desc}</p>
                                        <div class="pt-4 border-t border-slate-100 mt-auto relative z-10">
                                            <div class="flex items-center gap-2 mb-2">
                                                <div class="w-1.5 h-1.5 rounded-full bg-emerald-500"></div>
                                                <p class="text-xs font-bold text-emerald-700 uppercase tracking-wider">최종 기대 효과</p>
                                            </div>
                                            <p class="text-sm font-semibold text-slate-700 pl-3.5">\${d.effect}</p>
                                        </div>
                                    </div >
                                `).join('')}
                                </div>
                                <div class="bg-emerald-50 border border-emerald-100 rounded-2xl p-6 flex items-start space-x-4 mt-6">
                                    <div class="bg-emerald-500 p-2 rounded-lg mt-1"><i data-lucide="info" class="text-white w-5 h-5"></i></div>
                                    <div>
                                        <h4 class="text-emerald-900 font-bold mb-1 underline underline-offset-4 decoration-emerald-200 tracking-tight">💡 용어 정리: Tail Latency (꼬리 지연 시간)</h4>
                                        <p class="text-emerald-800 text-sm leading-relaxed">
                                            전체 요청 중 가장 느린 소수(p95, p99 등)의 응답 시간을 의미합니다. <strong>JVM 튜닝은 평균 응답 시간보다 이러한 'Tail Latency'를 안정화하는 데 목적</strong>이 있습니다. Heap 크기를 고정함으로써 JVM이 메모리를 늘리거나 줄일 때 발생하는 오버헤드와 Stop-The-World 현상을 원천 차단하여 p95 지표를 효과적으로 제어합니다.
                                        </p>
                                    </div>
                                </div>
                            </div>
                            `;
                        }
                        contentArea.innerHTML = html;

                        // 5. Conclusion Update
                        document.getElementById('conclusion-title').className = `text-\${data.color}-600 underline underline-offset-8 decoration-slate-200`;
                        document.getElementById('conclusion-text').innerText = data.conclusion.text;

                        document.getElementById('conclusion-bg').className = `absolute top-0 right-0 w-48 h-48 rounded-full -mr-24 -mt-24 opacity-10 bg-\${data.color}-500`;
                        const iconBg = document.getElementById('conclusion-icon-bg');
                        iconBg.className = `w-20 h-20 rounded-2xl flex items-center justify-center shadow-inner \${data.isTuning ? "bg-emerald-100" : "bg-green-100"}`;

                        if (window.lucide) lucide.createIcons();
                    }

                    // 초기화 (Load 탭 기본)
                    document.addEventListener('DOMContentLoaded', () => {
                        switchPerfTab('load');
                    });
                </script>
            </section>




            <!-- Responsive Holo Lightbox (Inline Implementation) -->
            <!-- Simple Fullscreen Lightbox -->
            <div id="simpleLightbox"
                class="fixed inset-0 z-[99999] hidden flex items-center justify-center bg-black/95 transition-opacity duration-300 opacity-0"
                onclick="closeLightbox()">
                <button onclick="closeLightbox()"
                    class="absolute top-6 right-8 text-white/50 hover:text-white transition-colors z-[100000]">
                    <i data-lucide="x" class="w-12 h-12"></i>
                </button>
                <div class="relative w-full h-full p-4 flex items-center justify-center">
                    <img id="lightboxImg" src="" alt="Full Screen View"
                        class="max-w-full max-h-full object-contain shadow-2xl select-none">
                </div>
            </div>
            <section class="py-32 bg-gray-50 border-t border-gray-100">
                <div class="max-w-[1440px] mx-auto px-6">
                    <div class="text-center mb-16 reveal-text">
                        <span class="text-orange-600 font-bold tracking-wider text-sm uppercase mb-2 block">Real-time
                            Monitoring</span>
                        <h2 class="text-4xl font-black mb-4 text-gray-900">AWS CloudWatch Metrics</h2>
                        <p class="text-gray-500 font-medium">보이지 않으면, 관리할 수 없습니다.</p>
                    </div>

                    <div class="reveal-card">
                        <div class="bento-box overflow-hidden bg-white border border-gray-200 shadow-2xl"
                            style="height: 800px;">
                            <div
                                class="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                                <div class="flex items-center gap-3">
                                    <div class="w-3 h-3 rounded-full bg-orange-500 animate-pulse"></div>
                                    <span class="text-sm font-bold text-gray-700">Live Dashboard:
                                        SecondaryBook-Dashboard</span>
                                </div>
                                <a href="https://cloudwatch.amazonaws.com/dashboard.html?dashboard=SecondaryBook-Dashboard&context=eyJSIjoidXMtZWFzdC0xIiwiRCI6ImN3LWRiLTEzNDA1MTA1MjAyNSIsIlUiOiJ1cy1lYXN0LTFfU3JKVzBoaEZPIiwiQyI6IjFvM3VicjkzNjZyMzZibHI1amFlbmpwazd0IiwiSSI6InVzLWVhc3QtMTpmNDM1NDdmYi01NzQ1LTRlM2ItODIyNS00OTkxYzdhMzhlNWEiLCJNIjoiUHVibGljIn0="
                                    target="_blank"
                                    class="text-xs text-blue-600 hover:underline flex items-center gap-1">
                                    새 창에서 보기 <i data-lucide="external-link" class="w-3 h-3"></i>
                                </a>
                            </div>

                            <iframe
                                src="https://cloudwatch.amazonaws.com/dashboard.html?dashboard=SecondaryBook-Dashboard&context=eyJSIjoidXMtZWFzdC0xIiwiRCI6ImN3LWRiLTEzNDA1MTA1MjAyNSIsIlUiOiJ1cy1lYXN0LTFfU3JKVzBoaEZPIiwiQyI6IjFvM3VicjkzNjZyMzZibHI1amFlbmpwazd0IiwiSSI6InVzLWVhc3QtMTpmNDM1NDdmYi01NzQ1LTRlM2ItODIyNS00OTkxYzdhMzhlNWEiLCJNIjoiUHVibGljIn0="
                                width="100%" height="100%" frameborder="0" style="border:0; min-height: 740px;"
                                allowfullscreen>
                            </iframe>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Code Modal (for BookClub Transaction) -->
            <div id="codeModal"
                class="fixed inset-0 z-[99995] hidden flex items-center justify-center bg-black/70 backdrop-blur-sm transition-opacity duration-300 opacity-0"
                onclick="closeCodeModal(event)">
                <div class="bg-[#1e1e1e] w-[90vw] max-w-4xl rounded-2xl shadow-2xl overflow-hidden transform transition-all duration-300 scale-95 opacity-0 flex flex-col max-h-[85vh]"
                    id="codeModalContent">

                    <!-- Header -->
                    <div class="flex items-center justify-between px-6 py-4 border-b border-gray-800 bg-[#252526]">
                        <div class="flex items-center gap-4">
                            <h3 class="text-white font-bold text-lg flex items-center gap-2">
                                <i data-lucide="code-2" class="w-5 h-5 text-purple-400"></i>
                                <span id="codeModalTitle">Code Implementation</span>
                            </h3>
                            <span id="codeModalBadge"
                                class="px-3 py-1 bg-purple-600 text-white text-xs rounded-full font-mono"></span>
                        </div>
                        <button onclick="closeCodeModal()" class="text-gray-400 hover:text-white transition-colors">
                            <i data-lucide="x" class="w-6 h-6"></i>
                        </button>
                    </div>

                    <!-- Code Content -->
                    <div class="flex-1 overflow-auto p-0">
                        <div class="p-6">
                            <div class="mb-6">
                                <p class="text-sm text-gray-400 mb-2" id="codeModalDesc"></p>
                            </div>
                            <pre
                                class="p-6 text-sm font-mono text-gray-300 bg-[#1e1e1e] leading-relaxed overflow-x-auto rounded-xl border border-gray-800"><code id="codeModalCode"></code></pre>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div class="px-6 py-3 bg-[#252526] border-t border-gray-800 flex justify-between items-center">
                        <span class="text-xs text-gray-500">Press ESC to close</span>
                        <div class="flex items-center gap-2 text-xs text-gray-400">
                            <i data-lucide="sparkles" class="w-4 h-4 text-purple-400"></i>
                            <span>Transaction Safety Pattern</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Code Diff Modal -->

            <div id="diffModal"
                class="fixed inset-0 z-[99990] hidden flex items-center justify-center bg-black/60 backdrop-blur-sm transition-opacity duration-300 opacity-0"
                onclick="closeDiffModal(event)">
                <div class="bg-[#1e1e1e] w-[90vw] max-w-6xl rounded-2xl shadow-2xl overflow-hidden transform transition-all duration-300 scale-95 opacity-0 flex flex-col max-h-[90vh]"
                    id="diffModalContent">

                    <!-- Header -->
                    <div class="flex items-center justify-between px-6 py-4 border-b border-gray-800 bg-[#252526]">
                        <div class="flex items-center gap-4">
                            <h3 class="text-white font-bold text-lg flex items-center gap-2">
                                <i data-lucide="git-compare" class="w-5 h-5 text-blue-400"></i>
                                Code Optimization Diff
                            </h3>
                            <span id="diffTitle"
                                class="px-3 py-1 bg-gray-700 text-gray-300 text-xs rounded-full font-mono"></span>
                        </div>
                        <button onclick="closeDiffModal()" class="text-gray-400 hover:text-white transition-colors">
                            <i data-lucide="x" class="w-6 h-6"></i>
                        </button>
                    </div>

                    <!-- Diff Content -->
                    <div
                        class="flex-1 overflow-auto p-0 grid grid-cols-1 md:grid-cols-2 divide-y md:divide-y-0 md:divide-x divide-gray-800">

                        <!-- Legacy Code -->
                        <div class="flex flex-col">
                            <div
                                class="px-4 py-2 bg-[#2d2d2d] border-b border-gray-800 flex justify-between items-center sticky top-0">
                                <span class="text-red-400 text-xs font-bold uppercase tracking-wider">Before
                                    (Legacy)</span>
                                <span class="text-gray-500 text-xs font-mono" id="legacyTime"></span>
                            </div>
                            <pre
                                class="p-6 text-sm font-mono text-gray-300 overflow-x-auto bg-[#1e1e1e] leading-relaxed"><code id="legacyCode"></code></pre>
                        </div>

                        <!-- Optimized Code -->
                        <div class="flex flex-col">
                            <div
                                class="px-4 py-2 bg-[#2d2d32] border-b border-gray-800 flex justify-between items-center sticky top-0">
                                <span class="text-green-400 text-xs font-bold uppercase tracking-wider">After
                                    (Optimized)</span>
                                <span class="text-gray-500 text-xs font-mono" id="optimizedTime"></span>
                            </div>
                            <pre
                                class="p-6 text-sm font-mono text-gray-300 overflow-x-auto bg-[#1e1e1e] leading-relaxed"><code id="optimizedCode"></code></pre>
                        </div>
                    </div>

                    <!-- Footer hint -->
                    <div class="px-6 py-3 bg-[#252526] border-t border-gray-800 text-right">
                        <span class="text-xs text-gray-500">Press ESC to close</span>
                    </div>
                </div>
            </div>

            <script>
                // Simple Lightbox Logic
                const sLightbox = document.getElementById('simpleLightbox');
                const sLbImg = document.getElementById('lightboxImg');

                window.openLightbox = function (src) {
                    sLbImg.src = src;
                    sLightbox.classList.remove('hidden');
                    void sLightbox.offsetWidth;
                    sLightbox.classList.remove('opacity-0');
                    document.body.style.overflow = 'hidden';
                };

                window.closeLightbox = function () {
                    sLightbox.classList.add('opacity-0');
                    setTimeout(() => {
                        sLightbox.classList.add('hidden');
                        sLbImg.src = '';
                        document.body.style.overflow = '';
                    }, 300);
                };

                // Diff Modal Logic
                const diffModal = document.getElementById('diffModal');
                const diffModalContent = document.getElementById('diffModalContent');

                const diffData = {
                    'main': {
                        title: 'Main Page List Query',
                        legacyTime: '150.4ms',
                        optimizedTime: '128.4ms',
                        legacy: `SELECT b.*,
                            (SELECT count(*)
                             FROM review r
                             WHERE r.book_id = b.id) as r_cnt
                     FROM book b
                     ORDER BY b.id DESC
                         LIMIT 20 OFFSET 0`,
                        optimized: `SELECT b.*, count(r.id) as r_cnt
                        FROM book b
                                 LEFT JOIN review r
                                           ON b.id = r.book_id
                        GROUP BY b.id
                        ORDER BY b.id DESC
                            LIMIT 20 OFFSET 0`
                    },
                    'purchase': {
                        title: 'Purchase History N+1',
                        legacyTime: '4,885ms',
                        optimizedTime: '355ms',
                        legacy: `// Service Layer (N+1 Problem)
List<Trade> trades = mapper.findAll(userId);

for(Trade t : trades) {
    // Queries DB loop times (blocking)
    Image thumb =
      imgMapper.findById(t.getId());
    t.setThumbnail(thumb);
}`,
                        optimized: `// Service Layer (Bulk Select)
List<Trade> trades = mapper.findAll(userId);
List<Long> ids = trades.map(Trade::getId);

// Single Query with IN Clause
List<Image> images =
  imgMapper.findAllByIds(ids);

matchImagesToTrades(trades, images);`
                    },
                    'chat': {
                        title: 'Chat Room List',
                        legacyTime: '143.0ms',
                        optimizedTime: '140.3ms',
                        legacy: `SELECT c.*,
                            (SELECT nick FROM member m
                             WHERE m.id = c.partner_id) as nick
                     FROM chat_room c
                     WHERE c.owner_id = ?`,
                        optimized: `SELECT c.*, m.nick
                        FROM chat_room c
                                 INNER JOIN member m
                                            ON c.partner_id = m.id
                        WHERE c.owner_id = ?
-- Converted Subquery to Join`
                    },
                    'payment': {
                        title: 'Payment Page Entry',
                        legacyTime: '168.0ms',
                        optimizedTime: '132.0ms',
                        legacy: `// Controller
User u = userMapper.findById(uid);
Book b = bookMapper.findById(bid);
Addr a = addrMapper.findByUser(uid);

// 3 Separate Round-trips to DB`,
                        optimized: `<select id="getPaymentCheckInfo">
  SELECT *
  FROM book b
  JOIN users u ON u.id = \u0023{uid}
  LEFT JOIN address a
    ON a.user_id = u.id
  WHERE b.id = \u0023{bid}
</select>
<!-- Single Round-trip -->`
                    },
                    'redis': {
                        title: 'Main Page Caching',
                        legacyTime: '111.0ms',
                        optimizedTime: '62.0ms',
                        legacy: `public List<Book> getMainBooks() {
    // Always hits Database
    return bookRepo.findAllDesc();
}`,
                        optimized: `@Cacheable(value = "mainBooks")
public List<Book> getMainBooks() {
    // Hits Redis first (Look-aside)
    // Only hits DB on cache miss
    return bookRepo.findAllDesc();
}`
                    },
                    'n1': {
                        title: 'N+1 Problem: Thumbnails',
                        legacyTime: '21 Queries',
                        optimizedTime: '2 Queries',
                        legacy: `// Service Layer (N+1 Issue)
List<Trade> trades = mapper.findAll();
for (Trade t : trades) {
    // Executes 1 query PER iteration
    // Total 20+1 queries responsible for heavy load
    Image img = imgMapper.findByTradeId(t.getId());
    t.setThumbnail(img);
}`,
                        optimized: `<!-- MyBatis Mapper (Optimization) -->
<select id="findAllImagesByTradeIds">
    SELECT * FROM trade_image
    WHERE trade_id IN
    <foreach item="id" collection="list"
             open="(" separator="," close=")">
        \u0023{id}
    </foreach>
</select>
<!-- Single query utilizing IN clause -->`
                    },
                    'concurrency': {
                        title: 'Concurrency Assurance (Lock)',
                        legacyTime: 'Overbooking',
                        optimizedTime: 'Safe',
                        legacy: `// Service Layer (Race Condition)
@Transactional
public void joinClub(Long clubId, User user) {
    Club club = mapper.findById(clubId);
    // If 2 users read same 'currentCount' here...
    if (club.getCurrentCount() < club.getMaxCount()) {
        mapper.insertMember(clubId, user.getId());
        mapper.incrementCount(clubId);
    }
}
// Result: 5/5 capacity becomes 6/5`,
                        optimized: `<!-- PostgreSQL: Pessimistic Lock -->
<select id="findByIdForUpdate" resultType="Club">
    SELECT * FROM club
    WHERE id = \u0023{id}
    FOR UPDATE
</select>
<!-- Locks row until transaction commit.
Other transactions wait here. -->`
                    }
                };

                window.openDiffModal = function (key) {
                    const data = diffData[key];
                    if (!data) return;

                    document.getElementById('diffTitle').innerText = data.title;
                    document.getElementById('legacyTime').innerText = data.legacyTime;
                    document.getElementById('optimizedTime').innerText = data.optimizedTime;
                    document.getElementById('legacyCode').innerText = data.legacy;
                    document.getElementById('optimizedCode').innerText = data.optimized;

                    diffModal.classList.remove('hidden');
                    // Force reflow
                    void diffModal.offsetWidth;
                    diffModal.classList.remove('opacity-0');
                    diffModalContent.classList.remove('scale-95', 'opacity-0');
                    diffModalContent.classList.add('scale-100', 'opacity-100');

                    document.body.style.overflow = 'hidden';

                    if (window.lucide) lucide.createIcons();
                };

                window.closeDiffModal = function (e) {
                    if (e && e.target !== diffModal && !e.target.closest('button')) return;

                    diffModal.classList.add('opacity-0');
                    diffModalContent.classList.remove('scale-100', 'opacity-100');
                    diffModalContent.classList.add('scale-95', 'opacity-0');

                    setTimeout(() => {
                        diffModal.classList.add('hidden');
                        document.body.style.overflow = '';
                    }, 300);
                };

                document.addEventListener('keydown', function (e) {
                    if (e.key === "Escape") {
                        closeLightbox();
                        closeDiffModal();
                        closeCodeModal();
                    }
                });

                // Code Modal Logic (for BookClub Transaction Pattern)
                const codeModal = document.getElementById('codeModal');
                const codeModalContent = document.getElementById('codeModalContent');

                const codeData = {
                    'bookclub': {
                        title: 'BookClub Transaction Safety',
                        badge: 'S3Service.java',
                        desc: 'TransactionSynchronization을 활용하여 DB 커밋 성공 후에만 S3 리소스를 삭제합니다. 트랜잭션 Rollback 시 기존 이미지가 보존되어 데이터 정합성을 보장합니다.',
                        code: `private void scheduleS3DeletionAfterCommit(String oldUrl) {
    if (oldUrl == null || oldUrl.isBlank()) return;
    if (!oldUrl.startsWith("http")) return;

    // Spring의 Transaction 동기화 메커니즘 활용
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // ✅ DB 커밋 성공 후에만 실행됨
                deleteS3ImageSafely(oldUrl);
            }

            // afterCompletion도 오버라이드 가능
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.info("Transaction rolled back. S3 deletion skipped.");
                }
            }
        }
    );
}

private void deleteS3ImageSafely(String s3Url) {
    try {
        String key = extractS3Key(s3Url);
        s3Client.deleteObject(bucketName, key);
        log.info("S3 image deleted: {}", key);
    } catch (Exception e) {
        // S3 삭제 실패는 로깅만 (DB는 이미 커밋됨)
        log.error("Failed to delete S3 image: {}", s3Url, e);
    }
}

/* 사용 예시: BookClubService */
@Transactional
public void updateBookClub(Long id, BookClubDto dto) {
    BookClub club = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("BookClub not found"));

    String oldImageUrl = club.getImageUrl();

    // 1. DB 업데이트
    club.updateImage(dto.getNewImageUrl());
    repository.save(club);

    // 2. 커밋 후 S3 삭제 예약 (안전한 순서 보장)
    scheduleS3DeletionAfterCommit(oldImageUrl);

    // ⚠️ 만약 여기서 예외 발생 시:
    // - DB Rollback → 기존 이미지 URL 보존
    // - S3 삭제 예약 취소 → 기존 파일 유지
}`
                    },
                    'bookclub-update': {
                        title: '이미지 수정 - updateBookClub()',
                        badge: 'BookClubService.java',
                        desc: '독서모임 이미지를 변경할 때, 새 이미지 업로드 후 DB 커밋이 성공한 경우에만 기존 S3 이미지를 삭제합니다. 트랜잭션이 실패하면 기존 이미지가 보존됩니다.',
                        code: `@Transactional
public void updateBookClub(Long id, BookClubDto dto) {
    // 1. 기존 독서모임 조회
    BookClub club = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("BookClub not found"));

    // 2. 기존 이미지 URL 백업 (삭제용)
    String oldImageUrl = club.getImageUrl();

    // 3. 새 이미지 업로드 (S3)
    String newImageUrl = s3Service.uploadImage(dto.getNewImage());

    // 4. DB 업데이트
    club.updateImage(newImageUrl);
    club.updateTitle(dto.getTitle());
    club.updateDescription(dto.getDescription());

    repository.save(club);

    // 5. ✅ 커밋 성공 후에만 기존 S3 이미지 삭제 예약
    scheduleS3DeletionAfterCommit(oldImageUrl);

    /* 시나리오별 동작:
     *
     * [성공 케이스]
     * - DB 커밋 성공 → afterCommit() 실행 → 기존 S3 이미지 삭제 ✅
     *
     * [실패 케이스 1: DB 제약조건 위반]
     * - save() 실패 → 트랜잭션 Rollback
     * - afterCommit() 실행 안 됨 → 기존 이미지 보존 ✅
     * - 새 이미지는 S3에 남지만 DB에는 참조 안 됨 (고아 파일)
     *
     * [실패 케이스 2: 동시성 충돌]
     * - 낙관적 락 예외 발생 → Rollback
     * - 기존 이미지 그대로 유지 ✅
     */
}`
                    },
                    'bookclub-delete': {
                        title: '게시글 삭제 - deleteBoardPost()',
                        badge: 'BookClubService.java',
                        desc: '독서모임 게시글을 소프트 삭제할 때, DB 삭제가 성공한 후에만 첨부 이미지를 S3에서 제거합니다. Rollback 시 이미지가 보존되어 데이터 복구가 가능합니다.',
                        code: `@Transactional
public boolean deleteBoardPost(Long clubSeq, Long postId) {
    // 1. 삭제할 게시글의 이미지 URL 조회
    String oldUrl = bookClubMapper.selectBoardImgUrl(clubSeq, postId);

    // 2. 게시글 소프트 삭제 (deleted_at 컬럼 업데이트)
    int result = bookClubMapper.softDeletePost(clubSeq, postId);

    // 3. ✅ DB 삭제 성공(커밋) 이후에만 이미지 정리
    if (result > 0) {
        scheduleS3DeletionAfterCommit(oldUrl);
    }

    return result > 0;

    /* 동작 흐름:
     *
     * [정상 삭제 케이스]
     * 1) softDeletePost() 성공 (result = 1)
     * 2) 트랜잭션 커밋
     * 3) afterCommit() 콜백 실행
     * 4) S3 이미지 삭제 ✅
     *
     * [삭제 실패 케이스]
     * 1) softDeletePost() 실패 (result = 0)
     * 2) scheduleS3DeletionAfterCommit() 호출 안 됨
     * 3) 이미지 보존 ✅
     *
     * [트랜잭션 Rollback 케이스]
     * 1) softDeletePost() 성공
     * 2) 이후 로직에서 예외 발생 (예: 권한 검증 실패)
     * 3) 트랜잭션 Rollback
     * 4) afterCommit() 실행 안 됨
     * 5) DB 복구 + 이미지 보존 ✅
     *
     * ⚠️ 주의사항:
     * - Soft Delete이므로 실제 데이터는 남아있음
     * - 나중에 복구 시 이미지도 함께 사용 가능
     * - Hard Delete라면 별도의 복구 전략 필요
     */
}`
                    },
                    'concurrency-lock': {
                        title: 'Concurrency Control - FOR UPDATE',
                        badge: 'BookClubMapper.xml',
                        desc: '독서모임 가입 승인 시 정원 초과를 방지하기 위해 비관적 락(Pessimistic Lock)을 사용합니다. FOR UPDATE로 book_club 행을 잠그고, 트랜잭션이 완료될 때까지 다른 트랜잭션의 접근을 차단합니다.',
                        code: `<!-- book_club 행 잠금 (동시성 제어) -->
<select id="lockBookClubForUpdate" resultType="java.lang.Long">
    SELECT book_club_seq
    FROM book_club
    WHERE book_club_seq = \\#{bookClubSeq}
    AND book_club_deleted_dt IS NULL
    FOR UPDATE
</select>

/* 사용 예시: BookClubService */
@Transactional
public void approveJoinRequest(Long clubSeq, Long userId) {
    // 1. ✅ book_club 행 잠금 (다른 트랜잭션 대기)
    Long lockedSeq = mapper.lockBookClubForUpdate(clubSeq);

    if (lockedSeq == null) {
        throw new NotFoundException("BookClub not found or deleted");
    }

    // 2. 현재 멤버 수 조회
    int currentCount = mapper.countMembers(clubSeq);

    // 3. 정원 확인
    BookClub club = mapper.selectOne(clubSeq);
    if (currentCount >= club.getCapacity()) {
        throw new CapacityExceededException("모임 정원 초과");
    }

    // 4. 멤버 상태 변경: WAIT → JOINED
    mapper.updateMemberStatus(clubSeq, userId, "JOINED");

    // 5. 커밋 시 락 해제
}

/* 동시성 시나리오:
 *
 * [문제 상황] - FOR UPDATE 없이
 * - 정원 19/20인 모임에 2명 동시 가입 시도
 * - 두 트랜잭션 모두 currentCount=19로 읽음
 * - 둘 다 승인 → 최종 21명 (정원 초과 ❌)
 *
 * [해결] - FOR UPDATE 적용
 * - Tx1: lockBookClubForUpdate() → 행 잠금 획득
 * - Tx2: lockBookClubForUpdate() → 대기 (Tx1 완료까지)
 * - Tx1: count=19, 승인, 커밋 → 락 해제
 * - Tx2: count=20, 정원 초과 예외 발생 ✅
 *
 * ⚠️ 주의사항:
 * - @Transactional 필수 (트랜잭션 범위 내에서만 락 유효)
 * - 데드락 방지를 위해 락 획득 순서 일관성 유지
 * - 장시간 락 보유 지양 (성능 저하 방지)
 */`
                    }
                };


                window.openCodeModal = function (key) {
                    const data = codeData[key];
                    if (!data) return;

                    document.getElementById('codeModalTitle').innerText = data.title;
                    document.getElementById('codeModalBadge').innerText = data.badge;
                    document.getElementById('codeModalDesc').innerText = data.desc;
                    document.getElementById('codeModalCode').innerText = data.code;

                    codeModal.classList.remove('hidden');
                    void codeModal.offsetWidth; // Force reflow
                    codeModal.classList.remove('opacity-0');
                    codeModalContent.classList.remove('scale-95', 'opacity-0');
                    codeModalContent.classList.add('scale-100', 'opacity-100');

                    document.body.style.overflow = 'hidden';

                    if (window.lucide) lucide.createIcons();
                };

                window.closeCodeModal = function (e) {
                    if (e && e.target !== codeModal && !e.target.closest('button')) return;

                    codeModal.classList.add('opacity-0');
                    codeModalContent.classList.remove('scale-100', 'opacity-100');
                    codeModalContent.classList.add('scale-95', 'opacity-0');

                    setTimeout(() => {
                        codeModal.classList.add('hidden');
                        document.body.style.overflow = '';
                    }, 300);
                };
            </script>
            <style>
                /* CSS for 3D perspective and spotlight overlay */
                .interactive-card {
                    position: relative;
                    transform-style: preserve-3d;
                    transition: transform 0.2s ease-out;
                }

                .spotlight-overlay {
                    position: absolute;
                    inset: 0;
                    background: radial-gradient(circle at var(--mouse-x) var(--mouse-y), rgba(255, 255, 255, 0.15) 0%, transparent 80%);
                    opacity: 0;
                    transition: opacity 0.3s ease;
                    pointer-events: none;
                    border-radius: inherit;
                    /* Inherit border-radius from parent */
                }

                .interactive-card:hover .spotlight-overlay {
                    opacity: 1;
                }

                /* Hero Float Animation */
                .hero-floater {
                    animation: float 3s ease-in-out infinite;
                }

                @keyframes float {
                    0% {
                        transform: translateY(0) rotate(3deg);
                    }

                    50% {
                        transform: translateY(-10px) rotate(3deg);
                    }

                    100% {
                        transform: translateY(0) rotate(3deg);
                    }
                }

                /* Hero Text Glass Panel */
                .hero-glass-panel {
                    background: rgba(255, 255, 255, 0.4);
                    backdrop-filter: blur(8px);
                    -webkit-backdrop-filter: blur(8px);
                    padding: 3rem;
                    border-radius: 2rem;
                    border: 1px solid rgba(255, 255, 255, 0.5);
                    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
                }
            </style>
            <!-- Hero Section with Spline 3D Background -->
            <section class="h-screen flex flex-col justify-center items-center text-center relative overflow-hidden">

                <!-- Spline 3D Iframe Background -->
                <!-- Spline 3D Background (Glassmorph Landing Page) -->
                <div class="absolute inset-0 z-0">
                    <iframe src='https://my.spline.design/glassmorphlandingpage-90h3MsIfBV9EbtGXOm6Tt8iP/'
                        frameborder='0' width='100%' height='100%'
                        class="w-full h-full scale-125 origin-center"></iframe>
                    <!-- Light Overlay for Glass Effect -->
                    <div class="absolute inset-0 bg-white/30 backdrop-blur-[2px] pointer-events-none"></div>
                </div>

                <div class="mb-8 reveal-text relative z-10">
                    <div
                        class="hero-floater relative w-24 h-24 bg-blue-600 rounded-[2rem] flex items-center justify-center mx-auto shadow-2xl mb-8 transform rotate-3 z-10">
                        <span class="text-white font-black text-4xl drop-shadow-lg">S</span>
                        <div class="absolute inset-0 bg-white opacity-20 rounded-[2rem] blur-sm -z-10"></div>
                    </div>
                    <h2 class="text-5xl md:text-7xl font-black mb-8 tracking-tight text-gray-900">
                        Your Book's Second Life.
                    </h2>
                    <p class="text-xl text-gray-500 mb-12 max-w-2xl mx-auto">
                        기술적 고민을 통해, 책과 사람을 잇는 가장 안전하고 따뜻한 방법을 만들었습니다.
                    </p>
                </div>

                <div class="reveal-text relative z-10 transition-all duration-1000 delay-300">
                    <a href="/home"
                        class="group inline-flex items-center gap-3 bg-gray-900 text-white px-10 py-5 rounded-full font-bold text-lg hover:bg-black transition-all shadow-xl hover:shadow-2xl hover:-translate-y-1">
                        서비스 시연 시작하기
                        <i data-lucide="arrow-right" class="w-5 h-5 group-hover:translate-x-1 transition-transform"></i>
                    </a>
                </div>

                <footer class="absolute bottom-8 text-sm text-gray-400">
                    &copy; 2026 SecondHand Books Team. All rights reserved.
                </footer>
            </section>

            <script>
                if (typeof lucide !== 'undefined') lucide.createIcons();

                const observerOptions = {
                    root: null,
                    rootMargin: '0px',
                    threshold: 0.1
                };

                const observer = new IntersectionObserver((entries, observer) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            entry.target.classList.add('active');
                            observer.unobserve(entry.target);
                        }
                    });
                }, observerOptions);

                document.querySelectorAll('.reveal-text, .reveal-card, .reveal-image').forEach(el => {
                    observer.observe(el);
                });

                /* --- INTERACTIVE 3D ENGINE --- */
                class InteractiveManager {
                    constructor() {
                        // Apply to elements explicitly marked with .interactive-3d
                        this.targets = document.querySelectorAll('.interactive-3d');
                        this.init();
                        this.initCursor();
                    }

                    init() {
                        this.targets.forEach(card => {
                            if (!card.classList.contains('interactive-card')) {
                                card.classList.add('interactive-card');

                                // Spotlight Container
                                const spotlight = document.createElement('div');
                                spotlight.classList.add('spotlight-overlay');
                                card.appendChild(spotlight);

                                card.addEventListener('mousemove', (e) => this.handleTilt(e, card));
                                card.addEventListener('mouseleave', () => this.resetTilt(card));
                            }
                        });
                    }

                    handleTilt(e, card) {
                        const rect = card.getBoundingClientRect();
                        const x = e.clientX - rect.left;
                        const y = e.clientY - rect.top;

                        // Set CSS Vars for Spotlight
                        card.style.setProperty('--mouse-x', `${x}px`);
                        card.style.setProperty('--mouse-y', `${y}px`);

                        // Calculate Tilt
                        const centerX = rect.width / 2;
                        const centerY = rect.height / 2;
                        const rotateX = ((y - centerY) / centerY) * -8; // Max 8deg
                        const rotateY = ((x - centerX) / centerX) * 8;

                        card.style.transform = `perspective(1000px) rotateX(\${rotateX}deg) rotateY(\${rotateY}deg) scale3d(1.02, 1.02, 1.02)`;
                    }

                    resetTilt(card) {
                        card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) scale3d(1, 1, 1)';
                    }

                    initCursor() {
                        const cursor = document.createElement('div');
                        cursor.classList.add('cursor-glow');
                        document.body.appendChild(cursor);

                        document.addEventListener('mousemove', (e) => {
                            cursor.style.left = e.clientX + 'px';
                            cursor.style.top = e.clientY + 'px';
                        });
                    }
                }

                // Initialize after DOM load
                document.addEventListener('DOMContentLoaded', () => {
                    new InteractiveManager();
                    loadInfrastructure();
                    loadRedisVisualizer();
                    loadLoadTestVisualizer();
                });

                /* --- ARCHITECTURE VISUALIZER (AJAX) --- */
                function loadInfrastructure() {
                    const container = document.getElementById('infra-ajax-container');
                    if (!container) return;

                    fetch('${pageContext.request.contextPath}/resources/presentation/infra_visualizer.html')
                        .then(response => {
                            if (!response.ok) throw new Error("Network response was not ok");
                            return response.text();
                        })
                        .then(html => {
                            container.innerHTML = html;
                            if (typeof lucide !== 'undefined') lucide.createIcons();
                        })
                        .catch(err => {
                            console.error('Failed to load infra visualizer', err);
                            container.innerHTML = '<div class="text-red-500 text-center">Failed to load infrastructure data.</div>';
                        });
                }

                /* --- REDIS VISUALIZER (AJAX) --- */
                function loadRedisVisualizer() {
                    const container = document.getElementById('redis-ajax-container');
                    if (!container) return;

                    fetch('${pageContext.request.contextPath}/resources/presentation/redis_visualizer.html')
                        .then(response => {
                            if (!response.ok) throw new Error("Network response was not ok");
                            return response.text();
                        })
                        .then(html => {
                            container.innerHTML = html;
                            if (typeof lucide !== 'undefined') lucide.createIcons();
                        })
                        .catch(err => {
                            console.error('Failed to load redis visualizer', err);
                            container.innerHTML = '<div class="text-red-500 text-center">Failed to load Redis strategy data.</div>';
                        });
                }

                /* --- LOAD TEST VISUALIZER (AJAX & CHARTS) --- */
                function loadLoadTestVisualizer() {
                    const container = document.getElementById('loadtest-ajax-container');
                    if (!container) return;

                    fetch('${pageContext.request.contextPath}/resources/presentation/loadtest_visualizer.html')
                        .then(response => {
                            if (!response.ok) throw new Error("Network response was not ok");
                            return response.text();
                        })
                        .then(html => {
                            container.innerHTML = html;
                            if (typeof lucide !== 'undefined') lucide.createIcons();
                            // Initialize Chart after HTML injection
                            initLoadTestCharts('smoke');
                        })
                        .catch(err => {
                            console.error('Failed to load loadtest visualizer', err);
                            container.innerHTML = '<div class="text-red-500 text-center">Failed to load Performance data.</div>';
                        });
                }

                /* --- LOAD TEST DATA & CHART LOGIC --- */
                let activeChart = null;

                const loadTestData = {
                    'smoke': {
                        title: '서비스 안정성 검증',
                        desc: '인프라 개선 과정을 포함한 최소 사양 및 기초 안정성 테스트입니다.',
                        badge: '인프라 개선 완료',
                        badgeColor: 'text-green-700 bg-green-100 border-green-200',
                        duration: '2m 30s',
                        totalReq: '893',
                        instance: 't3.small (Scaled Up)',
                        scaling: 'Min 2 / Max 8',
                        metrics: {
                            vus: '5',
                            latency: '73.0',
                            latencyUnit: 'ms',
                            success: '100',
                            rps: '1.54'
                        },
                        infraColor: 'from-slate-800 to-slate-900',
                        conclusion: {
                            summary: 't3.micro 환경의 한계를 확인하고 t3.small로 스케일업 및 오토스케일링 확장을 결정했습니다. 초기 불안정성을 완전히 해소하고 안정적인 베이스라인을 확보했습니다.',
                            tech: '서버 사양뿐만 아니라 최소 가용 인스턴스(Min Size) 확보가 초기 트래픽 급증 대응(Warm-up)에 얼마나 중요한지 확인하였습니다.'
                        },
                        series: [{
                            name: 'CPU Usage (%)',
                            type: 'area',
                            data: [5, 78.5, 15, 8]
                        }, {
                            name: 'Latency (ms)',
                            type: 'line',
                            data: [150, 2200, 44, 30]
                        }],
                        categories: ['10:15', '10:30', '10:45', '11:00'],
                        colors: ['#6366f1', '#f59e0b']
                    },
                    'load': {
                        title: '시스템 한계 성능 측정',
                        desc: '초기 인프라 환경(t3.micro)에서 50명의 동시 사용자를 수용하는 부하 테스트입니다.',
                        badge: '성능 기준 충족',
                        badgeColor: 'text-blue-700 bg-blue-100 border-blue-200',
                        duration: '2m 30s',
                        totalReq: '2,450',
                        instance: 't3.micro (1GB)',
                        scaling: 'Min 1 / Max 8',
                        metrics: {
                            vus: '50',
                            latency: '44.7',
                            latencyUnit: 'ms',
                            success: '100',
                            rps: '18.2'
                        },
                        infraColor: 'from-indigo-900 to-slate-900',
                        conclusion: {
                            summary: '초기 t3.micro 환경에서는 50명의 사용자 부하에도 CPU 점유율이 급증하며 불안정한 모습을 보였습니다. 이를 통해 서버 스케일업의 필요성을 정량적 데이터로 입증하였습니다.',
                            tech: 't3.micro(1GB RAM) 사양은 단일 인스턴스에서 JVM Heap과 OS 영역을 포함해 동시 접속자 50명을 처리하기에는 메모리 여유가 부족함을 확인했습니다.'
                        },
                        series: [{
                            name: 'VUs',
                            type: 'area',
                            data: [0, 10, 50, 50, 10, 0]
                        }, {
                            name: 'Latency (ms)',
                            type: 'line',
                            data: [25, 32, 38, 44.7, 30, 25]
                        }],
                        categories: ['0s', '30s', '60s', '90s', '120s', '135s'],
                        colors: ['#3b82f6', '#f59e0b']
                    },
                    'stress': {
                        title: '극한 부하 한계 테스트',
                        desc: '1500명의 동시 사용자를 투입하여 시스템의 붕괴 지점과 오토스케일링 성능을 검증합니다.',
                        badge: '극한 부하 견딤',
                        badgeColor: 'text-red-700 bg-red-100 border-red-200',
                        duration: '10m 00s',
                        totalReq: '380,866',
                        instance: 't3.small (Autoscaling)',
                        scaling: 'Min 2 / Max 8',
                        metrics: {
                            vus: '1,500',
                            latency: '5.05',
                            latencyUnit: 's',
                            success: '99.98',
                            rps: '667.9'
                        },
                        infraColor: 'from-red-900 to-slate-900',
                        conclusion: {
                            summary: '1500명의 극한 부하에서도 0.02%라는 경이로운 에러율을 기록했습니다. 지연 시간이 5.05초로 임계값을 미세하게 넘었으나, 시스템 전체 가용성은 성공적으로 유지되었습니다.',
                            tech: 'CPU 70% 기반 오토스케일링이 1500명의 유입 속도를 완벽히 따라가기에는 다소 지연(Scale-out Lag)이 발생했으나, 서킷 브레이커 없이도 DB 커넥션 풀이 고갈되지 않고 버텨냈습니다.'
                        },
                        series: [{
                            name: 'VUs',
                            type: 'area',
                            data: [0, 200, 500, 800, 1200, 1200, 1500, 1500, 0]
                        }, {
                            name: 'Latency (ms)',
                            type: 'line',
                            data: [50, 120, 450, 800, 1800, 2100, 4200, 5050, 100]
                        }],
                        categories: ['0m', '1m', '2m', '3m', '4m', '6m', '7m', '9m', '10m'],
                        colors: ['#ef4444', '#f59e0b']
                    },
                    'spike': {
                        title: '순간 부하 대응력 테스트',
                        desc: '사용자 수가 갑작스럽게 300명으로 급증할 때 서버의 복구 능력과 안정성을 확인합니다.',
                        badge: '순간 부하 방어 성공',
                        badgeColor: 'text-purple-700 bg-purple-100 border-purple-200',
                        duration: '3m 20s',
                        totalReq: '28,629',
                        instance: 't3.small (Autoscaling)',
                        scaling: 'Min 2 / Max 8',
                        metrics: {
                            vus: '300',
                            latency: '2.45',
                            latencyUnit: 's',
                            success: '99.99',
                            rps: '167.5'
                        },
                        infraColor: 'from-purple-900 to-slate-900',
                        conclusion: {
                            summary: '300명으로의 급격한 사용자 유입 상황에서 p95 응답 속도가 2.45초로 안정권에 머물렀습니다. 단 1회의 타임아웃만 발생하여 뛰어난 스파이크 대응력을 보여주었습니다.',
                            tech: '메인 엔드포인트에서 1건의 타임아웃이 발생했으며, 최대 응답 시간이 일시적으로 상승한 지점이 발견되었습니다. 이는 순간적인 네트워크/스레드 풀 경합일 가능성이 있어 큐 튜닝이 필요할 수 있습니다.'
                        },
                        series: [{
                            name: 'VUs',
                            type: 'area',
                            data: [0, 10, 300, 300, 10, 10, 0]
                        }, {
                            name: 'Latency (ms)',
                            type: 'line',
                            data: [40, 60, 1200, 2450, 500, 80, 50]
                        }],
                        categories: ['0s', '30s', '40s', '100s', '110s', '140s', '170s'],
                        colors: ['#8b5cf6', '#f59e0b']
                    }
                };

                window.switchLoadTestTab = function (type) {
                    document.querySelectorAll('.load-tab-btn').forEach(btn => {
                        if (btn.id === 'load-tab-btn-' + type) {
                            // Active Style
                            btn.classList.add('bg-white', 'shadow-sm', 'border-gray-200');
                            btn.classList.remove('text-gray-500', 'hover:text-gray-700', 'hover:bg-gray-100');

                            if (type === 'stress') btn.classList.add('text-red-600');
                            else if (type === 'spike') btn.classList.add('text-purple-600');
                            else btn.classList.add('text-indigo-600');
                        } else {
                            // Inactive Style
                            btn.classList.remove('bg-white', 'text-indigo-600', 'text-red-600', 'text-purple-600', 'shadow-sm', 'border-gray-200');
                            btn.classList.add('text-gray-500', 'hover:text-gray-700', 'hover:bg-gray-100');
                        }
                    });

                    initLoadTestCharts(type);
                };

                function initLoadTestCharts(type) {
                    const data = loadTestData[type];
                    if (!data) return;

                    // Update Text Content
                    document.getElementById('load-test-title').innerText = data.title;
                    document.getElementById('load-test-desc').innerText = data.desc;

                    const badge = document.getElementById('load-test-badge');
                    badge.innerText = data.badge;
                    badge.className = `px-3 py-1.5 rounded-full font-bold text-xs flex items-center gap-2 shadow-sm border shrink-0 \${data.badgeColor}`;

                    // Stats
                    document.getElementById('load-stat-duration').innerText = data.duration;
                    document.getElementById('load-stat-reqs').innerText = data.totalReq;

                    // Infra
                    document.getElementById('infra-instance').innerText = data.instance;
                    document.getElementById('infra-scaling').innerText = data.scaling;
                    // Infra Card BG Update
                    document.getElementById('infra-card-bg').className = `bg-gradient-to-br p-6 rounded-2xl shadow-lg text-white h-full relative overflow-hidden group \${data.infraColor}`;

                    // Key Metrics
                    document.getElementById('metric-vus').innerText = data.metrics.vus;
                    document.getElementById('metric-latency').innerText = data.metrics.latency;
                    document.getElementById('unit-latency').innerText = data.metrics.latencyUnit;
                    document.getElementById('metric-success').innerText = data.metrics.success;
                    document.getElementById('metric-rps').innerText = data.metrics.rps;

                    // Conclusion
                    document.getElementById('conclusion-summary').innerText = data.conclusion.summary;
                    document.getElementById('conclusion-tech').innerText = data.conclusion.tech;

                    // Conclusion Box Color
                    const conclusionBox = document.getElementById('conclusion-box');
                    if (type === 'stress') conclusionBox.className = "p-8 rounded-[2rem] shadow-lg text-white relative overflow-hidden transition-colors duration-500 bg-red-900";
                    else if (type === 'spike') conclusionBox.className = "p-8 rounded-[2rem] shadow-lg text-white relative overflow-hidden transition-colors duration-500 bg-purple-900";
                    else if (type === 'load') conclusionBox.className = "p-8 rounded-[2rem] shadow-lg text-white relative overflow-hidden transition-colors duration-500 bg-indigo-900";
                    else conclusionBox.className = "p-8 rounded-[2rem] shadow-lg text-white relative overflow-hidden transition-colors duration-500 bg-slate-800";

                    // Chart Rendering
                    if (activeChart) {
                        activeChart.destroy();
                    }

                    const options = {
                        series: data.series,
                        chart: {
                            height: 350,
                            type: 'area', // default, mixed in series
                            toolbar: { show: false },
                            zoom: { enabled: false }
                        },
                        dataLabels: { enabled: false },
                        stroke: {
                            curve: 'smooth',
                            width: [3, 3]
                        },
                        colors: data.colors,
                        fill: {
                            type: ['gradient', 'solid'],
                            gradient: {
                                shadeIntensity: 1,
                                opacityFrom: 0.3,
                                opacityTo: 0.05,
                                stops: [0, 90, 100]
                            }
                        },
                        xaxis: {
                            categories: data.categories,
                            axisBorder: { show: false },
                            axisTicks: { show: false }
                        },
                        yaxis: [
                            {
                                axisTicks: { show: true },
                                axisBorder: { show: true, color: data.colors[0] },
                                labels: { style: { colors: data.colors[0] } },
                                title: { text: "VUs / CPU", style: { color: data.colors[0] } }
                            },
                            {
                                opposite: true,
                                axisTicks: { show: true },
                                axisBorder: { show: true, color: data.colors[1] },
                                labels: { style: { colors: data.colors[1] } },
                                title: { text: "Latency (ms)", style: { color: data.colors[1] } }
                            }
                        ],
                        tooltip: {
                            shared: true,
                            intersect: false,
                            y: {
                                formatter: function (y) {
                                    if (typeof y !== "undefined") {
                                        return y.toFixed(0);
                                    }
                                    return y;
                                }
                            }
                        },
                        grid: {
                            borderColor: '#f1f5f9'
                        }
                    };

                    activeChart = new ApexCharts(document.querySelector("#loadTestChart"), options);
                    activeChart.render();
                }

                // Global functions for Redis Visualizer Interactivity
                window.switchRedisTab = function (tabName) {
                    // Buttons
                    document.querySelectorAll('.redis-tab-btn').forEach(btn => {
                        if (btn.id === 'redis-tab-btn-' + tabName) {
                            btn.classList.add('bg-white', 'text-blue-600', 'shadow-sm', 'border-gray-200');
                            btn.classList.remove('text-gray-500', 'hover:text-gray-700', 'hover:bg-gray-100');
                        } else {
                            btn.classList.remove('bg-white', 'text-blue-600', 'shadow-sm', 'border-gray-200');
                            btn.classList.add('text-gray-500', 'hover:text-gray-700', 'hover:bg-gray-100');
                        }
                    });
                    // Content
                    document.querySelectorAll('.redis-tab-content').forEach(content => {
                        if (content.id === 'redis-content-' + tabName) {
                            content.classList.remove('hidden');
                        } else {
                            content.classList.add('hidden');
                        }
                    });
                };

                window.setEvictionAction = function (action) {
                    // Reset all buttons
                    document.querySelectorAll('.evt-btn').forEach(btn => {
                        btn.className = "evt-btn px-4 py-2 rounded-xl text-xs font-black transition-all text-gray-500 hover:text-gray-700 flex items-center gap-1";
                    });

                    const activeBtn = document.getElementById('evt-btn-' + action);
                    const badge = document.getElementById('evt-badge');
                    const endpoint = document.getElementById('evt-endpoint');
                    const iconBox = document.getElementById('evt-icon-box');
                    const query = document.getElementById('evt-query');

                    let colorClass = "";
                    let endpointText = "";
                    let queryText = "";
                    let borderColor = "";

                    if (action === 'INSERT') {
                        colorClass = "bg-green-500";
                        endpointText = "POST /bookclubs";
                        queryText = "INSERT INTO book_clubs ...";
                        borderColor = "border-green-200";
                        activeBtn.className = "evt-btn px-4 py-2 rounded-xl text-xs font-black transition-all bg-green-500 text-white shadow-md scale-105 flex items-center gap-1";
                    } else if (action === 'UPDATE') {
                        colorClass = "bg-blue-500";
                        endpointText = "POST /bookclubs/1";
                        queryText = "UPDATE book_clubs SET ...";
                        borderColor = "border-blue-200";
                        activeBtn.className = "evt-btn px-4 py-2 rounded-xl text-xs font-black transition-all bg-blue-500 text-white shadow-md scale-105 flex items-center gap-1";
                    } else if (action === 'DELETE') {
                        colorClass = "bg-red-500";
                        endpointText = "POST /bookclubs/1/delete";
                        queryText = "DELETE FROM book_clubs WHERE id=1";
                        borderColor = "border-red-200";
                        activeBtn.className = "evt-btn px-4 py-2 rounded-xl text-xs font-black transition-all bg-red-500 text-white shadow-md scale-105 flex items-center gap-1";
                    }

                    badge.className = "px-2 py-0.5 rounded text-[10px] font-black text-white " + colorClass;
                    endpoint.innerText = endpointText;
                    query.innerText = queryText;
                    iconBox.className = `w-20 h-20 rounded-3xl flex items-center justify-center shadow-lg border-2 transition-colors duration-500 \${borderColor} bg-white text-gray-700`;
                };

                window.setReadFlowHit = function (isHit) {
                    const btnHit = document.getElementById('btn-hit');
                    const btnMiss = document.getElementById('btn-miss');
                    const pulseDot = document.getElementById('read-pulse-dot');
                    const msgHit = document.getElementById('msg-hit');
                    const msgMiss = document.getElementById('msg-miss');
                    const redisBox = document.getElementById('redis-box');
                    const repoSection = document.getElementById('repo-section');
                    const returnHit = document.getElementById('return-hit');
                    const returnMiss = document.getElementById('return-miss');

                    if (isHit) {
                        btnHit.classList.add('bg-white', 'text-green-600', 'shadow-sm', 'border-gray-100');
                        btnHit.classList.remove('text-gray-500', 'hover:text-gray-700');
                        btnMiss.classList.remove('bg-white', 'text-orange-600', 'shadow-sm', 'border-gray-100');
                        btnMiss.classList.add('text-gray-500', 'hover:text-gray-700');

                        pulseDot.className = "w-2.5 h-2.5 rounded-full bg-green-400 animate-pulse shadow-[0_0_10px_rgba(74,222,128,0.5)]";
                        msgHit.classList.remove('hidden');
                        msgMiss.classList.add('hidden');

                        redisBox.className = "p-5 rounded-2xl border-2 transition-all duration-500 border-green-400 bg-green-500 shadow-xl scale-105 flex flex-col items-center";
                        redisBox.innerHTML = '<i data-lucide="database-zap" class="text-white w-8 h-8"></i><span class="text-[10px] font-black block mt-2 text-center uppercase text-white tracking-wider">Redis Store</span>';
                        if (typeof lucide !== 'undefined') lucide.createIcons({ root: redisBox });

                        repoSection.classList.add('opacity-0', '-translate-y-4', 'max-h-0');
                        repoSection.classList.remove('max-h-[1000px]', 'opacity-100', 'translate-y-0');

                        returnHit.classList.remove('hidden');
                        returnMiss.classList.add('hidden');
                    } else {
                        btnHit.classList.remove('bg-white', 'text-green-600', 'shadow-sm', 'border-gray-100');
                        btnHit.classList.add('text-gray-500', 'hover:text-gray-700');
                        btnMiss.classList.add('bg-white', 'text-orange-600', 'shadow-sm', 'border-gray-100');
                        btnMiss.classList.remove('text-gray-500', 'hover:text-gray-700');

                        pulseDot.className = "w-2.5 h-2.5 rounded-full bg-orange-400 animate-pulse shadow-[0_0_10px_rgba(251,146,60,0.5)]";
                        msgHit.classList.add('hidden');
                        msgMiss.classList.remove('hidden');

                        redisBox.className = "p-5 rounded-2xl border-2 transition-all duration-500 border-white/20 bg-white/5 flex flex-col items-center";
                        // Note: To keep the icon white/gray when inactive
                        redisBox.innerHTML = '<i data-lucide="database" class="text-blue-100/50 w-8 h-8"></i><span class="text-[10px] font-black block mt-2 text-center uppercase text-blue-100/50 tracking-wider">Redis Store</span>';
                        if (typeof lucide !== 'undefined') lucide.createIcons({ root: redisBox });

                        repoSection.classList.remove('opacity-0', '-translate-y-4', 'max-h-0');
                        repoSection.classList.add('max-h-[1000px]', 'opacity-100', 'translate-y-0');

                        returnHit.classList.add('hidden');
                        returnMiss.classList.remove('hidden');
                    }
                };

                window.switchInfraTab = function (tabName) {
                    // Toggle buttons
                    document.querySelectorAll('.infra-tab-btn').forEach(btn => {
                        if (btn.id === 'tab-btn-' + tabName) {
                            btn.classList.add('bg-white', 'text-gray-900', 'shadow-md', 'border-gray-100');
                            btn.classList.remove('text-gray-400', 'hover:text-gray-600', 'hover:bg-gray-100');
                        } else {
                            btn.classList.remove('bg-white', 'text-gray-900', 'shadow-md', 'border-gray-100');
                            btn.classList.add('text-gray-400', 'hover:text-gray-600', 'hover:bg-gray-100');
                        }
                    });

                    // Toggle content
                    document.querySelectorAll('.infra-tab-content').forEach(content => {
                        if (content.id === 'infra-content-' + tabName) {
                            content.classList.remove('hidden');
                        } else {
                            content.classList.add('hidden');
                        }
                    });
                };
            </script>
        </body>

        </html>
