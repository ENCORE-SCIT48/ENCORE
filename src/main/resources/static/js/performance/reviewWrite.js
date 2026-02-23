$(function () {
    const performanceId = $("#performanceId").val();

    // 1) URL에서 reviewId 읽기
    const qs = new URLSearchParams(window.location.search);
    const reviewIdFromQuery = Number(qs.get("reviewId") || 0);

    // 2) hidden에서 reviewId 읽기(있으면 우선)
    const reviewIdFromHidden = Number($("#reviewId").val() || 0);

    // 3) 최종 reviewId
    const reviewId = reviewIdFromHidden || reviewIdFromQuery;

    function loadPerformanceInfo() {
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json"
        }).done(function (res) {
            const d = res?.data || {};
            $("#perfTitle").text(d?.title ?? "공연제목");

            const venueName = d?.venueName ?? "-";
            const address = d?.address ?? "-";
            const statusMap = { MUSICAL: "뮤지컬", PLAY: "연극", BAND: "밴드 공연" };
            const category = statusMap[d?.status] ?? (d?.status ?? "-");

            const year = d?.productionYear ?? "-";
            $("#perfSub").text(`${address} · ${venueName} · ${category} · ${year}`);
        });
    }

    function renderStars(value) {
        $("#stars .star").each(function () {
            const v = Number($(this).data("value"));
            if (v <= value) $(this).addClass("is-on").text("★");
            else $(this).removeClass("is-on").text("☆");
        });

        $("#selectedRating").val(String(value));
        $("#ratingHint").text(value > 0 ? `${value}점 선택됨` : "별점을 선택해 주세요");
        validate();
    }

    function validate() {
        const rating = Number($("#selectedRating").val() || 0);
        const content = ($("#content").val() || "").trim();
        $("#submitBtn").prop("disabled", !(rating > 0 && content.length >= 5));
    }

    $("#stars").on("click", ".star", function () {
        renderStars(Number($(this).data("value")));
    });

    $("#stars").on("mouseenter", ".star", function () {
        const value = Number($(this).data("value"));
        $("#stars .star").each(function () {
            const v = Number($(this).data("value"));
            $(this).text(v <= value ? "★" : "☆");
        });
    }).on("mouseleave", function () {
        renderStars(Number($("#selectedRating").val() || 0));
    });

    $("#content").on("input", validate);

    $("#backBtn").on("click", function () {
        history.back();
    });

    function loadReviewIfEditMode() {
        if (!reviewId) return;

        $.ajax({
            url: `/api/performances/${performanceId}/reviews/${reviewId}`,
            method: "GET",
            dataType: "json",
            xhrFields: { withCredentials: true }
        }).done(function (res) {
            const d = res?.data || {};
            renderStars(Number(d.rating || 0));
            $("#content").val(d.content || "");
            validate();
        }).fail(function () {
            alert("리뷰 불러오기 실패");
        });
    }

    $("#submitBtn").off("click").on("click", function () {
        const rating = Number($("#selectedRating").val() || 0);
        const content = ($("#content").val() || "").trim();
        const isEdit = !!reviewId;

        $("#submitBtn").prop("disabled", true);

        $.ajax({
            url: isEdit
                ? `/api/performances/${performanceId}/reviews/${reviewId}`
                : `/api/performances/${performanceId}/reviews`,
            method: isEdit ? "PATCH" : "POST",
            contentType: "application/json",
            data: JSON.stringify({ rating, content }),
            xhrFields: { withCredentials: true }
        }).done(function () {
            window.location.href = `/performances/${performanceId}?tab=review`;
        }).fail(function (xhr) {
            alert(xhr?.responseJSON?.message || "저장 실패");
            validate();
        });
    });

    // init
    renderStars(0);
    loadPerformanceInfo();
    loadReviewIfEditMode();
});
