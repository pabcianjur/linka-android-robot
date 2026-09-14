# BRIPORT FINAL

BRIPORT final architecture for BRILink activity reporting:

1. PWA collects report data and proof photo.
2. Apps Script backend stores proof, creates a job, and creates a prefilled Google Forms URL without submitting it.
3. PWA launches `briport://run?jobId=...`.
4. Android BRIPORT Robot retrieves the job and proof file.
5. AccessibilityService opens Chrome, advances Google Forms, opens the upload picker, selects the prepared proof file, then clicks KIRIM.

## Build
GitHub Actions is included. It uses Gradle 8.9, Java 17, AndroidX, and JVM target 17.

## Important
- The user must explicitly enable BRIPORT Robot under Android Accessibility settings.
- Google account login/session in Chrome must be valid for the official form.
- The robot depends on visible Google Forms and Android file-picker labels; if Google changes the UI, selectors may need maintenance.
- No CAPTCHA/security bypass is attempted.
- Backend does not programmatically submit the form; the final submission is performed in the user's Chrome session by the accessibility robot.
