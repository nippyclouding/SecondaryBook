<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="flex gap-6 h-[800px]">
  <div class="w-1/3 bg-white p-6 rounded-2xl border border-gray-200 overflow-y-auto">
    <h3 class="font-bold text-lg text-gray-900 mb-4">배너 편집</h3>
    <div class="space-y-4">
      <div>
        <label class="text-xs font-bold text-gray-500">제목</label>
        <input type="text" id="bannerTitle" class="w-full border rounded p-2 text-sm" oninput="updatePreview()" value="신뢰할 수 있는 중고 거래">
      </div>
      <div>
        <label class="text-xs font-bold text-gray-500">부제목</label>
        <textarea id="bannerSubtitle" class="w-full border rounded p-2 text-sm" rows="2" oninput="updatePreview()">금융의 안전함을 책 거래에도 담았습니다.</textarea>
      </div>

      <div class="grid grid-cols-2 gap-2">
        <div>
          <label class="text-xs font-bold text-gray-500">배경색 (시작)</label>
          <input type="color" id="colorFrom" class="w-full h-10 p-0 border-0 cursor-pointer" oninput="updatePreview()" value="#2563eb">
        </div>
        <div>
          <label class="text-xs font-bold text-gray-500">배경색 (끝)</label>
          <input type="color" id="colorTo" class="w-full h-10 p-0 border-0 cursor-pointer" oninput="updatePreview()" value="#3b82f6">
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="text-xs font-bold text-gray-500 mb-1 block">텍스트 정렬</label>
          <select id="textAlign" class="w-full border rounded p-2 text-sm" onchange="updatePreview()">
            <option value="text-left">왼쪽</option>
            <option value="text-center">중앙</option>
            <option value="text-right">오른쪽</option>
          </select>
        </div>
        <div>
          <label class="text-xs font-bold text-gray-500 mb-1 block">아이콘</label>
          <select id="iconName" class="w-full border rounded p-2 text-sm" onchange="updatePreview()">
            <option value="shield-check">보안 (Shield)</option>
            <option value="book-open">책 (Book)</option>
            <option value="gift">선물 (Gift)</option>
            <option value="star">별 (Star)</option>
            <option value="megaphone">확성기 (Megaphone)</option>
          </select>
        </div>
      </div>

      <div class="border-t pt-4 mt-2">
        <label class="text-xs font-bold text-gray-500">버튼 텍스트</label>
        <input type="text" id="btnText" class="w-full border rounded p-2 text-sm mb-2" oninput="updatePreview()" value="바로가기">

        <label class="text-xs font-bold text-gray-500">이동 링크 (URL)</label>
        <div class="flex gap-2">
          <input type="text" id="btnLink" class="w-full border rounded p-2 text-sm flex-1" value="/trade">
          <button onclick="openPageModal()" class="px-3 py-2 bg-gray-100 border rounded text-xs font-bold hover:bg-gray-200">페이지 생성</button>
        </div>
        <p class="text-[10px] text-gray-400 mt-1">* '페이지 생성' 버튼을 눌러 임시 이벤트 페이지를 만들 수 있습니다.</p>
      </div>

      <button onclick="saveBanner()" class="w-full bg-primary-600 text-white py-3 rounded-lg font-bold hover:bg-primary-700 shadow-md transition transform hover:scale-[1.02]">배너 저장</button>
    </div>
  </div>

  <div class="flex-1 flex flex-col gap-6">
    <div class="bg-gray-50 p-6 rounded-2xl border border-gray-200">
      <h3 class="font-bold text-sm text-gray-500 mb-2">Real-time Preview</h3>
      <div id="bannerPreview" class="relative overflow-hidden rounded-lg shadow-xl h-[280px] flex flex-col justify-center p-12 transition-all duration-300" style="background: linear-gradient(to right, #2563eb, #3b82f6);">
        <div id="previewContent" class="z-10 text-white max-w-lg w-full text-left">
          <i id="previewIcon" data-lucide="shield-check" class="w-12 h-12 text-white/80 mb-4 inline-block"></i>
          <h1 id="previewTitle" class="text-4xl font-extrabold mb-3 leading-tight drop-shadow-md">제목 미리보기</h1>
          <p id="previewSubtitle" class="text-white/90 text-lg mb-6 font-medium drop-shadow-sm">부제목 미리보기</p>
          <a href="#" id="previewBtn" class="bg-white text-gray-900 px-6 py-2.5 rounded-md font-bold text-sm hover:bg-gray-100 transition shadow-md inline-block">바로가기</a>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 overflow-hidden flex-1 flex flex-col">
      <div class="p-4 bg-gray-50 border-b font-bold text-sm flex justify-between items-center">
        <span>등록된 배너 목록</span>
        <button onclick="loadBannerList()" class="text-xs text-blue-500 hover:underline">새로고침</button>
      </div>
      <ul id="bannerList" class="divide-y divide-gray-100 overflow-y-auto flex-1 p-2 space-y-2">
      </ul>
    </div>
  </div>
