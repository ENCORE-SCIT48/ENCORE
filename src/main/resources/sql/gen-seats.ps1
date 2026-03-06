# 18 venues, total_seats each -> INSERT INTO seat (994 rows)
$venues = @(
    @('YES24 LIVE HALL', 47),
    @('블루스퀘어 마스터카드홀', 82),
    @('예술의전당 콘서트홀', 31),
    @('세종문화회관 대극장', 95),
    @('롯데콘서트홀', 58),
    @('홍대 롤링홀', 44),
    @('웨스트브릿지 라이브홀', 76),
    @('KT&G 상상마당 라이브홀', 23),
    @('노들섬 라이브하우스', 89),
    @('고양아람누리 아람극장', 62),
    @('성남아트센터 오페라하우스', 38),
    @('수원 SK아트리움 대공연장', 71),
    @('부산문화회관 대극장', 55),
    @('부산 벡스코 오디토리움', 19),
    @('대구 오페라하우스', 66),
    @('광주 예술의전당 대극장', 42),
    @('인천문화예술회관 대공연장', 73),
    @('춘천 KT&G 상상마당 공연장', 27)
)
$types = @('vip','r','s','a')
$lines = New-Object System.Collections.Generic.List[string]
foreach ($v in $venues) {
    $name = $v[0]
    $n = $v[1]
    $half = [Math]::Max(1, [int]($n/2))
    for ($i = 0; $i -lt $n; $i++) {
        $fl = if ($i -lt $half) { 1 } else { 2 }
        $inFloor = if ($fl -eq 1) { $i } else { $i - $half }
        $row = [int][Math]::Floor($inFloor / 10)
        $col = $inFloor % 10
        $letter = if ($fl -eq 1) { [char](65 + $row) } else { [char](83 + [Math]::Min($row, 8)) }
        $seatNum = "$letter-$($col+1)"
        $t = $types[$i % 4]
        $x = 10 + 20 * $col
        $y = if ($fl -eq 1) { 20 + 20 * $row } else { 100 + 20 * $row }
        $lines.Add("((SELECT venue_id FROM venue WHERE venue_name = '$name' LIMIT 1), $fl, $x, $y, '$seatNum', '$t', NOW(), NOW(), 0)")
    }
}
$header = @"
-- =============================================================================
-- 전체 좌석 (18곳, venue.total_seats 수만큼, 좌표 규칙: 1층/2층, 행당 10석)
-- ※ data-test-insert.sql 실행 후(venue·performance 삽입 후) 이 파일만 실행
-- =============================================================================
INSERT INTO seat (venue_id, seat_floor, x_pos, y_pos, seat_number, seat_type, created_at, updated_at, is_deleted) VALUES
"@
$header + ($lines -join ",`n") + ";`n" | Out-File -FilePath (Join-Path $PSScriptRoot 'data-test-insert-seats-full.sql') -Encoding UTF8
