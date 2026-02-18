// ==========================
// following-follower.js
// ==========================

document.addEventListener('DOMContentLoaded', () => {
    activateTab(activeTab);

    // 초기 리스트 로드
    fetchFollowingList();
    fetchFollowerList();
    fetchRecommendedFriends();

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

    if (tab === 'following') {
        tabFollowing.classList.add('active');
        tabFollower.classList.remove('active');
        followingContent.style.display = 'block';
        followerContent.style.display = 'none';
        fetchRecommendedFriends();
    } else {
        tabFollower.classList.add('active');
        tabFollowing.classList.remove('active');
        followerContent.style.display = 'block';
        followingContent.style.display = 'none';
    }
}

// ==========================
// API 호출
// ==========================
function fetchRecommendedFriends() {
    fetch('/api/recommended-friends')
        .then(res => res.json())
        .then(commonResponse => {
            if (!commonResponse.success) {
                console.error('추천 친구 조회 실패:', commonResponse.message);
                return;
            }
            renderRecommendFriends(commonResponse.data);
        })
        .catch(err => console.error('추천 친구 로드 실패', err));
}

function fetchFollowingList() {
    fetch(`/api/users/${targetId}/${profileMode}/following`)
        .then(res => res.json())
        .then(commonResponse => {
            if (!commonResponse.success) {
                console.error('팔로잉 리스트 조회 실패:', commonResponse.message);
                return;
            }
            renderUserList('following-container', commonResponse.data);
        })
        .catch(err => console.error('팔로잉 리스트 로드 실패', err));
}

function fetchFollowerList() {
    fetch(`/api/users/${targetId}/${profileMode}/follower`)
        .then(res => res.json())
        .then(commonResponse => {
            if (!commonResponse.success) {
                console.error('팔로워 리스트 조회 실패:', commonResponse.message);
                return;
            }
            renderUserList('follower-container', commonResponse.data);
        })
        .catch(err => console.error('팔로워 리스트 로드 실패', err));
}

// ==========================
// 렌더링 함수
// ==========================
function renderRecommendFriends(data) {
    const container = document.getElementById('recommend-container');
    container.innerHTML = '';

    if (!data || data.length === 0) {
        container.innerHTML = '<div class="text-muted">추천 친구가 없습니다.</div>';
        return;
    }

    data.forEach(user => {
        const div = document.createElement('div');
        div.className = 'user-card text-center';

        const btn = document.createElement('button');
        // ⚠️ user.isFollowing -> user.following 으로 수정
        const isFollow = user.following;

        btn.className = `btn btn-sm ${isFollow ? 'btn-following' : 'btn-unfollowed'}`;
        btn.innerText = isFollow ? '팔로우중' : '팔로우';

        btn.addEventListener('click', () => handleFollow(user.profileId, user.profileMode, btn));

        div.innerHTML = `
            <img src="${user.profileImageUrl || '/image/default-profile.png'}" class="rounded-circle mb-2" style="width:48px; height:48px;">
            <div class="fw-bold">${user.userName}</div>
        `;

        div.appendChild(btn);
        container.appendChild(div);
    });
}


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

        const btn = document.createElement('button');
        // ⚠️ user.isFollowing -> user.following 으로 수정
        const isFollow = user.following;

        btn.className = `btn btn-sm ${isFollow ? 'btn-following' : 'btn-unfollowed'}`;
        btn.innerText = isFollow ? '팔로우중' : '팔로우';

        btn.addEventListener('click', () => handleFollow(user.profileId, user.profileMode, btn));

        div.innerHTML = `
            <div class="d-flex align-items-center">
                <img src="${user.profileImageUrl || '/image/default-profile.png'}" class="rounded-circle" style="width:48px; height:48px; margin-right: 10px;">
                <div>
                    <a href="/user/profile/${user.profileId}/${user.profileMode}" class="text-sub fw-bold">${user.userName}</a><br>
                </div>
            </div>
        `;

        div.appendChild(btn);
        container.appendChild(div);
    });
}

// ==========================
// 팔로우 / 언팔로우 처리
// ==========================
function handleFollow(targetProfileId, targetProfileMode, buttonElement) {
    if (!buttonElement) return;
    buttonElement.disabled = true;

    fetch(`/api/users/${targetProfileId}/${targetProfileMode}/follow`, {
        method: 'POST'
    })
    .then(res => res.json())
    .then(result => {
        if (!result.success || !result.data) {
            console.error("데이터 로드 실패");
            return;
        }

        // 서버 응답에서 'following' 필드를 가져옵니다. (확인 완료된 필드명)
        const isFollowing = result.data.following;

        console.log("드디어 확인된 진짜 상태:", isFollowing);

        // 1. 텍스트 변경
        buttonElement.innerText = isFollowing ? "팔로우중" : "팔로우";

        // 2. 클래스 교체
        if (isFollowing) {
            buttonElement.classList.remove("btn-unfollowed");
            buttonElement.classList.add("btn-following");
        } else {
            buttonElement.classList.remove("btn-following");
            buttonElement.classList.add("btn-unfollowed");
        }
    })
    .catch(err => console.error("오류:", err))
    .finally(() => {
        buttonElement.disabled = false;
    });
}
