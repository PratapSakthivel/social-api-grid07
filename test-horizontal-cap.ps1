# Test script for horizontal cap - fire 110 bot comments
# Reset bot count first
docker exec -it grid07-redis redis-cli DEL post:1:bot_count

Write-Host "Firing 110 bot comment requests..." -ForegroundColor Yellow

for ($i = 1; $i -le 110; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/posts/1/comments" `
            -Method POST `
            -ContentType "application/json" `
            -Body "{`"authorId`":$i, `"authorType`":`"BOT`", `"content`":`"Bot comment $i`", `"depthLevel`":1}" `
            -ErrorAction SilentlyContinue
        
        if ($i % 10 -eq 0) {
            Write-Host "Sent $i requests..." -ForegroundColor Green
        }
    } catch {
        if ($i -gt 100) {
            Write-Host "Request $i rejected (expected after 100)" -ForegroundColor Cyan
        }
    }
}

Write-Host "`nChecking Redis bot count..." -ForegroundColor Yellow
$botCount = docker exec -it grid07-redis redis-cli GET post:1:bot_count
Write-Host "Bot count in Redis: $botCount" -ForegroundColor Green

Write-Host "`nChecking database..." -ForegroundColor Yellow
Write-Host "This should show exactly 100 bot comments for post_id=1" -ForegroundColor Cyan
