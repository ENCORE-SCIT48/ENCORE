/**
 * [Venue List Page Script]
 * - 검색창(부트스트랩 d-flex 폼) + 공연장 리스트 렌더링
 * - API: GET /api/venues?keyword=&page=&size=
 * - 응답: CommonResponse<Page<VenueListItemDto>>
 *
 * [HTML 전제]
 * - input  : #venueSearchInput
 * - list   : #venueList
 * - empty  : #venueEmpty
 * - button : .venue-search-btn  (없어도 동작하도록 optional 처리)
 */
(() => {
    const $input = document.getElementById("venueSearchInput");
    const $list = document.getElementById("venueList");
    const $empty = document.getElementById("venueEmpty");
    const $searchBtn = document.querySelector(".venue-search-btn");

    // 필수 DOM이 없으면 종료 (템플릿 누락/ID 오타 방지)
    if (!$input || !$list || !$empty) {
        console.error("[VenueList] Required elements not found. Check IDs: venueSearchInput, venueList, venueEmpty");
        return;
    }

    const DEFAULT_PAGE = 0;
    const DEFAULT_SIZE = Number($input?.dataset?.size || 10);

    let debounceTimer = null;

    /**
     * [보안/안정성] HTML Escape
     * - 서버에서 내려온 문자열을 그대로 innerHTML에 넣을 때 XSS 방지
     */
    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    /**
     * [표시용 라벨] VenueType enum 문자열을 한글로
     */
    function venueTypeLabel(type) {
        if (!type) return "기타";
        const map = {
            CONCERT_HALL: "콘서트홀",
            THEATER: "극장",
        };
        return map[type] || type;
    }

    /**
     * [렌더링] 카드 리스트 출력
     * - VenueListItemDto: venueId, venueName, address, venueType, totalSeats
     */
    function render(items) {
        $list.innerHTML = "";

        if (!items || items.length === 0) {
            $empty.classList.remove("d-none");
            return;
        }
        $empty.classList.add("d-none");

        const html = items
            .map((v) => {
                const id = v.venueId;
                const name = escapeHtml(v.venueName);
                const addr = escapeHtml(v.address);
                const type = venueTypeLabel(v.venueType);
                const seats = v.totalSeats;

                return `
                    <div class="venue-card" data-id="${id}">
                        <h3 class="venue-title">${name}</h3>
                        <p class="venue-sub">${addr}</p>
                        <div class="venue-meta">
                            <span class="badge text-bg-dark">${escapeHtml(type)}</span>
                            ${seats != null ? `<span class="badge text-bg-secondary">총 ${escapeHtml(seats)}석</span>` : ""}
                        </div>
                    </div>
                `;
            })
            .join("");

        $list.insertAdjacentHTML("beforeend", html);

        // 카드 클릭 시 상세 페이지 이동
        document.querySelectorAll(".venue-card").forEach((el) => {
            el.addEventListener("click", () => {
                const venueId = el.getAttribute("data-id");
                if (!venueId) return;
                window.location.href = `/venues/${venueId}`; // Page Controller 기준
            });
        });
    }

    /**
     * [API 호출]
     * - CommonResponse<Page<VenueListItemDto>>
     * - body.data.content 가 리스트
     */
    async function fetchVenues({ keyword = "", page = DEFAULT_PAGE, size = DEFAULT_SIZE }) {
        const params = new URLSearchParams();
        params.set("page", String(page));
        params.set("size", String(size));

        const trimmed = (keyword || "").trim();
        if (trimmed.length > 0) {
            params.set("keyword", trimmed);
        }

        const url = `/api/venues?${params.toString()}`;

        try {
            const res = await fetch(url, { headers: { Accept: "application/json" } });

            if (!res.ok) {
                console.error("[VenueList] fetch failed", res.status);
                render([]);
                return;
            }

            const body = await res.json();

            // body.data = Page 객체, body.data.content = 리스트
            const pageObj = body?.data;
            const items = pageObj?.content || [];

            render(items);
        } catch (e) {
            console.error("[VenueList] fetch error", e);
            render([]);
        }
    }

    /**
     * [검색 트리거 - 디바운스]
     * - 입력 중 잦은 호출 방지
     * - 검색은 항상 0페이지부터
     */
    function onSearchDebounced() {
        const keyword = ($input.value || "").trim();

        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            fetchVenues({ keyword, page: 0, size: DEFAULT_SIZE });
        }, 250);
    }

    /**
     * [검색 트리거 - 즉시]
     * - 버튼 클릭/엔터 등 즉시 실행용
     */
    function onSearchImmediate() {
        const keyword = ($input.value || "").trim();
        clearTimeout(debounceTimer); // [추가] 버튼/엔터는 즉시 실행하니까 대기 타이머 제거
        fetchVenues({ keyword, page: 0, size: DEFAULT_SIZE });
    }

    // 입력 시 디바운스 검색
    $input.addEventListener("input", onSearchDebounced);

    // 엔터(Enter) 입력 시 즉시 검색 (부트스트랩 form 사용 시 리로드 방지)
    $input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            onSearchImmediate();
        }
    });

    // 검색 버튼 클릭 시 즉시 검색
    $searchBtn?.addEventListener("click", onSearchImmediate);

    // 첫 진입 - 전체 목록 0페이지 호출
    fetchVenues({ keyword: "", page: 0, size: DEFAULT_SIZE });
})();
