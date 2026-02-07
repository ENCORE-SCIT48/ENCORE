document.addEventListener("DOMContentLoaded", () => {
    // --- [1. 요소 캐싱] ---
    const els = {
        form: document.getElementById("joinForm"),
        email: document.getElementById("email"),
        sendBtn: document.getElementById("sendBtn"),
        authSection: document.getElementById("authCodeSection"),
        authCode: document.getElementById("authCode"),
        verifyBtn: document.getElementById("verifyBtn"),
        timerBox: document.getElementById("timerContainer"),
        timer: document.getElementById("timer"),
        pw: document.getElementById("password"),
        pwConfirm: document.getElementById("passwordConfirm"),
        nick: document.getElementById("nickname"),
        agreeTerms: document.getElementById("agreeTerms"),
        agreePrivacy: document.getElementById("agreePrivacy"),
    };

    // --- [2. 상태] ---
    let state = {
        timerId: null,
        isEmailVerified: false,
    };

    // --- [3. 공통 유효성 표시] ---
    function setValidation(el, isValid, message = "") {
        const feedback =
            el.parentElement.querySelector(".invalid-feedback") ||
            el.closest(".mb-3")?.querySelector(".invalid-feedback");

        el.classList.toggle("is-valid", isValid);
        el.classList.toggle("is-invalid", !isValid);

        if (!isValid && feedback && message) feedback.innerText = message;
        return isValid;
    }

    // --- [4. 검증 함수들] ---
    const validators = {
        password() {
            const regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
            return setValidation(
                els.pw,
                regex.test(els.pw.value),
                "8자 이상, 영문+숫자 조합",
            );
        },
        confirm() {
            const ok =
                els.pw.value === els.pwConfirm.value &&
                els.pwConfirm.value !== "";
            return setValidation(
                els.pwConfirm,
                ok,
                "비밀번호가 일치하지 않습니다.",
            );
        },
        nickname() {
            const ok =
                els.nick.value.length >= 2 && els.nick.value.length <= 10;
            return setValidation(els.nick, ok, "닉네임은 2~10자");
        },
    };

    // --- [5. 실시간 검증 바인딩] ---
    els.pw.addEventListener("input", () => {
        validators.password();
        if (els.pwConfirm.value) validators.confirm();
    });
    els.pwConfirm.addEventListener("input", validators.confirm);
    els.nick.addEventListener("input", validators.nickname);

    // --- [6. 타이머] ---
    function startTimer(seconds) {
        clearInterval(state.timerId);
        let left = seconds;

        state.timerId = setInterval(() => {
            const m = String(Math.floor(left / 60)).padStart(2, "0");
            const s = String(left % 60).padStart(2, "0");
            els.timer.textContent = `${m}:${s}`;

            if (--left < 0) {
                clearInterval(state.timerId);
                els.timer.textContent = "시간 초과";
                els.timer.style.color = "red";
                els.verifyBtn.disabled = true;
            }
        }, 1000);
    }

    // --- [7. 이메일 전송] ---
    els.sendBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(els.email.value)) {
            return setValidation(
                els.email,
                false,
                "올바른 이메일 형식이 아닙니다.",
            );
        }

        try {
            const res = await fetch("/api/email/send", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: els.email.value }),
            });

            if (!res.ok) throw new Error("send fail");

            alert("인증 메일이 발송되었습니다.");
            els.authSection.style.display = "block";
            startTimer(300);
            setValidation(els.email, true);
        } catch (err) {
            alert("발송 실패. 이메일을 확인해주세요.");
            els.sendBtn.disabled = false;
        }
    });

    // --- [8. 인증 확인] ---
    els.verifyBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        try {
            const res = await fetch("/api/email/verify", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: els.email.value,
                    code: els.authCode.value,
                }),
            });

            if (!res.ok) throw new Error("verify fail");

            alert("인증 성공!");
            state.isEmailVerified = true;
            clearInterval(state.timerId);
            els.timerBox.className = "text-success fw-bold";
            els.timerBox.innerHTML = "✓ 인증 완료";
            [els.email, els.authCode, els.sendBtn, els.verifyBtn].forEach(
                (el) => (el.disabled = true),
            );
            setValidation(els.authCode, true);
        } catch (err) {
            setValidation(els.authCode, false, "인증번호가 틀렸습니다.");
        }
    });

    // --- [9. 최종 제출] ---
    els.form.addEventListener("submit", (e) => {
        e.preventDefault();

        const ok =
            validators.password() &
            validators.confirm() &
            validators.nickname();

        if (!state.isEmailVerified) {
            alert("이메일 인증을 먼저 완료해주세요.");
            return;
        }

        if (!els.agreeTerms.checked || !els.agreePrivacy.checked) {
            alert("필수 약관에 동의해야 합니다.");
            return;
        }

        if (!ok) {
            alert("입력값을 다시 확인해주세요.");
            document.querySelector(".is-invalid")?.focus();
            return;
        }

        // disabled 해제 후 제출
        els.email.disabled = false;
        e.target.submit();
    });
});
