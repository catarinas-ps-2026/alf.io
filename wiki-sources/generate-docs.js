const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');
const handlebars = require('handlebars');

// Register math helper
handlebars.registerHelper('math', function(lvalue, operator, rvalue, options) {
    lvalue = parseFloat(lvalue);
    rvalue = parseFloat(rvalue);
    return {
        "+": lvalue + rvalue,
        "-": lvalue - rvalue,
        "*": lvalue * rvalue,
        "/": lvalue / rvalue,
        "%": lvalue % rvalue
    }[operator];
});

const WIKI_SOURCES_DIR = __dirname;
const WIKI_DIR = path.join(WIKI_SOURCES_DIR, '../wiki');

const DATA_PATH = path.join(WIKI_SOURCES_DIR, 'test-cases.yml');
const DISENO_TEMPLATE_PATH = path.join(WIKI_SOURCES_DIR, 'templates/diseno.hbs');
const INFORME_TEMPLATE_PATH = path.join(WIKI_SOURCES_DIR, 'templates/informe.hbs');

const DISENO_OUTPUT_PATH = path.join(WIKI_DIR, 'Diseño-de-Casos-de-Prueba-Funcionales.md');
const INFORME_OUTPUT_PATH = path.join(WIKI_DIR, 'Informe-de-Casos-de-Prueba-Funcionales.md');

function generate() {
    try {
        const fileContents = fs.readFileSync(DATA_PATH, 'utf8');
        const data = yaml.load(fileContents);

        const disenoTemplateSource = fs.readFileSync(DISENO_TEMPLATE_PATH, 'utf8');
        const informeTemplateSource = fs.readFileSync(INFORME_TEMPLATE_PATH, 'utf8');

        const disenoTemplate = handlebars.compile(disenoTemplateSource, { noEscape: true });
        const informeTemplate = handlebars.compile(informeTemplateSource, { noEscape: true });

        const disenoOutput = disenoTemplate(data);
        const informeOutput = informeTemplate(data);

        fs.writeFileSync(DISENO_OUTPUT_PATH, disenoOutput);
        fs.writeFileSync(INFORME_OUTPUT_PATH, informeOutput);

        console.log('Successfully generated wiki documents.');
    } catch (e) {
        console.error('Error generating wiki documents:', e);
        process.exit(1);
    }
}

generate();
