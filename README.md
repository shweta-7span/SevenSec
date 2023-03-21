# SevenSec

## Known Issues

### Xiaomi
- When user Disable Battery Optimization and come again in the App then, Popup not close as at that time app did not get that user disabled the Optimization. When user click on the 'Disable' button again at that time, the popup goes and App's list of insalled application was show. Same thing happened in OneSec also.

- When the 7Sec service is Running and user went to recent apps and click on the "cross" button to clear the apps then, the service will be killed. It will be restarted after some time if user enable the "AutoStart" permission for 7Sec application.

- When user open selected application and 7Sec show the warning screen at that time, if user press home button and then, clear the app by clicking on "cross" button. At that time, service will killed and it will restart after sometimes if user enable the "AutoStart" permission. After that "Attempt / Warning" screen will not opened for selected application, even the service is running. It will not open untill user launch the 7Sec application. Same thing happened in OneSec also.

- OneSec service's restart take less time compare to 7Sec app's service's restart.

## Common Issues OR Improvements
- "Last attempt" time should be "Last use" time of selected application like OneSec. Currently, in 7Sec we are showing "Last attempt" time of the selected application.
- We need to add "Background settings" option in settings like OneSec to run the 7Sec smoothly in custom OS phones.

## OneSec's Features
- https://docs.google.com/document/d/1Y1Z_j73WWTwXeUYIt1bSwYN_X-uxHv3YzH3Ejd0elCk/edit?usp=sharing
