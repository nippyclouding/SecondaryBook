<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:include page="../common/header.jsp" />

<section class="max-w-4xl mx-auto bg-white border border-gray-100 rounded-3xl p-8 md:p-12 shadow-sm">
    <h1 class="text-3xl font-black text-gray-900 mb-3">개인정보처리방침</h1>
    <p class="text-sm text-gray-500 mb-10">시행일: 2026년 5월 24일</p>

    <div class="space-y-10 text-sm text-gray-700 leading-7">
        <section>
            <h2 class="text-lg font-bold text-gray-900 mb-3">1. 수집하는 개인정보</h2>
            <p>회사는 회원가입, 중고책 거래, 결제, 배송 및 정산 서비스 제공을 위하여 필요한 범위에서 정보를 수집합니다.</p>
            <ul class="list-disc pl-5 mt-3 space-y-1">
                <li>회원정보: 아이디, 비밀번호, 이메일, 휴대폰번호, 닉네임</li>
                <li>거래정보: 판매 및 구매 내역, 결제 상태, 주문번호, 배송지, 구매확정 및 정산 내역</li>
                <li>정산정보: 은행코드, 계좌번호, 예금주명</li>
                <li>서비스 이용정보: 로그인 기록, 문의 또는 분쟁 처리 과정에서 생성되는 기록</li>
            </ul>
        </section>

        <section>
            <h2 class="text-lg font-bold text-gray-900 mb-3">2. 이용 목적</h2>
            <ul class="list-disc pl-5 space-y-1">
                <li>회원 식별, 로그인 및 서비스 운영</li>
                <li>중고책 거래 중개, 안전결제 처리, 배송 및 판매자 정산</li>
                <li>환불, 민원 및 거래 분쟁 대응</li>
                <li>부정 이용 방지와 법령상 의무 이행</li>
            </ul>
        </section>

        <section>
            <h2 class="text-lg font-bold text-gray-900 mb-3">3. 보유 및 이용기간</h2>
            <p>개인정보는 이용 목적이 달성되면 지체 없이 파기합니다. 다만 관계 법령에 따라 다음 기록은 정해진 기간 동안 분리 보관하거나 접근을 제한하여 보존합니다.</p>
            <div class="overflow-x-auto mt-4">
                <table class="w-full border-collapse border border-gray-200 text-left">
                    <thead class="bg-gray-50 text-gray-900">
                        <tr>
                            <th class="border border-gray-200 px-4 py-3 font-bold">기록</th>
                            <th class="border border-gray-200 px-4 py-3 font-bold">보존기간</th>
                            <th class="border border-gray-200 px-4 py-3 font-bold">근거</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="border border-gray-200 px-4 py-3">계약 또는 청약철회 등에 관한 기록</td>
                            <td class="border border-gray-200 px-4 py-3">5년</td>
                            <td class="border border-gray-200 px-4 py-3">전자상거래 등에서의 소비자보호에 관한 법률 시행령</td>
                        </tr>
                        <tr>
                            <td class="border border-gray-200 px-4 py-3">대금결제 및 재화 등의 공급에 관한 기록</td>
                            <td class="border border-gray-200 px-4 py-3">5년</td>
                            <td class="border border-gray-200 px-4 py-3">전자상거래 등에서의 소비자보호에 관한 법률 시행령</td>
                        </tr>
                        <tr>
                            <td class="border border-gray-200 px-4 py-3">소비자의 불만 또는 분쟁처리에 관한 기록</td>
                            <td class="border border-gray-200 px-4 py-3">3년</td>
                            <td class="border border-gray-200 px-4 py-3">전자상거래 등에서의 소비자보호에 관한 법률 시행령</td>
                        </tr>
                        <tr>
                            <td class="border border-gray-200 px-4 py-3">로그인 기록</td>
                            <td class="border border-gray-200 px-4 py-3">3개월</td>
                            <td class="border border-gray-200 px-4 py-3">통신비밀보호법</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>

        <section>
            <h2 class="text-lg font-bold text-gray-900 mb-3">4. 파기 및 보호조치</h2>
            <p>법정 보존 대상 거래기록은 일반 회원정보와 구분하여 보존 목적 외 이용을 제한합니다. 보존기간이 끝나고 다른 법적 보존 사유가 없는 경우 복구할 수 없는 방법으로 파기합니다. 정산 계좌번호 등 민감한 거래정보는 암호화하여 저장합니다.</p>
        </section>

        <section>
            <h2 class="text-lg font-bold text-gray-900 mb-3">5. 문의</h2>
            <p>개인정보 및 거래기록 관련 문의는 고객센터(1544-0000) 또는 help@SecondHandBooks.com으로 접수할 수 있습니다.</p>
        </section>
    </div>
</section>

<jsp:include page="../common/footer.jsp" />
