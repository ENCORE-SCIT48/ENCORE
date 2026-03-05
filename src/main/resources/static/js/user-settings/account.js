/**
 * 회원정보 수정 폼 검증 (join.js와 유사한 패턴)
 * - 닉네임: 2~10자 필수
 * - 비밀번호 변경 시: 현재 비밀번호 입력, 새 비밀번호 8자 이상 영문+숫자, 확인 일치
 */
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("accountForm");
    const nickname = document.getElementById("nickname");
    const currentPassword = document.getElementById("currentPassword");
    const newPassword = document.getElementById("newPassword");
    const newPasswordConfirm = document.getElementById("newPasswordConfirm");

    function setValidation(el, isValid, message) {
        const feedback = el.closest(".mb-3")?.querySelector(".invalid-feedback");
        el.classList.toggle("is-valid", isValid);
        el.classList.toggle("is-invalid", !isValid);
        if (feedback && message) feedback.textContent = message;
        return isValid;
    }

    const validators = {
        nickname() {
            const v = (nickname.value || "").trim();
            const ok = v.length >= 2 && v.length <= 10;
            return setValidation(nickname, ok, "닉네임은 2~10자 이내로 입력해주세요.");
        },
        newPassword() {
            if (!newPassword.value.trim()) return true; // 비워두면 변경 안 함
            const regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
            return setValidation(newPassword, regex.test(newPassword.value), "8자 이상, 영문+숫자 조합 필수");
        },
        newPasswordConfirm() {
            if (!newPasswordConfirm.value.trim()) return true;
            const match = newPassword.value === newPasswordConfirm.value;
            return setValidation(newPasswordConfirm, match, "비밀번호가 일치하지 않습니다.");
        },
    };

    nickname.addEventListener("input", validators.nickname);
    newPassword.addEventListener("input", function () {
        validators.newPassword();
        if (newPasswordConfirm.value) validators.newPasswordConfirm();
    });
    newPasswordConfirm.addEventListener("input", validators.newPasswordConfirm);

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const wantPasswordChange = (newPassword.value || "").trim() !== "";
        if (wantPasswordChange && !(currentPassword.value || "").trim()) {
            alert("비밀번호 변경 시 현재 비밀번호를 입력해주세요.");
            currentPassword.focus();
            return;
        }

        const nicknameOk = validators.nickname();
        const newPwOk = validators.newPassword();
        const confirmOk = validators.newPasswordConfirm();

        if (!nicknameOk) {
            alert("닉네임을 2~10자 이내로 입력해주세요.");
            return;
        }
        if (wantPasswordChange && (!newPwOk || !confirmOk)) {
            alert("새 비밀번호를 확인해주세요.");
            return;
        }

        form.submit();
    });
});
