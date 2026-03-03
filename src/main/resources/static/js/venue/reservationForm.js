/**
 * [reservationForm.js] 공연자 전용 공연장 상세 + 대관 신청
 *
 * 흐름:
 *  1. GET /api/venues/{venueId}    → 공연장 정보 로드
 *  2. 날짜 칩(오늘~+13일) 렌더링   → 정기/임시 휴무 비활성
 *  3. 날짜 선택 → bookingUnit 기반 슬롯 그리드 렌더링
 *  4. 시작 슬롯 선택 → 종료 슬롯 선택 → 범위 하이라이트
 *  5. 요약(날짜/시간/대관료) + 하단 바 업데이트
 *  6. POST /api/reservations       → 신청 완료 → /venues/reservations/my
 */

(() => {
    // ─── 상수 ──────────────────────────────────────────────────
    const DAY_KO  = ['일', '월', '화', '수', '목', '금', '토'];
    const DAY_EN  = ['SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'];
    const MONTH_KO = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'];

    // ─── 상태 ──────────────────────────────────────────────────
    let venue = null;          // VenueDetailDto
    let selectedDate = null;   // Date 객체
    let startSlot = null;      // "HH:MM"
    let endSlot   = null;      // "HH:MM" (포함 종료)

    // ─── DOM ───────────────────────────────────────────────────
    const venueId    = document.getElementById('pageConfig')?.dataset?.venueId;
    const $venueInfo = document.getElementById('venueInfo');
    const $dateSection = document.getElementById('dateSection');
    const $slotSection = document.getElementById('slotSection');
    const $summarySection = document.getElementById('summarySection');
    const $msgSection = document.getElementById('msgSection');
    const $bottomBar  = document.getElementById('bottomBar');
    const $btnSubmit  = document.getElementById('btnSubmit');
    const $msgInput   = document.getElementById('msgInput');
    const $charCount  = document.getElementById('charCount');

    if (!venueId) {
        $venueInfo.innerHTML = `<p style="color:#E63946;padding:20px;">공연장 ID를 찾을 수 없습니다.</p>`;
        return;
    }

    // ─── 초기화 ────────────────────────────────────────────────
    (async function init() {
        await loadVenue();
        renderDates();
        bindMessage();
        bindSubmit();
    })();

    // ─── 공연장 정보 로드 ───────────────────────────────────────
    async function loadVenue() {
        try {
            const res  = await fetch(`/api/venues/${venueId}`, { credentials: 'include' });
            const json = await res.json();
            if (!json.success) throw new Error(json.message);
            venue = json.data;
            document.getElementById('headerTitle').textContent = venue.venueName;
            renderVenueInfo();
            show($dateSection);
            show($slotSection);
        } catch (e) {
            console.error('[reservationForm] 공연장 로드 실패:', e);
            $venueInfo.innerHTML = `<p style="color:#E63946;padding:20px;">공연장 정보를 불러오지 못했습니다.</p>`;
        }
    }

    // ─── 공연장 정보 카드 ───────────────────────────────────────
    function renderVenueInfo() {
        const v = venue;
        const typeMap = {CONCERT_HALL:'콘서트홀', THEATER:'극장', CLUB:'클럽', OUTDOOR:'야외', STUDIO:'스튜디오'};
        const typeLabel = typeMap[v.venueType] || (v.venueType || '기타');

        const imgHtml = v.venueImage
            ? `<img src="${esc(v.venueImage)}" alt="공연장 이미지" class="venue-image"/>`
            : `<div class="venue-image-placeholder"><i class="bi bi-building"></i></div>`;

        const facilitiesHtml = v.facilities?.length
            ? `<div class="facilities-wrap">${v.facilities.map(f => `<span class="facility-chip">${esc(f)}</span>`).join('')}</div>`
            : '';

        const closingHtml = buildClosingHtml(v);

        $venueInfo.innerHTML = `
        <div class="venue-card">
            ${imgHtml}
            <div class="venue-body">
                <div class="venue-name-row">
                    <span class="venue-name">${esc(v.venueName)}</span>
                    <span class="venue-type-badge">${esc(typeLabel)}</span>
                </div>
                <div class="venue-meta">
                    ${v.address    ? `<div class="meta-row"><i class="bi bi-geo-alt-fill"></i>${esc(v.address)}</div>` : ''}
                    ${v.contact    ? `<div class="meta-row"><i class="bi bi-telephone-fill"></i>${esc(v.contact)}</div>` : ''}
                    ${v.openTime   ? `<div class="meta-row"><i class="bi bi-clock-fill"></i>운영시간: ${esc(v.openTime)} ~ ${esc(v.closeTime)}</div>` : ''}
                    ${v.bookingUnit ? `<div class="meta-row"><i class="bi bi-hourglass-split"></i>예약 단위: ${v.bookingUnit}분</div>` : ''}
                    ${v.rentalFee   ? `<div class="meta-row"><i class="bi bi-cash-stack"></i>${v.rentalFee.toLocaleString()}원 / ${v.bookingUnit}분</div>` : ''}
                    ${v.totalSeats  ? `<div class="meta-row"><i class="bi bi-people-fill"></i>총 ${v.totalSeats}석</div>` : ''}
                    ${v.description ? `<div class="meta-row" style="align-items:flex-start;"><i class="bi bi-info-circle-fill" style="margin-top:2px;"></i><span>${esc(v.description)}</span></div>` : ''}
                </div>
                ${facilitiesHtml ? `<hr class="venue-divider"/>${facilitiesHtml}` : ''}
                ${closingHtml}
            </div>
        </div>`;
    }

    function buildClosingHtml(v) {
        const lines = [];
        if (v.regularClosingDays?.length) {
            const dayNames = v.regularClosingDays.map(d => {
                const idx = DAY_EN.indexOf(d);
                return idx >= 0 ? `${DAY_KO[idx]}요일` : d;
            });
            lines.push(`정기 휴무: ${dayNames.join(', ')}`);
        }
        if (v.temporaryClosingDates?.length) {
            lines.push(`임시 휴무: ${v.temporaryClosingDates.join(', ')}`);
        }
        return lines.length ? `<p class="closing-info"><i class="bi bi-x-circle"></i> ${lines.join(' | ')}</p>` : '';
    }

    // ─── 날짜 칩 렌더링 (오늘 ~ +13일) ────────────────────────
    function renderDates() {
        if (!venue) return;
        const today = new Date();
        today.setHours(0,0,0,0);
        const $list = document.getElementById('dateList');
        $list.innerHTML = '';

        for (let i = 0; i < 14; i++) {
            const d = new Date(today);
            d.setDate(today.getDate() + i);
            const isClosed = isClosedDate(d);
            const chip = document.createElement('div');
            chip.className = `date-chip${isClosed ? ' closed' : ''}`;
            chip.dataset.date = formatDate(d);
            chip.innerHTML = `
                <span class="day-name">${DAY_KO[d.getDay()]}</span>
                <span class="day-num">${d.getDate()}</span>
                <span class="month">${MONTH_KO[d.getMonth()]}</span>`;
            if (!isClosed) {
                chip.addEventListener('click', () => selectDate(d, chip));
            }
            $list.appendChild(chip);
        }
    }

    function isClosedDate(d) {
        if (!venue) return false;
        const dayEn = DAY_EN[d.getDay()];
        if (venue.regularClosingDays?.includes(dayEn)) return true;
        const dateStr = formatDate(d);
        if (venue.temporaryClosingDates?.includes(dateStr)) return true;
        return false;
    }

    function selectDate(date, chip) {
        document.querySelectorAll('.date-chip.selected').forEach(el => el.classList.remove('selected'));
        chip.classList.add('selected');
        selectedDate = date;
        startSlot = null;
        endSlot   = null;
        renderSlots();
        updateSummary();   // 슬롯 미선택 상태 → summary/bottomBar 숨김
        updateBottomBar(); // 날짜 바꿀 때 하단 바 초기화
        show($slotSection);
    }

    // ─── 시간 슬롯 렌더링 ───────────────────────────────────────
    function renderSlots() {
        const $grid = document.getElementById('slotGrid');
        $grid.innerHTML = '';
        if (!venue || !selectedDate) return;

        const slots = buildSlots();
        if (!slots.length) {
            $grid.innerHTML = `<p style="color:var(--text-muted);font-size:0.85rem;">운영 시간 정보가 없습니다.</p>`;
            return;
        }

        slots.forEach(time => {
            const chip = document.createElement('div');
            chip.className = 'slot-chip';
            chip.dataset.time = time;
            chip.textContent = time;
            chip.addEventListener('click', () => handleSlotClick(time));
            $grid.appendChild(chip);
        });
        updateSlotHighlight();
    }

    /**
     * openTime ~ closeTime 을 bookingUnit 분 간격으로 슬롯 생성
     * 마지막 슬롯 = closeTime (종료 선택용)
     */
    function buildSlots() {
        if (!venue.openTime || !venue.closeTime || !venue.bookingUnit) return [];
        const open  = parseTime(venue.openTime);
        const close = parseTime(venue.closeTime);
        const unit  = venue.bookingUnit;
        const slots = [];
        let cur = open;
        while (cur <= close) {
            slots.push(minutesToHHMM(cur));
            if (cur === close) break;
            cur = Math.min(cur + unit, close);
        }
        return slots;
    }

    function handleSlotClick(time) {
        if (!startSlot) {
            // 1차 클릭 → 시작 슬롯
            startSlot = time;
            endSlot   = null;
        } else if (time === startSlot) {
            // 같은 슬롯 클릭 → 초기화
            startSlot = null;
            endSlot   = null;
        } else if (!endSlot) {
            // 2차 클릭 → 종료 슬롯
            if (time <= startSlot) {
                // 시작보다 이전이면 시작을 새로 지정
                startSlot = time;
                endSlot   = null;
            } else {
                endSlot = time;
            }
        } else {
            // 다시 시작부터
            startSlot = time;
            endSlot   = null;
        }
        updateSlotHighlight();
        updateSummary();
        updateBottomBar();
    }

    function updateSlotHighlight() {
        document.querySelectorAll('.slot-chip').forEach(chip => {
            const t = chip.dataset.time;
            chip.classList.remove('start', 'end', 'in-range');
            if (t === startSlot) chip.classList.add('start');
            else if (t === endSlot)   chip.classList.add('end');
            else if (startSlot && endSlot && t > startSlot && t < endSlot)
                chip.classList.add('in-range');
        });
    }

    // ─── 요약 업데이트 ──────────────────────────────────────────
    function updateSummary() {
        if (!selectedDate || !startSlot || !endSlot) {
            hide($summarySection);
            hide($msgSection);
            return;
        }
        show($summarySection);
        show($msgSection);
        show($bottomBar);

        const dateStr = formatDateDisplay(selectedDate);
        const duration = calcDurationMinutes();
        const fee = calcFee(duration);

        document.getElementById('sumDate').textContent     = dateStr;
        document.getElementById('sumTime').textContent     = `${startSlot} ~ ${endSlot}`;
        document.getElementById('sumDuration').textContent = `${duration}분 (${(duration/60).toFixed(1)}시간)`;
        document.getElementById('sumFee').textContent      = fee ? `${fee.toLocaleString()}원` : '-';
    }

    function updateBottomBar() {
        if (!startSlot || !endSlot) {
            document.getElementById('feePreview').textContent = '선택 없음';
            $btnSubmit.disabled = true;
            return;
        }
        const fee = calcFee(calcDurationMinutes());
        document.getElementById('feePreview').textContent = fee ? `${fee.toLocaleString()}원` : '-';
        $btnSubmit.disabled = !selectedDate;
    }

    function calcDurationMinutes() {
        if (!startSlot || !endSlot) return 0;
        return parseTime(endSlot) - parseTime(startSlot);
    }

    function calcFee(minutes) {
        if (!venue.rentalFee || !venue.bookingUnit || minutes <= 0) return null;
        return Math.round((minutes / venue.bookingUnit) * venue.rentalFee);
    }

    // ─── 메시지 바인딩 ──────────────────────────────────────────
    function bindMessage() {
        $msgInput.addEventListener('input', () => {
            $charCount.textContent = $msgInput.value.length;
        });
    }

    // ─── 제출 ────────────────────────────────────────────────────
    function bindSubmit() {
        $btnSubmit.addEventListener('click', async () => {
            if (!selectedDate || !startSlot || !endSlot) return;

            const startAt = buildISO(selectedDate, startSlot);
            const endAt   = buildISO(selectedDate, endSlot);

            const body = {
                venueId: Number(venueId),
                startAt,
                endAt,
                message: $msgInput.value.trim() || null
            };

            $btnSubmit.disabled = true;
            $btnSubmit.textContent = '신청 중...';

            try {
                const res  = await fetch('/api/reservations', {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });
                const json = await res.json();

                if (json.success) {
                    alert('대관 신청이 완료되었습니다.');
                    window.location.href = '/venues/reservations/my';
                } else {
                    alert(`신청 실패: ${json.message || '알 수 없는 오류'}`);
                    $btnSubmit.disabled = false;
                    $btnSubmit.textContent = '대관 신청하기';
                }
            } catch (e) {
                console.error('[reservationForm] 신청 오류:', e);
                alert('신청 중 오류가 발생했습니다. 다시 시도해주세요.');
                $btnSubmit.disabled = false;
                $btnSubmit.textContent = '대관 신청하기';
            }
        });
    }

    // ─── 유틸 ────────────────────────────────────────────────────
    function parseTime(hhmm) {
        const [h, m] = hhmm.split(':').map(Number);
        return h * 60 + m;
    }

    function minutesToHHMM(minutes) {
        const h = String(Math.floor(minutes / 60)).padStart(2, '0');
        const m = String(minutes % 60).padStart(2, '0');
        return `${h}:${m}`;
    }

    function formatDate(d) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${dd}`;
    }

    function formatDateDisplay(d) {
        return `${d.getFullYear()}년 ${d.getMonth()+1}월 ${d.getDate()}일 (${DAY_KO[d.getDay()]})`;
    }

    function buildISO(date, hhmm) {
        const [h, m] = hhmm.split(':');
        const d = new Date(date);
        d.setHours(Number(h), Number(m), 0, 0);
        // LocalDateTime 포맷 (초 포함)
        const pad = n => String(n).padStart(2,'0');
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
    }

    function show(el) { if (el) el.style.display = ''; }
    function hide(el) { if (el) el.style.display = 'none'; }

    function esc(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }
})();
