const fs = require('fs');
const path = require('path');
const { XMLParser } = require('fast-xml-parser');

const reportPath = 'build/reports/jacoco/test/jacocoTestReport.xml';

if (!fs.existsSync(reportPath)) {
    console.error(`Report not found at ${reportPath}`);
    process.exit(1);
}

const xmlData = fs.readFileSync(reportPath, 'utf8');
const parser = new XMLParser({
    ignoreAttributes: false,
    attributeNamePrefix: ""
});

const jsonObj = parser.parse(xmlData);
const report = jsonObj.report;

const targetPackages = ['alfio/manager', 'alfio/manager/system', 'alfio/manager/user', 'alfio/manager/wallet'];

function getCoverage(element) {
    if (!element || !element.counter) return 0;
    const counters = Array.isArray(element.counter) ? element.counter : [element.counter];
    const instructions = counters.find(c => c.type === 'INSTRUCTION');
    if (instructions) {
        const missed = parseInt(instructions.missed || 0);
        const covered = parseInt(instructions.covered || 0);
        const total = missed + covered;
        return total > 0 ? (covered / total * 100) : 0;
    }
    return 0;
}

console.log(`${'Package/Class'.padEnd(60)} | ${'Coverage'.padEnd(10)}`);
console.log("-".repeat(75));

let totalMissed = 0;
let totalCovered = 0;

const packages = Array.isArray(report.package) ? report.package : [report.package];

packages.forEach(pkg => {
    const packageName = pkg.name;
    if (targetPackages.includes(packageName)) {
        const pkgCoverage = getCoverage(pkg);
        console.log(`${packageName.padEnd(60)} | ${pkgCoverage.toFixed(2).padStart(8)}%`);

        const classes = Array.isArray(pkg.class) ? pkg.class : [pkg.class];
        classes.forEach(clazz => {
            const classCoverage = getCoverage(clazz);
            if (classCoverage < 85) {
                console.log(`  ${clazz.name.padEnd(58)} | ${classCoverage.toFixed(2).padStart(8)}%`);
            }
        });

        const counters = Array.isArray(pkg.counter) ? pkg.counter : [pkg.counter];
        const instructions = counters.find(c => c.type === 'INSTRUCTION');
        if (instructions) {
            totalMissed += parseInt(instructions.missed || 0);
            totalCovered += parseInt(instructions.covered || 0);
        }
    }
});

console.log("-".repeat(75));
const totalInstructions = totalMissed + totalCovered;
const overallCoverage = totalInstructions > 0 ? (totalCovered / totalInstructions * 100) : 0;
console.log(`${'OVERALL'.padEnd(60)} | ${overallCoverage.toFixed(2).padStart(8)}%`);
