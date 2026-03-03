(() => {
   /**
    * DOM 요소들
    */
    const followBtn = document.getElementById("followBtn");
    const dmBtn = document.getElementById("btn-dm");
    const confirmBlockBtn = document.getElementById("confirmBlockBtn"); // 차단 모달 내 버튼
    const confirmReportBtn = document.getElementById("confirmReportBtn");
    const unblockBtn = document.getElementById("unblockBtn");
    /**
     * 공용 toast 함수 (실제 구현 필요)
     */
    function showToast(message) {
        // 예: 부트스트랩 Toast 또는 사용자 정의 UI
        console.log(message);
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
                headers: { "Content-Type": "application/json" },
                credentials: "include",
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
                credentials: "include",
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
/**
     * 차단(Block) 버튼 클릭 처리
     */
    if (confirmBlockBtn) {
        confirmBlockBtn.addEventListener("click", function () {
            const targetId = this.dataset.targetId;
            const targetType = "USER";
            const targetMode = this.dataset.targetMode; // 고정된 "USER"보다 dataset에서 가져오는 게 안전함

            confirmBlockBtn.disabled = true;

            fetch('/api/users/relations/block', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    targetId: targetId,
                    targetType: targetType,
                    targetProfileMode: targetMode
                })
            })
            .then(res => {
                if (!res.ok) throw new Error("차단 처리 중 서버 오류가 발생했습니다.");
                return res.json();
            })
            .then(res => {
                alert("사용자를 차단했습니다.");
                location.reload(); // 서버가 바뀐 관계를 인식해서 화면을 다시 그리게 함
            })
            .catch(err => {
                console.error("Block Error:", err);
                showToast(err.message);
            })
            .finally(() => {
                confirmBlockBtn.disabled = false;
                // Bootstrap 5 방식의 모달 닫기 (jQuery 의존성 없을 때 권장)
                const modal = bootstrap.Modal.getInstance(document.getElementById('blockModal'));
                if (modal) modal.hide();
            });
        });
    }

    /**
    * 신고(Report) 버튼 클릭 처리
    */
    if (confirmReportBtn) {
        confirmReportBtn.addEventListener("click", function () {
            // 1. 필요한 데이터 추출
            // HTML의 신고 버튼에 th:data-target-mode="${user.profileMode}"가 있어야 합니다.
            const targetId = this.dataset.targetId;
            const targetType = this.dataset.targetType;
            const targetName = this.dataset.targetName;

            // 2. 데이터 유효성 검사 (철저하게)
            // null, undefined, 빈 문자열 체크
            if (!targetId || !targetType || targetType === "undefined" || !targetName) {
                console.error("신고 데이터 누락 상세:", { targetId, targetType, targetName });
                alert("신고 대상 정보(모드 등)가 올바르지 않아 신고를 진행할 수 없습니다.");
                return; // 여기서 로직 종료 (신고 페이지 이동 안 함)
            }

            // 3. 모든 데이터가 정상일 때만 이동
            const url = `/report?targetId=${targetId}&targetType=${targetType}&targetName=${encodeURIComponent(targetName)}`;
            window.location.href = url;
            });
    }
    if (unblockBtn) {
        unblockBtn.addEventListener("click", function () {
            const targetId = this.dataset.targetId;
            const targetMode = this.dataset.targetMode;
            const targetType = "USER"

            if (!confirm("차단을 해제하시겠습니까?")) return;

            fetch('/api/users/relations/unblock', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    targetId: targetId,
                    targetProfileMode: targetMode,
                    targetType: targetType
                })
            })
            .then(res => {
                if (!res.ok) throw new Error("차단 해제 실패");
                alert("차단이 해제되었습니다.");
                location.reload(); // 다시 정상 프로필을 보여주기 위해 새로고침
            })
            .catch(err => {
                console.error("Unblock Error:", err);
                alert("차단 해제 중 오류가 발생했습니다.");
            });
        });
    }
})();
