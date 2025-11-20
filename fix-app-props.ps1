if (Test-Path 'src/main/resources/application.properties') {
    $content = Get-Content 'src/main/resources/application.properties' -Raw
    $content = $content -replace 'spring\.security\.oauth2\.client\.registration\.google\.client-id=[^\r\n]*', 'spring.security.oauth2.client.registration.google.client-id='
    $content = $content -replace 'spring\.security\.oauth2\.client\.registration\.google\.client-secret=[^\r\n]*', 'spring.security.oauth2.client.registration.google.client-secret='
    Set-Content 'src/main/resources/application.properties' -Value $content -NoNewline
}

