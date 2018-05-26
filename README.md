# Udacity CapstoneProject (*TrackMe*)
*TrackMe* is my final project for [Udacity's Android Developer Nanodegree Program](https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801 ).

*TrackMe* provides trip tracking which when activated tracks and records the user's movement. Trip tracking can be used to
help track vehicle usage, file trip expense reports, or compare differences in daily routes that are frequented by the user.

A user can start tracking a trip and see their trip track on a map. The app does not have to stay in the foreground for the
duration of the trip. If the user transitions to another application the tracking service used by the app continues collecting
location positions while the app is not in the foreground. The tracking service is promoted to a foreground service when 
the app is paused. While the tracking service is running as a foreground service, a notification is posted so that the 
user can stop the tracking service or open the app via the notification.

A log of the user's trips is maintained which can be sorted, filtered, and shared.  A user can select trips from the log and 
see the corresponding track of the trip on a map. The user can retrieve information about the trip by clicking on the track 
in the map view.  Multiple trip tracks can be viewed on the map simulataneously for comparison.

More description and pictures to follow...
