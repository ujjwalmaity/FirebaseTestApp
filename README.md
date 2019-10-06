# FirebaseTestApp

## Resource Uses

    - Firebase Authentication
    - Firebase Firestore

## Features

    1. Firebase OTP Authentication
    2. Saving user registration details in Firebase Cloud Firestore
    3. Activating or De-activating a user from backend

## Database Firestore Rule

```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    match /{document=**} {
      allow read, write: if false;
    }

    match /users/{document=**} {
      allow read: if isLogedInPhone();
      allow create: if isLogedInPhone();
      allow update, delete: if false;
    }

    function isLogedInPhone(){
    	return request.auth.token.phone_number != null;
    }

  }
}
```
