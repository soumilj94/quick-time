# QuickTime

QuickTime is revolutionzing the way you book & manage turf spaces. Designed with both players & facility owners in mind.
QuickTime streamlines the entire process, making it easier to find & secure your turf.

![Logo](https://github.com/soumilj94/quick-book/blob/main/Logo.png)
## Pre-requisites

To run the app without any issue you must to the following:

Open the app's **AndroidManifest.xml** file:

#### For Google Maps:
```bash
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="paste your API_KEY here"/>
```
#### For RazorPay:
```bash
<meta-data
    android:name="com.razorpay.ApiKey"
    android:value="paste your API_KEY here"/>
```
## Firebase Implementation
Open your Firebase console in your browser:
1. Enable `Authentication` and select `Email/Password`
2. Enable `Storage`
3. Enable `Crashlytics`
4. Enable `Cloud Firestore Database`
Add your Firebase `google-services.json` file at: 
**/project-directory/app/google-services.json**

Refer this to setup and connect the app with your firebase account:
[Firebase Setup](https://firebase.google.com/docs/android/setup)
