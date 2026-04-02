<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

    <div class="space-y-16 animate-[fadeIn_0.3s_ease-out]">
        <section>
            <div class="flex justify-between items-end mb-6 px-1">
                <div>
                    <h2 class="text-2xl font-black text-gray-900 tracking-tight flex items-center gap-2">
                        내 모임 <span id="my-club-count"
                            class="text-sm font-bold text-primary-600 bg-primary-50 px-2.5 py-0.5 rounded-full">0</span>
                    </h2>
                    <p class="text-sm font-medium text-gray-500 mt-1">참여 중이거나 운영 중인 모임</p>
                </div>
                <a href="/bookclubs"
                    class="flex items-center gap-2 px-5 py-2.5 bg-gray-900 text-white rounded-full text-sm font-bold hover:bg-black transition-all shadow-lg hover:shadow-xl hover:-translate-y-0.5">
                    <i data-lucide="plus" class="w-4 h-4"></i> 모임 만들기
                </a>
            </div>
            <div id="my-club-list" class="grid grid-cols-1 gap-4"></div>
            <div id="my-club-more" class="hidden text-center mt-8">
                <button onclick="GroupsTab.loadMoreMyClubs()"
                    class="px-6 py-2.5 bg-white border border-gray-200 rounded-full text-sm font-bold text-gray-600 hover:bg-gray-50 transition shadow-sm">
                    더보기 <i data-lucide="chevron-down" class="w-4 h-4 inline"></i>
                </button>
            </div>
        </section>

        <div class="border-t border-gray-100"></div>

        <section>
            <div class="mb-6 px-1">
                <h2 class="text-2xl font-black text-gray-900 tracking-tight flex items-center gap-2">
                    찜한 모임 <span id="wish-club-count"
                        class="text-sm font-bold text-red-500 bg-red-50 px-2.5 py-0.5 rounded-full">0</span>
                </h2>
                <p class="text-sm font-medium text-gray-500 mt-1">관심 등록한 모임 목록</p>
            </div>

            <div id="wish-club-list" class="grid grid-cols-1 gap-4"></div>

            <div id="wish-club-more" class="hidden text-center mt-8">
                <button onclick="GroupsTab.loadMoreWishClubs()"
                    class="px-6 py-2.5 bg-white border border-gray-200 rounded-full text-sm font-bold text-gray-600 hover:bg-gray-50 transition shadow-sm">
                    더보기 <i data-lucide="chevron-down" class="w-4 h-4 inline"></i>
                </button>
            </div>
        </section>
    </div>

    <script>
        (function () { // IIFE
            const PAGE_SIZE = 4;
            let myClubData = [];
            let myClubCursor = 0;
            let wishClubData = [];
            let wishClubCursor = 0;

            function escapeHtml(str) {
                if (!str) return '';
                return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
            }

            const methods = {
                init: () => {
                    methods.fetchMyClubs();
                    methods.fetchWishClubs();
                },

                fetchMyClubs: () => {
                    $.ajax({
                        url: '/profile/bookclub/list',
                        method: 'GET',
                        dataType: 'json',
                        success: (data) => {
                            myClubData = data || [];
                            $('#my-club-count').text(myClubData.length);
                            $('#my-club-list').empty();
                            myClubCursor = 0;
                            methods.renderMyClubs();
                        },
                        error: () => $('#my-club-list').html('<div class="text-center py-10 text-red-500">로드 실패</div>')
                    });
                },

                renderMyClubs: () => {
                    const container = $('#my-club-list');
                    const btn = $('#my-club-more');

                    if (myClubData.length === 0) {
                        container.html(methods.getEmptyHtml('가입한 모임이 없습니다.', '/bookclubs', '모임 둘러보기'));
                        btn.addClass('hidden');
                        return;
                    }

                    const nextCursor = myClubCursor + PAGE_SIZE;
                    const slice = myClubData.slice(myClubCursor, nextCursor);

                    let html = '';
                    slice.forEach(club => {
                        const img = methods.getImageUrl(club.bannerImgUrl || club.banner_img_url);
                        const link = '/bookclubs/' + (club.bookClubSeq || club.book_club_seq);
                        const isLeader = club.leader_yn === true;
                        const badge = isLeader
                            ? '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-gray-900 text-white shadow-sm tracking-wide">LEADER</span>'
                            : '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-50 text-blue-600 border border-blue-100">MEMBER</span>';

                        html += `
                    <div class="group bg-white border border-gray-100 rounded-3xl p-5 hover:shadow-lg transition-all duration-300 cursor-pointer flex gap-5 items-center relative"
                         onclick="location.href='\${link}'">
                        <div class="w-20 h-20 rounded-2xl bg-gray-50 overflow-hidden flex-shrink-0 shadow-inner">
                            <img src="\${img}" class="w-full h-full object-cover group-hover:scale-105 transition-transform"
                                 onerror="this.src='https://placehold.co/200x200?text=No+Image'">
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 mb-1">
                                \${badge}
                                <span class="text-[11px] font-bold text-gray-500 bg-gray-50 px-2 py-0.5 rounded">\${escapeHtml(club.book_club_rg || '전국')}</span>
                            </div>
                            <h4 class="text-base font-bold text-gray-900 truncate">\${escapeHtml(club.book_club_name)}</h4>
                            <p class="text-xs text-gray-500 mt-1 truncate">\${escapeHtml(club.description || club.book_club_desc || '')}</p>
                        </div>
                        <div class="hidden sm:block text-right pr-2">
                            <div class="inline-flex items-center gap-1 bg-gray-50 px-2.5 py-1 rounded-full text-xs font-bold text-gray-500">
                                <i data-lucide="users" class="w-3 h-3"></i>
                                <span>\${club.joined_member_count || 0} / \${club.maxMember || club.book_club_max_member}</span>
                            </div>
                        </div>
                    </div>`;
                    });

                    container.append(html);
                    myClubCursor = nextCursor;
                    if (myClubCursor >= myClubData.length) btn.addClass('hidden');
                    else btn.removeClass('hidden');

                    if (window.lucide) lucide.createIcons();
                },

                fetchWishClubs: () => {
                    $.ajax({
                        url: '/profile/wishlist/bookclub',
                        method: 'GET',
                        dataType: 'json',
                        success: (data) => {
                            wishClubData = data || [];
                            $('#wish-club-count').text(wishClubData.length);
                            $('#wish-club-list').empty();
                            wishClubCursor = 0;
                            methods.renderWishClubs();
                        },
                        error: () => $('#wish-club-list').html('<div class="text-center py-10 text-red-500">로드 실패</div>')
                    });
                },

                // [수정] 찜한 모임도 가로형 리스트 스타일로 변경
                renderWishClubs: () => {
                    const container = $('#wish-club-list');
                    const btn = $('#wish-club-more');

                    if (wishClubData.length === 0) {
                        container.html(methods.getEmptyHtml('찜한 모임이 없습니다.', '/bookclubs', '모임 찾기'));
                        btn.addClass('hidden');
                        return;
                    }

                    const nextCursor = wishClubCursor + PAGE_SIZE;
                    const slice = wishClubData.slice(wishClubCursor, nextCursor);

                    let html = '';
                    slice.forEach(club => {
                        const img = methods.getImageUrl(club.bannerImgUrl || club.banner_img_url);
                        const link = '/bookclubs/' + (club.bookClubSeq || club.book_club_seq);
                        const clubId = club.bookClubSeq || club.book_club_seq;

                        html += `
                    <div class="group bg-white border border-gray-100 rounded-3xl p-5 hover:shadow-lg transition-all duration-300 cursor-pointer flex gap-5 items-center relative"
                         onclick="location.href='\${link}'">

                        <div class="w-20 h-20 rounded-2xl bg-gray-50 overflow-hidden flex-shrink-0 shadow-inner">
                            <img src="\${img}" class="w-full h-full object-cover group-hover:scale-105 transition-transform"
                                 onerror="this.src='https://placehold.co/200x200?text=No+Image'">
                        </div>

                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 mb-1">
                                <span class="text-[11px] font-bold text-gray-500 bg-gray-50 px-2 py-0.5 rounded">\${escapeHtml(club.book_club_rg || '전국')}</span>
                            </div>
                            <h4 class="text-base font-bold text-gray-900 truncate">\${escapeHtml(club.book_club_name)}</h4>
                            <p class="text-xs text-gray-500 mt-1 truncate">\${escapeHtml(club.description || club.book_club_desc || '')}</p>
                        </div>

                        <div class="text-right pr-1">
                            <button onclick="GroupsTab.toggleWish(event, \${clubId})"
                                    class="p-2 bg-white border border-gray-100 rounded-full text-red-500 hover:bg-red-50 hover:border-red-100 transition shadow-sm group-hover:shadow">
                                <i data-lucide="heart" class="w-4 h-4 fill-current"></i>
                            </button>
                        </div>
                    </div>`;
                    });

                    container.append(html);
                    wishClubCursor = nextCursor;

                    if (wishClubCursor >= wishClubData.length) btn.addClass('hidden');
                    else btn.removeClass('hidden');

                    if (window.lucide) lucide.createIcons();
                },

                loadMoreMyClubs: () => methods.renderMyClubs(),
                loadMoreWishClubs: () => methods.renderWishClubs(),

                getImageUrl: (url) => {
                    if (!url) return 'https://placehold.co/400x200?text=No+Image';
                    return (url.startsWith('/') || url.startsWith('http')) ? url : '/upload/' + url;
                },

                getEmptyHtml: (msg, linkUrl, linkText) => {
                    return `
                <div class="col-span-full py-20 text-center bg-gray-50/50 rounded-[2rem] border-2 border-dashed border-gray-200">
                    <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300 shadow-sm">
                        <i data-lucide="folder-open" class="w-8 h-8"></i>
                    </div>
                    <p class="text-base text-gray-500 font-bold mb-4">\${msg}</p>
                    <a href="\${linkUrl}" class="inline-flex items-center gap-1 text-primary-600 font-bold text-sm hover:underline">\${linkText}</a>
                </div>`;
                },

                toggleWish: (event, clubId) => {
                    event.stopPropagation();
                    if (!confirm('찜 목록에서 삭제하시겠습니까?')) return;
                    fetch('/bookclubs/' + clubId + '/wish', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' }
                    }).then(res => res.json()).then(data => {
                        if (data.status === 'ok') {
                            wishClubData = wishClubData.filter(c => (c.bookClubSeq || c.book_club_seq) != clubId);
                            wishClubCursor = 0;
                            $('#wish-club-list').empty();
                            $('#wish-club-count').text(wishClubData.length);
                            methods.renderWishClubs();
                        }
                    });
                }
            };

            window.GroupsTab = methods;
            methods.init(); // 즉시 실행
        })();
    </script>