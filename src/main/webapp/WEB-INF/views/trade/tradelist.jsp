<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:choose>
    <c:when test="${not empty trades}">
        <div class="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-7 gap-x-5 gap-y-10">
            <c:forEach var="trade" items="${trades}">
                <div onclick="location.href='/trade/${trade.trade_seq}'"
                     class="group flex flex-col cursor-pointer transition-all duration-300 ease-out hover:-translate-y-1">
                    <div class="relative aspect-[1/1.3] overflow-hidden bg-gray-50 rounded-2xl border border-gray-100 shadow-[0_2px_8px_rgba(0,0,0,0.04)] mb-3 transform-gpu">
                        <img src="<c:out value='${trade.book_img}'/>" alt="<c:out value='${trade.book_title}'/>"
                             class="w-full h-full object-cover"
                             loading="lazy" />
                        <div class="absolute top-2.5 left-2.5 flex flex-wrap gap-1">
                            <c:choose>
                                <c:when test="${trade.sale_st.name() == 'SOLD'}">
                                    <div class="bg-gray-900/85 backdrop-blur-md text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-sm">
                                        판매완료
                                    </div>
                                </c:when>
                                <c:when test="${trade.sale_st.name() == 'RESERVED'}">
                                    <div class="bg-orange-500/90 backdrop-blur-md text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-sm">
                                        예약중
                                    </div>
                                </c:when>
                                <c:when test="${trade.sale_st.name() == 'SALE'}">
                                    <div class="bg-green-600/90 backdrop-blur-md text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-sm">
                                        판매중
                                    </div>
                                </c:when>
                            </c:choose>
                            <c:if test="${trade.book_st == 'NEW'}">
                                <div class="bg-blue-600/90 backdrop-blur-md text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-sm">새책</div>
                            </c:if>
                        </div>
                    </div>
                    <div class="flex-1 flex flex-col px-1">
                        <h3 class="font-bold text-gray-900 text-[15px] mb-1 line-clamp-1 leading-snug group-hover:text-blue-600 transition-colors tracking-tight">
                                <c:out value="${trade.book_title}"/>
                        </h3>
                        <div class="text-xs text-gray-500 font-medium mb-1.5 truncate">
                                <c:out value="${trade.book_author}"/>
                            <c:if test="${not empty trade.book_publisher}">
                                <span class="mx-1 text-gray-300 text-[10px]">|</span> <c:out value="${trade.book_publisher}"/>
                            </c:if>
                        </div>
                        <c:if test="${not empty trade.sale_title}">
                            <p class="text-xs text-gray-600 font-normal mb-2 truncate opacity-80"><c:out value="${trade.sale_title}"/></p>
                        </c:if>

                        <div class="mt-auto pt-1">
                            <div class="flex items-baseline gap-0.5 mb-2">
                                <span class="font-extrabold text-[17px] text-gray-900 tracking-tight"><fmt:formatNumber value="${trade.sale_price}" pattern="#,###" /></span>
                                <span class="text-[11px] font-bold text-gray-400 ml-0.5">원</span>
                            </div>

                            <div class="flex items-center text-[11px] text-gray-400 font-medium">
                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1 text-gray-300">
                                    <path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/>
                                    <circle cx="12" cy="10" r="3"/>
                                </svg>
                                <span class="truncate max-w-[100px]"><c:out value="${trade.sale_rg}"/></span>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div class="flex justify-center items-center gap-3 mt-12 mb-4">
            <c:if test="${currentPage > 1}">
                <a href="javascript:goPage(${currentPage - 1})"
                   class="group flex items-center justify-center w-9 h-9 rounded-full bg-white border border-gray-100 shadow-sm hover:shadow-md hover:border-gray-200 transition-all text-gray-400 hover:text-gray-900"
                   aria-label="Previous">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="m15 18-6-6 6-6"/>
                    </svg>
                </a>
            </c:if>

            <div class="flex items-center gap-1 bg-gray-100/80 backdrop-blur-sm px-1.5 py-1.5 rounded-full">
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <a href="javascript:goPage(${i})"
                       class="w-8 h-8 flex items-center justify-center rounded-full text-[13px] font-bold transition-all duration-200
                              ${i == currentPage
                                ? 'bg-white text-gray-900 shadow-sm scale-100 ring-1 ring-gray-200'
                                : 'text-gray-400 hover:text-gray-600 hover:bg-gray-200/50'}">
                            ${i}
                    </a>
                </c:forEach>
            </div>

            <c:if test="${currentPage < totalPages}">
                <a href="javascript:goPage(${currentPage + 1})"
                   class="group flex items-center justify-center w-9 h-9 rounded-full bg-white border border-gray-100 shadow-sm hover:shadow-md hover:border-gray-200 transition-all text-gray-400 hover:text-gray-900"
                   aria-label="Next">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="m9 18 6-6-6-6"/>
                    </svg>
                </a>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>
        <div class="py-32 text-center rounded-[2rem] border border-dashed border-gray-200 bg-gray-50/30">
            <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm border border-gray-100 text-gray-300">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="m21.174 6.812-10.826-6.812-10.348 6.812a4 4 0 0 0 -1.826 3.369v8.812a4 4 0 0 0 4 4h12.696a4 4 0 0 0 4-4v-8.812a4 4 0 0 0 -1.826-3.369z"/>
                    <path d="m16 22v-9.5a2.5 2.5 0 0 0 -5 0v9.5"/>
                </svg>
            </div>
            <p class="text-gray-900 font-bold text-lg mb-1 tracking-tight">등록된 상품이 없습니다.</p>
            <p class="text-gray-500 text-sm font-medium">다른 조건으로 검색하거나 필터를 변경해보세요.</p>
        </div>
    </c:otherwise>
</c:choose>