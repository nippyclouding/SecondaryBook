// 공통 페이징 렌더링 함수
function renderCommonPagination(containerId, total, curPage, size, callbackName) {
    var container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '';

    var totalPages = Math.ceil(total / size);
    if (totalPages <= 1) return; // 페이지가 1개면 표시 안함

    var wrapper = document.createElement('div');
    wrapper.className = 'flex items-center gap-1';

    // 이전 페이지 버튼
    if (curPage > 1) {
        var prevBtn = document.createElement('button');
        prevBtn.type = 'button';
        prevBtn.className = 'px-3 py-1.5 rounded border border-gray-300 bg-white text-gray-600 hover:bg-gray-50 text-xs';
        prevBtn.innerText = '이전';
        prevBtn.onclick = function() {
            window[callbackName](curPage - 1);
        };
        wrapper.appendChild(prevBtn);
    }

    // 숫자 버튼 (현재 페이지 기준 앞뒤 2개씩 표시)
    var startPage = Math.max(1, curPage - 2);
    var endPage = Math.min(totalPages, startPage + 4);
    if (endPage - startPage < 4) {
        startPage = Math.max(1, endPage - 4);
    }
    startPage = Math.max(1, startPage);

    for (var i = startPage; i <= endPage; i++) {
        var pageBtn = document.createElement('button');
        pageBtn.type = 'button';
        var baseClass = 'px-3 py-1.5 rounded border text-xs font-medium transition-all ';

        if (i === curPage) {
            pageBtn.className = baseClass + 'bg-primary-600 border-primary-600 text-white';
        } else {
            pageBtn.className = baseClass + 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50';
        }

        pageBtn.innerText = i;
        pageBtn.setAttribute('data-page', i);
        pageBtn.onclick = function() {
            window[callbackName](parseInt(this.getAttribute('data-page')));
        };
        wrapper.appendChild(pageBtn);
    }

    // 다음 페이지 버튼
    if (curPage < totalPages) {
        var nextBtn = document.createElement('button');
        nextBtn.type = 'button';
        nextBtn.className = 'px-3 py-1.5 rounded border border-gray-300 bg-white text-gray-600 hover:bg-gray-50 text-xs';
        nextBtn.innerText = '다음';
        nextBtn.onclick = function() {
            window[callbackName](curPage + 1);
        };
        wrapper.appendChild(nextBtn);
    }

    container.appendChild(wrapper);
}