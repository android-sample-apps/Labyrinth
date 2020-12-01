package org.bandev.labyrinth.account

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable

@SuppressLint("ParcelCreator")
class SyncService() : Service(), Parcelable {
    constructor(parcel: Parcel) : this() {
    }

    /*
         * Instantiate the sync adapter object.
         */
    override fun onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized(sSyncAdapterLock) {
            sSyncAdapter = sSyncAdapter ?: SyncAdapter(applicationContext, true)
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    override fun onBind(intent: Intent): IBinder {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         *
         * We should never be in a position where this is called before
         * onCreate() so the exception should never be thrown
         */
        return sSyncAdapter?.syncAdapterBinder ?: throw IllegalStateException()
    }

    companion object {
        // Storage for an instance of the sync adapter
        private var sSyncAdapter: SyncAdapter? = null
        // Object to use as a thread-safe lock
        private val sSyncAdapterLock = Any()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    object CREATOR : Parcelable.Creator<SyncService> {
        override fun createFromParcel(parcel: Parcel): SyncService {
            return SyncService(parcel)
        }

        override fun newArray(size: Int): Array<SyncService?> {
            return arrayOfNulls(size)
        }
    }
}