import re
import yaml
import sys

def parse_diseno(content):
    metadata = {}
    test_suites = []
    
    # Split by headers to get metadata sections
    sections = re.split(r'\n## ', content)
    for section in sections[:7]: # 0 to 6
        if not section.strip(): continue
        lines = section.split('\n')
        title = lines[0].strip()
        body = '\n'.join(lines[1:]).strip()
        metadata[title] = body

    # Parse test suites starting from section 7
    suites_content = '\n## '.join(sections[7:])
    # Split by H3 or H2 if it's "Creación de Usuarios"
    suite_blocks = re.split(r'\n###? ', suites_content)
    
    for block in suite_blocks:
        if not block.strip(): continue
        lines = block.split('\n')
        title = lines[0].strip()
        if title == "Diseño de Casos de Prueba": # skip the header of section 7
            rest = '\n'.join(lines[1:])
            # Sub-split this if needed
            sub_blocks = re.split(r'\n###? ', rest)
            for sb in sub_blocks:
                parse_suite_block(sb, test_suites)
        else:
            parse_suite_block(block, test_suites)
            
    return metadata, test_suites

def parse_suite_block(block, test_suites):
    if not block.strip(): return
    lines = block.split('\n')
    title = lines[0].strip()
    content = '\n'.join(lines[1:])
    
    suite = {
        'title': title,
        'cases': []
    }
    
    # Each suite seems to have one or more main test cases defined in a table
    # Then Analysis sections, then a Catalog table.
    
    # Find Case Definition Tables
    case_def_matches = re.findall(r'\| ID \| (CPF-\d+) \|.*?\n\| \*\*Funcionalidad\*\* \| (.*?) \|.*?\n\| \*\*Descripción\*\* \| (.*?) \|.*?\n\| \*\*Requisito Asociado\*\* \| (.*?) \|.*?\n\| \*\*Precondiciones\*\* \| (.*?) \|.*?\n(?:\| \*\*Datos de Entrada\*\* \| (.*?) \|.*?\n)?\| \*\*Pasos de Ejecución\*\* \| (.*?) \|.*?\n\| \*\*Técnicas de Pruebas?\*\* \| (.*?) \|.*?\n\| \*\*Prioridad\*\* \| (.*?) \|', content, re.DOTALL)
    
    # Special case for "Creación de Usuarios" which might have a slightly different table or multiple
    if not case_def_matches and "ID" in content and "Funcionalidad" in content:
        # Fallback for slightly different tables
        case_def_matches = re.findall(r'\| ID \| (CPF-\d+) \|.*?\n\| \*\*Funcionalidad\*\* \| (.*?) \|.*?\n\| \*\*Descripción\*\* \| (.*?) \|.*?\n\| \*\*Requisito Asociado\*\* \| (.*?) \|.*?\n\| \*\*Precondiciones\*\* \| (.*?) \|.*?\n(?:\| \*\*Datos de Entrada\*\* \| (.*?) \|.*?\n)?\| \*\*Pasos de Ejecución\*\* \| (.*?) \|.*?\n\| \*\*Técnicas de Pruebas?\*\* \| (.*?) \|', content, re.DOTALL)

    for match in case_def_matches:
        case_id = match[0]
        func = match[1].strip()
        desc = match[2].strip()
        req = match[3].strip()
        pre = match[4].strip()
        inputs = match[5].strip() if match[5] else None
        steps_raw = match[6].strip()
        tech_raw = match[7].strip()
        priority = match[8].strip() if len(match) > 8 else "Alta" # Default to Alta if missing
        
        steps = [s.strip() for s in re.split(r'\d+\.\s+|<br\s*/?>', steps_raw) if s.strip()]
        techniques = [t.strip() for t in tech_raw.split(',') if t.strip()]
        
        case = {
            'id': case_id,
            'functionality': func,
            'description': desc,
            'requirement': req,
            'preconditions': pre,
            'inputs': inputs,
            'steps': steps,
            'techniques': techniques,
            'priority': priority,
            'analysis': {
                'equivalency_partitions': [],
                'boundary_values': [],
                'decision_tables': [],
                'state_transitions': []
            },
            'catalog': []
        }
        
        # Parse Analysis (PE, VL, DT, ST)
        # These are usually in the same block after the case definition
        
        # PE
        pe_match = re.search(r'\*\*Partición de Equivalencia\*\*\n(.*?)(?:\n\n|\n\*\*|$)', content, re.DOTALL)
        if pe_match:
            pe_table = pe_match.group(1)
            pe_rows = re.findall(r'\| (.*?) \| (.*?) \| (.*?) \|', pe_table)
            for row in pe_rows[1:]: # Skip header
                if row[0].strip() == '---': continue
                case['analysis']['equivalency_partitions'].append({
                    'field': row[0].strip(),
                    'valid': row[1].strip(),
                    'invalid': row[2].strip()
                })

        # VL
        vl_match = re.search(r'\*\*Valores Límite\*\*\n(.*?)(?:\n\n|\n\*\*|$)', content, re.DOTALL)
        if vl_match:
            vl_table = vl_match.group(1)
            vl_rows = re.findall(r'\| (.*?) \| (.*?) \| (.*?) \| (.*?) \| (.*?) \|', vl_table)
            for row in vl_rows[1:]: # Skip header
                if row[0].strip() == '---': continue
                case['analysis']['boundary_values'].append({
                    'field': row[0].strip(),
                    'min_valid': row[1].strip(),
                    'min_invalid': row[2].strip(),
                    'max_valid': row[3].strip(),
                    'max_invalid': row[4].strip()
                })

        # DT
        dt_match = re.search(r'\*\*Tabla de Decisión.*?\*\*\n(.*?)(?:\n\n|\n\*\*|$)', content, re.DOTALL)
        if dt_match:
            dt_table = dt_match.group(1)
            dt_lines = [l.strip() for l in dt_table.strip().split('\n') if l.strip()]
            if dt_lines:
                # This is more complex to parse generically. I'll just store the rows for now.
                case['analysis']['decision_tables'].append(dt_table.strip())

        # ST
        st_match = re.search(r'\*\*Transición de Estados\*\*\n(.*?)(?:\n\n|\n\*\*|$)', content, re.DOTALL)
        if st_match:
            case['analysis']['state_transitions'].append(st_match.group(1).strip())

        # Catalog
        cat_match = re.search(r'\*\*Catálogo de Pruebas\*\*\n(.*?)(?:\n\n|\n\*\*|$)', content, re.DOTALL)
        if cat_match:
            cat_table = cat_match.group(1)
            # | #CP | Datos de Entrada | Resultado Esperado | Obs |
            cat_rows = re.findall(r'\| (CPF-.*?) \| (.*?) \| (.*?) \| (.*?) \|', cat_table)
            for row in cat_rows:
                case['catalog'].append({
                    'cp_id': row[0].strip(),
                    'inputs': row[1].strip(),
                    'expected': row[2].strip(),
                    'obs': row[3].strip()
                })
        
        suite['cases'].append(case)
    
    if suite['cases']:
        test_suites.append(suite)

