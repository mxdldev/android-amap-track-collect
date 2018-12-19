1.注册Service
 <service
            android:name=".alive.sync.service.SyncAdapterService"
            android:exported="true"
            android:process=":core">
            <intent-filter>
                <action android:name="android.content.SyncAdapter"/>
            </intent-filter>

            <meta-data
                android:name="android.content.SyncAdapter"
                android:resource="@xml/syncadapter"/>
            />
        </service>
        <service android:name=".alive.sync.service.SyncAccountService">
            <intent-filter>
                <action android:name="android.accounts.AccountAuthenticator"/>
            </intent-filter>

            <meta-data
                android:name="android.accounts.AccountAuthenticator"
                android:resource="@xml/authenticator"/>
        </service>

2.注册Provider

<provider
                      android:name=".alive.sync.provider.SyncContentProvider"
                      android:authorities="@string/account_auth_provider"
                      android:exported="false"
                      android:syncable="true"/>
3.调用
SyncControl.createSyncAccount