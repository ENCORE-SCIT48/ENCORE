document.addEventListener("DOMContentLoaded", function () {
    const sendBtn = document.querySelector(".btn-outline-secondary"); // 인증버튼
    const emailInput = document.getElementById("email");
    const authSection = document.getElementById("authCodeSection");
    const authCode = document.getElementById("authCode");
    const verifyBtn = document.getElementById("verifyBtn");
    const container = document.getElementById("timerContainer");
    let timerInterval;

    verifyBtn.addEventListener("click", function () {
        // 3. 입력한 이메일과 인증번호를 가져온다.
        const email = emailInput.value;
        const code = authCode.value;

        // 4. fetch로 서버(/api/email/verify)에 보낸다.
        // 5. 성공하면 "성공!" 알림을 띄우고, 실패하면 "다시 확인해라"라고 한다.
        fetch("/api/email/verify", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                email: email,
                code: code,
            }),
        }).then((res) => {
            if (res.ok) {
                alert("성공!");
                // 타이머 멈추기
                clearInterval(timerInterval);

                // 1. 기존 회색 클래스(text-muted) 제거하고 초록색 클래스(text-success) 추가
                container.classList.remove("text-muted");
                container.classList.add("text-success");
                container.innerHTML = "<b>인증 완료</b>";

                // 1. 이메일 입력창이랑 인증번호 입력창을 수정 못 하게 막기
                emailInput.readOnly = true;
                authCode.readOnly = true;

                // 2. 버튼들도 더 안 눌리게 비활성화
                sendBtn.disabled = true;
                verifyBtn.disabled = true;
                verifyBtn.innerText = "인증 완료";
            } else {
                alert("인증 번호가 틀렸습니다.");
            }
        });
    });
    function startTimer(durationInSeconds) {
        const timerDisplay = document.getElementById("timer");
        let timeLeft = durationInSeconds;

        // 혹시 이미 돌아가고 있는 타이머가 있다면 초기화
        if (timerInterval) {
            clearInterval(timerInterval);
        }

        timerInterval = setInterval(function () {
            let minutes = Math.floor(timeLeft / 60);
            let seconds = timeLeft % 60;

            // 한 자릿수일 때 앞에 0 붙이기 (ex: 09:05)
            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;

            timerDisplay.textContent = minutes + ":" + seconds;

            if (--timeLeft < 0) {
                clearInterval(timerInterval);
                timerDisplay.textContent = "시간 초과";
                timerDisplay.style.color = "red";

                // 시간 초과 시 '확인' 버튼 비활성화 (보안상 재발송 유도)
                document.getElementById("verifyBtn").disabled = true;
                alert("인증 시간이 만료되었습니다. 다시 발송해주세요.");
            }
        }, 1000);
    }
    sendBtn.addEventListener("click", function () {
        // 1. 이메일 입력값 가져오기
        const email = emailInput.value;

        // 버튼 비활성화 (연타 방지)
        sendBtn.disabled = true;
        sendBtn.innerText = "발송 완료";
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
                    startTimer(300); // 5분 타이머 시작
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
