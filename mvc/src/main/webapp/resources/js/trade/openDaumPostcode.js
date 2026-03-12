window.openDaumPostcode = function () {
    new daum.Postcode({
        oncomplete: function (data) {
            console.log('주소 선택됨', data);

            let region = '';
            if (data.sido && data.sigungu) {
                region = data.sido + ' ' + data.sigungu;
            } else if (data.sido) {
                region = data.sido;
            }

            console.log('세팅할 값:', region);

            const input = document.getElementById('sale_rg');
            console.log('input 객체:', input);

            if (input) {
                input.value = region;
            }
        }
    }).open();
};
