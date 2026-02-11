$(document).ready(function () {
    $('#btn-dm').on('click', function () {
    const profileId = $(this).data('profile-id');
    const profileMode = $(this).data('profile-mode');

        $.ajax({
            url: '/api/dm/request',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
            targetProfileId: profileId,
            targetProfileMode: profileMode
            }),

            success: function (res) {
                const { roomId, myParticipantStatus } = res.data;

                if (myParticipantStatus === 'PENDING') {
                    window.location.href = `/dm/${roomId}?pending=true`;
                } else if (myParticipantStatus === 'ACCEPTED') {
                    window.location.href = `/dm/${roomId}`;
                }
            },
            error: function (xhr) {
                alert('DM 요청 실패: ' + (xhr.responseJSON?.message || xhr.statusText));
            }
        });
    });
});
