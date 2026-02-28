$(function () {
    BlockManager.init();
});

const BlockManager = (function () {

    function init() {
        bindEvents();
        loadBlockList();
    }

    function bindEvents() {

        // 필터 탭 클릭
        $('#filterTab').on('click', '.nav-link', function () {
            $('#filterTab .nav-link').removeClass('active');
            $(this).addClass('active');

            const filter = $(this).data('filter');
            filterItems(filter);
        });

        // 차단 해제 버튼 (이벤트 위임)
        $('#blockList').on('click', '.btn-unblock', function () {
            const targetId = $(this).data('id');
            const targetType = $(this).data('type');
            const targetProfileMode = $(this).data('mode');

            unblock(targetId, targetType, targetProfileMode);
        });
    }

    /**
     * 차단 목록 조회
     */
    function loadBlockList() {
        $.ajax({
            url: '/api/users/relations/blocks',
            method: 'GET',
            success(response) {

                if (!response.success) {
                    alert(response.message);
                    return;
                }

                renderList(response.data);
            },
            error(xhr) {
                handleAjaxError(xhr);
            }
        });
    }

    /**
     * 차단 목록 렌더링 (XSS 방지)
     */
    function renderList(list) {

        const $container = $('#blockList');
        $container.empty();

        if (!list || list.length === 0) {
            $container.append(
                $('<div>')
                    .addClass('empty-state')
                    .text('차단된 내역이 없습니다.')
            );
            return;
        }

        list.forEach(item => {

            const mode = item.targetProfileMode || '';

            const $card = $('<div>')
                .addClass('block-card')
                .attr('data-category', item.targetType);

            const $tag = $('<div>')
                .addClass('category-tag')
                .text(item.typeDisplayName);

            const $content = $('<div>')
                .addClass('content-text')
                .text(item.name);

            const $button = $('<button>')
                .attr('type', 'button')
                .addClass('btn-unblock')
                .text('차단 해제')
                .data('id', item.targetId)
                .data('type', item.targetType)
                .data('mode', mode);

            $card.append($tag, $content, $button);
            $container.append($card);
        });
    }

    /**
     * 차단 해제 요청
     */
    function unblock(targetId, targetType, targetProfileMode) {

        if (!confirm('차단을 해제하시겠습니까?')) {
            return;
        }

        const requestData = {
            targetId: targetId,
            targetType: targetType,
            targetProfileMode: targetProfileMode || null
        };

        $.ajax({
            url: '/api/users/relations/unblock',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success(response) {

                if (!response.success) {
                    alert(response.message);
                    return;
                }

                alert(response.message || '차단이 해제되었습니다.');
                loadBlockList();
            },
            error(xhr) {
                handleAjaxError(xhr);
            }
        });
    }

    /**
     * 필터 처리
     */
    function filterItems(category) {

        const $cards = $('.block-card');

        if (category === 'all') {
            $cards.show();
            return;
        }

        $cards.hide();
        $(`.block-card[data-category="${category}"]`).show();
    }

    /**
     * 공통 에러 처리
     */
    function handleAjaxError(xhr) {

        let message = '서버 오류가 발생했습니다.';

        if (xhr.responseJSON && xhr.responseJSON.message) {
            message = xhr.responseJSON.message;
        }

        alert(message);
    }

    return {
        init: init
    };

})();
