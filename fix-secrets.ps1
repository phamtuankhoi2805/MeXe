# Script to remove secrets from application.properties in git history
$file = "src/main/resources/application.properties"
$content = Get-Content $file -Raw
$content = $content -replace "spring.security.oauth2.client.registration.google.client-id=.*", "spring.security.oauth2.client.registration.google.client-id="
$content = $content -replace "spring.security.oauth2.client.registration.google.client-secret=.*", "spring.security.oauth2.client.registration.google.client-secret="
Set-Content $file -Value $content -NoNewline

