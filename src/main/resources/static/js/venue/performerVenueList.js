/**
 * [performerVenueList.js] 공연자 전용 공연장 목록
 *
 * - GET /api/venues?keyword=&page=&size=  → 목록 렌더링
 * - 카드 클릭 → /venues/{venueId}/reservation  (예약 신청 페이지)
 * - 검색 디바운스 250ms
 */
(() => {
    // ── DOM ────────────────────────────────────────────────────
    const $input    = document.getElementById('venueSearchInput');
    const $list     = document.getElementById('venueList');
    const $empty    = document.getElementById('venueEmpty');

    if (!$input || !$list || !$empty) {
        console.error('[performerVenueList] 필수 DOM 없음');
        return;
    }

    const DEFAULT_PAGE = 0;
    const DEFAULT_SIZE = Number($input.dataset.size || 10);
    let debounceTimer  = null;

    // 페이지네이션 상태 (무한 스크롤용)
    let page    = DEFAULT_PAGE;
    let size    = DEFAULT_SIZE;
    let loading = false;
    let hasNext = true;

    // ── 유틸 ───────────────────────────────────────────────────
    const VENUE_TYPE = {
        CONCERT_HALL: '콘서트홀', THEATER: '극장',
        CLUB: '클럽', OUTDOOR: '야외', STUDIO: '스튜디오'
    };

    function esc(str) {
        return String(str ?? '')
            .replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    // ── 렌더링 ─────────────────────────────────────────────────
    function appendItems(items, reset = false) {
        if (reset) {
            $list.innerHTML = '';
        }

        if ((!items || !items.length) && !$list.children.length) {
            $empty.classList.remove('d-none');
            return;
        }
        $empty.classList.add('d-none');

        const html = items.map(v => {
            const typeLabel = VENUE_TYPE[v.venueType] || (v.venueType || '기타');
            const feeText   = v.rentalFee
                ? `${v.rentalFee.toLocaleString()}원 / ${v.bookingUnit || 60}분`
                : null;

            return `
            <div class="venue-card" data-id="${v.venueId}" role="button" tabindex="0">
                <div class="card-accent"></div>
                <div class="card-body">
                    <div class="card-top">
                        <span class="venue-name">${esc(v.venueName)}</span>
                        <span class="venue-type-badge">${esc(typeLabel)}</span>
                    </div>
                    <div class="card-info">
                        ${v.address    ? `<div class="info-row"><i class="bi bi-geo-alt-fill"></i>${esc(v.address)}</div>` : ''}
                        ${v.openTime   ? `<div class="info-row"><i class="bi bi-clock"></i>${esc(v.openTime)} ~ ${esc(v.closeTime)}</div>` : ''}
                        ${feeText      ? `<div class="info-row"><i class="bi bi-cash-stack"></i>${esc(feeText)}</div>` : ''}
                        ${v.totalSeats ? `<div class="info-row"><i class="bi bi-people-fill"></i>총 ${v.totalSeats}석</div>` : ''}
                    </div>
                    <div class="card-cta">
                        <button class="btn-reserve">대관 신청 →</button>
                    </div>
                </div>
            </div>`;
        }).join('');

        $list.insertAdjacentHTML('beforeend', html);

        // 카드 클릭 → /venues/{id}/reservation
        $list.querySelectorAll('.venue-card').forEach(el => {
            const go = () => {
                const id = el.dataset.id;
                if (id) window.location.href = `/venues/${id}/reservation`;
            };
            el.addEventListener('click', go);
            el.addEventListener('keydown', e => { if (e.key === 'Enter') go(); });
        });
    }

    // ── API 호출 ───────────────────────────────────────────────
    async function fetchVenues({ keyword = '', reset = false }) {
        if (loading || (!reset && !hasNext)) return;
        loading = true;

        if (reset) {
            page = DEFAULT_PAGE;
            hasNext = true;
        }

        const params = new URLSearchParams({ page, size });
        const kw = keyword.trim();
        if (kw) params.set('keyword', kw);

        try {
            const res  = await fetch(`/api/venues?${params}`, { credentials: 'include' });
            if (!res.ok) { console.error('[performerVenueList] fetch 실패', res.status); appendItems([], reset); return; }
            const body = await res.json();
            const pageObj = body?.data;
            const items   = pageObj?.content || [];

            // Spring Page 객체 기준: last 플래그가 있으면 사용
            if (typeof pageObj?.last === 'boolean') {
                hasNext = !pageObj.last;
            } else {
                // last 정보가 없으면 "현재 페이지의 아이템 개수 < size" 를 기준으로 추정
                hasNext = items.length === size;
            }

            appendItems(items, reset);
            page += 1;
        } catch (e) {
            console.error('[performerVenueList] fetch 오류', e);
            if (reset) appendItems([], true);
        } finally {
            loading = false;
        }
    }

    // ── 검색 이벤트 ────────────────────────────────────────────
    $input.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            fetchVenues({ keyword: $input.value, reset: true });
        }, 250);
    });

    $input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            clearTimeout(debounceTimer);
            fetchVenues({ keyword: $input.value, reset: true });
        }
    });

    // ── 무한 스크롤 ────────────────────────────────────────────
    window.addEventListener('scroll', () => {
        const scrollPosition = window.scrollY + window.innerHeight;
        const threshold      = document.body.offsetHeight - 200;
        if (scrollPosition >= threshold) {
            fetchVenues({ keyword: $input.value, reset: false });
        }
    });

    // ── 초기 로드 ──────────────────────────────────────────────
    fetchVenues({ keyword: '', reset: true });
})();
