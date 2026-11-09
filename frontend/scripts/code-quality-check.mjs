import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

const SRC_DIR = join(process.cwd(), 'src');
const violations = [];

function walk(dir) {
  const entries = readdirSync(dir, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...walk(full));
    } else if (/\.(ts|html)$/.test(entry.name)) {
      files.push(full);
    }
  }
  return files;
}

for (const file of walk(SRC_DIR)) {
  const rel = relative(process.cwd(), file).replace(/\\/g, '/');
  const content = readFileSync(file, 'utf8');
  const sizeBytes = statSync(file).size;

  if (sizeBytes > 24 * 1024) {
    violations.push(`${rel}: File size (${Math.round(sizeBytes / 1024)} KB) exceeds 24 KB modular limit.`);
  }

  if (file.endsWith('.ts') && !file.endsWith('.spec.ts')) {
    if (/:\s*any\b|\bas\s+any\b/.test(content)) {
      violations.push(`${rel}: Explicit 'any' type is forbidden; use strict types or unknown.`);
    }
    if (/constructor\s*\([^)]+\)/.test(content)) {
      violations.push(`${rel}: Constructor parameter injection is forbidden; use inject() instead.`);
    }
    if (/\b(localStorage|sessionStorage)\b/.test(content)) {
      violations.push(`${rel}: Browser storage (localStorage/sessionStorage) is forbidden for security.`);
    }
  }

  if (file.endsWith('.html') || file.endsWith('.ts')) {
    if (/\$any\s*\(/.test(content)) {
      violations.push(`${rel}: Template '$any()' cast is forbidden.`);
    }
    if (/\[innerHTML\]/.test(content)) {
      violations.push(`${rel}: Unsanitized '[innerHTML]' binding is forbidden.`);
    }
    if (/\*ngFor\b|\*ngIf\b/.test(content)) {
      violations.push(`${rel}: Legacy structural directives (*ngFor/*ngIf) are forbidden; use @for/@if.`);
    }
  }
}

if (violations.length > 0) {
  console.error('Code quality check failed:\n' + violations.map(v => `  - ${v}`).join('\n'));
  process.exit(1);
}

console.log('Code quality check passed: 0 violations found.');
