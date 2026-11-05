# Privacy

Mailbox reads and sends your mail through the servers you set it up with. Nothing else of
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

Your mail provider's privacy policy covers what it keeps of your mail; Mozilla's covers
`autoconfig.thunderbird.net`: <https://www.mozilla.org/privacy/>.

There is no sign-in through Google, Microsoft or anyone else, no push service, no update
check and no server of this project's.

## The lock screen

With [Glance](https://github.com/wanderwildwood/hitome) installed and its panel on, Mailbox hands
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

<!-- shared-about privacy section -->
## Who we are

Mailbox is made by wander wildwood, an independent developer: one person, not a company. wander wildwood is the data controller for anything the app itself handles, and that is very little. What you put in Mailbox stays on your phone. The only things that go out are the requests described above: the connections to your mail servers, which carry your login and the mail you fetch and send; when you set up an account, the lookup of how to reach your provider, which tells the address's own domain, DNS and Mozilla's autoconfig service the domain, not the address; and the pictures in a message, fetched from wherever the sender put them when you choose to show them. The developer receives nothing, so the developer holds no data about you.

## How to contact us

Write to email@wanderthe.dev about your data, this policy or anything else. The same address is in the app, under About.

## Your rights

If you are in the EU, the EEA or the UK, the GDPR gives you the right to:

- see the personal data held about you (access)
- have it corrected (rectification) or deleted (erasure)
- limit how it is used (restriction)
- object to its use (objection)
- get a copy to take elsewhere (portability)
- withdraw your consent at any time, where something rests on it
- complain to a data protection supervisory authority, for example the one where you live

Similar laws elsewhere, such as the CCPA in California, give you the right to know, delete and correct, and to opt out of the sale or sharing of your data. Nothing from Mailbox is sold or shared.

Since Mailbox keeps what it stores only on your phone, you can exercise most of these rights yourself: remove an account or delete mail in the app, export your settings to a file, clear the app's data in Android's settings, or uninstall the app, which deletes all of it. For anything you cannot do yourself, write to the address above.

Mailbox connects to your mail provider and Mozilla's autoconfig service. They are separate from this app and from its developer, and what they receive is covered by their own privacy policies; ask them about what they hold.
<!-- /shared-about privacy section -->
