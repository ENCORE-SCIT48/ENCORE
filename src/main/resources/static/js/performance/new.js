$(function () {
    const $venueSelect = $("#venueId");
    const $form = $("#performanceForm");
    const $saveBtn = $("#saveBtn");
    const performanceId = $("#performanceId").val();
    const isEdit = !!performanceId;

    function loadVenues(onLoaded) {
        $.ajax({
            url: "/api/venues",
            method: "GET",
            dataType: "json",
            data: { page: 0, size: 50 },
        })
            .done(function (res) {
                const content = res?.data?.content ?? [];
                const list = Array.isArray(content) ? content : [];
                $venueSelect.empty();
                $venueSelect.append("<option value=''>공연장을 선택하세요</option>");
                list.forEach(function (v) {
                    const id = v.venueId;
                    if (!id) return;
                    const name = v.venueName || "공연장 " + id;
                    const address = v.address || "";
                    const label = address ? name + " · " + address : name;
                    $venueSelect.append(
                        $("<option></option>").val(id).text(label)
                    );
                });

                if (typeof onLoaded === "function") {
                    onLoaded();
                }
            })
            .fail(function (xhr) {
                console.error("[PerformanceNew] load venues failed", xhr.status, xhr.responseText);
                alert("공연장 목록을 불러오지 못했습니다. 나중에 다시 시도해 주세요.");
            });
    }

    function buildPayload() {
        const title = $("#title").val().trim();
        const venueId = $venueSelect.val();
        const category = $("#category").val();
        const capacityRaw = $("#capacity").val();
        const performanceImageUrl = $("#performanceImageUrl").val().trim();
        const description = $("#description").val();

        const payload = {
            title: title || null,
            description: description || null,
            performanceImageUrl: performanceImageUrl || null,
            category: category || null,
            capacity: capacityRaw ? Number(capacityRaw) : null,
            venueId: venueId ? Number(venueId) : null,
        };
        return payload;
    }

    function loadPerformanceForEdit() {
        if (!isEdit) return;
        $.ajax({
            url: `/api/performances/${performanceId}`,
            method: "GET",
            dataType: "json",
        })
            .done(function (res) {
                const d = res?.data;
                if (!d) return;
                $("#title").val(d.title || "");
                $("#description").val(d.description || "");
                $("#performanceImageUrl").val(d.performanceImageUrl || "");
                $("#capacity").val(d.capacity != null ? d.capacity : "");
                if (d.category) {
                    $("#category").val(d.category);
                }
                if (d.venueId) {
                    $venueSelect.val(String(d.venueId));
                }
            })
            .fail(function (xhr) {
                console.error("[PerformanceNew] load performance failed", xhr.status, xhr.responseText);
                alert("공연 정보를 불러오지 못했습니다.");
            });
    }

    $form.on("submit", function () {
        const data = buildPayload();
        if (!data.title) {
            alert("공연 제목을 입력해 주세요.");
            return false;
        }
        if (!data.venueId) {
            alert("공연장을 선택해 주세요.");
            return false;
        }

        $saveBtn.prop("disabled", true);

        $.ajax({
            url: isEdit ? `/api/performances/${performanceId}` : "/api/performances",
            method: isEdit ? "PUT" : "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(data),
        })
            .done(function (res) {
                const id = res?.data?.performanceId || performanceId;
                if (id) {
                    window.location.href = "/performances/" + id;
                } else {
                    window.location.href = "/performances";
                }
            })
            .fail(function (xhr) {
                console.error("[PerformanceNew] create failed", xhr.status, xhr.responseText);
                alert("공연 등록에 실패했습니다. " + (xhr.responseJSON?.message || "입력 값을 다시 확인해 주세요."));
            })
            .always(function () {
                $saveBtn.prop("disabled", false);
            });

        return false;
    });

    loadVenues(function () {
        if (isEdit) {
            loadPerformanceForEdit();
        }
    });
});

