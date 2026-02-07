$(function () {
    const performanceId = $("#performanceId").val();

    function escapeHtml(str) {
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function renderDetail(d) {
        const title = d?.title ?? "-";
        const status = d?.status ?? "-";
        const capacity = d?.capacity ?? "-";
        const description = d?.description ?? "-";

        $("#detailWrap").html(`
            <h4 class="mb-2">${escapeHtml(title)}</h4>
            <div class="mb-2">
                <span class="badge text-bg-secondary">${escapeHtml(status)}</span>
                <span class="ms-2 text-muted">정원: ${escapeHtml(capacity)}</span>
            </div>
            <hr/>
            <div class="small text-muted mb-1">설명</div>
            <div>${escapeHtml(description)}</div>
        `);
    }

    function loadDetail() {
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json",
        })
            .done(function (res) {
                const data = res?.data;

                if (!data) {
                    console.error("[performance detail] data 없음", res);
                    $("#detailWrap").html(`<div class="text-danger">응답 형식 오류(data 없음)</div>`);
                    return;
                }

                renderDetail(data);
            })
            .fail(function (xhr) {
                console.error("[performance detail] 상세 조회 실패", xhr);
                $("#detailWrap").html(`<div class="text-danger">상세 조회 실패</div>`);
            });
    }

    loadDetail();
});
