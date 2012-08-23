//get the phone's current location
//adapted from Ketai geolocation example
import ketai.sensors.*; 

double longitude, latitude, altitude;
KetaiLocation location;
boolean locationReady;

void getPhoneLocation() {
  if (location.getProvider() == "none") { 
    locationReady = false;
  } else {
    locationReady = true;
  }
}

void onLocationEvent(double _latitude, double _longitude, double _altitude)
{
  locationReady = true;
  longitude = _longitude;
  latitude = _latitude;
  altitude = _altitude;
}

