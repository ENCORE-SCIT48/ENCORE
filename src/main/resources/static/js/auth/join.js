document.addEventListener("DOMContentLoaded", function () {
    const sendBtn = document.querySelector(".btn-outline-secondary"); // 인증버튼
    const emailInput = document.getElementById("email");
    const authSection = document.getElementById("authCodeSection");

    sendBtn.addEventListener("click", function () {
        // 1. 이메일 입력값 가져오기
        const email = emailInput.value;

        // 버튼 비활성화 (연타 방지)
        sendBtn.disabled = true;
        sendBtn.innerText = "발송 중...";
        // 2. Fetch를 이용해서 서버에 보내기 (여기를 직접 짜보세요!)
        // 힌트: fetch("/url", { method: "POST", ... })
        fetch("/api/email/send", {
            method: "post",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                email: email,
            }),
        })
            // 3. 성공했을 때 authSection 보여주기
            // 힌트: .style.display = "block"
            .then((res) => {
                if (res.ok) {
                    alert("메일이 발송되었습니다!");
                    authSection.style.display = "block";
                } else {
                    alert("실패했습니다. 이메일을 확인하세요.");
                    // 실패하면 다시 누를 수 있게 복구
                    sendBtn.disabled = false;
                    sendBtn.innerText = "인증";
                }
            });
    });
});
