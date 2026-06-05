import xml.etree.ElementTree as ET
import sys

def get_coverage(element):
    instructions = element.find('./counter[@type="INSTRUCTION"]')
    if instructions is not None:
        missed = int(instructions.get('missed'))
        covered = int(instructions.get('covered'))
        total = missed + covered
        return (covered / total * 100) if total > 0 else 0
    return 0

tree = ET.parse('build/reports/jacoco/test/jacocoTestReport.xml')
root = tree.getroot()

target_packages = ['alfio/manager', 'alfio/manager/system', 'alfio/manager/user', 'alfio/manager/wallet']
excluded_subpackages = ['alfio/manager/i18n', 'alfio/manager/openid', 'alfio/manager/payment', 'alfio/manager/support']

print(f"{'Package/Class':<60} | {'Coverage':<10}")
print("-" * 75)

total_missed = 0
total_covered = 0

for package in root.findall('package'):
    package_name = package.get('name')
    if any(package_name == tp for tp in target_packages):
        pkg_coverage = get_coverage(package)
        print(f"{package_name:<60} | {pkg_coverage:>8.2f}%")
        
        for clazz in package.findall('class'):
            class_name = clazz.get('name')
            class_coverage = get_coverage(clazz)
            if class_coverage < 85:
                print(f"  {class_name:<58} | {class_coverage:>8.2f}%")
        
        instructions = package.find('./counter[@type="INSTRUCTION"]')
        total_missed += int(instructions.get('missed'))
        total_covered += int(instructions.get('covered'))

print("-" * 75)
total = total_missed + total_covered
overall_coverage = (total_covered / total * 100) if total > 0 else 0
print(f"{'OVERALL':<60} | {overall_coverage:>8.2f}%")
