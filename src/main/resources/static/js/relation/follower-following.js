document.addEventListener('DOMContentLoaded', () => {
    activateTab(activeTab);

    if (activeTab === 'following') {
        fetchRecommendedFriends();
    }

    fetchFollowingList();
    fetchFollowerList();

    // 탭 버튼 이벤트
    document.getElementById('tab-following').addEventListener('click', () => activateTab('following'));
    document.getElementById('tab-follower').addEventListener('click', () => activateTab('follower'));
});

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

function fetchRecommendedFriends() {
    fetch('/api/recommended-friends')
        .then(res => res.json())
        .then(data => renderRecommendFriends(data))
        .catch(err => console.error('추천 친구 로드 실패', err));
}

function fetchFollowingList() {
    fetch(`/api/users/${targetProfileId}/${profileMode}/following`)
        .then(res => res.json())
        .then(data => renderUserList('following-container', data))
        .catch(err => console.error('팔로잉 리스트 로드 실패', err));
}

function fetchFollowerList() {
    fetch(`/api/users/${targetProfileId}/${profileMode}/follower`)
        .then(res => res.json())
        .then(data => renderUserList('follower-container', data))
        .catch(err => console.error('팔로워 리스트 로드 실패', err));
}

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

        div.innerHTML = `
            <img src="${user.profileImageUrl || '/images/default-profile.png'}" class="rounded-circle mb-2" style="width:48px; height:48px;">
            <div class="fw-bold">${user.name}</div>
            <button class="btn btn-primary btn-sm mt-1" onclick="handleFollow(${user.id}, '${user.profileMode}', this)">팔로우</button>
        `;

        container.appendChild(div);
    });
}

function renderUserList(containerId, data) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    data.forEach(user => {
        const div = document.createElement('div');
        div.className = 'list-group-item d-flex align-items-center justify-content-between';

        div.innerHTML = `
            <div class="d-flex align-items-center">
                <img src="${user.profileImageUrl || '/images/default-profile.png'}" class="rounded-circle" style="width:48px; height:48px; margin-right: 10px;">
                <div>
                    <a href="/user/profile/${user.id}" class="text-sub fw-bold">${user.name}</a><br>
                    <small class="text-muted">@${user.username || ''}</small>
                </div>
            </div>
            <button
                class="btn btn-sm ${user.isFollowing ? 'btn-outline-danger' : 'btn-primary'}"
                onclick="handleFollow(${user.id}, '${user.profileMode}', this)">
                ${user.isFollowing ? '팔로우중' : '팔로우'}
            </button>
        `;

        container.appendChild(div);
    });
}

function handleFollow(targetProfileId, targetProfileMode, buttonElement) {
    buttonElement.disabled = true;

    fetch(`/api/users/${targetProfileId}/${targetProfileMode}/follow`, {
        method: 'POST'
    })
    .then(res => res.json())
    .then(result => {
        if (!result.success) {
            console.error("팔로우 실패");
            return;
        }

        const isFollowing = result.data.isFollowing;

        if (isFollowing) {
            buttonElement.innerText = "팔로우중";
            buttonElement.classList.remove("btn-primary");
            buttonElement.classList.add("btn-outline-danger");
        } else {
            buttonElement.innerText = "팔로우";
            buttonElement.classList.remove("btn-outline-danger");
            buttonElement.classList.add("btn-primary");
        }
    })
    .catch(err => console.error("팔로우 처리 실패", err))
    .finally(() => {
        buttonElement.disabled = false;
    });
}
