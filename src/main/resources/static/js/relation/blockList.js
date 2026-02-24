$(document).ready(function() {
    loadBlockList();

    $('#filterTab .nav-link').on('click', function(e) {
        e.preventDefault();
        $('#filterTab .nav-link').removeClass('active');
        $(this).addClass('active');

        const filter = $(this).data('filter');
        filterItems(filter);
    });
});

function loadBlockList() {
    $.ajax({
        url: '/api/users/relations/blocks',
        method: 'GET',
        success: function(response) {
            // 서버 응답 구조가 CommonResponse라면 response.data를 사용
            const list = response.data || response;
            let html = '';

            if (!list || list.length === 0) {
                html = '<div class="empty-state">차단된 내역이 없습니다.</div>';
            } else {
                list.forEach(item => {
                    // targetType을 category로 사용 (USER, VENUE 등)
                    // targetProfileMode가 null인 경우를 대비해 빈 문자열 처리
                    const mode = item.targetProfileMode || '';

                    html += `
                        <div class="block-card" data-category="${item.targetType}">
                            <div class="category-tag">${item.typeDisplayName}</div>
                            <div class="content-text">${item.name}</div>
                            <button class="btn-unblock"
                                onclick="unblockItem(${item.targetId}, '${item.targetType}', '${mode}')">
                                차단 해제
                            </button>
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

function unblockItem(targetId, targetType, targetProfileMode) {
    if(!confirm('차단을 해제하시겠습니까?')) return;

    const requestData = {
        targetId: targetId,
        targetType: targetType,
        targetProfileMode: targetProfileMode === '' ? null : targetProfileMode
    };

    $.ajax({
        url: '/api/users/relations/unblock',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function(response) {
            alert('차단이 해제되었습니다.');
            loadBlockList();
        },
        error: function(xhr) {
            if (xhr.status === 404) {
                alert('이미 차단 해제되었거나 내역을 찾을 수 없습니다.');
                loadBlockList();
            } else {
                alert('차단 해제 중 오류가 발생했습니다.');
            }
        }
    });
}

function filterItems(category) {
    if (category === 'all') {
        $('.block-card').show();
    } else {
        $('.block-card').hide();
        // data-category에 들어간 targetType(USER, VENUE 등)과
        // 탭의 data-filter 값이 일치해야 합니다.
        $(`.block-card[data-category="${category}"]`).show();
    }
}
