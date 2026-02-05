$('#chatPostForm').submit(function(e) {
    e.preventDefault(); // 기본 제출 막기

    const data = {
        title: $('#title').val(),
        content: $('#content').val(),
        maxMember: $('#maxMember').val(),
        performanceId: $('#performanceId').val()
    };

    $.ajax({
        url: '/performance/' + data.performanceId + '/chat/post',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(res) {
            if(res.success) {
                window.location.href = '/performance/' + data.performanceId + '/chat/list';
            } else {
                alert(res.message);
            }
        },
        error: function() {
            alert('서버 에러 발생');
        }
    });
});
