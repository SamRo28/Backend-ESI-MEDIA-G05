Param(
  [switch]$SkipTests
)

Write-Host "== SonarQube analysis (backend) =="
if (-not $env:SONAR_HOST_URL) { Write-Error "Missing SONAR_HOST_URL environment variable"; exit 1 }
if (-not $env:SONAR_TOKEN)    { Write-Error "Missing SONAR_TOKEN environment variable"; exit 1 }
if (-not $env:PROJECT_KEY)    { Write-Error "Missing PROJECT_KEY environment variable (backend project key)"; exit 1 }

Push-Location $PSScriptRoot\..\
try {
  $commonOpts = @("-Dsonar.projectKey=$($env:PROJECT_KEY)", "-Dsonar.host.url=$($env:SONAR_HOST_URL)", "-Dsonar.login=$($env:SONAR_TOKEN)")
  if ($SkipTests) {
    mvn -DskipTests=true clean package | Out-Host
    mvn @commonOpts sonar:sonar -DskipTests=true | Out-Host
  } else {
    mvn clean verify | Out-Host
    mvn @commonOpts sonar:sonar | Out-Host
  }
}
finally { Pop-Location }

Write-Host "Done. Check the project in SonarQube: $($env:PROJECT_KEY)"

