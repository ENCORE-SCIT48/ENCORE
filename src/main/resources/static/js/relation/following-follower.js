// ==========================
// following-follower.js (리팩토링)
// ==========================

document.addEventListener('DOMContentLoaded', () => {
    activateTab(activeTab);

    // 초기 리스트 로드
    fetchList('following', 'recommend-container', `/api/users/me/recommended-friends`)
    fetchList('following', 'following-container', `/api/users/${targetId}/${profileMode}/following`);
    fetchList('follower', 'follower-container', `/api/users/${targetId}/${profileMode}/follower`);

    // 탭 버튼 이벤트
    document.getElementById('tab-following')
        .addEventListener('click', () => activateTab('following'));
    document.getElementById('tab-follower')
        .addEventListener('click', () => activateTab('follower'));
});

// ==========================
// 탭 전환
// ==========================
function activateTab(tab) {
    const tabFollowing = document.getElementById('tab-following');
    const tabFollower = document.getElementById('tab-follower');
    const followingContent = document.getElementById('following-content');
    const followerContent = document.getElementById('follower-content');

    const isFollowingTab = tab === 'following';

    tabFollowing.classList.toggle('active', isFollowingTab);
    tabFollower.classList.toggle('active', !isFollowingTab);

    followingContent.classList.toggle('hidden', !isFollowingTab);
    followerContent.classList.toggle('hidden', isFollowingTab);

    followingContent.classList.toggle('hidden', !isFollowingTab);
    followerContent.classList.toggle('hidden', isFollowingTab);
}

// ==========================
// API 호출 공통 함수
// ==========================
function fetchList(type, containerId, url) {
    fetch(url)
        .then(res => res.json())
        .then(commonResponse => {
            if (!commonResponse.success) {
                console.error(`${type} 리스트 조회 실패:`, commonResponse.message);
                return;
            }
            renderUserList(containerId, commonResponse.data);
        })
        .catch(err => console.error(`${type} 리스트 로드 실패:`, err));
}

// ==========================
// 렌더링 함수 공통
// ==========================
function renderUserList(containerId, data) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    if (!data || data.length === 0) {
        container.innerHTML = '<div class="text-muted">리스트가 없습니다.</div>';
        return;
    }

    data.forEach(user => {
        const div = document.createElement('div');
        div.className = 'list-group-item d-flex align-items-center justify-content-between';

        // 프로필 영역
        div.innerHTML = `
            <div class="d-flex align-items-center">
                <img src="${user.profileImageUrl || '/image/default-profile.png'}" class="rounded-circle profile-thumb">
                <div>
                    <a href="/member/profile/${user.profileId}/${user.profileMode}" class="text-sub fw-bold">${user.userName}</a>
                </div>
            </div>
        `;

        // 버튼 영역
        if (user.profileId === loginProfileId && user.profileMode === loginProfileMode) {
            const infoDiv = document.createElement('div');
            infoDiv.className = 'text-muted';
            infoDiv.innerText = '본인';
            div.appendChild(infoDiv);
        } else {
            const btn = document.createElement('button');
            btn.className = `btn btn-sm ${user.isFollowing ? 'btn-following' : 'btn-unfollowed'}`;
            btn.innerText = user.isFollowing ? '팔로우중' : '팔로우';
            btn.addEventListener('click', () => handleFollow(user.profileId, user.profileMode, btn));
            div.appendChild(btn);
        }

        container.appendChild(div);
    });
}

// ==========================
// 팔로우 / 언팔로우 처리
// ==========================
function handleFollow(targetProfileId, targetProfileMode, buttonElement) {
    if (!buttonElement) return;
    buttonElement.disabled = true;

    fetch(`/api/users/${targetProfileId}/${targetProfileMode}/follow`, { method: 'POST' })
        .then(res => res.json())
        .then(result => {
            if (!result.success || !result.data) {
                console.error("팔로우 처리 실패");
                return;
            }

            const isFollowing = result.data.isFollowing; // DTO @JsonProperty 적용됨
            buttonElement.innerText = isFollowing ? '팔로우중' : '팔로우';

            buttonElement.classList.toggle('btn-following', isFollowing);
            buttonElement.classList.toggle('btn-unfollowed', !isFollowing);
        })
        .catch(err => console.error('오류:', err))
        .finally(() => buttonElement.disabled = false);
}
