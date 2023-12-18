# Project_06 - Notes App
<span style="font-size: smaller;"><strong>Jacob Fritz worked on this</strong></span>

---
<span style="font-size: smaller;"><strong> Description </strong> </span>
*Disclaimer* I was unable to complete all the required functionality of the final project due to time constraints and the difficulty. However, I believe I have successfully implemented all functionality of the sign up screen and the home screen, covering the first 6 components of the rubric 

This app allows for a user to:
- Sign in with a name, email, password, and profile picture
- View both their favorite restaurants and all restaurants within a 50 mile radius
- Search for an query those restaurants
- Open a navigation drawer that displays their information and offers them a chance to sign out or move to the other screens

To begin the user is shown the sign in page where they can click on the default profile image and upload a new one using camerax. That image is then saved to firestore storage.
<br>
<br>
The user can then enter their information: Name, Email, and Password. The email and password will be used to create'login a profile for this person in firebase authentication and the name, image url, and email will be saved to firebase firestore. The user then would click next to get into the homescreen
<br>
<br>
When the user is taken to the home screen they are showed a toolbar with two buttons at the top, the left "hamburger" button opens the navigation drawer. The right "search" button opens a searchview where the user can query restaurant names. Below that are two fragments holding recycler views that display favorite restaurants based on firebase information and all restaurants. Again, only restaurans within a certain distance are displayed. The restaurants are clickable
<br>
<br>
After logging in once users are immediately shown the homescreen
<br>
<br>
Restaurants are stored in firebase with a Name (string), Items (array), Prices (array), Location (geopoint), and userFav(array) <- this details the users that favorite this restaurant. Restaurants are displayed in the various recycler views based on conditons.



## Functionality
'*' indicates tested in GIF  
The following **required** functionality is completed:
<br>
Implementation of the first 6 items in the rubric. Sign in Activity, Session Saving with shared Preferences, Uploading Image of the user, Implementation of Navigation Drawer and it’s fragments, Implementation of Recent Restaurants in Horizontal Scrollable manner in Home Activity, Implementation of All Restaurants in Vertical Scrollable manner in Home Activity.

**Demonstrated**
<br>
**START** 
<br>
The first case is a user already in authentication
* The user enters a name: Jacob Fritz, an email: jamfritz@iu.edu, a password: ilove323, and a picture which is taken with the [Capture] button after clicking on the defualt pfp the user selects [Next]. Displayed on the homescreen is all the restaurants and all of the users favorite restaurants. The user clicks the [hamburger] button to show the navigation drawer where again all the information is displayed and the items are clickable. The user selects [home] to return to the home screen. The [search] button in the toolbar is then selected and the user queries for Chipotle. The recycler view updates showing only chiptole. The user clicks out of the searchView by hitting the [x] and is returned a screen showing all of the restaurants and all of the favorited restaurants. [hamburger] is selected and the user selects [Log Out]

Not in authentication
* Only difference from above is the user is created in firebase and everything is uploaded. Also, the user has less favorite restaurants
 
<br>

**END**


---
## Video Walkthrough
Watch a demonstration of the different options when working with the finalproject app in the gif available on Github
Here's a walkthrough of a few translations:
**there was a slight adjustment to the buttons size that was implemented after the video was created and the changes do not alter the performance of the app -> however this changes clossly
reflects the UI requested in the PDF**
<br>
<img src='https://github.com/jfritz25/FinalProject323/blob/master/app/AppDemo.gif' title='FinalProject Video Walkthrough' width='50%' height = '50%' alt='Video Walkthrough' />



## Notes
UI Challenges:
- Displaying data read in from firestore
- Setting up a navigation drawer component for the first time
- Setting up a functioning search bar
- Camera preview issues

Backend Challenges:
- Querying authentication, firestore, and firebase storage. 
- Communicating through all of the activities and fragments
- Overall just very very difficult and time consuming

## License

    Copyright [2023] [Jacob Fritz]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.