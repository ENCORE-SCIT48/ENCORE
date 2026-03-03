let allReservations = [];
let currentFilter = 'ALL';
let currentVenueId = null;

/* ────────────────────────────────────────────
   초기화: 내 공연장 목록 로드 → 탭 생성
──────────────────────────────────────────── */
async function init() {
    try {
        const res = await fetch('/api/venues/my', { credentials: 'include' });
        const json = await res.json();
        if (!json.success) throw new Error(json.message);

        const venues = json.data;
        if (!venues || venues.length === 0) {
            renderEmptyVenues();
            return;
        }

        renderVenueTabs(venues);
        selectVenue(venues[0].venueId, venues[0].venueName);
    } catch (e) {
        console.error('[venueReservations] 공연장 목록 로드 실패:', e.message);
        document.getElementById('reservationList').innerHTML = `
            <div class="empty-state">
                <i class="bi bi-exclamation-circle"></i>
                <p>공연장 정보를 불러오지 못했습니다.</p>
            </div>`;
    }
}

/* ────────────────────────────────────────────
   공연장 탭 렌더링
──────────────────────────────────────────── */
function renderVenueTabs(venues) {
    const container = document.getElementById('venueTabs');
    container.innerHTML = venues.map(v => `
        <button
            class="venue-tab"
            data-venue-id="${v.venueId}"
            onclick="selectVenue(${v.venueId}, '${escHtml(v.venueName)}')">
            ${escHtml(v.venueName)}
        </button>
    `).join('');
}

/* ────────────────────────────────────────────
   공연장 탭 선택
──────────────────────────────────────────── */
function selectVenue(venueId, venueName) {
    currentVenueId = venueId;
    currentFilter = 'ALL';

    // 탭 active 처리
    document.querySelectorAll('.venue-tab').forEach(btn => {
        btn.classList.toggle('active', Number(btn.dataset.venueId) === venueId);
    });

    // 상태 필터 초기화
    document.querySelectorAll('.filter-tab').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.status === 'ALL');
    });

    // 헤더 타이틀 변경
    document.getElementById('headerTitle').textContent = venueName + ' - 대관 요청';

    loadReservations();
}

/* ────────────────────────────────────────────
   대관 요청 목록 로드
──────────────────────────────────────────── */
async function loadReservations() {
    document.getElementById('reservationList').innerHTML = `
        <div class="loading">
            <div class="spinner-border text-danger" role="status" style="width:2rem;height:2rem;"></div>
        </div>`;
    try {
        const res = await fetch(`/api/venues/${currentVenueId}/reservations`, { credentials: 'include' });
        const json = await res.json();
        if (!json.success) throw new Error(json.message);
        allReservations = json.data;
        renderList();
    } catch (e) {
        console.error(`[venueReservations] 대관 목록 로드 실패 - venueId=${currentVenueId}:`, e.message);
        document.getElementById('reservationList').innerHTML = `
            <div class="empty-state">
                <i class="bi bi-exclamation-circle"></i>
                <p>불러오는 중 오류가 발생했습니다.</p>
            </div>`;
    }
}

