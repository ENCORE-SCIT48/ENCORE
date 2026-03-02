/**
 * [myVenues.js] 호스트 내 공연장 관리 페이지
 *
 * 흐름:
 *  1. GET /api/venues/my  → 내 공연장 목록 로드
 *  2. 각 카드에 "대관 관리" → /venues/reservations/manage
 *                "수정"     → /venues/{venueId}/edit
 *
 * 의존 DOM: #venueList, #toastWrap, #toastMsg
 */

/* ────────────────────────────────────────────
   공연장 타입 한글 변환
──────────────────────────────────────────── */
const VENUE_TYPE_LABEL = {
    CONCERT_HALL: '콘서트홀',
    THEATER:      '극장',
    CLUB:         '클럽',
    OUTDOOR:      '야외',
    STUDIO:       '스튜디오',
};

/* ────────────────────────────────────────────
   진입점
──────────────────────────────────────────── */
async function loadMyVenues() {
    try {
        const res  = await fetch('/api/venues/my', { credentials: 'include' });
        const json = await res.json();

        if (!json.success) {
            console.error('[myVenues] API 실패:', json.message);
            throw new Error(json.message);
        }

        const venues = json.data;
        console.log(`[myVenues] ${venues.length}개 로드 완료`);

        if (!venues || venues.length === 0) {
            renderEmpty();
            return;
        }

        renderList(venues);

    } catch (e) {
        console.error('[myVenues] 로드 실패:', e.message);
        document.getElementById('venueList').innerHTML = `
            <div class="empty-state">
                <i class="bi bi-exclamation-circle"></i>
                <p>공연장 정보를 불러오지 못했습니다.</p>
            </div>`;
    }
}

/* ────────────────────────────────────────────
   목록 렌더링
──────────────────────────────────────────── */
function renderList(venues) {
    document.getElementById('venueList').innerHTML =
        venues.map(v => buildCard(v)).join('');
}

/* ────────────────────────────────────────────
   카드 빌드
──────────────────────────────────────────── */
function buildCard(v) {
    const typeLabel = VENUE_TYPE_LABEL[v.venueType] || (v.venueType || '기타');

    const infoRows = [
        v.address    ? `<div class="info-row"><i class="bi bi-geo-alt-fill"></i>${esc(v.address)}</div>` : '',
        v.openTime   ? `<div class="info-row"><i class="bi bi-clock"></i>${esc(v.openTime)} ~ ${esc(v.closeTime || '')}</div>` : '',
        v.rentalFee  ? `<div class="info-row"><i class="bi bi-cash-stack"></i>${v.rentalFee.toLocaleString()}원 / ${v.bookingUnit || 60}분</div>` : '',
        v.totalSeats ? `<div class="info-row"><i class="bi bi-people-fill"></i>총 ${v.totalSeats}석</div>` : '',
    ].join('');

    return `
    <div class="venue-card">
        <div class="card-top">
            <span class="venue-name">${esc(v.venueName)}</span>
            <span class="venue-type-badge">${esc(typeLabel)}</span>
        </div>
        <div class="card-info">${infoRows}</div>
        <div class="card-actions">
            <button class="btn-action btn-manage"
                    onclick="window.location.href='/venues/reservations/manage'">
                <i class="bi bi-calendar-check"></i> 대관 관리
            </button>
            <button class="btn-action btn-edit"
                    onclick="window.location.href='/venues/${v.venueId}/edit'">
                <i class="bi bi-pencil"></i> 수정
            </button>
        </div>
    </div>`;
}

/* ────────────────────────────────────────────
   빈 상태
──────────────────────────────────────────── */
function renderEmpty() {
    document.getElementById('venueList').innerHTML = `
        <div class="empty-state">
            <i class="bi bi-building-x"></i>
            <p>등록된 공연장이 없습니다.</p>
            <a href="/venues/new" class="btn-register">
                <i class="bi bi-plus-lg"></i> 공연장 등록하기
            </a>
        </div>`;
}

/* ────────────────────────────────────────────
   유틸
──────────────────────────────────────────── */
function esc(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

let toastTimer;
function showToast(msg) {
    const wrap = document.getElementById('toastWrap');
    document.getElementById('toastMsg').textContent = msg;
    wrap.style.display = 'block';
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { wrap.style.display = 'none'; }, 2500);
}

/* ────────────────────────────────────────────
   실행
──────────────────────────────────────────── */
loadMyVenues();
