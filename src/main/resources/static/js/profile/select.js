document.addEventListener("DOMContentLoaded", () => {
    const profileItems = document.querySelectorAll(".profile-item");

    profileItems.forEach((item) => {
        item.addEventListener("click", () => {
            const selectedMode = item.getAttribute("data-mode");
            if (selectedMode) {
                switchProfileMode(selectedMode);
            }
        });
    });
});

/**
 * 컨트롤러의 @RequestParam ActiveMode mode 에 맞춰 데이터를 전송합니다.
 */
function switchProfileMode(mode) {
    // 1. 서버의 @PostMapping("/switch")으로 보낼 데이터 준비
    const formData = new URLSearchParams();
    formData.append("mode", mode);

    // 2. Fetch 요청
    fetch("/profiles/switch", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            // Spring Security CSRF 보호가 활성화되어 있다면 아래 주석을 해제하세요.
            // 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: formData,
        credentials: "include",
    })
        .then((response) => {
            // 컨트롤러가 "redirect:..."를 반환하면 response.redirected가 true가 됩니다.
            if (response.redirected) {
                window.location.href = response.url; // 서버가 지정한 경로(setup 또는 메인)로 이동
            } else {
                // 리다이렉트가 아닐 경우 응답 처리에 따라 메인으로 이동
                window.location.href = "/feed";
            }
        })
        .catch((error) => {
            console.error("프로필 전환 중 오류 발생:", error);
            alert("프로필 전환에 실패했습니다.");
        });
}