/* ────────────────────────────────────────────
   목록 렌더링
──────────────────────────────────────────── */
function renderList() {
    const list = currentFilter === 'ALL'
        ? allReservations
        : allReservations.filter(r => r.status === currentFilter);

    const container = document.getElementById('reservationList');
    if (list.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="bi bi-calendar-x"></i>
                <p>대관 요청이 없습니다.</p>
            </div>`;
        return;
    }
    container.innerHTML = list.map(r => buildCard(r)).join('');
}

/* ────────────────────────────────────────────
   카드 빌드
──────────────────────────────────────────── */
function buildCard(r) {
    const statusLabel = { PENDING: '대기중', APPROVED: '승인됨', REJECTED: '거절됨' }[r.status] || r.status;
    const statusIcon  = { PENDING: 'clock', APPROVED: 'check-circle', REJECTED: 'x-circle' }[r.status] || 'circle';

    const actionBtns = r.status === 'PENDING' ? `
        <div class="action-row">
            <button class="btn-approve" onclick="approve(${r.reservationId})">
                <i class="bi bi-check2"></i> 승인
            </button>
            <button class="btn-reject" onclick="toggleRejectForm(${r.reservationId})">
                <i class="bi bi-x"></i> 거절
            </button>
        </div>
        <div class="reject-form" id="rejectForm-${r.reservationId}">
            <textarea id="rejectReason-${r.reservationId}" placeholder="거절 사유를 입력해주세요."></textarea>
            <button class="btn-reject-confirm" onclick="reject(${r.reservationId})">거절 확정</button>
        </div>` : '';

    return `
    <div class="reservation-card" id="card-${r.reservationId}">
        <div class="card-header-row">
            <span class="performer-name">${escHtml(r.performerStageName || '공연자 #' + r.performerId)}</span>
            <span class="status-badge status-${r.status}">
                <i class="bi bi-${statusIcon}-fill"></i>${statusLabel}
            </span>
        </div>
        <div class="card-info">
            <div class="info-row"><i class="bi bi-calendar3"></i>${formatDate(r.startAt)} ~ ${formatDate(r.endAt)}</div>
            <div class="info-row"><i class="bi bi-cash-stack"></i>${r.rentalFee ? r.rentalFee.toLocaleString() + '원 / 시간' : '-'}</div>
            ${r.message ? `<div class="info-row"><i class="bi bi-chat-left-text"></i>${escHtml(r.message)}</div>` : ''}
            ${r.rejectReason ? `<div class="info-row" style="color:#b91c1c;"><i class="bi bi-info-circle-fill"></i>거절 사유: ${escHtml(r.rejectReason)}</div>` : ''}
        </div>
        ${actionBtns}
    </div>`;
}

/* ────────────────────────────────────────────
   승인
──────────────────────────────────────────── */
async function approve(reservationId) {
    if (!confirm('이 대관 요청을 승인하시겠습니까?')) return;
    try {
        const res = await fetch(`/api/reservations/${reservationId}/approve`, {
            method: 'PATCH', credentials: 'include'
        });
        const json = await res.json();
        if (!json.success) throw new Error(json.message);
        showToast('승인되었습니다.');
        updateCardStatus(reservationId, 'APPROVED');
    } catch (e) {
        console.error(`[venueReservations] 승인 실패 - reservationId=${reservationId}:`, e.message);
        showToast('오류: ' + e.message);
    }
}

/* ────────────────────────────────────────────
   거절 폼 토글
──────────────────────────────────────────── */
function toggleRejectForm(reservationId) {
    document.getElementById(`rejectForm-${reservationId}`).classList.toggle('show');
}

/* ────────────────────────────────────────────
   거절
──────────────────────────────────────────── */
async function reject(reservationId) {
    const reason = document.getElementById(`rejectReason-${reservationId}`).value.trim();
    if (!reason) { showToast('거절 사유를 입력해주세요.'); return; }
    if (!confirm('이 대관 요청을 거절하시겠습니까?')) return;
    try {
        const res = await fetch(`/api/reservations/${reservationId}/reject`, {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ rejectReason: reason })
        });
        const json = await res.json();
        if (!json.success) throw new Error(json.message);
        showToast('거절되었습니다.');
        updateCardStatus(reservationId, 'REJECTED', reason);
    } catch (e) {
        console.error(`[venueReservations] 거절 실패 - reservationId=${reservationId}:`, e.message);
        showToast('오류: ' + e.message);
    }
}

/* ────────────────────────────────────────────
   카드 상태 업데이트 (페이지 새로고침 없음)
──────────────────────────────────────────── */
function updateCardStatus(reservationId, status, reason) {
    const idx = allReservations.findIndex(r => r.reservationId === reservationId);
    if (idx !== -1) {
        allReservations[idx].status = status;
        if (reason) allReservations[idx].rejectReason = reason;
    }
    renderList();
}

/* ────────────────────────────────────────────
   공연장 없을 때
──────────────────────────────────────────── */
function renderEmptyVenues() {
    document.getElementById('venueTabs').innerHTML = '';
    document.getElementById('reservationList').innerHTML = `
        <div class="empty-state">
            <i class="bi bi-building-x"></i>
            <p>등록된 공연장이 없습니다.</p>
        </div>`;
}

/* ────────────────────────────────────────────
   유틸
──────────────────────────────────────────── */
let toastTimer;
function showToast(msg) {
    const wrap = document.getElementById('toastWrap');
    document.getElementById('toastMsg').textContent = msg;
    wrap.style.display = 'block';
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { wrap.style.display = 'none'; }, 2500);
}

function formatDate(dt) {
    if (!dt) return '-';
    return dt.replace('T', ' ').substring(0, 16);
}

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

/* ────────────────────────────────────────────
   상태 필터 탭 이벤트
──────────────────────────────────────────── */
document.querySelectorAll('.filter-tab').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-tab').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.status;
        renderList();
    });
});

/* ────────────────────────────────────────────
   진입점
──────────────────────────────────────────── */
init();
