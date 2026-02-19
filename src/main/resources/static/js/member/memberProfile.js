document.addEventListener("DOMContentLoaded", function () {
    const dmBtn = document.getElementById("btn-dm");
    const followBtn = document.getElementById("followBtn");

    /**
     * 팔로우 버튼 처리
     */
    if (followBtn) {
        followBtn.addEventListener("click", function () {
            // dataset에서 값을 읽어올 때, HTML의 data-profile-mode와 일치하는지 확인
            const profileId = this.dataset.profileId;
            const profileMode = this.dataset.profileMode;

            if (!profileId || !profileMode || profileMode === "undefined") {
                console.error("데이터 누락:", { profileId, profileMode });
                return;
            }

            // 버튼 비활성화 (중복 클릭 방지)
            followBtn.disabled = true;

            fetch(`/api/users/${profileId}/${profileMode}/follow`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            })
            .then(response => {
                if (!response.ok) throw new Error("네트워크 응답에 문제가 있습니다.");
                return response.json();
            })
            .then(res => {
                if (!res.success) {
                    throw new Error(res.message);
                }

                // 1. 서버에서 온 최신 팔로우 상태 적용
                const nextState = res.data.isFollowing;

                if (nextState) {
                    // 팔로잉 중일 때 스타일
                    followBtn.innerText = "팔로잉";
                    followBtn.classList.replace("btn-danger", "btn-outline-danger");
                    followBtn.classList.remove("text-white");
                } else {
                    // 팔로우 안 한 상태 스타일
                    followBtn.innerText = "팔로우";
                    followBtn.classList.replace("btn-outline-danger", "btn-danger");
                    followBtn.classList.add("text-white");
                }

                // 2. 팔로워 수 실시간 업데이트
                const followerCountElement = document.getElementById("followerCount");
                if (followerCountElement && res.data.followerCount !== undefined) {
                    followerCountElement.innerText = res.data.followerCount;
                }
            })
            // <--- 여기에 있던 'g' 오타를 지웠습니다.
            .catch(error => {
                console.error("Follow Error:", error);
                alert("처리 중 오류가 발생했습니다: " + error.message);
            })
            .finally(() => {
                // 성공하든 실패하든 버튼 잠금 해제
                followBtn.disabled = false;
            });
        });
    }

    /**
     * DM 요청 처리 (기존 로직 유지)
     */
    if (dmBtn) {
        dmBtn.addEventListener("click", function () {
            const profileId = this.dataset.profileId;
            const profileMode = this.dataset.profileMode;

            fetch("/api/dms", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    targetProfileId: profileId,
                    targetProfileMode: profileMode
                })
            })
            .then(response => response.json())
            .then(res => {
                if (!res.success) throw new Error(res.message);
                window.location.href = `/dm/${res.data.roomId}`;
            })
            .catch(error => {
                console.error("DM Error:", error);
                alert("DM 요청 실패: " + error.message);
            });
        });
    }
});
