$(document).ready(() => {
    $('#chatPostForm').on('submit', (event) => {
        event.preventDefault();

        const requestData = {
            performanceId: $('#performanceId').val(),
            title: $('#title').val(),
            content: $('#content').val(),
            maxMember: $('#maxMember').val()
        };

        $.ajax({
            url: '/chat/post',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: (response) => {
                alert('글이 생성되었습니다.');
                // 필요하면 상세 페이지로 이동
                // window.location.href = `/chat/${response.data.chatPostId}`;
            },
            error: (error) => {
                alert('글 생성에 실패했습니다.');
                console.error(error);
            }
        });
    });
});
