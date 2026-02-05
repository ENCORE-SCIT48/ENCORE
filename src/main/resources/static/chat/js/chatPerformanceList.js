  $(function(){
        // 뒤로가기
        $('#backBtn').click(function(){
            window.history.back();
        });

        // 카드 클릭 시 상세 페이지 이동
        $('.chat-card').click(function(){
            // data-performance-id -> performanceId로 읽음
            const performanceId = $(this).attr('data-performance-id');
            const chatId = $(this).data('id');
            window.location.href = '/performance/' + performanceId + '/chat/' + chatId;
        });

        // 글쓰기 버튼 클릭
           $('#writeBtn').click(function(){
               const performanceId = $(this).data('performanceId');
               window.location.href = '/performance/' + performanceId + '/chat/post';
           });

        // 하단 네비 버튼 예시
        $('#btnParty').click(()=>alert('파티모집 클릭'));
        $('#btnEmergency').click(()=>alert('긴급모집 클릭'));
        $('#btnHome').click(()=>alert('홈 클릭'));
        $('#btnDM').click(()=>alert('DM 클릭'));
        $('#btnChatRoom').click(()=>alert('채팅방 클릭'));
    });
