# week1-week2
Two HashMaps are created:

usernameMap → stores username → userId

attemptMap → stores username → number of attempts.

When a user checks a username, the system first updates the attempt count in attemptMap.

The system then checks usernameMap using containsKey() to determine if the username already exists.

If the username exists, the system generates alternative suggestions by:

Appending numbers (e.g., username1, username2)

Replacing _ with 

The system also provides a method to find the most attempted username by scanning the attempt frequency map.