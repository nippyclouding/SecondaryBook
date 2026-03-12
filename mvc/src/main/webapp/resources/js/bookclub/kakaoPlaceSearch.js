/**
 * 카카오 지도 장소 검색 공통 모듈
 * 독서모임 생성/수정 시 오프라인 위치 검색에 사용
 */
var KakaoPlaceSearch = (function() {
    var map = null;
    var marker = null;
    var ps = null;
    var geocoder = null;
    var infowindow = null;
    var selectedPlace = null;
    var onSelectCallback = null;

    /**
     * 지도 초기화
     * @param {string} containerId - 지도를 표시할 컨테이너 ID
     * @param {function} onSelect - 장소 선택 시 콜백 함수
     */
    function init(containerId, onSelect) {
        onSelectCallback = onSelect;

        var container = document.getElementById(containerId);
        if (!container) {
            console.error('지도 컨테이너를 찾을 수 없습니다:', containerId);
            return;
        }

        var options = {
            center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울 시청 기본 좌표
            level: 3
        };

        map = new kakao.maps.Map(container, options);
        ps = new kakao.maps.services.Places();
        geocoder = new kakao.maps.services.Geocoder();
        infowindow = new kakao.maps.InfoWindow({ zIndex: 1 });

        marker = new kakao.maps.Marker({
            map: map
        });
        marker.setVisible(false);

        // 지도 클릭 이벤트 - 클릭한 위치의 주소를 가져와서 선택
        kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
            var latlng = mouseEvent.latLng;

            // 클릭한 위치의 주소 정보 조회
            geocoder.coord2Address(latlng.getLng(), latlng.getLat(), function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    var address = result[0];
                    var roadAddr = address.road_address ? address.road_address.address_name : '';
                    var jibunAddr = address.address ? address.address.address_name : '';

                    // 장소 객체 생성 (검색 결과와 동일한 형태)
                    var place = {
                        place_name: roadAddr || jibunAddr,
                        road_address_name: roadAddr,
                        address_name: jibunAddr,
                        x: latlng.getLng(),
                        y: latlng.getLat()
                    };

                    // 마커 표시
                    marker.setPosition(latlng);
                    marker.setVisible(true);

                    // 인포윈도우 표시
                    var content = '<div style="padding:5px;font-size:12px;max-width:200px;word-break:break-all;">' +
                                  (roadAddr || jibunAddr) + '</div>';
                    infowindow.setContent(content);
                    infowindow.open(map, marker);

                    selectedPlace = place;

                    // 콜백 호출
                    if (onSelectCallback) {
                        onSelectCallback(place);
                    }
                }
            });
        });
    }

    /**
     * 장소 검색
     * @param {string} keyword - 검색 키워드
     * @param {function} callback - 검색 결과 콜백 (results, status)
     */
    function searchPlaces(keyword, callback) {
        if (!keyword || keyword.trim() === '') {
            if (callback) callback([], 'EMPTY');
            return;
        }

        if (!ps) {
            console.error('장소 검색 서비스가 초기화되지 않았습니다.');
            if (callback) callback([], 'ERROR');
            return;
        }

        ps.keywordSearch(keyword, function(data, status, pagination) {
            if (status === kakao.maps.services.Status.OK) {
                if (callback) callback(data, status, pagination);
            } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
                if (callback) callback([], status);
            } else {
                console.error('장소 검색 오류:', status);
                if (callback) callback([], status);
            }
        });
    }

    /**
     * 장소 선택
     * @param {object} place - 카카오 장소 검색 결과 객체
     */
    function selectPlace(place) {
        selectedPlace = place;

        // 지도 중심 이동 및 마커 표시
        var position = new kakao.maps.LatLng(place.y, place.x);
        map.setCenter(position);
        marker.setPosition(position);
        marker.setVisible(true);

        // 인포윈도우 표시
        var content = '<div style="padding:5px;font-size:12px;max-width:200px;word-break:break-all;">' +
                      place.place_name + '</div>';
        infowindow.setContent(content);
        infowindow.open(map, marker);

        // 콜백 호출
        if (onSelectCallback) {
            onSelectCallback(place);
        }
    }

    /**
     * 선택된 장소 초기화
     */
    function clearSelection() {
        selectedPlace = null;
        marker.setVisible(false);
        infowindow.close();
    }

    /**
     * 선택된 장소 정보 반환
     */
    function getSelectedPlace() {
        return selectedPlace;
    }

    /**
     * 장소 정보를 저장용 문자열로 변환
     * - 장소 검색: 장소명만 (예: "스타벅스 강남역점")
     * - 지도 클릭: 시/도 구/군 (예: "서울 강남구")
     * @param {object} place - 카카오 장소 검색 결과 객체
     */
    function formatPlaceString(place) {
        if (!place) return '';

        var placeName = place.place_name || '';
        var address = place.road_address_name || place.address_name || '';

        // 장소명이 있고, 주소와 다르면 (검색으로 선택한 경우) -> 장소명만
        if (placeName && placeName !== address) {
            return placeName;
        }

        // 지도 클릭으로 선택한 경우 (장소명 = 주소) -> 시/도 구/군
        return extractRegion(address);
    }

    /**
     * 주소에서 시/도 + 구/군 추출
     * 예: "서울특별시 강남구 강남대로 390" -> "서울 강남구"
     * @param {string} address - 전체 주소
     */
    function extractRegion(address) {
        if (!address) return '';

        var parts = address.split(' ');
        if (parts.length < 2) return address;

        // 시/도 축약
        var sido = parts[0]
            .replace('특별시', '')
            .replace('광역시', '')
            .replace('특별자치시', '')
            .replace('특별자치도', '')
            .replace('도', '');

        // 구/군/시 (두 번째 부분)
        var sigungu = parts[1] || '';

        return sido + ' ' + sigungu;
    }

    /**
     * 좌표로 지도 중심 이동
     * @param {number} lat - 위도
     * @param {number} lng - 경도
     */
    function setCenter(lat, lng) {
        if (!map) return;
        var position = new kakao.maps.LatLng(lat, lng);
        map.setCenter(position);
    }

    /**
     * 지도 리사이즈 (탭 전환 등에서 사용)
     */
    function relayout() {
        if (map) {
            map.relayout();
        }
    }

    /**
     * 기존 저장된 위치 표시 (주소로 검색하여 지도에 표시)
     * @param {string} savedLocation - 저장된 위치 문자열
     */
    function displaySavedLocation(savedLocation) {
        if (!savedLocation || savedLocation === '온라인') return;

        // "장소명 (주소)" 형식에서 장소명 추출
        var placeName = savedLocation;
        var parenIndex = savedLocation.indexOf(' (');
        if (parenIndex !== -1) {
            placeName = savedLocation.substring(0, parenIndex);
        }

        // 장소명으로 검색하여 첫 번째 결과 표시
        searchPlaces(placeName, function(results, status) {
            if (status === kakao.maps.services.Status.OK && results.length > 0) {
                var place = results[0];
                var position = new kakao.maps.LatLng(place.y, place.x);
                map.setCenter(position);
                marker.setPosition(position);
                marker.setVisible(true);
            }
        });
    }

    return {
        init: init,
        searchPlaces: searchPlaces,
        selectPlace: selectPlace,
        clearSelection: clearSelection,
        getSelectedPlace: getSelectedPlace,
        formatPlaceString: formatPlaceString,
        setCenter: setCenter,
        relayout: relayout,
        displaySavedLocation: displaySavedLocation
    };
})();
