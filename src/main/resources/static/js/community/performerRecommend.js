document.addEventListener("DOMContentLoaded", function () {

  // 필터 버튼 토글
  $("#filterToggleBtn").click(function () {
    $("#filterArea").slideToggle(200);
  });

  $(document).on("click", ".recruit-btn", function () {

    const targetProfileId = Number(this.dataset.profileId);
    const targetProfileMode = this.dataset.profileMode;

    console.log("targetProfileId:", targetProfileId);
    console.log("targetProfileMode:", targetProfileMode);

    $.ajax({
      url: "/api/dms",
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify({
        targetProfileId: targetProfileId,
        targetProfileMode: targetProfileMode
      }),
      success: function (response) {

        const roomId = response.data.roomId;

        if (!roomId) {
          alert("채팅방 생성 실패");
          return;
        }

        window.location.href = "/dm/" + roomId;
      },
      error: function (xhr) {
        console.log(xhr.responseText);
        alert("DM 생성 중 오류가 발생했습니다.");
      }
    });

  });

});