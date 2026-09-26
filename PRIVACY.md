# Privacy

Email reads and sends your mail through the servers you set it up with. Nothing else of
yours leaves the phone, and nothing is sent to this project.

The rest of this page is the evidence, because a privacy policy that cannot be checked is
just a promise.

## Where it connects

- **Your mail servers**, the IMAP or POP3 server and the SMTP server you give it, to fetch
  and send mail. Nothing else is sent to anyone for your mail to work.
- **When you set up an account, and only then**, it looks up how to reach your provider:
  the address's own domain (`autoconfig.<domain>` and `<domain>/.well-known/autoconfig/`),
  its mail exchanger over DNS, and Mozilla's list of providers at
  `autoconfig.thunderbird.net`, which is told the domain, not the address. The code is in
  `feature/autodiscovery/autoconfig`.
- **Pictures in a message** come from wherever the sender put them, and are fetched only
  when you press "Show pictures".

There is no sign-in through Google, Microsoft or anyone else, no push service, no update
check and no server of this project's.

## The lock screen

With [Glance](https://github.com/wanderwildwood/hitome) installed and its panel on, Email hands
it one number - how much mail in the unified inbox is unread - to show on the lock screen. It
answers Glance alone and hands over nothing while **Settings → General settings → Notifications →
Unread mail on the lock screen** is off. No subject, sender or text is shared.

## Permissions

From `aapt2 dump badging` on the APK:

- `INTERNET`, `ACCESS_NETWORK_STATE` — to reach your mail servers, and to know when it can.
- `READ_CONTACTS` — to suggest addresses as you type and show your contacts' names. Asked
  for, and can be refused.
- `POST_NOTIFICATIONS`, `VIBRATE` — to say when new mail arrives.
- `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`, `WAKE_LOCK`, `FOREGROUND_SERVICE*`,
  `READ_SYNC_SETTINGS` — to check for mail on time, and to hold a push connection open.
- `CAMERA` — only to scan a QR code when importing settings, and asked for only then.
- `USE_BIOMETRIC`, `USE_FINGERPRINT` — to ask for the phone's own lock before showing a saved
  password in an account's server settings.

## What is stored

Your accounts, their passwords and the mail they fetch, in the app's private storage on the
phone. Settings can be exported to a file you choose, and imported from one.

## No analytics

No crash reporting, no telemetry, no advertising identifier. Thunderbird's own telemetry is
replaced by a module that does nothing (`feature/telemetry/noop`), and the build has no
Google Play services.

## Checking for yourself

```
aapt2 dump badging tayori.apk | grep uses-permission
```