</div>

<div id="pageModal" class="hidden fixed inset-0 bg-black/50 z-50 flex items-center justify-center backdrop-blur-sm">
  <div class="bg-white rounded-xl shadow-2xl w-[600px] max-h-[90vh] flex flex-col overflow-hidden">
    <div class="p-4 border-b flex justify-between items-center bg-gray-50">
      <h3 class="font-bold">임시 이벤트 페이지 생성</h3>
      <button onclick="closePageModal()" class="text-gray-400 hover:text-gray-600"><i data-lucide="x" class="w-5 h-5"></i></button>
    </div>
    <div class="p-6 space-y-4 overflow-y-auto">
      <div>
        <label class="block text-xs font-bold text-gray-500 mb-1">페이지 제목</label>
        <input type="text" id="tempPageTitle" class="w-full border rounded p-2 text-sm" placeholder="예: 여름방학 이벤트">
      </div>
      <div>
        <label class="block text-xs font-bold text-gray-500 mb-1">내용 (HTML)</label>
        <textarea id="tempPageContent" class="w-full border rounded p-2 text-sm font-mono bg-gray-50" rows="10" placeholder="<h1>이벤트 안내</h1><p>내용을 입력하세요...</p>"></textarea>
        <p class="text-[10px] text-gray-400 mt-1">* 간단한 HTML 태그를 사용할 수 있습니다.</p>
      </div>
    </div>
    <div class="p-4 border-t bg-gray-50 flex justify-end gap-2">
      <button onclick="closePageModal()" class="px-4 py-2 bg-white border rounded text-sm hover:bg-gray-50">취소</button>
      <button onclick="saveTempPage()" class="px-4 py-2 bg-blue-600 text-white rounded text-sm font-bold hover:bg-blue-700">페이지 생성 & 링크 적용</button>
    </div>
  </div>
</div>

