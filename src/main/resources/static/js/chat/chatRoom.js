function sendMessage() {
    const message = $('#chatInput').val().trim();
    if (!message) return;

    $.ajax({
      url: `/api/chat/room/${roomId}/messages`,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ content: message }),
      success: function () {
        $('#chatInput').val('');
        loadMessages();
      },
      error: function () {
        alert('메시지 전송 실패');
      }
    });
  }

  // 엔터로 전송
  $('#chatInput').on('keydown', function (e) {
    if (e.key === 'Enter') {
      sendMessage();
    }
  });

  // 버튼 클릭으로 전송
  $('#sendBtn').on('click', function () {
    sendMessage();
  });

function loadMessages() {
  $.ajax({
    url: `/api/chat/room/${roomId}/messages`,
    method: 'GET',
    success: function (response) {
      const messages = response.data; // <- 이렇게 가져와야 실제 메시지 배열
      $('#chatArea').empty();

      messages.forEach(msg => {
        $('#chatArea').append(`
          <div class="message-row">
            <div class="avatar"></div>
            <div>
              <div class="message-name">${msg.senderName}</div>
              <div class="message-content">${msg.content}</div>
            </div>
          </div>
        `);
      });

      $('#chatArea').scrollTop($('#chatArea')[0].scrollHeight);
    }

  });
}

$(document).ready(function () {
    loadMessages(); // 입장하자마자 메시지 불러오기
});
