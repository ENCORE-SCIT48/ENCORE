(() => {
    /**
     * 팔로우 버튼 & DM 버튼 DOM
     */
    const followBtn = document.getElementById("followBtn");
    const dmBtn = document.getElementById("btn-dm");

    /**
     * 공용 toast 함수 (실제 구현 필요)
     */
    function showToast(message) {
        // 예: 부트스트랩 Toast 또는 사용자 정의 UI
        console.log("Toast:", message);
    }

    /**
     * 팔로우 버튼 클릭 처리
     */
    if (followBtn) {
        followBtn.addEventListener("click", function () {
            const profileId = this.dataset.profileId;
            const profileMode = this.dataset.profileMode;

            if (!profileId || !profileMode || profileMode === "undefined") {
                console.error("데이터 누락:", { profileId, profileMode });
                showToast("데이터가 올바르지 않습니다.");
                return;
            }

            // 중복 클릭 방지
            followBtn.disabled = true;

            fetch(`/api/users/${profileId}/${profileMode}/follow`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            })
            .then(res => {
                if (!res.ok) throw new Error("네트워크 응답에 문제가 있습니다.");
                return res.json();
            })
            .then(res => {
                if (!res.success) throw new Error(res.message);

                const isFollowing = res.data.isFollowing;

                // 버튼 상태 및 스타일 업데이트
                followBtn.innerText = isFollowing ? "팔로잉" : "팔로우";
                followBtn.classList.toggle("btn-following", isFollowing);
                followBtn.classList.toggle("btn-unfollowed", !isFollowing);

                // 팔로워 수 실시간 업데이트
                const followerCountElement = document.getElementById("followerCount");
                if (followerCountElement && res.data.followerCount !== undefined) {
                    followerCountElement.innerText = res.data.followerCount;
                }
            })
            .catch(err => {
                console.error("Follow Error:", err);
                showToast("팔로우 처리 실패: " + err.message);
            })
            .finally(() => {
                followBtn.disabled = false;
            });
        });
    }

    /**
     * DM 버튼 클릭 처리
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
            .then(res => res.json())
            .then(res => {
                if (!res.success) throw new Error(res.message);
                window.location.href = `/dm/${res.data.roomId}`;
            })
            .catch(err => {
                console.error("DM Error:", err);
                showToast("DM 요청 실패: " + err.message);
            });
        });
    }
})();