def parse_informe(content):
    metadata = {}
    execution_results = {}
    
    sections = re.split(r'\n## ', content)
    for section in sections:
        if not section.strip(): continue
        lines = section.split('\n')
        title = lines[0].strip()
        body = '\n'.join(lines[1:]).strip()
        
        if title.startswith('1.') or title.startswith('2.') or title.startswith('3.') or title.startswith('4.') or title.startswith('5.') or title.startswith('7.') or title.startswith('8.') or title.startswith('9.'):
            metadata[title] = body
        elif 'Resultados de Pruebas Funcionales' in title or title.startswith('6.'):
            # Parse individual results
            results = re.split(r'\n\*\*(CPF-.*?)\*\*\n', body)
            # The first part might be the title "Resultados..."
            for i in range(1, len(results), 2):
                cp_id = results[i].strip()
                table_content = results[i+1].strip()
                
                # Extract from table
                # | ID | Descripción | Tipo | Estado | Defectos |
                # ...
                # | Resultado esperado | Resultado obtenido |
                # ...
                # | Evidencia |
                
                res = {}
                desc_match = re.search(r'<td>(.*?)</td>\s*<td>Manual</td>\s*<td>(.*?)</td>\s*<td>(.*?)</td>', table_content, re.DOTALL)
                if desc_match:
                    res['description'] = desc_match.group(1).strip()
                    res['type'] = 'Manual'
                    res['status'] = desc_match.group(2).strip()
                    res['defects'] = desc_match.group(3).strip()
                
                expected_obtained_match = re.search(r'Resultado esperado</th>\s*<th.*?>Resultado obtenido</th>\s*</tr>\s*<tr>\s*<td.*?>(.*?)</td>\s*<td.*?>(.*?)</td>', table_content, re.DOTALL)
                if expected_obtained_match:
                    res['expected'] = expected_obtained_match.group(1).strip()
                    res['obtained'] = expected_obtained_match.group(2).strip()
                
                evidence = []
                # <img src="(.*?)" alt="(.*?)">(?:<br>\s*(.*?))?</td>
                ev_matches = re.findall(r'<img src="(.*?)" alt="(.*?)">', table_content)
                # This is tricky because text can be after multiple images
                ev_text_match = re.search(r'<th colspan="5">Evidencia</th>\s*</tr>\s*<tr>\s*<td colspan="5">(.*?)</td>', table_content, re.DOTALL)
                if ev_text_match:
                    text_all = ev_text_match.group(1).strip()
                    # Clean up images from text
                    text_clean = re.sub(r'<img.*?>', '', text_all).replace('<br>', '\n').strip()
                    
                    for img, alt in ev_matches:
                        evidence.append({
                            'image': img,
                            'alt': alt,
                            'text': text_clean
                        })
                
                res['evidence'] = evidence
                execution_results[cp_id] = res
                
    return metadata, execution_results

with open('wiki/Diseño-de-Casos-de-Prueba-Funcionales.md', 'r') as f:
    diseno_content = f.read()

with open('wiki/Informe-de-Casos-de-Prueba-Funcionales.md', 'r') as f:
    informe_content = f.read()

m_diseno, suites = parse_diseno(diseno_content)
m_informe, execution = parse_informe(informe_content)

# Merge metadata
metadata = {
    'diseno': m_diseno,
    'informe': m_informe
}

# Attach execution results to catalog
for suite in suites:
    for case in suite['cases']:
        for cp in case['catalog']:
            if cp['cp_id'] in execution:
                cp['execution'] = execution[cp['cp_id']]

output = {
    'metadata': metadata,
    'test_suites': suites
}

with open('wiki-sources/test-cases.yml', 'w') as f:
    yaml.dump(output, f, sort_keys=False, allow_unicode=True)

print("Done")
