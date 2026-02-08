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
        pw: document.getElementById("password"),
        pwConfirm: document.getElementById("passwordConfirm"),
        nick: document.getElementById("nickname"),
        agreeTerms: document.getElementById("agreeTerms"),
        agreePrivacy: document.getElementById("agreePrivacy"),
    };

    // --- [2. 상태 관리] ---
    let state = {
        timerId: null,
        isEmailVerified: false,
    };

    // --- [3. 유틸리티 함수] ---

    // 유효성 UI 업데이트
    function setValidation(el, isValid, message = "") {
        const feedback =
            el.closest(".mb-3")?.querySelector(".invalid-feedback") ||
            el.parentElement.querySelector(".invalid-feedback");

        el.classList.toggle("is-valid", isValid);
        el.classList.toggle("is-invalid", !isValid);

        if (!isValid && feedback && message) feedback.innerText = message;
        return isValid;
    }

    // 타이머 시작
    function startTimer(seconds) {
        if (state.timerId) clearInterval(state.timerId);

        els.timerBox.className = "text-muted small mt-2";
        els.timerBox.innerHTML = '남은 시간: <span id="timer">05:00</span>';
        const timerDisplay = document.getElementById("timer");

        let left = seconds;
        els.verifyBtn.disabled = false;

        const update = (t) => {
            const m = String(Math.floor(t / 60)).padStart(2, "0");
            const s = String(t % 60).padStart(2, "0");
            timerDisplay.textContent = `${m}:${s}`;
        };

        update(left);
        state.timerId = setInterval(() => {
            left--;
            if (left >= 0) {
                update(left);
            } else {
                clearInterval(state.timerId);
                timerDisplay.textContent = "시간 초과";
                timerDisplay.style.color = "#e63946";
                els.verifyBtn.disabled = true;
            }
        }, 1000);
    }

    // 약관 스크롤 감지 및 자동 체크 설정
    const setupModalScroll = (bodyId, btnId, checkboxEl) => {
        const body = document.getElementById(bodyId);
        const btn = document.getElementById(btnId);

        if (!body || !btn || !checkboxEl) return;

        // 직접 클릭 차단 (e.isTrusted 활용)
        checkboxEl.addEventListener("click", (e) => {
            if (e.isTrusted) {
                e.preventDefault();
                alert("보기를 눌러 약관을 끝까지 읽으셔야 동의가 가능합니다.");
            }
        });

        // 스크롤 바닥 감지
        body.addEventListener("scroll", () => {
            const isBottom =
                body.scrollHeight - body.scrollTop <= body.clientHeight + 10;
            if (isBottom && btn.disabled) {
                btn.disabled = false;
                btn.textContent = "확인 및 동의";
                btn.classList.replace("btn-dark", "btn-danger");
            }
        });

        // 동의 버튼 클릭 시 처리
        btn.addEventListener("click", () => {
            checkboxEl.checked = true;
        });
    };

    // --- [4. 필드별 검증 로직] ---
    const validators = {
        password() {
            const regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
            return setValidation(
                els.pw,
                regex.test(els.pw.value),
                "8자 이상, 영문+숫자 조합 필수",
            );
        },
        confirm() {
            const isMatch =
                els.pw.value === els.pwConfirm.value &&
                els.pwConfirm.value !== "";
            return setValidation(
                els.pwConfirm,
                isMatch,
                "비밀번호가 일치하지 않습니다.",
            );
        },
        nickname() {
            const isLengthOk =
                els.nick.value.length >= 2 && els.nick.value.length <= 10;
            return setValidation(
                els.nick,
                isLengthOk,
                "닉네임은 2~10자 사이여야 합니다.",
            );
        },
    };

    // --- [5. 이벤트 바인딩] ---

    // 실시간 검증
    els.pw.addEventListener("input", () => {
        validators.password();
        if (els.pwConfirm.value) validators.confirm();
    });
    els.pwConfirm.addEventListener("input", validators.confirm);
    els.nick.addEventListener("input", validators.nickname);

    // 이메일 인증코드 발송
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

        // UX: 즉시 버튼 잠금 및 상태 표시
        els.sendBtn.disabled = true;
        els.sendBtn.textContent = "발송 중...";

        try {
            const res = await fetch("/api/email/send", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: els.email.value }),
            });

            if (!res.ok) throw new Error();

            alert("인증 메일이 발송되었습니다.");
            els.authSection.style.display = "block";
            startTimer(300);
            setValidation(els.email, true);
            els.sendBtn.textContent = "재발송";
        } catch (err) {
            alert("발송 실패. 다시 시도해주세요.");
            els.sendBtn.textContent = "인증";
        } finally {
            els.sendBtn.disabled = false;
        }
    });

    // 인증코드 확인
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

            if (!res.ok) throw new Error();

            alert("인증되었습니다.");
            state.isEmailVerified = true;
            clearInterval(state.timerId);
            els.timerBox.className = "text-success fw-bold small mt-2";
            els.timerBox.innerHTML = "✓ 이메일 인증 완료";
            [els.email, els.authCode, els.sendBtn, els.verifyBtn].forEach(
                (el) => (el.disabled = true),
            );
            setValidation(els.authCode, true);
        } catch (err) {
            setValidation(els.authCode, false, "인증번호가 일치하지 않습니다.");
        }
    });

    // 약관 스크롤 로직 초기화
    setupModalScroll("termsBody", "confirmTerms", els.agreeTerms);
    setupModalScroll("privacyBody", "confirmPrivacy", els.agreePrivacy);

    // 최종 폼 제출
    els.form.addEventListener("submit", (e) => {
        e.preventDefault();

        const results = [
            validators.password(),
            validators.confirm(),
            validators.nickname(),
        ];
        const isAllInputOk = results.every((res) => res === true);

        if (!state.isEmailVerified) return alert("이메일 인증을 완료해주세요.");
        if (!els.agreeTerms.checked || !els.agreePrivacy.checked)
            return alert("필수 약관에 동의해야 합니다.");
        if (!isAllInputOk) return alert("입력 정보를 확인해주세요.");

        // 서버 전송을 위해 disabled 해제
        els.email.disabled = false;
        els.form.submit();
    });
});
