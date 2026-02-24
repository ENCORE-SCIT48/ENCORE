$(document).ready(function() {
    // 1. 초기 데이터 로드 (로그인 유저 정보는 서버 세션에서 처리하므로 따로 안 보냄)
    loadBlockList();

    // 2. 탭 필터링 이벤트
    $('#filterTab .nav-link').on('click', function(e) {
        e.preventDefault();
        $('#filterTab .nav-link').removeClass('active');
        $(this).addClass('active');

        const filter = $(this).data('filter');
        filterItems(filter);
    });
});

// 데이터 불러오기 함수
function loadBlockList() {
    $.ajax({
        url: '/api/block/list', // 서버의 API 주소
        method: 'GET',
        success: function(data) {
            let html = '';
            if (data.length === 0) {
                html = '<div class="empty-state">차단된 내역이 없습니다.</div>';
            } else {
                data.forEach(item => {
                    html += `
                        <div class="block-card" data-category="${item.type}">
                            <div class="category-tag">${item.type}</div>
                            <div class="content-text">${item.name}</div>
                            <button class="btn-unblock" onclick="unblockItem(${item.id})">차단 해제</button>
                        </div>`;
                });
            }
            $('#blockList').html(html);
        },
        error: function() {
            alert('데이터를 불러오는데 실패했습니다.');
        }
    });
}

// 차단 해제 함수 (비동기)
function unblockItem(id) {
    if(!confirm('차단을 해제하시겠습니까?')) return;

    $.ajax({
        url: '/api/block/unblock',
        method: 'POST',
        data: { id: id },
        success: function(response) {
            // 성공 시 리스트 다시 불러오기 (혹은 해당 DOM만 제거)
            loadBlockList();
        }
    });
}

// 클라이언트 사이드 필터링
function filterItems(category) {
    if (category === 'all') {
        $('.block-card').show();
    } else {
        $('.block-card').hide();
        $(`.block-card[data-category="${category}"]`).show();
    }
}
