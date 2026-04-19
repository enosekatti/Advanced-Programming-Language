$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outputsDir = Join-Path $root "outputs"

if (!(Test-Path $outputsDir)) {
    New-Item -ItemType Directory -Path $outputsDir | Out-Null
}

Write-Host "=== Assignment 7 Test Runner ==="
Write-Host "Project root: $root"

# C test
Write-Host "`n[1/3] Building and running C implementation..."
Push-Location (Join-Path $root "c")
try {
    $gcc = Get-Command gcc -ErrorAction SilentlyContinue
    if ($null -eq $gcc) {
        "GCC not found. Install GCC/MinGW to run this section." |
            Tee-Object -FilePath (Join-Path $outputsDir "c_output.txt")
    }
    else {
        gcc statistics.c -o statistics.exe
        if ($LASTEXITCODE -ne 0) {
            throw "C compilation failed."
        }

        .\statistics.exe | Tee-Object -FilePath (Join-Path $outputsDir "c_output.txt")
    }
}
finally {
    Pop-Location
}

# OCaml test
Write-Host "`n[2/3] Building and running OCaml implementation..."
Push-Location (Join-Path $root "ocaml")
try {
    $ocamlc = Get-Command ocamlc -ErrorAction SilentlyContinue
    if ($null -eq $ocamlc) {
        "OCaml compiler not found. Install OCaml to run this section." |
            Tee-Object -FilePath (Join-Path $outputsDir "ocaml_output.txt")
    }
    else {
        ocamlc -o statistics.exe statistics.ml
        if ($LASTEXITCODE -ne 0) {
            throw "OCaml compilation failed."
        }
        .\statistics.exe | Tee-Object -FilePath (Join-Path $outputsDir "ocaml_output.txt")
    }
}
finally {
    Pop-Location
}

# Python test
Write-Host "`n[3/3] Running Python implementation..."
Push-Location (Join-Path $root "python")
try {
    python .\statistics_calculator.py |
        Tee-Object -FilePath (Join-Path $outputsDir "python_output.txt")
}
finally {
    Pop-Location
}

Write-Host "`nAll available tests completed."
