/**
 * [파일명] venueForm.js
 * [설명] 공연장 등록(CREATE) / 수정·삭제(UPDATE) 통합 관리
 *        Thymeleaf → pageConfig div의 data-mode, data-venue-id 로 모드 분기
 */
const venueManager = (() => {
    let currentStep = 1;
    const totalSteps = 3;

    // Thymeleaf가 data-mode, data-venue-id를 pageConfig div에 심어줌
    const config  = document.getElementById('pageConfig')?.dataset ?? {};
    const isEdit  = config.mode === 'UPDATE';
    const venueId = config.venueId ?? null;

    // prefill 완료 여부 — step2 진입 시 완료 전이면 완료 후 draw() 재실행
    let prefillDone = !isEdit; // CREATE면 처음부터 true

    let temporaryClosingDates = [];
    let floors = [{ id: Date.now(), name: "1층", seats: [] }];
    let currentFloorIdx = 0;

    let canvas, ctx;
    let selectedSeat = null;
    let isDragging   = false;
    let lastPos      = { x: 0, y: 0 };
    const SEAT_SIZE_RATIO = 0.05;

    // ── 공통 에러 핸들러 ──────────────────────────────────────────────────
    const handleApiError = async (response, action) => {
        const map = { 400:'입력값을 다시 확인해주세요.', 401:'로그인이 필요합니다.',
                      403:'권한이 없습니다.', 404:'공연장을 찾을 수 없습니다.', 409:'이미 등록된 공연장입니다.' };
        let msg = map[response.status];
        if (!msg) {
            try { const b = await response.json(); msg = b.message || `서버 오류 (${response.status})`; }
            catch { msg = `서버 오류 (${response.status})`; }
        }
        alert(`${action} 실패: ${msg}`);
    };

    // ── 유효성 검사 ───────────────────────────────────────────────────────
    const validateStep = (step) => {
        if (step === 1) {
            const name    = document.querySelector('input[name="venueName"]');
            const address = document.getElementById('addressInput');
            if (!name?.value.trim())    { alert("공연장 이름을 입력해주세요."); name.focus(); return false; }
            if (!address?.value.trim()) { alert("공연장 주소를 입력해주세요."); venueManager.searchAddress(); return false; }
            return true;
        }
        if (step === 2) {
            if (floors.reduce((s, f) => s + f.seats.length, 0) === 0) {
                alert("최소 하나 이상의 좌석을 배치해주세요."); return false;
            }
            return true;
        }
        if (step === 3) {
            const fee = document.querySelector('input[name="rentalFee"]');
            if (!fee?.value || parseInt(fee.value) < 0) { alert("올바른 대관료를 입력해주세요. (0원 이상)"); fee.focus(); return false; }
            const open  = document.querySelector('input[name="openTime"]');
            const close = document.querySelector('input[name="closeTime"]');
            if (open?.value && close?.value && open.value >= close.value) {
                alert("마감 시간은 오픈 시간보다 늦어야 합니다."); close.focus(); return false;
            }
            return true;
        }
        return true;
    };

    // ── UI 업데이트 ───────────────────────────────────────────────────────
    const updateUI = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        document.querySelectorAll('.step-content').forEach(el => el.classList.add('d-none'));
        document.getElementById(`step${currentStep}`)?.classList.remove('d-none');
        document.querySelectorAll('.step').forEach((el, idx) => el.classList.toggle('active', idx + 1 <= currentStep));

        const label = isEdit ? '공연장 수정' : '공연장 등록';
        const headerTitle = document.getElementById('headerTitle');
        if (headerTitle) headerTitle.innerText = `${label} (${currentStep}/${totalSteps})`;

        if (currentStep === 2) {
            requestAnimationFrame(() => {
                venueManager.seatEditor.init();
                // prefill이 이미 완료됐으면 바로 그리고, 아직이면 prefill 완료 시 자동 draw
                if (prefillDone) venueManager.seatEditor.draw();
            });
        }

        const btnNext   = document.getElementById('btnNext');
        const btnSubmit = document.getElementById('btnSubmit');
        if (currentStep === totalSteps) {
            btnNext?.classList.add('d-none');
            btnSubmit?.classList.remove('d-none');
        } else {
            btnNext?.classList.remove('d-none');
            btnSubmit?.classList.add('d-none');
        }
    };

    /**
     * 기존 공연장 데이터를 폼에 채움 (UPDATE 모드 전용)
     * GET /api/venues/{venueId}/form 호출 → floors 재그룹핑
     */
    const prefill = async () => {
        try {
            const res = await fetch(`/api/venues/${venueId}/form`);
            if (!res.ok) { await handleApiError(res, '데이터 로드'); return; }
            const json = await res.json();
            const v    = json.data ?? json;

            // Step 1
            const set = (name, val) => { const el = document.querySelector(`[name="${name}"]`); if (el) el.value = val ?? ''; };
            set('venueName',     v.venueName);
            set('venueType',     v.venueType);
            set('contact',       v.contact);
            set('description',   v.description);
            set('addressDetail', v.addressDetail);
            const addrEl = document.getElementById('addressInput');
            if (addrEl) addrEl.value = v.address ?? '';

            // 편의시설 체크
            (v.facilities ?? []).forEach(fac => {
                const el = document.querySelector(`input[name="facilities"][value="${fac}"]`);
                if (el) el.checked = true;
            });

            // 이미지
            if (v.imageUrl) {
                const img = document.getElementById('previewImg');
                const btn = document.getElementById('btnRemoveImg');
                if (img) { img.src = v.imageUrl; img.classList.remove('d-none'); }
                if (btn) btn.classList.remove('d-none');
            }

            // Step 2 — flat seats → floors 재그룹핑
            if (v.seats?.length) {
                const grouped = {};
                v.seats.forEach(s => {
                    const key = s.floor ?? 1;
                    if (!grouped[key]) grouped[key] = [];
                    grouped[key].push({ id: s.id, xRatio: s.xratio, yRatio: s.yratio, label: s.label, grade: s.grade });
                });
                floors = Object.keys(grouped).sort().map(f => ({
                    id: Date.now() + Number(f),
                    name: `${f}층`,
                    seats: grouped[f]
                }));
                console.log('[prefill] floors:', JSON.stringify(floors));
            }

            // prefill 완료 표시 — 이미 step2 열려있으면 즉시 재렌더링
            prefillDone = true;
            if (currentStep === 2) {
                venueManager.seatEditor.renderTabs();
                venueManager.seatEditor.draw();
            }

            // Step 3
            set('openTime',    v.openTime);
            set('closeTime',   v.closeTime);
            set('rentalFee',   v.rentalFee);
            set('bookingUnit', v.bookingUnit);

            // 정기 휴무일
            (v.regularClosingDays ?? []).forEach(day => {
                const el = document.querySelector(`input[name="regularClosingDays"][value="${day}"]`);
                if (el) el.checked = true;
            });

            // 임시 휴무일
            temporaryClosingDates = v.temporaryClosingDates ?? [];
            venueManager.renderTempDates();

        } catch (e) {
            console.error('[prefill] 오류:', e);
            alert("데이터를 불러오는 중 오류가 발생했습니다.");
        }
    };

    /**
     * 폼 데이터 수집 후 FormData 반환
     * floors → seats 변환 (xRatio*1000 → 정수)
     */
    const buildFormData = () => {
        const form = document.getElementById('venueForm');
        const dto  = {
            venueName:          form.querySelector('[name="venueName"]').value.trim(),
            venueType:          form.querySelector('[name="venueType"]').value,
            contact:            form.querySelector('[name="contact"]').value.trim(),
            address:            form.querySelector('[name="address"]').value.trim(),
            addressDetail:      form.querySelector('[name="addressDetail"]').value.trim(),
            description:        form.querySelector('[name="description"]').value.trim(),
            facilities:         [...form.querySelectorAll('[name="facilities"]:checked')].map(el => el.value),
            openTime:           form.querySelector('[name="openTime"]').value,
            closeTime:          form.querySelector('[name="closeTime"]').value,
            rentalFee:          Number(form.querySelector('[name="rentalFee"]').value),
            bookingUnit:        Number(form.querySelector('[name="bookingUnit"]').value),
            regularClosingDays: [...form.querySelectorAll('[name="regularClosingDays"]:checked')].map(el => el.value),
            temporaryClosingDates,
            // floors(내부 에디터 구조) → 서버 SeatCreateRequest 구조로 변환
            // xRatio(0~1 소수) → xPos(0~1000 정수) 변환 후 서버 전송
            seats: floors.flatMap((f, floorIdx) =>
                f.seats.map(s => ({
                    floor:  floorIdx + 1,
                    x:      Math.round(s.xRatio * 1000),
                    y:      Math.round(s.yRatio * 1000),
                    number: s.label,
                    type:   s.grade
                }))
            ),
        };
        const fd = new FormData();
        fd.append('venueData', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
        const img = document.getElementById('venueImage').files[0];
        if (img) fd.append('venueImage', img);
        return fd;
    };

    // ── 공개 API ─────────────────────────────────────────────────────────
    return {

        /**
         * 페이지 초기화 — DOMContentLoaded에서 호출
         * CREATE/UPDATE 모드 감지 후 버튼 텍스트 설정, UPDATE면 prefill 실행
         */
        init: () => {
            const btnSubmit = document.getElementById('btnSubmit');
            const btnDelete = document.getElementById('btnDelete');
            if (btnSubmit) btnSubmit.innerText = isEdit ? '수정 완료' : '공연장 등록 완료';
            if (btnDelete) btnDelete.classList.toggle('d-none', !isEdit); // UPDATE일 때만 표시
            updateUI();
            if (isEdit) prefill();
        },

        nextStep: () => {
            if (validateStep(currentStep) && currentStep < totalSteps) { currentStep++; updateUI(); }
        },
        prevStep: () => {
            if (currentStep > 1) { currentStep--; updateUI(); }
            else { if (confirm("작성 중인 내용이 저장되지 않습니다. 목록으로 돌아갈까요?")) location.href = '/venues'; }
        },

        // ── 등록 / 수정 제출 ───────────────────────────────────────────────
        submitData: async () => {
            if (!validateStep(3)) return;
            const action = isEdit ? '수정' : '등록';
            if (!confirm(`공연장을 ${action}하시겠습니까?`)) return;

            const btnSubmit = document.getElementById('btnSubmit');
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${action} 중...`;

            try {
                const res = await fetch(
                    isEdit ? `/api/venues/${venueId}` : '/api/venues',
                    { method: isEdit ? 'PUT' : 'POST', body: buildFormData() }
                );
                if (res.ok) { alert(`공연장이 성공적으로 ${action}되었습니다!`); location.href = '/venues'; }
                else await handleApiError(res, action);
            } catch (e) {
                console.error(`[VenueForm] ${action} 오류:`, e);
                alert("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            } finally {
                btnSubmit.disabled  = false;
                btnSubmit.innerText = isEdit ? '수정 완료' : '공연장 등록 완료';
            }
        },

        // ── 삭제 ──────────────────────────────────────────────────────────
        deleteVenue: async () => {
            if (!confirm("공연장을 삭제하시겠습니까?\n삭제된 정보는 복구할 수 없습니다.")) return;

            const btnDelete = document.getElementById('btnDelete');
            btnDelete.disabled = true;
            btnDelete.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>삭제 중...`;

            try {
                const res = await fetch(`/api/venues/${venueId}`, { method: 'DELETE' });
                if (res.ok) { alert("공연장이 삭제되었습니다."); location.href = '/venues'; }
                else await handleApiError(res, '삭제');
            } catch (e) {
                console.error('[VenueForm] 삭제 오류:', e);
                alert("네트워크 오류가 발생했습니다.");
            } finally {
                btnDelete.disabled  = false;
                btnDelete.innerText = '공연장 삭제';
            }
        },

        // ── 좌석 에디터 ───────────────────────────────────────────────────
        seatEditor: {
            currentMode: 'add',
            currentGrade: 'vip',
            gradeColors: { vip: '#fbbf24', r: '#f87171', s: '#60a5fa', a: '#34d399' },

            init: () => {
                canvas = document.getElementById('seatCanvas');
                if (!canvas) return;
                ctx = canvas.getContext('2d');
                const wrapper = canvas.parentElement;
                canvas.width  = wrapper.clientWidth;
                canvas.height = wrapper.clientHeight;

                const getPos = (e) => {
                    const rect = canvas.getBoundingClientRect();
                    return {
                        x: (e.touches ? e.touches[0].clientX : e.clientX) - rect.left,
                        y: (e.touches ? e.touches[0].clientY : e.clientY) - rect.top
                    };
                };

                const start = (e) => {
                    const pos  = getPos(e);
                    const size = canvas.width * SEAT_SIZE_RATIO;
                    const hit  = (s) => {
                        const px = s.xRatio * canvas.width, py = s.yRatio * canvas.height;
                        return pos.x >= px && pos.x <= px + size && pos.y >= py && pos.y <= py + size;
                    };

                    if (venueManager.seatEditor.currentMode === 'delete') {
                        const idx = floors[currentFloorIdx].seats.findIndex(hit);
                        if (idx > -1) { floors[currentFloorIdx].seats.splice(idx, 1); venueManager.seatEditor.draw(); }
                        return;
                    }

                    const seats    = floors[currentFloorIdx].seats;
                    const foundIdx = [...seats].reverse().findIndex(hit);
                    const realIdx  = foundIdx === -1 ? -1 : seats.length - 1 - foundIdx;

                    if (venueManager.seatEditor.currentMode === 'add') {
                        if (realIdx === -1) venueManager.seatEditor.addSeat(pos);
                        else selectedSeat = seats[realIdx];
                    } else if (venueManager.seatEditor.currentMode === 'move') {
                        if (realIdx > -1) { selectedSeat = seats[realIdx]; isDragging = true; lastPos = pos; }
                        else selectedSeat = null;
                    }
                    venueManager.seatEditor.draw();
                };

                const move = (e) => {
                    if (!isDragging || !selectedSeat || venueManager.seatEditor.currentMode !== 'move') return;
                    if (e.cancelable) e.preventDefault();
                    const pos = getPos(e);
                    selectedSeat.xRatio = Math.max(0, Math.min(1 - SEAT_SIZE_RATIO, selectedSeat.xRatio + (pos.x - lastPos.x) / canvas.width));
                    selectedSeat.yRatio = Math.max(0, Math.min(1 - SEAT_SIZE_RATIO, selectedSeat.yRatio + (pos.y - lastPos.y) / canvas.height));
                    lastPos = pos;
                    venueManager.seatEditor.draw();
                };

                canvas.onmousedown = start; canvas.ontouchstart = start;
                window.onmousemove = move;  window.ontouchmove  = move;
                window.onmouseup = window.ontouchend = () => isDragging = false;

                venueManager.seatEditor.renderTabs();
                venueManager.seatEditor.draw();
            },

            setMode: (mode) => {
                venueManager.seatEditor.currentMode = mode;
                document.querySelectorAll('.mode-btn').forEach(btn => {
                    btn.classList.toggle('active', btn.id === `btnMode${mode.charAt(0).toUpperCase() + mode.slice(1)}`);
                });
                selectedSeat = null;
                venueManager.seatEditor.draw();
            },

            setGrade: (grade) => {
                venueManager.seatEditor.currentGrade = grade;
                document.querySelectorAll('.seat-type-btn').forEach(btn => btn.classList.toggle('active', btn.classList.contains(grade)));
            },

            draw: () => {
                if (!ctx) return;
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                const size = canvas.width * SEAT_SIZE_RATIO;
                floors[currentFloorIdx].seats.forEach(s => {
                    ctx.fillStyle   = venueManager.seatEditor.gradeColors[s.grade || 'vip'];
                    ctx.strokeStyle = selectedSeat === s ? "#E63946" : "#333";
                    ctx.lineWidth   = selectedSeat === s ? 2 : 1;
                    ctx.beginPath();
                    ctx.roundRect(s.xRatio * canvas.width, s.yRatio * canvas.height, size, size, size * 0.2);
                    ctx.fill(); ctx.stroke();
                    ctx.fillStyle = "#000"; ctx.font = `bold ${size * 0.4}px Noto Sans KR`;
                    ctx.textAlign = "center"; ctx.textBaseline = "middle";
                    ctx.fillText(s.label, s.xRatio * canvas.width + size / 2, s.yRatio * canvas.height + size / 2);
                });
                venueManager.seatEditor.updateStats();
            },

            updateStats: () => {
                const all = floors.flatMap(f => f.seats);
                const set = (id, val) => { const el = document.getElementById(id); if (el) el.innerText = val; };
                set('countVIP', all.filter(s => s.grade === 'vip').length);
                set('countR',   all.filter(s => s.grade === 'r').length);
                set('countS',   all.filter(s => s.grade === 's').length);
                set('countA',   all.filter(s => s.grade === 'a').length);
                set('totalSeatsDisplay', all.length);
            },

            addSeat: (pos) => {
                const input = document.getElementById('seatLabelInput');
                const label = input?.value.trim() || 'A1';
                const size  = canvas.width * SEAT_SIZE_RATIO;
                const seat  = {
                    xRatio: Math.max(0, Math.min(1 - SEAT_SIZE_RATIO, pos ? (pos.x - size / 2) / canvas.width  : 0.4)),
                    yRatio: Math.max(0, Math.min(1 - SEAT_SIZE_RATIO, pos ? (pos.y - size / 2) / canvas.height : 0.4)),
                    label, grade: venueManager.seatEditor.currentGrade, id: Date.now()
                };
                floors[currentFloorIdx].seats.push(seat);
                selectedSeat = seat;
                const match = label.match(/^([A-Za-z]+)(\d+)$/);
                if (match && input) input.value = match[1] + (parseInt(match[2]) + 1);
                venueManager.seatEditor.draw();
            },

            renderTabs: () => {
                const container = document.getElementById('floorTabs');
                if (!container) return;
                container.innerHTML = floors.map((f, i) => `
                    <div class="floor-btn ${i === currentFloorIdx ? 'active' : ''}" onclick="venueManager.seatEditor.switchFloor(${i})">
                        ${f.name}
                        ${i > 0 ? `<i class="bi bi-x ms-1" onclick="event.stopPropagation(); venueManager.seatEditor.deleteFloor(${i})"></i>` : ''}
                    </div>
                `).join('');
            },

            switchFloor: (i) => { currentFloorIdx = i; selectedSeat = null; venueManager.seatEditor.renderTabs(); venueManager.seatEditor.draw(); },
            addFloor:    ()  => { floors.push({ id: Date.now(), name: `${floors.length + 1}층`, seats: [] }); venueManager.seatEditor.renderTabs(); },
            deleteFloor: (idx) => {
                if (floors.length <= 1) return;
                if (confirm(`${floors[idx].name} 삭제하시겠습니까?`)) {
                    floors.splice(idx, 1); currentFloorIdx = 0;
                    venueManager.seatEditor.renderTabs(); venueManager.seatEditor.draw();
                }
            }
        },

        // ── 주소 / 이미지 ─────────────────────────────────────────────────
        searchAddress: () => {
            if (typeof daum === "undefined") return alert("주소 서비스를 불러올 수 없습니다.");
            new daum.Postcode({
                oncomplete: (data) => {
                    document.getElementById('addressInput').value =
                        data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
                    document.getElementById('addressDetail').focus();
                }
            }).open();
        },

        handleImgPreview: (input) => {
            if (!input.files?.[0]) return;
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.getElementById('previewImg');
                const btn = document.getElementById('btnRemoveImg');
                if (img) { img.src = e.target.result; img.classList.remove('d-none'); }
                if (btn) btn.classList.remove('d-none');
            };
            reader.readAsDataURL(input.files[0]);
        },

        removeImage: () => {
            const input = document.getElementById('venueImage');
            const img   = document.getElementById('previewImg');
            const btn   = document.getElementById('btnRemoveImg');
            if (input) input.value = "";
            if (img)   { img.src = ""; img.classList.add('d-none'); }
            if (btn)   btn.classList.add('d-none');
        },

        // ── 임시 휴무일 ───────────────────────────────────────────────────
        addTempDate: () => {
            const el = document.getElementById('tempDateInput');
            if (!el.value) return alert("날짜를 선택해 주세요.");
            if (temporaryClosingDates.includes(el.value)) return alert("이미 추가된 날짜입니다.");
            temporaryClosingDates.push(el.value);
            venueManager.renderTempDates();
            el.value = '';
        },
        removeTempDate: (i) => { temporaryClosingDates.splice(i, 1); venueManager.renderTempDates(); },
        renderTempDates: () => {
            const list = document.getElementById('tempDateList');
            if (list) list.innerHTML = temporaryClosingDates.map((d, i) => `
                <span class="badge-date">
                    ${d} <button type="button" class="remove-btn" onclick="venueManager.removeTempDate(${i})">×</button>
                </span>`).join('');
        },
    };
})();

// 페이지 로드 후 초기화
document.addEventListener('DOMContentLoaded', () => venueManager.init());
