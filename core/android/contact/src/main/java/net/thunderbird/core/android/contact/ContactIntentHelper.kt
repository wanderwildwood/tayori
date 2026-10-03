package net.thunderbird.core.android.contact

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.view.View
import com.fsck.k9.mail.Address

object ContactIntentHelper {
    /**
     * Contacts (enishi), the address book made to sit beside this app. When it is on the phone
     * every contacts request goes to it by name, so a phone with two contacts apps does not ask
     * "Complete action using" for each kind of request in turn. When it is not, the request goes
     * out unnamed, exactly as before.
     */
    const val CONTACTS_APP = "com.wanderwildwood.enishi"

    @JvmStatic
    fun preferContactsApp(context: Context, intent: Intent): Intent {
        val named = Intent(intent).setPackage(CONTACTS_APP)
        return if (named.resolveActivity(context.packageManager) != null) named else intent
    }

    /** A person, shown in Contacts if it is here, or in Android's own quick view if not. */
    @JvmStatic
    fun showContact(context: Context, view: View, lookupUri: Uri) {
        val quick = Intent(ContactsContract.QuickContact.ACTION_QUICK_CONTACT)
            .setData(lookupUri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val named = preferContactsApp(context, quick)
        if (named.`package` != null) {
            context.startActivity(named)
        } else {
            ContactsContract.QuickContact.showQuickContact(
                context, view, lookupUri, ContactsContract.QuickContact.MODE_LARGE, null,
            )
        }
    }

    @JvmStatic
    fun getContactPickerIntent(): Intent {
        return Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI)
    }

    /**
     * Get Intent to add information to an existing contact or add a new one.
     *
     * @param address An {@link Address} instance containing the email address
     *              of the entity you want to add to the contacts. Optionally
     *              the instance also contains the (display) name of that
     *              entity.
     */
    fun getAddEmailContactIntent(address: Address): Intent {
        return Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.fromParts("mailto", address.address, null)
            putExtra(ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION, address.toString())

            if (address.personal != null) {
                putExtra(ContactsContract.Intents.Insert.NAME, address.personal)
            }
        }
    }

    /**
     * Get Intent to add a phone number to an existing contact or add a new one.
     *
     * @param phoneNumber
     *         The phone number to add to a contact, or to use when creating a new contact.
     */
    fun getAddPhoneContactIntent(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
            putExtra(ContactsContract.Intents.Insert.PHONE, Uri.decode(phoneNumber))
        }
    }
}