<script>
  // 1. 미리보기 업데이트 (정렬 기능 수정됨)
  window.updatePreview = function() {
    const title = document.getElementById('bannerTitle').value;
    const sub = document.getElementById('bannerSubtitle').value;
    const c1 = document.getElementById('colorFrom').value;
    const c2 = document.getElementById('colorTo').value;
    const btn = document.getElementById('btnText').value;
    const align = document.getElementById('textAlign').value;
    const icon = document.getElementById('iconName').value;

    const container = document.getElementById('bannerPreview');
    const contentDiv = document.getElementById('previewContent');
    const iconEl = document.getElementById('previewIcon');

    // 텍스트 & 색상 적용
    document.getElementById('previewTitle').innerText = title;
    document.getElementById('previewSubtitle').innerText = sub;
    document.getElementById('previewBtn').innerText = btn;

    // 그라데이션 (인라인 스타일 사용)
    container.style.background = `linear-gradient(to right, \${c1}, \${c2})`;

    // 정렬 적용 (텍스트 정렬 class + 박스 위치 정렬 margin)
    let posClass = 'mr-auto'; // 기본값 (왼쪽)
    if (align === 'text-center') {
      posClass = 'mx-auto'; // 중앙 정렬
    } else if (align === 'text-right') {
      posClass = 'ml-auto'; // 우측 정렬
    }

    contentDiv.className = `z-10 text-white max-w-lg w-full \${align} \${posClass}`;

    // 아이콘 변경 (Lucide 재렌더링 필요하므로 속성 변경 후 호출)
    iconEl.setAttribute('data-lucide', icon);
    lucide.createIcons();
  }

  // 2. 배너 저장
  window.saveBanner = async function() {
    // [1] 유효성 검사
    const linkValue = document.getElementById('btnLink').value;
    if (linkValue.includes('<html') || linkValue.includes('<!DOCTYPE')) {
      alert('잘못된 링크 형식입니다. 페이지를 다시 생성해주세요.\n(HTML 코드가 링크에 포함되어 있습니다)');
      return;
    }

    const data = {
      title: document.getElementById('bannerTitle').value,
      subtitle: document.getElementById('bannerSubtitle').value,
      bgColorFrom: document.getElementById('colorFrom').value,
      bgColorTo: document.getElementById('colorTo').value,
      btnText: document.getElementById('btnText').value,
      btnLink: linkValue,
      iconName: document.getElementById('iconName').value,
      textAlign: document.getElementById('textAlign').value
    };
    // textAlign 컬럼이 없으면 subtitle 앞부분에 태그로 숨겨서 저장하는 트릭 사용
    data.subtitle = data.textAlign + '|||' + data.subtitle;

    try {
      const res = await adminFetch('/admin/api/banners', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
      });
      if (res.ok) {
        alert('배너가 게시되었습니다.');
        loadBannerList();
      } else {
        alert('저장에 실패했습니다. (서버 오류)');
      }
    } catch (e) {
      console.error(e);
      alert('저장 중 오류가 발생했습니다.');
    }
  }

  // 3. 배너 목록 로드
  window.loadBannerList = async function() {
    const res = await fetch('/admin/api/banners');
    const list = await res.json();

    const ul = document.getElementById('bannerList');
    ul.innerHTML = list.map(b => {
      // 저장된 부제목에서 정렬 정보 분리 (트릭 해제)
      let sub = b.subtitle;
      if(sub && sub.includes('|||')) sub = sub.split('|||')[1];

      return `
            <li class="p-3 bg-gray-50 rounded-lg flex justify-between items-center border border-gray-100 group hover:border-blue-200 transition">
                <div class="flex items-center gap-3 overflow-hidden">
                    <div class="w-10 h-10 rounded-md shadow-sm shrink-0" style="background: linear-gradient(to right, \${b.bgColorFrom}, \${b.bgColorTo})"></div>
                    <div class="flex flex-col min-w-0">
                        <span class="font-bold text-sm text-gray-800 truncate">\${b.title}</span>
                        <span class="text-xs text-gray-500 truncate">\${sub}</span>
                    </div>
                </div>
                <button onclick="deleteBanner(\${b.bannerSeq})" class="text-gray-400 hover:text-red-500 p-2 hover:bg-red-50 rounded transition"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
            </li>`;
    }).join('');
    lucide.createIcons();
  }

  // 4. 배너 삭제
  window.deleteBanner = async function(seq) {
    if(!confirm('삭제하시겠습니까?')) return;
    await adminFetch(`/admin/api/banners/\${seq}`, { method: 'DELETE' });
    loadBannerList();
  }

  // --- [임시 페이지 생성 로직] ---
  window.openPageModal = function() {
    document.getElementById('pageModal').classList.remove('hidden');
  }
  window.closePageModal = function() {
    document.getElementById('pageModal').classList.add('hidden');
  }
  // [수정] 임시 페이지 저장 함수
  window.saveTempPage = async function() {
    const title = document.getElementById('tempPageTitle').value;
    const content = document.getElementById('tempPageContent').value;

    if (!title || !content) {
      alert('제목과 내용을 입력하세요.');
      return;
    }

    try {
      const res = await adminFetch('/admin/api/pages', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({title, content})
      });
      if (!res.ok) {
        throw new Error("서버 저장 실패: " + res.status);
      }

      const pageId = await res.text();

      if (isNaN(pageId)) {
        throw new Error("유효하지 않은 응답입니다.");
      }

      // 링크 입력창에 자동 입력
      document.getElementById('btnLink').value = `/page/view/\${pageId}`;
      closePageModal();
      alert('페이지가 생성되었습니다. 링크가 적용되었습니다.');

    } catch (e) {
      console.error(e);
      alert('페이지 생성 중 오류가 발생했습니다. 다시 시도해 주세요.');
    }
  }
  // 초기화
  lucide.createIcons();
  updatePreview();
  loadBannerList();
  </script>