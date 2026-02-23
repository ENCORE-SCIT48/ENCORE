document.addEventListener("DOMContentLoaded", function () {

    const dmBtn = document.getElementById("btn-dm");
    const followBtn = document.getElementById("followBtn");

    /**
     * DM 요청 처리
     */
    if (dmBtn) {
        dmBtn.addEventListener("click", function () {

            const profileId = dmBtn.dataset.profileId;
            const profileMode = dmBtn.dataset.profileMode;

            fetch("/api/dms", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    targetProfileId: profileId,
                    targetProfileMode: profileMode
                })
            })
            .then(response => response.json())
            .then(res => {

                if (!res.success) {
                    throw new Error(res.message);
                }

                const { roomId } = res.data;
                window.location.href = `/dm/${roomId}`;
            })
            .catch(error => {
                alert("DM 요청 실패: " + error.message);
            });
        });
    }

    /**
     * 팔로우 버튼 토글
     */
    if (followBtn) {

        followBtn.addEventListener("click", function () {

            const isFollowing =
                followBtn.innerText.trim() === "팔로잉";

            if (isFollowing) {
                followBtn.innerText = "팔로우";
                followBtn.classList.remove("btn-secondary");
                followBtn.classList.add("follow-btn", "text-white");
            } else {
                followBtn.innerText = "팔로잉";
                followBtn.classList.remove("follow-btn", "text-white");
                followBtn.classList.add("btn-secondary");
            }
        });
    }
});
