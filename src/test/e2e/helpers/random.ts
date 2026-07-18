export function randomString(length = 8): string {
    const chars = "abcdefghijklmnopqrstuvwxyz0123456789";
    let result = "";
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

export function randomEmail(domain = "e2e.test"): string {
    return `test-${randomString(8)}@${domain}`;
}

export function randomName(prefix = "E2E"): string {
    return `${prefix} ${randomString(6)}`;
}

export function timestamp(): number {
    return Date.now();
}
