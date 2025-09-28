import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NominatimService {

  private apiUrl: string = "https://nominatim.openstreetmap.org/search?q="
  constructor(private http: HttpClient) {}

  getGeoLocationWithAdress(param: string): Observable<any> {
    return this.http.get(`${this.apiUrl}${param}&format=geojson`);
  }
/*
  apiUrl = "https://nominatim.openstreetmap.org/search?q=17+Strada+Pictor+Alexandru+Romano%2C+Bukarest&format=geojson"
*/
/*
  urlCustom = "https://nominatim.openstreetmap.org/reverse?format=geojson&lat=" + lat + "&lon=" + lon;
*/


  lat: number;
  long: number;


  /*  constructor(private nominatimService: NominatimService) {
      let lat: number;
      let long: number;
    }*/

/*  lat;
  long: number = any;*/



  error() {
    console.log("Sorry, no position available.");
  }


/*
  https://nominatim.openstreetmap.org/search?q=17+Strada+Pictor+Alexandru+Romano%2C+Bukarest&format=geojson
*/

}
