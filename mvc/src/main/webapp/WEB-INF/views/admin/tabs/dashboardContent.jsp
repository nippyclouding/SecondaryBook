<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm relative overflow-hidden group">
        <div class="absolute right-0 top-0 w-24 h-24 bg-primary-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
        <div class="relative z-10">
            <p class="text-gray-500 text-xs font-bold uppercase mb-1">Total Users</p>
            <h3 class="text-3xl font-black text-gray-900 mb-2"><fmt:formatNumber value="${memberCount}" pattern="#,###" /></h3>
            <div class="flex items-center gap-1 text-xs font-bold text-emerald-500"><i data-lucide="users" class="w-3 h-3"></i> 전체 회원</div>
        </div>
    </div>
    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm relative overflow-hidden group">
        <div class="absolute right-0 top-0 w-24 h-24 bg-purple-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
        <div class="relative z-10">
            <p class="text-gray-500 text-xs font-bold uppercase mb-1">Active Trades</p>
            <h3 class="text-3xl font-black text-gray-900 mb-2"><fmt:formatNumber value="${tradeCount}" pattern="#,###" /></h3>
            <div class="flex items-center gap-1 text-xs font-bold text-emerald-500"><i data-lucide="book" class="w-3 h-3"></i> 등록된 상품</div>
        </div>
    </div>
    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm relative overflow-hidden group">
        <div class="absolute right-0 top-0 w-24 h-24 bg-orange-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
        <div class="relative z-10">
            <p class="text-gray-500 text-xs font-bold uppercase mb-1">Total Groups</p>
            <h3 class="text-3xl font-black text-gray-900 mb-2"><fmt:formatNumber value="${clubCount}" pattern="#,###" /></h3>
            <div class="flex items-center gap-1 text-xs font-bold text-primary-600"><i data-lucide="users" class="w-3 h-3"></i> 운영 중인 모임</div>
        </div>
    </div>
</div>

<div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
        <h3 class="font-bold text-gray-900 mb-4 text-sm">주간 가입 및 거래 추이</h3>
        <div class="h-64"><canvas id="mainChart"></canvas></div>
    </div>
    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
        <h3 class="font-bold text-gray-900 mb-4 text-sm">카테고리별 거래 비중</h3>
        <div class="h-64 flex justify-center"><canvas id="doughnutChart"></canvas></div>
    </div>
</div>

<script>
    // [1] 최근 7일 날짜 배열 생성 함수 ('YYYY-MM-DD')
    function getLast7Days() {
        const dates = [];
        for (let i = 6; i >= 0; i--) {
            const d = new Date();
            d.setDate(d.getDate() - i);
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            dates.push(`\${year}-\${month}-\${day}`);
        }
        return dates;
    }

    // [2] 빈 날짜 0으로 채우는 함수
    function fillDataGap(dateLabels, rawData) {
        // DB 데이터를 날짜별로 매핑하기 편하게 변환 (예: {'2026-01-20': 5, ...})
        const dataMap = {};
        if (rawData) {
            rawData.forEach(item => {
                dataMap[item.date] = item.count;
            });
        }

        // 기준 날짜 배열(dateLabels)을 순회하며 데이터가 없으면 0 넣기
        return dateLabels.map(date => dataMap[date] || 0);
    }

    async function loadCharts() {
        try {
            const response = await fetch('/admin/api/stats');
            if (!response.ok) throw new Error('Network response was not ok');
            const res = await response.json();

            // 1. X축 라벨 생성 (오늘 기준 최근 7일)
            const labels = getLast7Days();

            // 2. DB 데이터와 날짜 매핑 (빈 날짜는 0으로)
            const signupData = fillDataGap(labels, res.dailySignups);
            const tradeData = fillDataGap(labels, res.dailyTrades);

            // 3. 차트 그리기
            renderMainChart(labels, signupData, tradeData);
            renderDoughnutChart(res.categories);

        } catch (err) {
            console.error("Chart load failed", err);
        }
    }

    function renderMainChart(labels, signupData, tradeData) {
        const ctx = document.getElementById('mainChart').getContext('2d');

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels, // 생성한 7일 날짜 배열
                datasets: [
                    {
                        label: '신규 가입',
                        data: signupData, // 0이 채워진 데이터
                        borderColor: '#3b82f6',
                        backgroundColor: 'rgba(59, 130, 246, 0.1)',
                        fill: true,
                        tension: 0.4
                    },
                    {
                        label: '상품 등록',
                        data: tradeData, // 0이 채워진 데이터
                        borderColor: '#10b981',
                        borderDash: [5, 5],
                        fill: false,
                        tension: 0.4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false,
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 } // 정수 단위로 표시
                    }
                }
            }
        });
    }

    function renderDoughnutChart(categories) {
        const ctx = document.getElementById('doughnutChart').getContext('2d');

        const labels = categories ? categories.map(c => c.name) : [];
        const data = categories ? categories.map(c => c.count) : [];

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: ['#3b82f6', '#34d399', '#f59e0b', '#ef4444', '#a78bfa'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'right' } },
                cutout: '70%'
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function() {
        loadCharts();
    });
</script>