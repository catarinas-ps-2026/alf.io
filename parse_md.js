const fs = require('fs');

function parseDiseno(content) {
    const metadata = {};
    const testSuites = [];
    
    // Split by any header level
    const blocks = content.split(/\n(?=#+ )/);
    
    let currentSectionNum = 0;
    
    for (const block of blocks) {
        if (!block.trim()) continue;
        const match = /^(#+)\s+(.*?)\n/.exec(block);
        if (!match) continue;
        
        const level = match[1].length;
        const title = match[2].trim();
        const body = block.substring(match[0].length).trim();
        
        const titleNumMatch = /^(\d+)\./.exec(title);
        if (titleNumMatch) {
            currentSectionNum = parseInt(titleNumMatch[1]);
        }
        
        if (level === 2 && currentSectionNum >= 1 && currentSectionNum <= 6) {
            metadata[title] = body;
        } else if (level === 3 || title === "Creación de Usuarios") {
            // This is likely a suite
            parseSuiteBlock(title, body, testSuites);
        } else if (title === "Diseño de Casos de Prueba" || currentSectionNum === 7) {
            // This is the container for suites, split it further if it has suites
            if (body.includes('### ')) {
                const subBlocks = body.split(/\n(?=### )/);
                for (const sb of subBlocks) {
                    const sm = /^(###)\s+(.*?)\n/.exec(sb);
                    if (sm) {
                        parseSuiteBlock(sm[2].trim(), sb.substring(sm[0].length).trim(), testSuites);
                    }
                }
            }
        }
    }

    return { metadata, testSuites };
}

function extractTableValue(content, key) {
    const regex = new RegExp(`\\| \\*\\*${key}\\*\\* \\| (.*?) \\|`);
    const match = regex.exec(content);
    return match ? match[1].trim() : null;
}

function parseSuiteBlock(title, content, testSuites) {
    if (!content.trim()) return;
    
    const suite = {
        title: title.replace(/^#+\s+/, '').trim(),
        cases: []
    };
    
    const idMatch = /\| ID \| (CPF-\d+) \|/.exec(content);
    if (idMatch) {
        const caseId = idMatch[1];
        const functionality = extractTableValue(content, "Funcionalidad");
        const description = extractTableValue(content, "Descripción");
        const requirement = extractTableValue(content, "Requisito Asociado");
        const preconditions = extractTableValue(content, "Precondiciones");
        const inputs = extractTableValue(content, "Datos de Entrada");
        const stepsRaw = extractTableValue(content, "Pasos de Ejecución");
        const techniquesRaw = extractTableValue(content, "Técnicas de Pruebas?");
        const priority = extractTableValue(content, "Prioridad") || "Alta";
        
        const steps = stepsRaw ? stepsRaw.split(/\d+\.\s+|<br\s*\/?>/).map(s => s.trim()).filter(s => s) : [];
        const techniques = techniquesRaw ? techniquesRaw.split(',').map(t => t.trim()).filter(t => t) : [];
        
        const caseObj = {
            id: caseId,
            functionality,
            description,
            requirement,
            preconditions,
            inputs,
            steps,
            techniques,
            priority,
            analysis: {
                equivalency_partitions: [],
                boundary_values: [],
                decision_tables: [],
                state_transitions: []
            },
            catalog: []
        };
        
        // PE
        const peRegex = /\*\*Partición de Equivalencia\*\*\n([\s\S]*?)(?:\n\n|\n\*\*|$|#)/;
        const peMatch = peRegex.exec(content);
        if (peMatch) {
            const rows = peMatch[1].trim().split('\n').filter(l => l.includes('|') && !l.includes('---'));
            for (let i = 1; i < rows.length; i++) {
                const cols = rows[i].split('|').map(c => c.trim()).filter(c => c !== '');
                if (cols.length >= 3) {
                    caseObj.analysis.equivalency_partitions.push({
                        field: cols[0],
                        valid: cols[1],
                        invalid: cols[2]
                    });
                }
            }
        }
        if (caseObj.analysis.equivalency_partitions.length === 0 && content.includes('| Campo | Clase Válida | Clases No Válidas |')) {
             const pePart = content.split('| Campo | Clase Válida | Clases No Válidas |')[1].split('\n\n')[0];
             const rows = pePart.trim().split('\n').filter(l => l.includes('|') && !l.includes('---'));
             for (let row of rows) {
                const cols = row.split('|').map(c => c.trim()).filter(c => c !== '');
                if (cols.length >= 3) {
                    caseObj.analysis.equivalency_partitions.push({
                        field: cols[0],
                        valid: cols[1],
                        invalid: cols[2]
                    });
                }
             }
        }

        // VL
        const vlRegex = /\*\*Valores Límite\*\*\n([\s\S]*?)(?:\n\n|\n\*\*|$|#)/;
        const vlMatch = vlRegex.exec(content);
        if (vlMatch) {
            const rows = vlMatch[1].trim().split('\n').filter(l => l.includes('|') && !l.includes('---'));
            for (let i = 1; i < rows.length; i++) {
                const cols = rows[i].split('|').map(c => c.trim()).filter(c => c !== '');
                if (cols.length >= 5) {
                    caseObj.analysis.boundary_values.push({
                        field: cols[0],
                        min_valid: cols[1],
                        min_invalid: cols[2],
                        max_valid: cols[3],
                        max_invalid: cols[4]
                    });
                }
            }
        }
        if (caseObj.analysis.boundary_values.length === 0 && content.includes('| Campo | Límite Inferior Válido | Límite Inferior No Válido | Límite Superior Válido | Límite Superior No Válido |')) {
             const vlPart = content.split('| Campo | Límite Inferior Válido | Límite Inferior No Válido | Límite Superior Válido | Límite Superior No Válido |')[1].split('\n\n')[0];
             const rows = vlPart.trim().split('\n').filter(l => l.includes('|') && !l.includes('---'));
             for (let row of rows) {
                const cols = row.split('|').map(c => c.trim()).filter(c => c !== '');
                if (cols.length >= 5) {
                    caseObj.analysis.boundary_values.push({
                        field: cols[0],
                        min_valid: cols[1],
                        min_invalid: cols[2],
                        max_valid: cols[3],
                        max_invalid: cols[4]
                    });
                }
             }
        }

        // DT
        const dtRegex = /\*\*Tabla de Decisión.*?\*\*\n([\s\S]*?)(?:\n\n|\n\*\*|$|#)/g;
        let dtMatch;
        while ((dtMatch = dtRegex.exec(content)) !== null) {
            caseObj.analysis.decision_tables.push(dtMatch[1].trim());
        }

        // ST
        const stRegex = /\*\*Transición de Estados\*\*\n([\s\S]*?)(?:\n\n|\n\*\*|$|#)/;
        const stMatch = stRegex.exec(content);
        if (stMatch) {
            caseObj.analysis.state_transitions.push(stMatch[1].trim());
        }

        // Catalog
        const catalogRegex = /\*\*Catálogo de Pruebas\*\*\n([\s\S]*?)(?:\n\n|\n\*\*|$|#)/g;
        let catMatch;
        while ((catMatch = catalogRegex.exec(content)) !== null) {
            const rows = catMatch[1].trim().split('\n').filter(l => l.includes('|') && !l.includes('---'));
            for (let i = 1; i < rows.length; i++) {
                const cols = rows[i].split('|').map(c => c.trim()).filter(c => c !== '');
                if (cols.length >= 4) {
                    caseObj.catalog.push({
                        cp_id: cols[0],
                        inputs: cols[1],
                        expected: cols[2],
                        obs: cols[3]
                    });
                }
            }
        }
        
        suite.cases.push(caseObj);
    }
    
    if (suite.cases.length > 0) {
        testSuites.push(suite);
    }
}

function parseInforme(content) {
    const metadata = {};
    const executionResults = {};
    
    const blocks = content.split(/\n(?=#+ )/);
    for (const block of blocks) {
        if (!block.trim()) continue;
        const match = /^(#+)\s+(.*?)\n/.exec(block);
        if (!match) continue;
        
        const title = match[2].trim();
        const body = block.substring(match[0].length).trim();
        
        if (/^[12345789]\./.test(title)) {
            metadata[title] = body;
        } else if (/^6\./.test(title) || title.includes('Resultados de Pruebas Funcionales')) {
            const resultBlocks = body.split(/\n\*\*(CPF-.*?|CONF-.*?)\*\*\n/);
            for (let i = 1; i < resultBlocks.length; i += 2) {
                const rawId = resultBlocks[i].trim();
                const tableContent = resultBlocks[i+1].trim();
                
                let cpId = rawId;
                if (cpId.startsWith('CONF-')) {
                    cpId = cpId.replace('CONF-', 'CPF-');
                }
                
                const res = {};
                const descMatch = /<td>(.*?)<\/td>\s*<td>Manual<\/td>\s*<td>(.*?)<\/td>\s*<td>(.*?)<\/td>/.exec(tableContent);
                if (descMatch) {
                    res.description = descMatch[1].trim();
                    res.type = 'Manual';
                    res.status = descMatch[2].trim();
                    res.defects = descMatch[3].trim();
                }
                
                const expectedObtainedMatch = /Resultado esperado<\/th>\s*<th.*?>Resultado obtenido<\/th>\s*<\/tr>\s*<tr>\s*<td.*?>(.*?)<\/td>\s*<td.*?>(.*?)<\/td>/.exec(tableContent);
                if (expectedObtainedMatch) {
                    res.expected = expectedObtainedMatch[1].trim();
                    res.obtained = expectedObtainedMatch[2].trim();
                }
                
                const evidence = [];
                const imgRegex = /<img src="(.*?)" alt="(.*?)">/g;
                let imgMatch;
                while ((imgMatch = imgRegex.exec(tableContent)) !== null) {
                    evidence.push({
                        image: imgMatch[1],
                        alt: imgMatch[2]
                    });
                }
                
                const evTextMatch = /<th colspan="5">Evidencia<\/th>\s*<\/tr>\s*<tr>\s*<td colspan="5">([\s\S]*?)<\/td>/.exec(tableContent);
                if (evTextMatch) {
                    const textAll = evTextMatch[1].trim();
                    const textClean = textAll.replace(/<img.*?>/g, '').replace(/<br>/g, '\n').trim();
                    evidence.forEach(ev => { ev.text = textClean; });
                }
                
                res.evidence = evidence;
                executionResults[cpId] = res;
            }
        }
    }
    return { metadata, executionResults };
}

const disenoContent = fs.readFileSync('wiki/Diseño-de-Casos-de-Prueba-Funcionales.md', 'utf8');
const informeContent = fs.readFileSync('wiki/Informe-de-Casos-de-Prueba-Funcionales.md', 'utf8');

const { metadata: mDiseno, testSuites: suites } = parseDiseno(disenoContent);
const { metadata: mInforme, executionResults: execution } = parseInforme(informeContent);

for (const suite of suites) {
    for (const caseObj of suite.cases) {
        for (const cp of caseObj.catalog) {
            let searchId = cp.cp_id;
            if (execution[searchId]) {
                cp.execution = execution[searchId];
            }
        }
    }
}

const finalOutput = {
    metadata: {
        diseno: mDiseno,
        informe: mInforme
    },
    test_suites: suites
};

function toYaml(obj, indent = 0) {
    const spaces = '  '.repeat(indent);
    if (obj === null) return 'null';
    if (typeof obj === 'string') {
        if (obj.includes('\n')) {
            return '|-\n' + obj.split('\n').map(line => spaces + '  ' + line).join('\n');
        }
        if (obj.includes(':') || obj.includes('-') || obj.includes('[') || obj.includes(']') || obj.includes('#') || obj.includes('"')) {
            return '"' + obj.replace(/"/g, '\\"') + '"';
        }
        return obj;
    }
    if (typeof obj !== 'object') return obj.toString();
    if (Array.isArray(obj)) {
        if (obj.length === 0) return '[]';
        return '\n' + obj.map(item => spaces + '- ' + toYaml(item, indent + 1)).join('\n');
    }
    
    let yaml = '';
    for (const [key, value] of Object.entries(obj)) {
        const valYaml = toYaml(value, indent + 1);
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
            yaml += spaces + key + ':\n' + valYaml + '\n';
        } else if (Array.isArray(value) && value.length > 0) {
          yaml += spaces + key + ':' + valYaml + '\n';
        } else {
            yaml += spaces + key + ': ' + valYaml + '\n';
        }
    }
    return yaml.trim();
}

fs.writeFileSync('wiki-sources/test-cases.yml', toYaml(finalOutput));
console.log('Done');
