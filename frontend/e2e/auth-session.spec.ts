import { expect, test } from '@playwright/test';

test('unauthenticated users are redirected away from profile', async ({ page }) => {
  await page.route('**/api/v1/auth/refresh', route => route.fulfill({
    status: 401,
    contentType: 'application/json',
    body: JSON.stringify({ success: false, message: 'No active session', data: null }),
  }));

  await page.goto('/profile');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'تسجيل الدخول' })).toBeVisible();
});

test('login keeps JWT out of JavaScript storage and opens the correct profile', async ({ page, context }) => {
  await page.route('**/api/v1/auth/refresh', route => route.fulfill({
    status: 401,
    contentType: 'application/json',
    body: JSON.stringify({ success: false, message: 'No active session', data: null }),
  }));
  await page.route('**/api/v1/auth/login', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    headers: { 'set-cookie': 'ACCESS_TOKEN=opaque-test-value; HttpOnly; Path=/api/v1; SameSite=Lax' },
    body: JSON.stringify({ success: true, message: 'Login successful', data: {
      userId: 27, email: 'player@example.test', fullName: 'Test Player', role: 'PLAYER'
    } }),
  }));
  await page.route('**/api/v1/players/me', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ success: true, message: 'ok', data: {
      id: 7, userId: 27, fullName: 'Test Player', email: 'player@example.test', age: 18,
      position: 'STRIKER', videoUrls: [], secondaryPositions: []
    } }),
  }));

  await page.goto('/login');
  await page.getByLabel('البريد الإلكتروني').fill('player@example.test');
  await page.getByLabel('كلمة المرور').fill('Secure-Password-123');
  await page.getByRole('button', { name: 'تسجيل الدخول' }).click();

  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByRole('heading', { name: 'لوحة التحكم والخدمات الرياضية' })).toBeVisible();
  expect(await page.evaluate(() => localStorage.getItem('sports_auth_data'))).toBeNull();
  const accessCookie = (await context.cookies()).find(cookie => cookie.name === 'ACCESS_TOKEN');
  expect(accessCookie?.httpOnly).toBeTruthy();
  expect(await page.evaluate(() => document.cookie)).not.toContain('ACCESS_TOKEN');
});
