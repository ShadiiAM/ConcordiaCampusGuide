#!/bin/bash
# This shows what coverage data SonarQube should have received

echo "=== Checking if JaCoCo report was generated ==="
if [ -f "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml" ]; then
    echo "✅ JaCoCo XML report exists"
    echo ""
    echo "Report size:"
    ls -lh app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
    echo ""
    echo "First few lines:"
    head -20 app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
else
    echo "❌ JaCoCo report NOT found"
fi
