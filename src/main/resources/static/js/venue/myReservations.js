let allReservations = [];
let currentFilter = 'ALL';

async function loadReservations() {
    try {
        const res = await fetch('/api/reservations/my', { credentials: 'include' });
        const json = await res.json();
        if (!json.success) throw new Error(json.message);
        allReservations = json.data;
        renderList();
    } catch (e) {
        console.error('[myReservations] 내 대관 목록 로드 실패:', e.message);
        document.getElementById('reservationList').innerHTML = `
            <div class="empty-state">
                <i class="bi bi-exclamation-circle"></i>
                <p>불러오는 중 오류가 발생했습니다.</p>
            </div>`;
    }
}

function renderList() {
    const list = currentFilter === 'ALL'
        ? allReservations
        : allReservations.filter(r => r.status === currentFilter);

    const container = document.getElementById('reservationList');
    if (list.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="bi bi-calendar-x"></i>
                <p>대관 신청 내역이 없습니다.</p>
            </div>`;
        return;
    }
    container.innerHTML = list.map(r => buildCard(r)).join('');
}

function buildCard(r) {
    const statusLabel = { PENDING: '대기중', APPROVED: '승인됨', REJECTED: '거절됨' }[r.status] || r.status;
    const statusIcon  = { PENDING: 'clock', APPROVED: 'check-circle', REJECTED: 'x-circle' }[r.status] || 'circle';

    const rejectBox = r.status === 'REJECTED' && r.rejectReason ? `
        <hr class="divider">
        <div class="reject-box">
            <i class="bi bi-info-circle-fill"></i>
            <span>거절 사유: ${escHtml(r.rejectReason)}</span>
        </div>` : '';

    return `
    <div class="reservation-card">
        <div class="card-header-row">
            <span class="venue-name">${escHtml(r.venueName)}</span>
            <span class="status-badge status-${r.status}">
                <i class="bi bi-${statusIcon}-fill"></i>${statusLabel}
            </span>
        </div>
        <div class="card-info">
            <div class="info-row"><i class="bi bi-geo-alt-fill"></i>${escHtml(r.address)}</div>
            <div class="info-row"><i class="bi bi-calendar3"></i>${formatDate(r.startAt)} ~ ${formatDate(r.endAt)}</div>
            <div class="info-row"><i class="bi bi-cash-stack"></i>${r.rentalFee ? r.rentalFee.toLocaleString() + '원 / 시간' : '-'}</div>
            ${r.message ? `<div class="info-row"><i class="bi bi-chat-left-text"></i>${escHtml(r.message)}</div>` : ''}
        </div>
        ${rejectBox}
    </div>`;
}

function formatDate(dt) {
    if (!dt) return '-';
    return dt.replace('T', ' ').substring(0, 16);
}

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// 필터 탭 이벤트
document.querySelectorAll('.filter-tab').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-tab').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.status;
        renderList();
    });
});

loadReservations();
