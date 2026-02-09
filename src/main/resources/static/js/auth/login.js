// src/main/resources/static/js/auth/login.js

document.addEventListener("DOMContentLoaded", function () {
    const emailInput = document.getElementById("email");
    const rememberMeCheck = document.getElementById("rememberMe");
    const loginForm = document.querySelector("form");

    // 1. 페이지 로드 시 저장된 이메일이 있는지 확인
    const savedEmail = localStorage.getItem("rememberedEmail");
    if (savedEmail) {
        emailInput.value = savedEmail;
        rememberMeCheck.checked = true;
    }

    // 2. 폼 제출 시 '아이디 저장' 체크 여부에 따라 저장/삭제
    loginForm.addEventListener("submit", function () {
        if (rememberMeCheck.checked) {
            localStorage.setItem("rememberedEmail", emailInput.value);
        } else {
            localStorage.removeItem("rememberedEmail");
        }
    });

    // 3. 비밀번호 찾기 클릭 시 안내 (임시)
    const findPwLink = document.querySelector('a[href="#"]');
    if (findPwLink) {
        findPwLink.addEventListener("click", (e) => {
            e.preventDefault();
            alert(
                "비밀번호 찾기 기능은 준비 중입니다. 관리자에게 문의하세요! 😎",
            );
        });
    }
});
