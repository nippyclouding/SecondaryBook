<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="space-y-8 animate-[fadeIn_0.3s_ease-out]">
    <div class="flex justify-between items-center px-1">
        <div>
            <h2 class="text-2xl font-black text-gray-900 tracking-tight">배송지 관리</h2>
            <p class="text-sm font-medium text-gray-500 mt-1">최대 5개까지 등록할 수 있습니다.</p>
        </div>
        <button onclick="AddressTab.openModal()" class="flex items-center gap-2 px-5 py-2.5 bg-primary-600 text-white rounded-full text-sm font-bold hover:bg-primary-700 transition-all shadow-lg hover:shadow-xl hover:-translate-y-0.5">
            <i data-lucide="plus" class="w-4 h-4"></i>
            <span>새 배송지</span>
        </button>
    </div>

    <div id="addr-list-container" class="grid grid-cols-1 md:grid-cols-2 gap-5">
        <div class="animate-pulse p-6 border border-gray-100 rounded-3xl bg-white shadow-sm h-40"></div>
    </div>
</div>

<div id="addr-modal" class="hidden fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm transition-opacity" onclick="AddressTab.closeModal()"></div>

    <div class="bg-white w-full max-w-md rounded-[2rem] shadow-2xl overflow-hidden relative z-10 transform transition-all scale-100">
        <div class="bg-white/80 backdrop-blur-md px-6 py-5 border-b border-gray-100 flex justify-between items-center sticky top-0 z-10">
            <h3 class="text-lg font-black text-gray-900 tracking-tight" id="modal-title">새 배송지 추가</h3>
            <button onclick="AddressTab.closeModal()" class="p-2 bg-gray-100 rounded-full text-gray-500 hover:bg-gray-200 hover:text-gray-900 transition">
                <i data-lucide="x" class="w-5 h-5"></i>
            </button>
        </div>

        <div class="p-8">
            <form id="form-addr" onsubmit="AddressTab.submitAddress(event)" class="space-y-6">
                <input type="hidden" name="addr_seq" id="addr_seq" value="0">

                <div class="space-y-1.5">
                    <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">
                        배송지명 <span class="text-[10px] font-normal text-gray-400 ml-1">(최대 17자)</span>
                    </label>
                    <input type="text" name="addr_nm" id="addr_nm" placeholder="예: 우리집, 회사" maxlength="17"
                           class="w-full bg-gray-50 border-0 rounded-2xl px-5 py-3.5 text-sm font-bold text-gray-900 focus:ring-2 focus:ring-primary-500/20 focus:bg-white transition placeholder-gray-400" required
                           oninput="AddressTab.checkLength(this, 17)"/>
                </div>

                <div class="space-y-1.5">
                    <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">주소</label>
                    <div class="flex gap-2">
                        <input type="text" name="post_no" id="post_no" placeholder="우편번호" readonly
                               class="w-28 bg-gray-100 border-0 rounded-2xl px-5 py-3.5 text-sm font-medium text-gray-600 cursor-default" required />
                        <button type="button" onclick="AddressTab.execPostcode()"
                                class="flex-1 bg-white border border-gray-200 text-gray-600 rounded-2xl text-xs font-bold hover:bg-primary-50 hover:text-primary-600 hover:border-primary-200 transition shadow-sm">
                            우편번호 검색
                        </button>
                    </div>
                    <input type="text" name="addr_h" id="addr_h" placeholder="기본 주소" readonly
                           class="w-full bg-gray-50 border-0 rounded-2xl px-5 py-3.5 text-sm font-medium text-gray-900 mt-2 cursor-default" required />

                    <div class="relative">
                        <input type="text" name="addr_d" id="addr_d" placeholder="상세 주소를 입력해주세요 (최대 50자)" maxlength="50"
                               class="w-full bg-gray-50 border-0 rounded-2xl px-5 py-3.5 text-sm font-medium text-gray-900 focus:ring-2 focus:ring-primary-500/20 focus:bg-white transition mt-2 placeholder-gray-400 pr-12" required
                               oninput="AddressTab.checkLength(this, 50)"/>
                        <span class="absolute right-4 bottom-3.5 text-[10px] text-gray-400 font-bold pointer-events-none" id="addr_d_count"></span>
                    </div>
                </div>

                <div class="flex items-center gap-3 p-1">
                    <input type="checkbox" name="default_yn" id="chk-default" value="1"
                           class="w-5 h-5 text-primary-600 border-gray-300 rounded-lg focus:ring-primary-500 cursor-pointer transition">
                    <label for="chk-default" class="text-sm font-bold text-gray-700 select-none cursor-pointer">
                        기본 배송지로 설정
                    </label>
                </div>

                <div class="pt-2">
                    <button type="submit"
                            class="w-full bg-primary-600 text-white py-4 rounded-2xl font-bold text-sm hover:bg-primary-700 shadow-lg hover:shadow-xl hover:-translate-y-0.5 transition-all">
                        저장하기
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<script>
    (function() {
        let currentAddrCount = 0;
        let addressesData = [];

        function escapeHtml(str) {
            if (!str) return '';
            return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
        }

        const actions = {
            init: () => {
                actions.loadAddressList();
            },

            loadAddressList: async () => {
                // ... (기존 loadAddressList 로직 동일) ...
                try {
                    const response = await fetch('/profile/address/list');
                    if (!response.ok) throw new Error('Network error');
                    const data = await response.json();

                    addressesData = data || [];
                    currentAddrCount = addressesData.length;

                    const container = document.getElementById('addr-list-container');

                    if (currentAddrCount === 0) {
                        container.innerHTML = `
                            <div class="col-span-full py-20 text-center bg-gray-50/50 rounded-[2rem] border-2 border-dashed border-gray-200">
                                <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300 shadow-sm">
                                    <i data-lucide="map-pin-off" class="w-8 h-8"></i>
                                </div>
                                <p class="text-base text-gray-500 font-bold mb-4">등록된 배송지가 없습니다.</p>
                                <button onclick="AddressTab.openModal()" class="inline-flex items-center gap-1.5 text-primary-600 font-bold text-sm hover:underline bg-white border border-gray-200 px-5 py-2.5 rounded-full transition shadow-sm hover:shadow">
                                    첫 배송지 등록하기
                                </button>
                            </div>`;
                        if(window.lucide) lucide.createIcons();
                        return;
                    }

                    let html = '';
                    addressesData.forEach((item, index) => {
                        const isDefault = (item.default_yn === 1);
                        const borderClass = isDefault ? 'border-primary-500 ring-4 ring-primary-500/5 bg-primary-50/30' : 'border-gray-100 hover:border-gray-300 bg-white';
                        const defaultBadge = isDefault
                            ? '<span class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-primary-600 text-white shadow-sm">기본</span>'
                            : '';

                        let actionBtns = '';
                        if (!isDefault) {
                            actionBtns = `<button onclick="AddressTab.setDefault(\${item.addr_seq})" class="text-xs font-bold text-gray-500 hover:text-primary-600 bg-gray-50 px-3 py-1.5 rounded-lg transition">기본 설정</button>`;
                        }

                        html += `
                            <div class="group relative p-6 rounded-[1.5rem] border \${borderClass} shadow-sm transition-all duration-300 hover:shadow-lg">
                                <div class="flex justify-between items-start mb-3">
                                    <div class="flex items-center gap-2">
                                        <span class="text-lg font-black text-gray-900 tracking-tight">\${escapeHtml(item.addr_nm)}</span>
                                        \${defaultBadge}
                                    </div>
                                    <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                        <button onclick="AddressTab.openEditModal(\${index})" class="p-2 text-gray-400 hover:text-primary-600 hover:bg-primary-50 rounded-full transition">
                                            <i data-lucide="edit-2" class="w-4 h-4"></i>
                                        </button>
                                        <button onclick="AddressTab.deleteAddr(\${item.addr_seq})" class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-full transition">
                                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                                        </button>
                                    </div>
                                </div>

                                <div class="space-y-1 mb-5">
                                    <p class="text-sm font-medium text-gray-600">[\${escapeHtml(item.post_no)}]</p>
                                    <p class="text-sm font-bold text-gray-800 leading-snug">\${escapeHtml(item.addr_h)}</p>
                                    <p class="text-sm font-medium text-gray-500">\${escapeHtml(item.addr_d)}</p>
                                </div>

                                <div class="flex justify-end">
                                    \${actionBtns}
                                </div>
                            </div>`;
                    });

                    container.innerHTML = html;
                    if(window.lucide) lucide.createIcons();

                } catch (error) {
                    console.error('실패:', error);
                    document.getElementById('addr-list-container').innerHTML =
                        '<div class="col-span-full py-12 text-center text-red-500 bg-red-50 rounded-3xl font-bold text-sm">목록을 불러오지 못했습니다.</div>';
                }
            },

            // [추가] 글자 수 체크 함수
            checkLength: (input, max) => {
                if (input.value.length > max) {
                    input.value = input.value.slice(0, max);
                }

                // 상세주소 카운터 표시 (선택사항)
                if (input.id === 'addr_d') {
                    const counter = document.getElementById('addr_d_count');
                    if(counter) counter.innerText = input.value.length + '/' + max;
                }
            },

            openModal: () => {
                if (currentAddrCount >= 5) {
                    alert('배송지는 최대 5개까지만 등록할 수 있습니다.\n불필요한 배송지를 삭제 후 다시 시도해주세요.');
                    return;
                }
                actions.resetForm();
                document.getElementById('modal-title').textContent = '새 배송지 추가';
                const modal = document.getElementById('addr-modal');
                modal.classList.remove('hidden');
                modal.classList.add('flex');
            },

            // ... (openEditModal, closeModal, resetForm 등 기존 로직 동일) ...
            openEditModal: (index) => {
                const item = addressesData[index];
                if (!item) return;

                actions.resetForm();
                document.getElementById('addr_seq').value = item.addr_seq;
                document.getElementById('addr_nm').value = item.addr_nm;
                document.getElementById('post_no').value = item.post_no;
                document.getElementById('addr_h').value = item.addr_h;
                document.getElementById('addr_d').value = item.addr_d;
                document.getElementById('chk-default').checked = (item.default_yn === 1);

                // 글자수 카운트 초기화
                if(document.getElementById('addr_d_count')) {
                    document.getElementById('addr_d_count').innerText = item.addr_d.length + '/50';
                }

                document.getElementById('modal-title').textContent = '배송지 수정';
                const modal = document.getElementById('addr-modal');
                modal.classList.remove('hidden');
                modal.classList.add('flex');
            },

            closeModal: () => {
                const modal = document.getElementById('addr-modal');
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            },

            resetForm: () => {
                document.getElementById('form-addr').reset();
                document.getElementById('addr_seq').value = 0;
                if(document.getElementById('addr_d_count')) document.getElementById('addr_d_count').innerText = '';
            },

            submitAddress: async (e) => {
                e.preventDefault();
                // [추가] 길이 유효성 검사 (서버 전송 전 한 번 더 체크)
                const addrNm = document.getElementById('addr_nm').value;
                const addrD = document.getElementById('addr_d').value;

                if(addrNm.length > 20) {
                    alert('배송지명은 20자 이내로 입력해주세요.');
                    return;
                }
                if(addrD.length > 50) {
                    alert('상세 주소는 50자 이내로 입력해주세요.');
                    return;
                }

                // ... (기존 전송 로직) ...
                const form = document.getElementById('form-addr');
                const formData = new FormData(form);
                const seq = document.getElementById('addr_seq').value;
                const url = (seq == 0 || seq == '0') ? '/profile/address/add' : '/profile/address/update';

                if(!document.getElementById('post_no').value) {
                    alert('주소를 검색해주세요.');
                    return;
                }

                const params = new URLSearchParams();
                for (const [key, value] of formData.entries()) {
                    params.append(key, value);
                }

                try {
                    const res = await fetch(url, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: params.toString()
                    });

                    const result = await res.text();

                    if (result === 'success') {
                        actions.closeModal();
                        actions.loadAddressList();
                    } else if (result === 'count_limit') {
                        alert('배송지는 최대 5개까지만 등록할 수 있습니다.');
                    } else {
                        alert('저장에 실패했습니다.');
                    }
                } catch (err) {
                    console.error(err);
                    alert('오류가 발생했습니다.');
                }
            },

            deleteAddr: async (seq) => {
                if (!confirm('정말 삭제하시겠습니까?')) return;
                try {
                    const res = await fetch('/profile/address/delete', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: 'addr_seq=' + seq
                    });
                    if (await res.text() === 'success') actions.loadAddressList();
                    else alert('삭제 실패');
                } catch (err) { alert('오류 발생'); }
            },

            setDefault: async (seq) => {
                if (!confirm('기본 배송지로 설정하시겠습니까?')) return;
                try {
                    const res = await fetch('/profile/address/setDefault', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: 'addr_seq=' + seq
                    });
                    if (await res.text() === 'success') actions.loadAddressList();
                    else alert('설정 실패');
                } catch (err) { alert('오류 발생'); }
            },

            execPostcode: () => {
                new daum.Postcode({
                    oncomplete: function(data) {
                        var addr = data.roadAddress;
                        var extraAddr = '';
                        if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) extraAddr += data.bname;
                        if (data.buildingName !== '' && data.apartment === 'Y') extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                        if (extraAddr !== '') addr += ' (' + extraAddr + ')';

                        document.getElementById('post_no').value = data.zonecode;
                        document.getElementById('addr_h').value = addr;
                        document.getElementById('addr_d').focus();
                    }
                }).open();
            }
        };

        window.AddressTab = actions;
        actions.init();
    })();
</script>