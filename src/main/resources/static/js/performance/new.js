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
        const description = $("#description").val();

        return {
            title: title || null,
            description: description || null,
            performanceImageUrl: null,
            category: category || null,
            capacity: capacityRaw ? Number(capacityRaw) : null,
            venueId: venueId ? Number(venueId) : null,
        };
    }

    function buildFormData() {
        const fd = new FormData();
        fd.append("performanceData", new Blob([JSON.stringify(buildPayload())], { type: "application/json" }));
        const img = document.getElementById("performanceImage").files[0];
        if (img) fd.append("performanceImage", img);
        return fd;
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
                $("#capacity").val(d.capacity != null ? d.capacity : "");
                if (d.category) {
                    $("#category").val(d.category);
                }
                if (d.venueId) {
                    $venueSelect.val(String(d.venueId));
                }
                if (d.performanceImageUrl) {
                    $("#performanceImagePreviewImg").attr("src", d.performanceImageUrl);
                    $("#performanceImagePreview").show();
                    $("#performanceImageLabel").text("현재 이미지 (새 파일 선택 시 교체)");
                }
            })
            .fail(function (xhr) {
                console.error("[PerformanceNew] load performance failed", xhr.status, xhr.responseText);
                alert("공연 정보를 불러오지 못했습니다.");
            });
    }

    $("#performanceImage").on("change", function () {
        const file = this.files[0];
        const $preview = $("#performanceImagePreview");
        const $img = $("#performanceImagePreviewImg");
        const $label = $("#performanceImageLabel");
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                $img.attr("src", e.target.result);
                $preview.show();
                $label.text(file.name);
            };
            reader.readAsDataURL(file);
        } else {
            $img.attr("src", "");
            $preview.hide();
            $label.text("클릭하여 이미지 선택 (선택 사항)");
        }
    });

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
            contentType: false,
            processData: false,
            dataType: "json",
            data: buildFormData(),
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

