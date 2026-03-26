@echo off
REM PO-Toolbox executable wrapper script (Windows)
REM Prefers native executable, falls back to JAR
if exist "%~dp0build\package\PO-Toolbox\PO-Toolbox.exe" (
    "%~dp0build\package\PO-Toolbox\PO-Toolbox.exe" %*
) else (
    for %%f in ("%~dp0build\libs\po-toolbox-*-win.jar") do (
        java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -jar "%%f" %*
        goto :eof
    )
    echo Error: No JAR found in build\libs. Run: gradlew shadowJar
)
